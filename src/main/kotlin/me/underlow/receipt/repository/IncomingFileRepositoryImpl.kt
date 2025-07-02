package me.underlow.receipt.repository

import me.underlow.receipt.model.IncomingFile
import me.underlow.receipt.model.BillStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import java.sql.Statement

class IncomingFileRepositoryImpl(private val jdbcTemplate: JdbcTemplate) : IncomingFileRepository {

    private val rowMapper = RowMapper<IncomingFile> { rs, _ ->
        IncomingFile(
            id = rs.getLong("id"),
            filename = rs.getString("filename"),
            filePath = rs.getString("file_path"),
            uploadDate = rs.getTimestamp("upload_date").toLocalDateTime(),
            status = BillStatus.valueOf(rs.getString("status")),
            checksum = rs.getString("checksum"),
            userId = rs.getLong("user_id")
        )
    }

    override fun save(incomingFile: IncomingFile): IncomingFile {
        return if (incomingFile.id == null) {
            val keyHolder = GeneratedKeyHolder()
            jdbcTemplate.update({
                connection ->
                val ps = connection.prepareStatement(
                    "INSERT INTO incoming_files (filename, file_path, upload_date, status, checksum, user_id) VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
                )
                ps.setString(1, incomingFile.filename)
                ps.setString(2, incomingFile.filePath)
                ps.setTimestamp(3, java.sql.Timestamp.valueOf(incomingFile.uploadDate))
                ps.setString(4, incomingFile.status.name)
                ps.setString(5, incomingFile.checksum)
                ps.setLong(6, incomingFile.userId)
                ps
            }, keyHolder)
            val generatedId = keyHolder.keyList.firstOrNull()?.get("id") as? Number
            incomingFile.copy(id = generatedId?.toLong())
        } else {
            jdbcTemplate.update(
                "UPDATE incoming_files SET filename = ?, file_path = ?, upload_date = ?, status = ?, checksum = ?, user_id = ? WHERE id = ?",
                incomingFile.filename, incomingFile.filePath, java.sql.Timestamp.valueOf(incomingFile.uploadDate), 
                incomingFile.status.name, incomingFile.checksum, incomingFile.userId, incomingFile.id
            )
            incomingFile
        }
    }

    override fun findById(id: Long): IncomingFile? {
        return jdbcTemplate.query(
            "SELECT id, filename, file_path, upload_date, status, checksum, user_id FROM incoming_files WHERE id = ?",
            rowMapper, id
        ).firstOrNull()
    }

    override fun findByUserId(userId: Long): List<IncomingFile> {
        return jdbcTemplate.query(
            "SELECT id, filename, file_path, upload_date, status, checksum, user_id FROM incoming_files WHERE user_id = ?",
            rowMapper, userId
        )
    }

    override fun findByChecksum(checksum: String): IncomingFile? {
        return jdbcTemplate.query(
            "SELECT id, filename, file_path, upload_date, status, checksum, user_id FROM incoming_files WHERE checksum = ?",
            rowMapper, checksum
        ).firstOrNull()
    }

    override fun findByStatus(status: BillStatus): List<IncomingFile> {
        return jdbcTemplate.query(
            "SELECT id, filename, file_path, upload_date, status, checksum, user_id FROM incoming_files WHERE status = ?",
            rowMapper, status.name
        )
    }

    override fun findAll(): List<IncomingFile> {
        return jdbcTemplate.query(
            "SELECT id, filename, file_path, upload_date, status, checksum, user_id FROM incoming_files",
            rowMapper
        )
    }

    override fun delete(id: Long): Boolean {
        val rowsAffected = jdbcTemplate.update("DELETE FROM incoming_files WHERE id = ?", id)
        return rowsAffected > 0
    }
}