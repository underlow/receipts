package me.underlow.receipt.repository

import me.underlow.receipt.model.LoginEvent
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import java.sql.Statement
import java.time.LocalDateTime

class LoginEventRepositoryImpl(private val jdbcTemplate: JdbcTemplate) : LoginEventRepository {

    override fun save(loginEvent: LoginEvent): LoginEvent {
        return if (loginEvent.id == null) {
            val keyHolder = GeneratedKeyHolder()
            jdbcTemplate.update({
                connection ->
                val ps = connection.prepareStatement("INSERT INTO login_events (user_id, timestamp, ip_address) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)
                ps.setLong(1, loginEvent.userId)
                ps.setTimestamp(2, java.sql.Timestamp.valueOf(loginEvent.timestamp))
                ps.setString(3, loginEvent.ipAddress)
                ps
            }, keyHolder)
            loginEvent.copy(id = keyHolder.key?.toLong())
        } else {
            jdbcTemplate.update("UPDATE login_events SET user_id = ?, timestamp = ?, ip_address = ? WHERE id = ?",
                loginEvent.userId, java.sql.Timestamp.valueOf(loginEvent.timestamp), loginEvent.ipAddress, loginEvent.id)
            loginEvent
        }
    }
}
