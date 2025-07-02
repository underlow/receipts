package me.underlow.receipt.repository

import me.underlow.receipt.model.Receipt
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import java.sql.Statement

class ReceiptRepositoryImpl(private val jdbcTemplate: JdbcTemplate) : ReceiptRepository {

    private val rowMapper = RowMapper<Receipt> { rs, _ ->
        Receipt(
            id = rs.getLong("id"),
            userId = rs.getLong("user_id"),
            billId = rs.getObject("bill_id") as? Long
        )
    }

    override fun save(receipt: Receipt): Receipt {
        return if (receipt.id == null) {
            val keyHolder = GeneratedKeyHolder()
            jdbcTemplate.update({
                connection ->
                val ps = connection.prepareStatement(
                    "INSERT INTO receipts (user_id, bill_id) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS
                )
                ps.setLong(1, receipt.userId)
                ps.setObject(2, receipt.billId)
                ps
            }, keyHolder)
            val generatedId = keyHolder.keyList.firstOrNull()?.get("id") as? Number
            receipt.copy(id = generatedId?.toLong())
        } else {
            jdbcTemplate.update(
                "UPDATE receipts SET user_id = ?, bill_id = ? WHERE id = ?",
                receipt.userId, receipt.billId, receipt.id
            )
            receipt
        }
    }

    override fun findById(id: Long): Receipt? {
        return jdbcTemplate.query(
            "SELECT id, user_id, bill_id FROM receipts WHERE id = ?",
            rowMapper, id
        ).firstOrNull()
    }

    override fun findByUserId(userId: Long): List<Receipt> {
        return jdbcTemplate.query(
            "SELECT id, user_id, bill_id FROM receipts WHERE user_id = ?",
            rowMapper, userId
        )
    }

    override fun findByBillId(billId: Long): List<Receipt> {
        return jdbcTemplate.query(
            "SELECT id, user_id, bill_id FROM receipts WHERE bill_id = ?",
            rowMapper, billId
        )
    }

    override fun findAll(): List<Receipt> {
        return jdbcTemplate.query(
            "SELECT id, user_id, bill_id FROM receipts",
            rowMapper
        )
    }

    override fun delete(id: Long): Boolean {
        val rowsAffected = jdbcTemplate.update("DELETE FROM receipts WHERE id = ?", id)
        return rowsAffected > 0
    }
}