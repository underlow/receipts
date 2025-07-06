package me.underlow.receipt.repository

import me.underlow.receipt.model.IncomingFile
import me.underlow.receipt.model.ItemStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import java.sql.Statement
import java.time.LocalDate

class IncomingFileRepositoryImpl(private val jdbcTemplate: JdbcTemplate) : IncomingFileRepository {

    private val rowMapper = RowMapper<IncomingFile> { rs, _ ->
        IncomingFile(
            id = rs.getLong("id"),
            filename = rs.getString("filename"),
            filePath = rs.getString("file_path"),
            uploadDate = rs.getTimestamp("upload_date").toLocalDateTime(),
            status = ItemStatus.valueOf(rs.getString("status")),
            checksum = rs.getString("checksum"),
            userId = rs.getLong("user_id"),
            ocrRawJson = rs.getString("ocr_raw_json"),
            extractedAmount = rs.getBigDecimal("extracted_amount")?.toDouble(),
            extractedDate = rs.getDate("extracted_date")?.toLocalDate(),
            extractedProvider = rs.getString("extracted_provider"),
            ocrProcessedAt = rs.getTimestamp("ocr_processed_at")?.toLocalDateTime(),
            ocrErrorMessage = rs.getString("ocr_error_message")
        )
    }

    override fun save(incomingFile: IncomingFile): IncomingFile {
        return if (incomingFile.id == null) {
            val keyHolder = GeneratedKeyHolder()
            jdbcTemplate.update({
                connection ->
                val ps = connection.prepareStatement(
                    "INSERT INTO incoming_files (filename, file_path, upload_date, status, checksum, user_id, ocr_raw_json, extracted_amount, extracted_date, extracted_provider, ocr_processed_at, ocr_error_message) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
                )
                ps.setString(1, incomingFile.filename)
                ps.setString(2, incomingFile.filePath)
                ps.setTimestamp(3, java.sql.Timestamp.valueOf(incomingFile.uploadDate))
                ps.setString(4, incomingFile.status.name)
                ps.setString(5, incomingFile.checksum)
                ps.setLong(6, incomingFile.userId)
                ps.setString(7, incomingFile.ocrRawJson)
                ps.setBigDecimal(8, incomingFile.extractedAmount?.toBigDecimal())
                ps.setDate(9, incomingFile.extractedDate?.let { java.sql.Date.valueOf(it) })
                ps.setString(10, incomingFile.extractedProvider)
                ps.setTimestamp(11, incomingFile.ocrProcessedAt?.let { java.sql.Timestamp.valueOf(it) })
                ps.setString(12, incomingFile.ocrErrorMessage)
                ps
            }, keyHolder)
            val generatedId = keyHolder.keyList.firstOrNull()?.get("id") as? Number
            incomingFile.copy(id = generatedId?.toLong())
        } else {
            jdbcTemplate.update(
                "UPDATE incoming_files SET filename = ?, file_path = ?, upload_date = ?, status = ?, checksum = ?, user_id = ?, ocr_raw_json = ?, extracted_amount = ?, extracted_date = ?, extracted_provider = ?, ocr_processed_at = ?, ocr_error_message = ? WHERE id = ?",
                incomingFile.filename, incomingFile.filePath, java.sql.Timestamp.valueOf(incomingFile.uploadDate), 
                incomingFile.status.name, incomingFile.checksum, incomingFile.userId, incomingFile.ocrRawJson,
                incomingFile.extractedAmount?.toBigDecimal(), incomingFile.extractedDate?.let { java.sql.Date.valueOf(it) },
                incomingFile.extractedProvider, incomingFile.ocrProcessedAt?.let { java.sql.Timestamp.valueOf(it) },
                incomingFile.ocrErrorMessage, incomingFile.id
            )
            incomingFile
        }
    }

    override fun findById(id: Long): IncomingFile? {
        return jdbcTemplate.query(
            "SELECT id, filename, file_path, upload_date, status, checksum, user_id, ocr_raw_json, extracted_amount, extracted_date, extracted_provider, ocr_processed_at, ocr_error_message FROM incoming_files WHERE id = ?",
            rowMapper, id
        ).firstOrNull()
    }

    override fun findByUserId(userId: Long): List<IncomingFile> {
        return jdbcTemplate.query(
            "SELECT id, filename, file_path, upload_date, status, checksum, user_id, ocr_raw_json, extracted_amount, extracted_date, extracted_provider, ocr_processed_at, ocr_error_message FROM incoming_files WHERE user_id = ?",
            rowMapper, userId
        )
    }

    override fun findByChecksum(checksum: String): IncomingFile? {
        return jdbcTemplate.query(
            "SELECT id, filename, file_path, upload_date, status, checksum, user_id, ocr_raw_json, extracted_amount, extracted_date, extracted_provider, ocr_processed_at, ocr_error_message FROM incoming_files WHERE checksum = ?",
            rowMapper, checksum
        ).firstOrNull()
    }

    override fun findByStatus(status: ItemStatus): List<IncomingFile> {
        return jdbcTemplate.query(
            "SELECT id, filename, file_path, upload_date, status, checksum, user_id, ocr_raw_json, extracted_amount, extracted_date, extracted_provider, ocr_processed_at, ocr_error_message FROM incoming_files WHERE status = ?",
            rowMapper, status.name
        )
    }

    override fun findByUserIdAndStatus(userId: Long, status: ItemStatus): List<IncomingFile> {
        return jdbcTemplate.query(
            "SELECT id, filename, file_path, upload_date, status, checksum, user_id, ocr_raw_json, extracted_amount, extracted_date, extracted_provider, ocr_processed_at, ocr_error_message FROM incoming_files WHERE user_id = ? AND status = ?",
            rowMapper, userId, status.name
        )
    }

    override fun findAll(): List<IncomingFile> {
        return jdbcTemplate.query(
            "SELECT id, filename, file_path, upload_date, status, checksum, user_id, ocr_raw_json, extracted_amount, extracted_date, extracted_provider, ocr_processed_at, ocr_error_message FROM incoming_files",
            rowMapper
        )
    }

    override fun delete(id: Long): Boolean {
        val rowsAffected = jdbcTemplate.update("DELETE FROM incoming_files WHERE id = ?", id)
        return rowsAffected > 0
    }
}