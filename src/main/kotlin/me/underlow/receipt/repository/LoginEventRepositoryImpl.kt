package me.underlow.receipt.repository

import me.underlow.receipt.model.LoginEvent
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import java.sql.Statement
import java.time.LocalDateTime

class LoginEventRepositoryImpl(private val jdbcTemplate: JdbcTemplate) : LoginEventRepository {

    override fun save(loginEvent: LoginEvent): LoginEvent {
        return if (loginEvent.id == null) {
            // Insert new login event - let database handle timestamp
            val keyHolder = GeneratedKeyHolder()
            jdbcTemplate.update({
                connection ->
                val ps = connection.prepareStatement("INSERT INTO login_events (user_id) VALUES (?)", Statement.RETURN_GENERATED_KEYS)
                ps.setLong(1, loginEvent.userId)
                ps
            }, keyHolder)
            val generatedId = keyHolder.keyList.firstOrNull()?.get("id") as? Number
            loginEvent.copy(id = generatedId?.toLong())
        } else {
            jdbcTemplate.update("UPDATE login_events SET user_id = ? WHERE id = ?",
                loginEvent.userId, loginEvent.id)
            loginEvent
        }
    }
}
