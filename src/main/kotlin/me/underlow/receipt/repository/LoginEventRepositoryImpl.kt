package me.underlow.receipt.repository

import me.underlow.receipt.model.LoginEvent
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import java.sql.Statement
import java.time.LocalDateTime

class LoginEventRepositoryImpl(private val jdbcTemplate: JdbcTemplate) : LoginEventRepository {

    private val logger = LoggerFactory.getLogger(LoginEventRepositoryImpl::class.java)

    override fun save(loginEvent: LoginEvent): LoginEvent {
        return if (loginEvent.id == null) {
            logger.debug("Inserting new login event for user ID: {}", loginEvent.userId)
            val keyHolder = GeneratedKeyHolder()
            jdbcTemplate.update({
                connection ->
                val ps = connection.prepareStatement("INSERT INTO login_events (user_id) VALUES (?)", Statement.RETURN_GENERATED_KEYS)
                ps.setLong(1, loginEvent.userId)
                ps
            }, keyHolder)
            val generatedId = keyHolder.keyList.firstOrNull()?.get("id") as? Number
            val savedLoginEvent = loginEvent.copy(id = generatedId?.toLong())
            logger.info("New login event inserted with ID: {}", savedLoginEvent.id)
            savedLoginEvent
        } else {
            logger.debug("Updating login event with ID: {}", loginEvent.id)
            jdbcTemplate.update("UPDATE login_events SET user_id = ? WHERE id = ?",
                loginEvent.userId, loginEvent.id)
            logger.info("Login event with ID {} updated successfully.", loginEvent.id)
            loginEvent
        }
    }
}
