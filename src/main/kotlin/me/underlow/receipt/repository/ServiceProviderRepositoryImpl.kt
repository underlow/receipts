package me.underlow.receipt.repository

import me.underlow.receipt.model.ServiceProvider
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import java.sql.Statement

class ServiceProviderRepositoryImpl(private val jdbcTemplate: JdbcTemplate) : ServiceProviderRepository {

    private val rowMapper = RowMapper<ServiceProvider> { rs, _ ->
        ServiceProvider(
            id = rs.getLong("id"),
            name = rs.getString("name"),
            category = rs.getString("category"),
            defaultPaymentMethod = rs.getString("default_payment_method"),
            isActive = rs.getBoolean("is_active"),
            comment = rs.getString("comment")
        )
    }

    override fun save(serviceProvider: ServiceProvider): ServiceProvider {
        return if (serviceProvider.id == null) {
            val keyHolder = GeneratedKeyHolder()
            jdbcTemplate.update({
                connection ->
                val ps = connection.prepareStatement(
                    "INSERT INTO service_providers (name, category, default_payment_method, is_active, comment) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
                )
                ps.setString(1, serviceProvider.name)
                ps.setString(2, serviceProvider.category)
                ps.setString(3, serviceProvider.defaultPaymentMethod)
                ps.setBoolean(4, serviceProvider.isActive)
                ps.setString(5, serviceProvider.comment)
                ps
            }, keyHolder)
            val generatedId = keyHolder.keyList.firstOrNull()?.get("id") as? Number
            serviceProvider.copy(id = generatedId?.toLong())
        } else {
            jdbcTemplate.update(
                "UPDATE service_providers SET name = ?, category = ?, default_payment_method = ?, is_active = ?, comment = ? WHERE id = ?",
                serviceProvider.name, serviceProvider.category, serviceProvider.defaultPaymentMethod, 
                serviceProvider.isActive, serviceProvider.comment, serviceProvider.id
            )
            serviceProvider
        }
    }

    override fun findById(id: Long): ServiceProvider? {
        return jdbcTemplate.query(
            "SELECT id, name, category, default_payment_method, is_active, comment FROM service_providers WHERE id = ?",
            rowMapper, id
        ).firstOrNull()
    }

    override fun findAll(): List<ServiceProvider> {
        return jdbcTemplate.query(
            "SELECT id, name, category, default_payment_method, is_active, comment FROM service_providers",
            rowMapper
        )
    }

    override fun delete(id: Long): Boolean {
        val rowsAffected = jdbcTemplate.update("DELETE FROM service_providers WHERE id = ?", id)
        return rowsAffected > 0
    }
}