package me.underlow.receipt.repository

import me.underlow.receipt.model.PaymentMethod
import me.underlow.receipt.model.PaymentMethodType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import java.sql.Statement

class PaymentMethodRepositoryImpl(private val jdbcTemplate: JdbcTemplate) : PaymentMethodRepository {

    private val rowMapper = RowMapper<PaymentMethod> { rs, _ ->
        PaymentMethod(
            id = rs.getLong("id"),
            name = rs.getString("name"),
            type = PaymentMethodType.valueOf(rs.getString("type")),
            comment = rs.getString("comment")
        )
    }

    override fun save(paymentMethod: PaymentMethod): PaymentMethod {
        return if (paymentMethod.id == null) {
            val keyHolder = GeneratedKeyHolder()
            jdbcTemplate.update({
                connection ->
                val ps = connection.prepareStatement(
                    "INSERT INTO payment_methods (name, type, comment) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
                )
                ps.setString(1, paymentMethod.name)
                ps.setString(2, paymentMethod.type.name)
                ps.setString(3, paymentMethod.comment)
                ps
            }, keyHolder)
            val generatedId = keyHolder.keyList.firstOrNull()?.get("id") as? Number
            paymentMethod.copy(id = generatedId?.toLong())
        } else {
            jdbcTemplate.update(
                "UPDATE payment_methods SET name = ?, type = ?, comment = ? WHERE id = ?",
                paymentMethod.name, paymentMethod.type.name, paymentMethod.comment, paymentMethod.id
            )
            paymentMethod
        }
    }

    override fun findById(id: Long): PaymentMethod? {
        return jdbcTemplate.query(
            "SELECT id, name, type, comment FROM payment_methods WHERE id = ?",
            rowMapper, id
        ).firstOrNull()
    }

    override fun findAll(): List<PaymentMethod> {
        return jdbcTemplate.query(
            "SELECT id, name, type, comment FROM payment_methods",
            rowMapper
        )
    }

    override fun delete(id: Long): Boolean {
        val rowsAffected = jdbcTemplate.update("DELETE FROM payment_methods WHERE id = ?", id)
        return rowsAffected > 0
    }
}