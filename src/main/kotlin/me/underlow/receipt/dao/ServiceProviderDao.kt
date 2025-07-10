package me.underlow.receipt.dao

import me.underlow.receipt.model.ServiceProvider
import me.underlow.receipt.model.ServiceProviderState
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.jdbc.support.KeyHolder
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * Data Access Object for ServiceProvider entity operations.
 * Provides CRUD operations for service providers using JdbcTemplate.
 */
@Repository
class ServiceProviderDao(
    private val jdbcTemplate: JdbcTemplate,
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
) {
    
    private val serviceProviderRowMapper = ServiceProviderRowMapper()
    
    /**
     * Find service provider by ID.
     * @param id Service provider ID
     * @return ServiceProvider if found, null otherwise
     */
    fun findById(id: Long): ServiceProvider? {
        return try {
            val sql = """
                SELECT id, name, avatar, comment, comment_for_ocr, regular, custom_fields, state, created_date, modified_date 
                FROM service_providers 
                WHERE id = :id
            """
            val params = MapSqlParameterSource("id", id)
            namedParameterJdbcTemplate.queryForObject(sql, params, serviceProviderRowMapper)
        } catch (e: EmptyResultDataAccessException) {
            null
        }
    }
    
    /**
     * Find all service providers ordered by name.
     * @return List of all service providers
     */
    fun findAll(): List<ServiceProvider> {
        val sql = """
            SELECT id, name, avatar, comment, comment_for_ocr, regular, custom_fields, state, created_date, modified_date 
            FROM service_providers 
            ORDER BY name
        """
        return namedParameterJdbcTemplate.query(sql, serviceProviderRowMapper)
    }
    
    /**
     * Find service providers by state.
     * @param state Service provider state to filter by
     * @return List of service providers with matching state
     */
    fun findByState(state: ServiceProviderState): List<ServiceProvider> {
        val sql = """
            SELECT id, name, avatar, comment, comment_for_ocr, regular, custom_fields, state, created_date, modified_date 
            FROM service_providers 
            WHERE state = :state 
            ORDER BY name
        """
        val params = MapSqlParameterSource("state", state.name)
        return namedParameterJdbcTemplate.query(sql, params, serviceProviderRowMapper)
    }
    
    /**
     * Find active service providers that have custom fields.
     * @return List of active service providers with custom fields
     */
    fun findActiveProvidersWithCustomFields(): List<ServiceProvider> {
        val sql = """
            SELECT id, name, avatar, comment, comment_for_ocr, regular, custom_fields, state, created_date, modified_date 
            FROM service_providers 
            WHERE state = 'ACTIVE' AND custom_fields IS NOT NULL 
            ORDER BY name
        """
        return namedParameterJdbcTemplate.query(sql, serviceProviderRowMapper)
    }
    
    /**
     * Check if service provider exists by name.
     * @param name Service provider name
     * @return true if service provider exists, false otherwise
     */
    fun existsByName(name: String): Boolean {
        val sql = "SELECT COUNT(*) FROM service_providers WHERE name = :name"
        val params = MapSqlParameterSource("name", name)
        val count = namedParameterJdbcTemplate.queryForObject(sql, params, Int::class.java) ?: 0
        return count > 0
    }
    
    /**
     * Save service provider to database. Creates new service provider or updates existing one.
     * @param serviceProvider Service provider to save
     * @return Saved service provider with ID populated
     */
    fun save(serviceProvider: ServiceProvider): ServiceProvider {
        return if (serviceProvider.id == 0L) {
            insert(serviceProvider)
        } else {
            update(serviceProvider)
        }
    }
    
    /**
     * Delete service provider by ID.
     * @param id Service provider ID to delete
     */
    fun delete(id: Long) {
        val sql = "DELETE FROM service_providers WHERE id = :id"
        val params = MapSqlParameterSource("id", id)
        namedParameterJdbcTemplate.update(sql, params)
    }
    
    /**
     * Insert new service provider into database.
     * @param serviceProvider Service provider to insert
     * @return Service provider with generated ID
     */
    private fun insert(serviceProvider: ServiceProvider): ServiceProvider {
        val sql = """
            INSERT INTO service_providers (name, avatar, comment, comment_for_ocr, regular, custom_fields, state, created_date, modified_date) 
            VALUES (:name, :avatar, :comment, :commentForOcr, :regular, :customFields, :state, :createdDate, :modifiedDate)
        """
        
        val now = LocalDateTime.now()
        val params = MapSqlParameterSource()
            .addValue("name", serviceProvider.name)
            .addValue("avatar", serviceProvider.avatar)
            .addValue("comment", serviceProvider.comment)
            .addValue("commentForOcr", serviceProvider.commentForOcr)
            .addValue("regular", serviceProvider.regular.name)
            .addValue("customFields", serviceProvider.customFields)
            .addValue("state", serviceProvider.state.name)
            .addValue("createdDate", now)
            .addValue("modifiedDate", now)
        
        val keyHolder: KeyHolder = GeneratedKeyHolder()
        namedParameterJdbcTemplate.update(sql, params, keyHolder, arrayOf("id"))
        
        val generatedId = keyHolder.key?.toLong() ?: throw IllegalStateException("Failed to get generated ID")
        
        return serviceProvider.copy(id = generatedId, createdDate = now, modifiedDate = now)
    }
    
    /**
     * Update existing service provider in database.
     * @param serviceProvider Service provider to update
     * @return Updated service provider
     */
    private fun update(serviceProvider: ServiceProvider): ServiceProvider {
        val sql = """
            UPDATE service_providers 
            SET name = :name, avatar = :avatar, comment = :comment, comment_for_ocr = :commentForOcr, 
                regular = :regular, custom_fields = :customFields, state = :state, modified_date = :modifiedDate 
            WHERE id = :id
        """
        
        val now = LocalDateTime.now()
        val params = MapSqlParameterSource()
            .addValue("id", serviceProvider.id)
            .addValue("name", serviceProvider.name)
            .addValue("avatar", serviceProvider.avatar)
            .addValue("comment", serviceProvider.comment)
            .addValue("commentForOcr", serviceProvider.commentForOcr)
            .addValue("regular", serviceProvider.regular.name)
            .addValue("customFields", serviceProvider.customFields)
            .addValue("state", serviceProvider.state.name)
            .addValue("modifiedDate", now)
        
        val rowsUpdated = namedParameterJdbcTemplate.update(sql, params)
        
        if (rowsUpdated == 0) {
            throw IllegalArgumentException("Service provider with ID ${serviceProvider.id} not found")
        }
        
        return serviceProvider.copy(modifiedDate = now)
    }
}