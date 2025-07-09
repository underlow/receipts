package me.underlow.receipt.service

import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.random.Random

/**
 * Service for handling file validation, storage, and naming for uploaded receipt images.
 * Provides core file handling capabilities with security validation and unique filename generation.
 */
@Service
class FileUploadService(
    private val environment: Environment
) {

    companion object {
        private const val MAX_FILE_SIZE_BYTES = 20 * 1024 * 1024L // 20MB
        private const val MAX_FILENAME_LENGTH = 200
        private val ALLOWED_MIME_TYPES = setOf(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
        )
        private val ALLOWED_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "webp")

        // File headers for security validation
        private val JPEG_HEADER = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())
        private val PNG_HEADER = byteArrayOf(0x89.toByte(), 0x50.toByte(), 0x4E.toByte(), 0x47.toByte())
        private val GIF_HEADER = byteArrayOf(0x47.toByte(), 0x49.toByte(), 0x46.toByte())
        private val WEBP_RIFF_HEADER = byteArrayOf(0x52.toByte(), 0x49.toByte(), 0x46.toByte(), 0x46.toByte())
        private val WEBP_WEBP_HEADER = byteArrayOf(0x57.toByte(), 0x45.toByte(), 0x42.toByte(), 0x50.toByte())
    }

    /**
     * Validates uploaded file for type, size, and security requirements.
     * Performs comprehensive validation including MIME type, file size, header validation,
     * and security checks to prevent malicious uploads.
     *
     * @param file MultipartFile to validate
     * @return true if file passes all validation checks, false otherwise
     */
    fun validateFile(file: MultipartFile): Boolean {
        // Check for null or empty file
        if (file.isEmpty || file.size == 0L) {
            return false
        }

        // Check filename exists
        val originalFilename = file.originalFilename
        if (originalFilename.isNullOrBlank()) {
            return false
        }

        // Check MIME type
        val contentType = file.contentType
        if (contentType == null || contentType !in ALLOWED_MIME_TYPES) {
            return false
        }

        // Check file size
        if (file.size > MAX_FILE_SIZE_BYTES) {
            return false
        }

        // Check file extension
        val extension = getFileExtension(originalFilename).lowercase()
        if (extension !in ALLOWED_EXTENSIONS) {
            return false
        }

        // Validate file header matches MIME type
        val fileBytes = try {
            file.bytes
        } catch (e: IOException) {
            return false
        }

        return validateFileHeader(fileBytes, contentType)
    }

    /**
     * Saves uploaded file to configured inbox path with unique filename.
     * Generates unique filename to prevent conflicts and stores file securely.
     *
     * @param file MultipartFile to save
     * @return relative path to saved file
     * @throws IOException if file saving fails
     */
    fun saveFile(file: MultipartFile): String {
        val inboxPath = environment.getProperty("receipts.inbox-path")
            ?: throw IllegalStateException("Inbox path not configured")

        val inboxDir = File(inboxPath)

        // *** FIX: Convert to an absolute path before use ***
        val absoluteInboxDir = if (!inboxDir.isAbsolute) {
            inboxDir.absoluteFile
        } else {
            inboxDir
        }

        // Now, all subsequent operations use the same, unambiguous absolute path.
        if (!absoluteInboxDir.exists()) {
            if (!absoluteInboxDir.mkdirs()) {
                throw IOException("Failed to create storage directory: ${absoluteInboxDir.path}")
            }
        }

        val uniqueFilename = generateUniqueFilename(file.originalFilename)
        // targetFile will now be created with an absolute path
        val targetFile = File(absoluteInboxDir, uniqueFilename)

        try {
            // transferTo() with an absolute path works as expected, saving to the specified location.
            file.transferTo(targetFile)
            return uniqueFilename
        } catch (e: IOException) {
            throw IOException("Failed to save file: ${e.message}", e)
        }
    }

    /**
     * Generates unique filename to prevent conflicts while preserving file extension.
     * Sanitizes filename to prevent directory traversal and other security issues.
     *
     * @param originalFilename original filename from uploaded file
     * @return unique sanitized filename
     */
    fun generateUniqueFilename(originalFilename: String?): String {
        val sanitizedFilename = sanitizeFilename(originalFilename)
        val extension = getFileExtension(sanitizedFilename)
        val baseName = getBaseName(sanitizedFilename)

        // Generate unique identifier using timestamp and UUID
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val uniqueId = UUID.randomUUID().toString().substring(0, 8)
        val randomSuffix = Random.nextInt(1000, 9999)

        val uniqueBaseName = if (baseName.isBlank()) {
            "upload_${timestamp}_${uniqueId}_${randomSuffix}"
        } else {
            "${baseName}_${timestamp}_${uniqueId}_${randomSuffix}"
        }

        return if (extension.isNotBlank()) {
            "$uniqueBaseName.$extension"
        } else {
            uniqueBaseName
        }
    }

    /**
     * Sanitizes filename to prevent directory traversal and other security issues.
     * Removes dangerous characters and limits filename length.
     */
    private fun sanitizeFilename(filename: String?): String {
        if (filename.isNullOrBlank()) {
            return ""
        }

        // Remove directory traversal patterns and dangerous characters
        val sanitized = filename
            .replace(Regex("[/\\\\:*?\"<>|]"), "_")
            .replace(Regex("\\.\\./"), "")
            .replace(Regex("\\.\\.\\\\"), "")
            .replace(Regex("^\\./"), "")
            .replace(Regex("^\\.\\\\"), "")
            .trim()

        // Limit filename length
        return if (sanitized.length > MAX_FILENAME_LENGTH) {
            val extension = getFileExtension(sanitized)
            val baseName = getBaseName(sanitized)
            val maxBaseLength = MAX_FILENAME_LENGTH - extension.length - 1
            if (maxBaseLength > 0) {
                "${baseName.take(maxBaseLength)}.$extension"
            } else {
                sanitized.take(MAX_FILENAME_LENGTH)
            }
        } else {
            sanitized
        }
    }

    /**
     * Extracts file extension from filename.
     */
    private fun getFileExtension(filename: String): String {
        val lastDotIndex = filename.lastIndexOf('.')
        return if (lastDotIndex != -1 && lastDotIndex < filename.length - 1) {
            filename.substring(lastDotIndex + 1)
        } else {
            ""
        }
    }

    /**
     * Extracts base name (without extension) from filename.
     */
    private fun getBaseName(filename: String): String {
        val lastDotIndex = filename.lastIndexOf('.')
        return if (lastDotIndex != -1) {
            filename.substring(0, lastDotIndex)
        } else {
            filename
        }
    }

    /**
     * Validates file header matches expected MIME type for security.
     */
    private fun validateFileHeader(fileBytes: ByteArray, contentType: String): Boolean {
        if (fileBytes.isEmpty()) {
            return false
        }

        return when (contentType) {
            "image/jpeg" -> startsWithBytes(fileBytes, JPEG_HEADER)
            "image/png" -> startsWithBytes(fileBytes, PNG_HEADER)
            "image/gif" -> startsWithBytes(fileBytes, GIF_HEADER)
            "image/webp" -> startsWithBytes(fileBytes, WEBP_RIFF_HEADER) &&
                    containsBytes(fileBytes, WEBP_WEBP_HEADER, 8)

            else -> false
        }
    }

    /**
     * Checks if byte array starts with expected header bytes.
     */
    private fun startsWithBytes(fileBytes: ByteArray, header: ByteArray): Boolean {
        if (fileBytes.size < header.size) {
            return false
        }

        for (i in header.indices) {
            if (fileBytes[i] != header[i]) {
                return false
            }
        }

        return true
    }

    /**
     * Checks if byte array contains expected bytes at specific offset.
     */
    private fun containsBytes(fileBytes: ByteArray, expectedBytes: ByteArray, offset: Int): Boolean {
        if (fileBytes.size < offset + expectedBytes.size) {
            return false
        }

        for (i in expectedBytes.indices) {
            if (fileBytes[offset + i] != expectedBytes[i]) {
                return false
            }
        }

        return true
    }
}
