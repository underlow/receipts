package me.underlow.receipt.dao

import me.underlow.receipt.model.InboxEntity
import me.underlow.receipt.model.InboxState
import me.underlow.receipt.model.EntityType
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

/**
 * RowMapper for converting database rows to InboxEntity objects.
 * Handles nullable fields and proper type conversion for enum values.
 */
class InboxEntityRowMapper : RowMapper<InboxEntity> {
    
    override fun mapRow(rs: ResultSet, rowNum: Int): InboxEntity {
        return InboxEntity(
            id = rs.getString("id"),
            uploadedImage = rs.getString("uploaded_image"),
            uploadDate = rs.getTimestamp("upload_date").toLocalDateTime(),
            ocrResults = rs.getString("ocr_results"), // Nullable field
            linkedEntityId = rs.getString("linked_entity_id"), // Nullable field
            linkedEntityType = rs.getString("linked_entity_type")?.let { EntityType.valueOf(it) }, // Nullable enum
            state = InboxState.valueOf(rs.getString("state")),
            failureReason = rs.getString("failure_reason") // Nullable field
        )
    }
}