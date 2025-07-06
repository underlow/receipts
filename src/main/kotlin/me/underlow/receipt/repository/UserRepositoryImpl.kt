package me.underlow.receipt.repository

import me.underlow.receipt.model.User
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import java.sql.Statement
import java.time.LocalDateTime

class UserRepositoryImpl(private val jdbcTemplate: JdbcTemplate) : UserRepository {

    private val logger = LoggerFactory.getLogger(UserRepositoryImpl::class.java)

    private val rowMapper = RowMapper<User> { rs, _ ->
        User(
            id = rs.getLong("id"),
            email = rs.getString("email"),
            name = rs.getString("name"),
            createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
            lastLoginAt = rs.getTimestamp("last_login_at").toLocalDateTime()
        )
    }

    override fun findByEmail(email: String): User? {
        return jdbcTemplate.query("SELECT id, email, name, created_at, last_login_at FROM users WHERE email = ?", rowMapper, email).firstOrNull()
    }

    override fun findById(id: Long): User? {
        return jdbcTemplate.query("SELECT id, email, name, created_at, last_login_at FROM users WHERE id = ?", rowMapper, id).firstOrNull()
    }

    override fun save(user: User): User {
        return if (user.id == null) {
            val keyHolder = GeneratedKeyHolder()
            jdbcTemplate.update({
                connection ->
                val ps = connection.prepareStatement("INSERT INTO users (email, name, last_login_at) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)
                ps.setString(1, user.email)
                ps.setString(2, user.name)
                ps.setTimestamp(3, java.sql.Timestamp.valueOf(user.lastLoginAt))
                ps
            }, keyHolder)
            val generatedId = keyHolder.keyList.firstOrNull()?.get("id") as? Number
            val savedUser = user.copy(id = generatedId?.toLong())
            logger.info("New user registered with ID: {} for email: {}", savedUser.id, user.email)
            savedUser
        } else {
            jdbcTemplate.update("UPDATE users SET last_login_at = ? WHERE id = ?",
                java.sql.Timestamp.valueOf(user.lastLoginAt), user.id)
            logger.info("User login updated for ID: {}", user.id)
            user
        }
    }
}
