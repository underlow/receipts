package me.underlow.receipt.service

import me.underlow.receipt.config.ReceiptsProperties
import me.underlow.receipt.model.BillStatus
import me.underlow.receipt.model.IncomingFile
import me.underlow.receipt.repository.IncomingFileRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Service responsible for processing files detected in the inbox directory.
 * Handles file movement, checksum calculation, and IncomingFile entity creation.
 */
@Service
class FileProcessingService(
    private val incomingFileRepository: IncomingFileRepository,
    private val receiptsProperties: ReceiptsProperties,
    private val incomingFileOcrService: IncomingFileOcrService
) {
    private val logger = LoggerFactory.getLogger(FileProcessingService::class.java)

    /**
     * Processes a file from the inbox directory - calculates checksum, moves to storage,
     * creates an IncomingFile entity in PENDING status, and triggers OCR processing.
     */
    fun processFile(file: File, userId: Long): IncomingFile? {
        return try {
            logger.info("Processing file: ${file.name}")

            // Calculate file checksum first to detect duplicates
            val checksum = calculateFileChecksum(file)

            // Check if file already exists by checksum
            val existingFile = incomingFileRepository.findByChecksum(checksum)
            if (existingFile != null) {
                logger.warn("File ${file.name} already exists with checksum $checksum, skipping")
                return null
            }

            // Generate storage path and move file
            val storagePath = generateStoragePath(file.name)
            val movedFile = moveFileToStorage(file, storagePath)

            // Create IncomingFile entity
            val incomingFile = IncomingFile(
                filename = file.name,
                filePath = movedFile.absolutePath,
                uploadDate = LocalDateTime.now(),
                status = BillStatus.PENDING,
                checksum = checksum,
                userId = userId
            )

            val savedFile = incomingFileRepository.save(incomingFile)
            logger.info("Successfully processed file: ${file.name}, created IncomingFile with ID: ${savedFile.id}")
            
            // Trigger OCR processing if available
            if (incomingFileOcrService.isOcrProcessingAvailable()) {
                logger.info("Triggering OCR processing for file: ${file.name}")
                try {
                    incomingFileOcrService.processIncomingFile(savedFile)
                } catch (e: Exception) {
                    logger.error("Error during OCR processing for file: ${file.name}", e)
                    // Continue execution - file is still processed even if OCR fails
                }
            } else {
                logger.warn("OCR processing not available for file: ${file.name}")
            }
            
            savedFile

        } catch (e: Exception) {
            logger.error("Error processing file: ${file.name}", e)
            null
        }
    }

    /**
     * Calculates SHA-256 checksum for file content to enable duplicate detection.
     */
    fun calculateFileChecksum(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = Files.readAllBytes(file.toPath())
        val hashBytes = digest.digest(bytes)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Generates organized storage path based on current date and filename.
     * Creates files in format: /attachments/yyyy-MM-dd-filename
     * Handles duplicates by adding -1, -2, etc. before the file extension.
     */
    fun generateStoragePath(filename: String): Path {
        val now = LocalDateTime.now()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val datePath = now.format(dateFormatter)

        val attachmentsPath = Path.of(receiptsProperties.attachmentsPath)

        // Create attachments directory if it doesn't exist
        Files.createDirectories(attachmentsPath)

        // Generate unique filename with date prefix
        val uniqueFilename = generateUniqueFilename(attachmentsPath, datePath, filename)

        return attachmentsPath.resolve(uniqueFilename)
    }

    /**
     * Generates a unique filename by adding incremental suffixes if duplicates exist.
     * Format: yyyy-MM-dd-filename or yyyy-MM-dd-filename-1, yyyy-MM-dd-filename-2, etc.
     */
    private fun generateUniqueFilename(basePath: Path, datePath: String, originalFilename: String): String {
        val fileNameWithoutExtension = originalFilename.substringBeforeLast(".")
        val fileExtension = if (originalFilename.contains(".")) {
            ".${originalFilename.substringAfterLast(".")}"
        } else {
            ""
        }

        var counter = 0
        var candidateFilename: String

        do {
            candidateFilename = if (counter == 0) {
                "$datePath-$fileNameWithoutExtension$fileExtension"
            } else {
                "$datePath-$fileNameWithoutExtension-$counter$fileExtension"
            }
            counter++
        } while (Files.exists(basePath.resolve(candidateFilename)))

        return candidateFilename
    }

    /**
     * Moves file from inbox to permanent storage location.
     */
    fun moveFileToStorage(sourceFile: File, targetPath: Path): File {
        logger.debug("Moving file from ${sourceFile.absolutePath} to $targetPath")

        // Ensure target directory exists
        Files.createDirectories(targetPath.parent)

        // Move file to target location
        Files.move(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING)

        logger.debug("Successfully moved file to $targetPath")
        return targetPath.toFile()
    }

    /**
     * Checks if a file is ready for processing (not locked, has proper extension).
     */
    fun isFileReadyForProcessing(file: File): Boolean {
        // Check if file is readable and not empty
        if (!file.canRead() || file.length() == 0L) {
            logger.warn("File ${file.name} is not readable or empty")
            return false
        }

        // Check for supported file extensions (images, PDFs)
        val supportedExtensions = listOf("pdf", "jpg", "jpeg", "png", "gif", "bmp", "tiff")
        val fileExtension = file.extension.lowercase()

        if (fileExtension !in supportedExtensions) {
            logger.warn("File ${file.name} has unsupported extension: $fileExtension")
            return false
        }

        // Try to check if file is locked by another process
        return try {
            file.renameTo(file) // This will fail if file is locked
        } catch (e: Exception) {
            logger.warn("File ${file.name} appears to be locked: ${e.message}")
            false
        }
    }
}
