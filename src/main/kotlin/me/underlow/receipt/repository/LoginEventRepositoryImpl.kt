package me.underlow.receipt.repository

import me.underlow.receipt.model.LoginEvent
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import java.sql.Statement
import java.time.LocalDateTime

class LoginEventRepositoryImpl(private val jdbcTemplate: JdbcTemplate) : LoginEventRepository {

    override fun save(loginEvent: LoginEvent): LoginEvent {
        return if (loginEvent.id == null) {
            // Insert new login event - let database handle timestamp if not provided
            val keyHolder = GeneratedKeyHolder()
            if (loginEvent.ipAddress != null) {
                jdbcTemplate.update({
                    connection ->
                    val ps = connection.prepareStatement("INSERT INTO login_events (user_id, ip_address) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)
                    ps.setLong(1, loginEvent.userId)
                    ps.setString(2, loginEvent.ipAddress)
                    ps
                }, keyHolder)
            } else {
                jdbcTemplate.update({
                    connection ->
                    val ps = connection.prepareStatement("INSERT INTO login_events (user_id) VALUES (?)", Statement.RETURN_GENERATED_KEYS)
                    ps.setLong(1, loginEvent.userId)
                    ps
                }, keyHolder)
            }
            val generatedId = keyHolder.keyList.firstOrNull()?.get("id") as? Number
            loginEvent.copy(id = generatedId?.toLong())
        } else {
            jdbcTemplate.update("UPDATE login_events SET user_id = ?, ip_address = ? WHERE id = ?",
                loginEvent.userId, loginEvent.ipAddress, loginEvent.id)
            loginEvent
        }
    }
}
