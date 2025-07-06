package me.underlow.receipt.dao

import me.underlow.receipt.model.User
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.jdbc.support.KeyHolder
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * Data Access Object for User entity operations.
 * Provides CRUD operations for users using JdbcTemplate.
 */
@Repository
class UserDao(
    private val jdbcTemplate: JdbcTemplate,
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
) {
    
    private val userRowMapper = UserRowMapper()
    
    /**
     * Find user by email address.
     * @param email User's email address
     * @return User if found, null otherwise
     */
    fun findByEmail(email: String): User? {
        return try {
            val sql = "SELECT id, email, name, avatar, created_at, updated_at FROM users WHERE email = :email"
            val params = MapSqlParameterSource("email", email)
            namedParameterJdbcTemplate.queryForObject(sql, params, userRowMapper)
        } catch (e: EmptyResultDataAccessException) {
            null
        }
    }
    
    /**
     * Check if user exists by email.
     * @param email User's email address
     * @return true if user exists, false otherwise
     */
    fun existsByEmail(email: String): Boolean {
        val sql = "SELECT COUNT(*) FROM users WHERE email = :email"
        val params = MapSqlParameterSource("email", email)
        val count = namedParameterJdbcTemplate.queryForObject(sql, params, Int::class.java) ?: 0
        return count > 0
    }
    
    /**
     * Save user to database. Creates new user or updates existing one.
     * @param user User to save
     * @return Saved user with ID populated
     */
    fun save(user: User): User {
        return if (user.id == null) {
            insert(user)
        } else {
            update(user)
        }
    }
    
    /**
     * Find user by ID.
     * @param id User ID
     * @return User if found, null otherwise
     */
    fun findById(id: Long): User? {
        return try {
            val sql = "SELECT id, email, name, avatar, created_at, updated_at FROM users WHERE id = :id"
            val params = MapSqlParameterSource("id", id)
            namedParameterJdbcTemplate.queryForObject(sql, params, userRowMapper)
        } catch (e: EmptyResultDataAccessException) {
            null
        }
    }
    
    /**
     * Insert new user into database.
     * @param user User to insert
     * @return User with generated ID
     */
    private fun insert(user: User): User {
        val sql = """
            INSERT INTO users (email, name, avatar, created_at, updated_at) 
            VALUES (:email, :name, :avatar, :createdAt, :updatedAt)
        """
        
        val now = LocalDateTime.now()
        val params = MapSqlParameterSource()
            .addValue("email", user.email)
            .addValue("name", user.name)
            .addValue("avatar", user.avatar)
            .addValue("createdAt", now)
            .addValue("updatedAt", now)
        
        val keyHolder: KeyHolder = GeneratedKeyHolder()
        namedParameterJdbcTemplate.update(sql, params, keyHolder, arrayOf("id"))
        
        val generatedId = keyHolder.key?.toLong() ?: throw IllegalStateException("Failed to get generated ID")
        
        return user.copy(id = generatedId, createdAt = now, updatedAt = now)
    }
    
    /**
     * Update existing user in database.
     * @param user User to update
     * @return Updated user
     */
    private fun update(user: User): User {
        val sql = """
            UPDATE users 
            SET email = :email, name = :name, avatar = :avatar, updated_at = :updatedAt 
            WHERE id = :id
        """
        
        val now = LocalDateTime.now()
        val params = MapSqlParameterSource()
            .addValue("id", user.id)
            .addValue("email", user.email)
            .addValue("name", user.name)
            .addValue("avatar", user.avatar)
            .addValue("updatedAt", now)
        
        val rowsUpdated = namedParameterJdbcTemplate.update(sql, params)
        
        if (rowsUpdated == 0) {
            throw IllegalArgumentException("User with ID ${user.id} not found")
        }
        
        return user.copy(updatedAt = now)
    }
    
    /**
     * Upsert user - insert if not exists, update if exists.
     * Uses ON CONFLICT to handle race conditions at database level.
     * @param email User's email address
     * @param name User's display name
     * @param avatar User's avatar URL (optional)
     * @return The created or updated user
     */
    fun upsert(email: String, name: String, avatar: String?): User {
        val now = LocalDateTime.now()
        
        // Use PostgreSQL's ON CONFLICT clause for atomic upsert
        val sql = """
            INSERT INTO users (email, name, avatar, created_at, updated_at) 
            VALUES (:email, :name, :avatar, :createdAt, :updatedAt)
            ON CONFLICT (email) 
            DO UPDATE SET 
                name = EXCLUDED.name,
                avatar = EXCLUDED.avatar,
                updated_at = EXCLUDED.updated_at
            RETURNING id, email, name, avatar, created_at, updated_at
        """
        
        val params = MapSqlParameterSource()
            .addValue("email", email)
            .addValue("name", name)
            .addValue("avatar", avatar)
            .addValue("createdAt", now)
            .addValue("updatedAt", now)
        
        return namedParameterJdbcTemplate.queryForObject(sql, params, userRowMapper)
            ?: throw IllegalStateException("Failed to upsert user")
    }
}