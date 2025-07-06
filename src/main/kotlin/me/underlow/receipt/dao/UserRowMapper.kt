package me.underlow.receipt.dao

import me.underlow.receipt.model.User
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.time.LocalDateTime

/**
 * RowMapper for converting database rows to User objects.
 * Handles nullable fields and proper type conversion.
 */
class UserRowMapper : RowMapper<User> {
    
    override fun mapRow(rs: ResultSet, rowNum: Int): User {
        return User(
            id = rs.getLong("id"),
            email = rs.getString("email"),
            name = rs.getString("name"),
            avatar = rs.getString("avatar"), // Nullable field
            createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
            updatedAt = rs.getTimestamp("updated_at").toLocalDateTime()
        )
    }
}