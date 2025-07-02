package me.underlow.receipt.repository

import me.underlow.receipt.model.IncomingFile
import me.underlow.receipt.model.BillStatus

interface IncomingFileRepository {
    fun save(incomingFile: IncomingFile): IncomingFile
    fun findById(id: Long): IncomingFile?
    fun findByUserId(userId: Long): List<IncomingFile>
    fun findByChecksum(checksum: String): IncomingFile?
    fun findByStatus(status: BillStatus): List<IncomingFile>
    fun findAll(): List<IncomingFile>
    fun delete(id: Long): Boolean
}