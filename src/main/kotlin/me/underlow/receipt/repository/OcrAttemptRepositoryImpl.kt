package me.underlow.receipt.repository

import me.underlow.receipt.model.EntityType
import me.underlow.receipt.model.OcrAttempt
import me.underlow.receipt.model.OcrProcessingStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.PreparedStatement
import java.sql.Statement
import java.sql.Timestamp

/**
 * JDBC implementation of OCR attempt repository
 */
@Repository
class OcrAttemptRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate
) : OcrAttemptRepository {

    private val rowMapper = RowMapper<OcrAttempt> { rs, _ ->
        OcrAttempt(
            id = rs.getLong("id"),
            entityType = EntityType.valueOf(rs.getString("entity_type")),
            entityId = rs.getLong("entity_id"),
            attemptTimestamp = rs.getTimestamp("attempt_timestamp").toLocalDateTime(),
            ocrEngineUsed = rs.getString("ocr_engine_used"),
            processingStatus = OcrProcessingStatus.valueOf(rs.getString("processing_status")),
            extractedDataJson = rs.getString("extracted_data_json"),
            errorMessage = rs.getString("error_message"),
            rawResponse = rs.getString("raw_response"),
            userId = rs.getLong("user_id")
        )
    }

    override fun save(ocrAttempt: OcrAttempt): OcrAttempt {
        return if (ocrAttempt.id == null) {
            // Insert new record
            val keyHolder = GeneratedKeyHolder()
            val sql = """
                INSERT INTO ocr_attempts (entity_type, entity_id, attempt_timestamp, ocr_engine_used, 
                                        processing_status, extracted_data_json, error_message, raw_response, user_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
            
            jdbcTemplate.update({ connection ->
                val ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
                ps.setString(1, ocrAttempt.entityType.name)
                ps.setLong(2, ocrAttempt.entityId)
                ps.setTimestamp(3, Timestamp.valueOf(ocrAttempt.attemptTimestamp))
                ps.setString(4, ocrAttempt.ocrEngineUsed)
                ps.setString(5, ocrAttempt.processingStatus.name)
                ps.setString(6, ocrAttempt.extractedDataJson)
                ps.setString(7, ocrAttempt.errorMessage)
                ps.setString(8, ocrAttempt.rawResponse)
                ps.setLong(9, ocrAttempt.userId)
                ps
            }, keyHolder)
            
            val generatedId = keyHolder.key?.toLong() ?: throw RuntimeException("Failed to get generated ID")
            ocrAttempt.copy(id = generatedId)
        } else {
            // Update existing record
            val sql = """
                UPDATE ocr_attempts 
                SET entity_type = ?, entity_id = ?, attempt_timestamp = ?, ocr_engine_used = ?, 
                    processing_status = ?, extracted_data_json = ?, error_message = ?, raw_response = ?, user_id = ?
                WHERE id = ?
            """.trimIndent()
            
            jdbcTemplate.update(
                sql,
                ocrAttempt.entityType.name,
                ocrAttempt.entityId,
                Timestamp.valueOf(ocrAttempt.attemptTimestamp),
                ocrAttempt.ocrEngineUsed,
                ocrAttempt.processingStatus.name,
                ocrAttempt.extractedDataJson,
                ocrAttempt.errorMessage,
                ocrAttempt.rawResponse,
                ocrAttempt.userId,
                ocrAttempt.id
            )
            ocrAttempt
        }
    }

    override fun findById(id: Long): OcrAttempt? {
        val sql = "SELECT * FROM ocr_attempts WHERE id = ?"
        return try {
            jdbcTemplate.queryForObject(sql, rowMapper, id)
        } catch (e: Exception) {
            null
        }
    }

    override fun findByEntityTypeAndEntityId(entityType: EntityType, entityId: Long): List<OcrAttempt> {
        val sql = "SELECT * FROM ocr_attempts WHERE entity_type = ? AND entity_id = ? ORDER BY attempt_timestamp DESC"
        return jdbcTemplate.query(sql, rowMapper, entityType.name, entityId)
    }

    override fun findByUserId(userId: Long): List<OcrAttempt> {
        val sql = "SELECT * FROM ocr_attempts WHERE user_id = ? ORDER BY attempt_timestamp DESC"
        return jdbcTemplate.query(sql, rowMapper, userId)
    }

    override fun findByProcessingStatus(status: OcrProcessingStatus): List<OcrAttempt> {
        val sql = "SELECT * FROM ocr_attempts WHERE processing_status = ? ORDER BY attempt_timestamp DESC"
        return jdbcTemplate.query(sql, rowMapper, status.name)
    }

    override fun findLatestByEntityTypeAndEntityId(entityType: EntityType, entityId: Long): OcrAttempt? {
        val sql = """
            SELECT * FROM ocr_attempts 
            WHERE entity_type = ? AND entity_id = ? 
            ORDER BY attempt_timestamp DESC 
            LIMIT 1
        """.trimIndent()
        return try {
            jdbcTemplate.queryForObject(sql, rowMapper, entityType.name, entityId)
        } catch (e: Exception) {
            null
        }
    }

    override fun delete(id: Long): Boolean {
        val sql = "DELETE FROM ocr_attempts WHERE id = ?"
        return jdbcTemplate.update(sql, id) > 0
    }

    override fun deleteByEntityTypeAndEntityId(entityType: EntityType, entityId: Long): Int {
        val sql = "DELETE FROM ocr_attempts WHERE entity_type = ? AND entity_id = ?"
        return jdbcTemplate.update(sql, entityType.name, entityId)
    }
}