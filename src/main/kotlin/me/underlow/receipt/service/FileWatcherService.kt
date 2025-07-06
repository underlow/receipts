package me.underlow.receipt.service

import me.underlow.receipt.config.ReceiptsProperties
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

/**
 * Service that monitors the inbox directory for new files every 30 seconds.
 * When new files are detected, they are processed through the FileProcessingService.
 */
@Service
class FileWatcherService(
    private val fileProcessingService: FileProcessingService,
    private val receiptsProperties: ReceiptsProperties
) {
    private val logger = LoggerFactory.getLogger(FileWatcherService::class.java)
    
    // Default user ID for files found in inbox (in real implementation, this could be 
    // determined by folder structure or configuration)
    private val defaultUserId: Long = 1L

    /**
     * Scheduled task that runs every 30 seconds to scan the inbox directory for new files.
     */
    @Scheduled(fixedDelay = 30000) // 30 seconds
    fun scanInboxDirectory() {
        try {
            val inboxPath = Path.of(receiptsProperties.inboxPath)
            
            // Create inbox directory if it doesn't exist
            if (!Files.exists(inboxPath)) {
                logger.debug("Creating inbox directory: $inboxPath")
                Files.createDirectories(inboxPath)
                return
            }
            
            // Check if directory is readable
            if (!Files.isReadable(inboxPath)) {
                logger.error("Inbox directory is not readable: $inboxPath")
                return
            }
            
            logger.debug("Scanning inbox directory: $inboxPath")
            
            // Get all files in the inbox directory
            val files = getFilesInDirectory(inboxPath.toFile())
            
            if (files.isEmpty()) {
                logger.debug("No files found in inbox directory")
                return
            }
            
            logger.info("Found ${files.size} files in inbox directory")
            
            // Process each file
            files.forEach { file ->
                processInboxFile(file)
            }
            
        } catch (e: Exception) {
            logger.error("Error scanning inbox directory", e)
        }
    }

    /**
     * Processes a single file found in the inbox directory.
     */
    private fun processInboxFile(file: File) {
        try {
            logger.debug("Processing inbox file: ${file.name}")
            
            // Check if file is ready for processing
            if (!fileProcessingService.isFileReadyForProcessing(file)) {
                logger.debug("File ${file.name} is not ready for processing, skipping")
                return
            }
            
            // Process the file
            val incomingFile = fileProcessingService.processFile(file, defaultUserId)
            
            if (incomingFile != null) {
                logger.info("Successfully processed file: ${file.name}, created IncomingFile with ID: ${incomingFile.id}")
            } else {
                logger.warn("Failed to process file: ${file.name} (possibly duplicate)")
            }
            
        } catch (e: Exception) {
            logger.error("Error processing inbox file: ${file.name}", e)
        }
    }

    /**
     * Gets all regular files from the specified directory (non-recursive).
     * Filters out hidden files, directories, and system files.
     */
    private fun getFilesInDirectory(directory: File): List<File> {
        return try {
            directory.listFiles { file ->
                // Only include regular files, not directories or hidden files
                file.isFile && 
                !file.isHidden && 
                !file.name.startsWith(".") &&
                file.canRead()
            }?.toList() ?: emptyList()
        } catch (e: Exception) {
            logger.error("Error listing files in directory: ${directory.absolutePath}", e)
            emptyList()
        }
    }

    /**
     * Manual trigger for scanning the inbox directory (useful for testing or manual operations).
     */
    fun triggerScan() {
        logger.info("Manual scan triggered")
        scanInboxDirectory()
    }

    /**
     * Gets the current inbox path being monitored.
     */
    fun getInboxPath(): String {
        return receiptsProperties.inboxPath
    }

    /**
     * Checks if the inbox directory exists and is accessible.
     */
    fun isInboxAccessible(): Boolean {
        return try {
            val inboxPath = Path.of(receiptsProperties.inboxPath)
            Files.exists(inboxPath) && Files.isReadable(inboxPath) && Files.isDirectory(inboxPath)
        } catch (e: Exception) {
            logger.error("Error checking inbox accessibility", e)
            false
        }
    }
}