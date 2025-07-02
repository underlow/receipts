package me.underlow.receipt.service

import me.underlow.receipt.model.IncomingFile
import me.underlow.receipt.model.BillStatus
import me.underlow.receipt.repository.IncomingFileRepository
import me.underlow.receipt.repository.UserRepository
import org.springframework.stereotype.Service

/**
 * Business logic service for managing IncomingFile entities
 */
@Service
class IncomingFileService(
    private val incomingFileRepository: IncomingFileRepository,
    private val userRepository: UserRepository
) {

    /**
     * Finds an IncomingFile by ID and verifies user ownership via email
     */
    fun findByIdAndUserEmail(fileId: Long, userEmail: String): IncomingFile? {
        val user = userRepository.findByEmail(userEmail) ?: return null
        val incomingFile = incomingFileRepository.findById(fileId) ?: return null
        
        return if (incomingFile.userId == user.id) incomingFile else null
    }

    /**
     * Finds all IncomingFiles for a user by email with optional status filtering
     */
    fun findByUserEmailAndStatus(userEmail: String, status: BillStatus? = null): List<IncomingFile> {
        val user = userRepository.findByEmail(userEmail) ?: return emptyList()
        
        return if (status != null) {
            incomingFileRepository.findByUserId(user.id!!).filter { it.status == status }
        } else {
            incomingFileRepository.findByUserId(user.id!!)
        }
    }

    /**
     * Finds all IncomingFiles for a user by email with pagination support
     */
    fun findByUserEmailWithPagination(
        userEmail: String, 
        status: BillStatus? = null,
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "uploadDate",
        sortDirection: String = "desc"
    ): Pair<List<IncomingFile>, Long> {
        val user = userRepository.findByEmail(userEmail) ?: return Pair(emptyList(), 0L)
        val userId = user.id!!
        
        // Get all files for the user first (in a real implementation, this would be optimized with database-level pagination)
        val allFiles = if (status != null) {
            incomingFileRepository.findByUserId(userId).filter { it.status == status }
        } else {
            incomingFileRepository.findByUserId(userId)
        }
        
        // Sort the files
        val sortedFiles = when (sortBy) {
            "filename" -> if (sortDirection == "asc") allFiles.sortedBy { it.filename } else allFiles.sortedByDescending { it.filename }
            "status" -> if (sortDirection == "asc") allFiles.sortedBy { it.status.name } else allFiles.sortedByDescending { it.status.name }
            "uploadDate" -> if (sortDirection == "asc") allFiles.sortedBy { it.uploadDate } else allFiles.sortedByDescending { it.uploadDate }
            else -> allFiles.sortedByDescending { it.uploadDate }
        }
        
        // Apply pagination
        val startIndex = page * size
        val endIndex = minOf(startIndex + size, sortedFiles.size)
        val paginatedFiles = if (startIndex < sortedFiles.size) {
            sortedFiles.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
        
        return Pair(paginatedFiles, sortedFiles.size.toLong())
    }

    /**
     * Updates the status of an IncomingFile if user owns it
     */
    fun updateStatus(fileId: Long, userEmail: String, newStatus: BillStatus): Boolean {
        val incomingFile = findByIdAndUserEmail(fileId, userEmail) ?: return false
        
        val updatedFile = incomingFile.copy(status = newStatus)
        incomingFileRepository.save(updatedFile)
        return true
    }

    /**
     * Deletes an IncomingFile if user owns it
     */
    fun deleteFile(fileId: Long, userEmail: String): Boolean {
        findByIdAndUserEmail(fileId, userEmail) ?: return false
        return incomingFileRepository.delete(fileId)
    }

    /**
     * Gets file statistics for a user
     */
    fun getFileStatistics(userEmail: String): Map<BillStatus, Int> {
        val files = findByUserEmailAndStatus(userEmail)
        val groupedFiles = files.groupBy { it.status }.mapValues { it.value.size }
        
        // Ensure all statuses are present with default value 0
        return BillStatus.values().associateWith { status ->
            groupedFiles[status] ?: 0
        }
    }
}