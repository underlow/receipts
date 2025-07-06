package me.underlow.receipt.repository

import me.underlow.receipt.model.Receipt
import me.underlow.receipt.model.ItemStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import java.sql.Statement

class ReceiptRepositoryImpl(private val jdbcTemplate: JdbcTemplate) : ReceiptRepository {

    private val rowMapper = RowMapper<Receipt> { rs, _ ->
        Receipt(
            id = rs.getLong("id"),
            userId = rs.getLong("user_id"),
            billId = rs.getObject("bill_id") as? Long,
            filename = rs.getString("filename"),
            filePath = rs.getString("file_path"),
            uploadDate = rs.getTimestamp("upload_date")?.toLocalDateTime(),
            checksum = rs.getString("checksum"),
            status = rs.getString("status")?.let { ItemStatus.valueOf(it) } ?: ItemStatus.NEW,
            ocrRawJson = rs.getString("ocr_raw_json"),
            extractedAmount = rs.getObject("extracted_amount") as? Double,
            extractedDate = rs.getDate("extracted_date")?.toLocalDate(),
            extractedProvider = rs.getString("extracted_provider"),
            ocrProcessedAt = rs.getTimestamp("ocr_processed_at")?.toLocalDateTime(),
            ocrErrorMessage = rs.getString("ocr_error_message"),
            originalIncomingFileId = rs.getObject("original_incoming_file_id") as? Long
        )
    }

    override fun save(receipt: Receipt): Receipt {
        return if (receipt.id == null) {
            val keyHolder = GeneratedKeyHolder()
            jdbcTemplate.update({
                connection ->
                val ps = connection.prepareStatement(
                    "INSERT INTO receipts (user_id, bill_id, filename, file_path, upload_date, checksum, status, ocr_raw_json, extracted_amount, extracted_date, extracted_provider, ocr_processed_at, ocr_error_message, original_incoming_file_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
                )
                ps.setLong(1, receipt.userId)
                ps.setObject(2, receipt.billId)
                ps.setString(3, receipt.filename)
                ps.setString(4, receipt.filePath)
                ps.setTimestamp(5, receipt.uploadDate?.let { java.sql.Timestamp.valueOf(it) })
                ps.setString(6, receipt.checksum)
                ps.setString(7, receipt.status.name)
                ps.setString(8, receipt.ocrRawJson)
                ps.setObject(9, receipt.extractedAmount)
                ps.setDate(10, receipt.extractedDate?.let { java.sql.Date.valueOf(it) })
                ps.setString(11, receipt.extractedProvider)
                ps.setTimestamp(12, receipt.ocrProcessedAt?.let { java.sql.Timestamp.valueOf(it) })
                ps.setString(13, receipt.ocrErrorMessage)
                ps.setObject(14, receipt.originalIncomingFileId)
                ps
            }, keyHolder)
            val generatedId = keyHolder.keyList.firstOrNull()?.get("id") as? Number
            receipt.copy(id = generatedId?.toLong())
        } else {
            jdbcTemplate.update(
                "UPDATE receipts SET user_id = ?, bill_id = ?, filename = ?, file_path = ?, upload_date = ?, checksum = ?, status = ?, ocr_raw_json = ?, extracted_amount = ?, extracted_date = ?, extracted_provider = ?, ocr_processed_at = ?, ocr_error_message = ?, original_incoming_file_id = ? WHERE id = ?",
                receipt.userId, receipt.billId, receipt.filename, receipt.filePath, 
                receipt.uploadDate?.let { java.sql.Timestamp.valueOf(it) }, receipt.checksum, receipt.status.name,
                receipt.ocrRawJson, receipt.extractedAmount, receipt.extractedDate?.let { java.sql.Date.valueOf(it) },
                receipt.extractedProvider, receipt.ocrProcessedAt?.let { java.sql.Timestamp.valueOf(it) }, 
                receipt.ocrErrorMessage, receipt.originalIncomingFileId, receipt.id
            )
            receipt
        }
    }

    override fun findById(id: Long): Receipt? {
        return jdbcTemplate.query(
            "SELECT id, user_id, bill_id, filename, file_path, upload_date, checksum, status, ocr_raw_json, extracted_amount, extracted_date, extracted_provider, ocr_processed_at, ocr_error_message, original_incoming_file_id FROM receipts WHERE id = ?",
            rowMapper, id
        ).firstOrNull()
    }

    override fun findByUserId(userId: Long): List<Receipt> {
        return jdbcTemplate.query(
            "SELECT id, user_id, bill_id, filename, file_path, upload_date, checksum, status, ocr_raw_json, extracted_amount, extracted_date, extracted_provider, ocr_processed_at, ocr_error_message, original_incoming_file_id FROM receipts WHERE user_id = ?",
            rowMapper, userId
        )
    }

    override fun findByBillId(billId: Long): List<Receipt> {
        return jdbcTemplate.query(
            "SELECT id, user_id, bill_id, filename, file_path, upload_date, checksum, status, ocr_raw_json, extracted_amount, extracted_date, extracted_provider, ocr_processed_at, ocr_error_message, original_incoming_file_id FROM receipts WHERE bill_id = ?",
            rowMapper, billId
        )
    }

    override fun findByStatus(status: ItemStatus): List<Receipt> {
        return jdbcTemplate.query(
            "SELECT id, user_id, bill_id, filename, file_path, upload_date, checksum, status, ocr_raw_json, extracted_amount, extracted_date, extracted_provider, ocr_processed_at, ocr_error_message, original_incoming_file_id FROM receipts WHERE status = ?",
            rowMapper, status.name
        )
    }

    override fun findByUserIdAndStatus(userId: Long, status: ItemStatus): List<Receipt> {
        return jdbcTemplate.query(
            "SELECT id, user_id, bill_id, filename, file_path, upload_date, checksum, status, ocr_raw_json, extracted_amount, extracted_date, extracted_provider, ocr_processed_at, ocr_error_message, original_incoming_file_id FROM receipts WHERE user_id = ? AND status = ?",
            rowMapper, userId, status.name
        )
    }

    override fun findAll(): List<Receipt> {
        return jdbcTemplate.query(
            "SELECT id, user_id, bill_id, filename, file_path, upload_date, checksum, status, ocr_raw_json, extracted_amount, extracted_date, extracted_provider, ocr_processed_at, ocr_error_message, original_incoming_file_id FROM receipts",
            rowMapper
        )
    }

    override fun delete(id: Long): Boolean {
        val rowsAffected = jdbcTemplate.update("DELETE FROM receipts WHERE id = ?", id)
        return rowsAffected > 0
    }
}