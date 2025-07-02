package me.underlow.receipt.repository

import me.underlow.receipt.model.Bill
import me.underlow.receipt.model.BillStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import java.sql.Statement

class BillRepositoryImpl(private val jdbcTemplate: JdbcTemplate) : BillRepository {

    private val rowMapper = RowMapper<Bill> { rs, _ ->
        Bill(
            id = rs.getLong("id"),
            filename = rs.getString("filename"),
            filePath = rs.getString("file_path"),
            uploadDate = rs.getTimestamp("upload_date").toLocalDateTime(),
            status = BillStatus.valueOf(rs.getString("status")),
            ocrRawJson = rs.getString("ocr_raw_json"),
            extractedAmount = rs.getObject("extracted_amount") as? Double,
            extractedDate = rs.getDate("extracted_date")?.toLocalDate(),
            extractedProvider = rs.getString("extracted_provider"),
            userId = rs.getLong("user_id")
        )
    }

    override fun save(bill: Bill): Bill {
        return if (bill.id == null) {
            val keyHolder = GeneratedKeyHolder()
            jdbcTemplate.update({
                connection ->
                val ps = connection.prepareStatement(
                    "INSERT INTO bills (filename, file_path, upload_date, status, ocr_raw_json, extracted_amount, extracted_date, extracted_provider, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
                )
                ps.setString(1, bill.filename)
                ps.setString(2, bill.filePath)
                ps.setTimestamp(3, java.sql.Timestamp.valueOf(bill.uploadDate))
                ps.setString(4, bill.status.name)
                ps.setString(5, bill.ocrRawJson)
                ps.setObject(6, bill.extractedAmount)
                ps.setDate(7, bill.extractedDate?.let { java.sql.Date.valueOf(it) })
                ps.setString(8, bill.extractedProvider)
                ps.setLong(9, bill.userId)
                ps
            }, keyHolder)
            val generatedId = keyHolder.keyList.firstOrNull()?.get("id") as? Number
            bill.copy(id = generatedId?.toLong())
        } else {
            jdbcTemplate.update(
                "UPDATE bills SET filename = ?, file_path = ?, upload_date = ?, status = ?, ocr_raw_json = ?, extracted_amount = ?, extracted_date = ?, extracted_provider = ?, user_id = ? WHERE id = ?",
                bill.filename, bill.filePath, java.sql.Timestamp.valueOf(bill.uploadDate), bill.status.name,
                bill.ocrRawJson, bill.extractedAmount, bill.extractedDate?.let { java.sql.Date.valueOf(it) },
                bill.extractedProvider, bill.userId, bill.id
            )
            bill
        }
    }

    override fun findById(id: Long): Bill? {
        return jdbcTemplate.query(
            "SELECT id, filename, file_path, upload_date, status, ocr_raw_json, extracted_amount, extracted_date, extracted_provider, user_id FROM bills WHERE id = ?",
            rowMapper, id
        ).firstOrNull()
    }

    override fun findByUserId(userId: Long): List<Bill> {
        return jdbcTemplate.query(
            "SELECT id, filename, file_path, upload_date, status, ocr_raw_json, extracted_amount, extracted_date, extracted_provider, user_id FROM bills WHERE user_id = ?",
            rowMapper, userId
        )
    }

    override fun findAll(): List<Bill> {
        return jdbcTemplate.query(
            "SELECT id, filename, file_path, upload_date, status, ocr_raw_json, extracted_amount, extracted_date, extracted_provider, user_id FROM bills",
            rowMapper
        )
    }

    override fun delete(id: Long): Boolean {
        val rowsAffected = jdbcTemplate.update("DELETE FROM bills WHERE id = ?", id)
        return rowsAffected > 0
    }
}