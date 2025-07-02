package me.underlow.receipt.repository

import me.underlow.receipt.model.Payment
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import java.math.BigDecimal
import java.sql.Statement

class PaymentRepositoryImpl(private val jdbcTemplate: JdbcTemplate) : PaymentRepository {

    private val rowMapper = RowMapper<Payment> { rs, _ ->
        Payment(
            id = rs.getLong("id"),
            serviceProviderId = rs.getLong("service_provider_id"),
            paymentMethodId = rs.getLong("payment_method_id"),
            amount = rs.getBigDecimal("amount"),
            currency = rs.getString("currency"),
            invoiceDate = rs.getDate("invoice_date").toLocalDate(),
            paymentDate = rs.getDate("payment_date").toLocalDate(),
            billId = rs.getObject("bill_id") as? Long,
            userId = rs.getLong("user_id"),
            comment = rs.getString("comment")
        )
    }

    override fun save(payment: Payment): Payment {
        return if (payment.id == null) {
            val keyHolder = GeneratedKeyHolder()
            jdbcTemplate.update({
                connection ->
                val ps = connection.prepareStatement(
                    "INSERT INTO payments (service_provider_id, payment_method_id, amount, currency, invoice_date, payment_date, bill_id, user_id, comment) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
                )
                ps.setLong(1, payment.serviceProviderId)
                ps.setLong(2, payment.paymentMethodId)
                ps.setBigDecimal(3, payment.amount)
                ps.setString(4, payment.currency)
                ps.setDate(5, java.sql.Date.valueOf(payment.invoiceDate))
                ps.setDate(6, java.sql.Date.valueOf(payment.paymentDate))
                ps.setObject(7, payment.billId)
                ps.setLong(8, payment.userId)
                ps.setString(9, payment.comment)
                ps
            }, keyHolder)
            val generatedId = keyHolder.keyList.firstOrNull()?.get("id") as? Number
            payment.copy(id = generatedId?.toLong())
        } else {
            jdbcTemplate.update(
                "UPDATE payments SET service_provider_id = ?, payment_method_id = ?, amount = ?, currency = ?, invoice_date = ?, payment_date = ?, bill_id = ?, user_id = ?, comment = ? WHERE id = ?",
                payment.serviceProviderId, payment.paymentMethodId, payment.amount, payment.currency,
                java.sql.Date.valueOf(payment.invoiceDate), java.sql.Date.valueOf(payment.paymentDate),
                payment.billId, payment.userId, payment.comment, payment.id
            )
            payment
        }
    }

    override fun findById(id: Long): Payment? {
        return jdbcTemplate.query(
            "SELECT id, service_provider_id, payment_method_id, amount, currency, invoice_date, payment_date, bill_id, user_id, comment FROM payments WHERE id = ?",
            rowMapper, id
        ).firstOrNull()
    }

    override fun findByUserId(userId: Long): List<Payment> {
        return jdbcTemplate.query(
            "SELECT id, service_provider_id, payment_method_id, amount, currency, invoice_date, payment_date, bill_id, user_id, comment FROM payments WHERE user_id = ?",
            rowMapper, userId
        )
    }

    override fun findByServiceProviderId(serviceProviderId: Long): List<Payment> {
        return jdbcTemplate.query(
            "SELECT id, service_provider_id, payment_method_id, amount, currency, invoice_date, payment_date, bill_id, user_id, comment FROM payments WHERE service_provider_id = ?",
            rowMapper, serviceProviderId
        )
    }

    override fun findAll(): List<Payment> {
        return jdbcTemplate.query(
            "SELECT id, service_provider_id, payment_method_id, amount, currency, invoice_date, payment_date, bill_id, user_id, comment FROM payments",
            rowMapper
        )
    }

    override fun delete(id: Long): Boolean {
        val rowsAffected = jdbcTemplate.update("DELETE FROM payments WHERE id = ?", id)
        return rowsAffected > 0
    }
}