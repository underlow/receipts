package me.underlow.receipt.dao

import me.underlow.receipt.model.ServiceProvider
import me.underlow.receipt.model.ServiceProviderState
import me.underlow.receipt.model.RegularFrequency
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

/**
 * RowMapper for converting database rows to ServiceProvider objects.
 * Handles nullable fields, enum conversions, and proper type conversion.
 */
class ServiceProviderRowMapper : RowMapper<ServiceProvider> {
    
    override fun mapRow(rs: ResultSet, rowNum: Int): ServiceProvider {
        return ServiceProvider(
            id = rs.getLong("id"),
            name = rs.getString("name"),
            avatar = rs.getString("avatar"), // Nullable field
            comment = rs.getString("comment"), // Nullable field
            commentForOcr = rs.getString("comment_for_ocr"), // Nullable field
            regular = RegularFrequency.valueOf(rs.getString("regular")),
            customFields = rs.getString("custom_fields"), // Nullable field
            state = ServiceProviderState.valueOf(rs.getString("state")),
            createdDate = rs.getTimestamp("created_date").toLocalDateTime(),
            modifiedDate = rs.getTimestamp("modified_date").toLocalDateTime()
        )
    }
}