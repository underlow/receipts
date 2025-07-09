package me.underlow.receipt.dao

import me.underlow.receipt.model.InboxEntity
import me.underlow.receipt.model.InboxState
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * Data Access Object for InboxEntity operations.
 * Provides CRUD operations for inbox entities using JdbcTemplate.
 */
@Repository
class InboxEntityDao(
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
) {
    
    private val inboxEntityRowMapper = InboxEntityRowMapper()
    
    /**
     * Find inbox entity by ID.
     * @param id InboxEntity ID
     * @return InboxEntity if found, null otherwise
     */
    fun findById(id: String): InboxEntity? {
        return try {
            val sql = """
                SELECT id, uploaded_image, upload_date, ocr_results, linked_entity_id, 
                       linked_entity_type, state, failure_reason 
                FROM inbox 
                WHERE id = :id
            """
            val params = MapSqlParameterSource("id", id)
            namedParameterJdbcTemplate.queryForObject(sql, params, inboxEntityRowMapper)
        } catch (e: EmptyResultDataAccessException) {
            null
        }
    }
    
    /**
     * Find all inbox entities with pagination and sorting.
     * @param page page number (0-based)
     * @param size page size
     * @param sortBy field to sort by (default: "upload_date")
     * @param sortDirection sort direction (default: "DESC")
     * @return list of inbox entities
     */
    fun findAll(page: Int, size: Int, sortBy: String = "upload_date", sortDirection: String = "DESC"): List<InboxEntity> {
        val allowedSortFields = setOf("upload_date", "state", "id")
        val safeSortBy = if (sortBy in allowedSortFields) sortBy else "upload_date"
        val safeSortDirection = if (sortDirection.uppercase() in setOf("ASC", "DESC")) sortDirection.uppercase() else "DESC"
        
        val sql = """
            SELECT id, uploaded_image, upload_date, ocr_results, linked_entity_id, 
                   linked_entity_type, state, failure_reason 
            FROM inbox 
            ORDER BY $safeSortBy $safeSortDirection
            LIMIT :size OFFSET :offset
        """
        
        val params = MapSqlParameterSource()
            .addValue("size", size)
            .addValue("offset", page * size)
        
        return namedParameterJdbcTemplate.query(sql, params, inboxEntityRowMapper)
    }
    
    /**
     * Get total count of inbox entities.
     * @return total number of entities
     */
    fun getTotalCount(): Int {
        val sql = "SELECT COUNT(*) FROM inbox"
        return namedParameterJdbcTemplate.queryForObject(sql, MapSqlParameterSource(), Int::class.java) ?: 0
    }
    
    /**
     * Find inbox entities by state.
     * @param state InboxState to filter by
     * @return list of inbox entities with specified state
     */
    fun findByState(state: InboxState): List<InboxEntity> {
        val sql = """
            SELECT id, uploaded_image, upload_date, ocr_results, linked_entity_id, 
                   linked_entity_type, state, failure_reason 
            FROM inbox 
            WHERE state = :state
            ORDER BY upload_date DESC
        """
        
        val params = MapSqlParameterSource("state", state.name)
        return namedParameterJdbcTemplate.query(sql, params, inboxEntityRowMapper)
    }
    
    /**
     * Save inbox entity to database. Creates new entity or updates existing one.
     * @param entity InboxEntity to save
     * @return saved InboxEntity
     */
    fun save(entity: InboxEntity): InboxEntity {
        val existingEntity = findById(entity.id)
        return if (existingEntity == null) {
            insert(entity)
        } else {
            update(entity)
        }
    }
    
    /**
     * Insert new inbox entity into database.
     * @param entity InboxEntity to insert
     * @return inserted InboxEntity
     */
    private fun insert(entity: InboxEntity): InboxEntity {
        val sql = """
            INSERT INTO inbox (id, uploaded_image, upload_date, ocr_results, linked_entity_id, 
                             linked_entity_type, state, failure_reason, created_at, updated_at) 
            VALUES (:id, :uploadedImage, :uploadDate, :ocrResults, :linkedEntityId, 
                    :linkedEntityType, :state, :failureReason, :createdAt, :updatedAt)
        """
        
        val now = LocalDateTime.now()
        val params = MapSqlParameterSource()
            .addValue("id", entity.id)
            .addValue("uploadedImage", entity.uploadedImage)
            .addValue("uploadDate", entity.uploadDate)
            .addValue("ocrResults", entity.ocrResults)
            .addValue("linkedEntityId", entity.linkedEntityId)
            .addValue("linkedEntityType", entity.linkedEntityType?.name)
            .addValue("state", entity.state.name)
            .addValue("failureReason", entity.failureReason)
            .addValue("createdAt", now)
            .addValue("updatedAt", now)
        
        val rowsInserted = namedParameterJdbcTemplate.update(sql, params)
        
        if (rowsInserted == 0) {
            throw IllegalStateException("Failed to insert inbox entity")
        }
        
        return entity
    }
    
    /**
     * Update existing inbox entity in database.
     * @param entity InboxEntity to update
     * @return updated InboxEntity
     */
    private fun update(entity: InboxEntity): InboxEntity {
        val sql = """
            UPDATE inbox 
            SET uploaded_image = :uploadedImage, upload_date = :uploadDate, ocr_results = :ocrResults, 
                linked_entity_id = :linkedEntityId, linked_entity_type = :linkedEntityType, 
                state = :state, failure_reason = :failureReason, updated_at = :updatedAt 
            WHERE id = :id
        """
        
        val now = LocalDateTime.now()
        val params = MapSqlParameterSource()
            .addValue("id", entity.id)
            .addValue("uploadedImage", entity.uploadedImage)
            .addValue("uploadDate", entity.uploadDate)
            .addValue("ocrResults", entity.ocrResults)
            .addValue("linkedEntityId", entity.linkedEntityId)
            .addValue("linkedEntityType", entity.linkedEntityType?.name)
            .addValue("state", entity.state.name)
            .addValue("failureReason", entity.failureReason)
            .addValue("updatedAt", now)
        
        val rowsUpdated = namedParameterJdbcTemplate.update(sql, params)
        
        if (rowsUpdated == 0) {
            throw IllegalArgumentException("InboxEntity with ID ${entity.id} not found")
        }
        
        return entity
    }
    
    /**
     * Delete inbox entity by ID.
     * @param id InboxEntity ID
     * @return true if entity was deleted, false if not found
     */
    fun deleteById(id: String): Boolean {
        val sql = "DELETE FROM inbox WHERE id = :id"
        val params = MapSqlParameterSource("id", id)
        val rowsDeleted = namedParameterJdbcTemplate.update(sql, params)
        return rowsDeleted > 0
    }
}