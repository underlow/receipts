package me.underlow.receipt.service

import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.imageio.ImageIO
import kotlin.random.Random

/**
 * Service for handling avatar upload, resize, and cleanup operations.
 * Provides functionality to upload service provider avatars with automatic resizing to 200x200 pixels.
 */
@Service
class AvatarService(
    private val environment: Environment
) {
    
    companion object {
        private const val AVATAR_SIZE = 200
        private const val MAX_AVATAR_SIZE_BYTES = 10 * 1024 * 1024L // 10MB
        private const val MAX_FILENAME_LENGTH = 200
        private val SUPPORTED_FORMATS = setOf("jpg", "jpeg", "png", "gif", "webp")
        private val SUPPORTED_MIME_TYPES = setOf(
            "image/jpeg",
            "image/png", 
            "image/gif",
            "image/webp"
        )
        
        // File headers for validation
        private val JPEG_HEADER = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())
        private val PNG_HEADER = byteArrayOf(0x89.toByte(), 0x50.toByte(), 0x4E.toByte(), 0x47.toByte())
        private val GIF_HEADER = byteArrayOf(0x47.toByte(), 0x49.toByte(), 0x46.toByte())
        private val WEBP_RIFF_HEADER = byteArrayOf(0x52.toByte(), 0x49.toByte(), 0x46.toByte(), 0x46.toByte())
        private val WEBP_WEBP_HEADER = byteArrayOf(0x57.toByte(), 0x45.toByte(), 0x42.toByte(), 0x50.toByte())
    }
    
    /**
     * Validates uploaded avatar file for format, size, and security requirements.
     * Checks MIME type, file size, supported formats, and file headers.
     * 
     * @param file MultipartFile to validate
     * @return true if file passes all validation checks, false otherwise
     */
    fun validateAvatarFile(file: MultipartFile): Boolean {
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
        if (contentType == null || contentType !in SUPPORTED_MIME_TYPES) {
            return false
        }
        
        // Check file size
        if (file.size > MAX_AVATAR_SIZE_BYTES) {
            return false
        }
        
        // Check file extension
        val extension = getFileExtension(originalFilename).lowercase()
        if (extension !in SUPPORTED_FORMATS) {
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
     * Uploads avatar file and resizes it to 200x200 pixels.
     * Generates unique filename and saves resized avatar to configured path.
     * 
     * @param file MultipartFile to upload and resize
     * @return Unique filename of saved avatar
     * @throws IllegalArgumentException if file validation fails
     * @throws IllegalStateException if avatar path not configured
     * @throws IOException if file processing fails
     */
    fun uploadAndResizeAvatar(file: MultipartFile): String {
        if (!validateAvatarFile(file)) {
            throw IllegalArgumentException("Invalid avatar file")
        }
        
        val avatarPath = getAvatarPath()
        val avatarDir = File(avatarPath)
        
        // Create avatar directory if it doesn't exist
        if (!avatarDir.exists()) {
            if (!avatarDir.mkdirs()) {
                throw IOException("Failed to create avatar directory: ${avatarDir.path}")
            }
        }
        
        // Generate unique filename
        val uniqueFilename = generateUniqueAvatarFilename(file.originalFilename)
        val targetFile = File(avatarDir, uniqueFilename)
        
        try {
            // Read and resize the image
            val originalImage = ImageIO.read(file.inputStream)
            val resizedImage = resizeImage(originalImage, AVATAR_SIZE, AVATAR_SIZE)
            
            // Save the resized image
            val formatName = getImageFormat(file.contentType ?: "image/jpeg")
            ImageIO.write(resizedImage, formatName, targetFile)
            
            return uniqueFilename
        } catch (e: IOException) {
            throw IOException("Failed to process avatar: ${e.message}", e)
        }
    }
    
    /**
     * Cleans up old avatar file when avatar is replaced.
     * Safely removes old avatar file from storage.
     * 
     * @param avatarFilename Filename of old avatar to remove
     */
    fun cleanupOldAvatar(avatarFilename: String?) {
        if (avatarFilename.isNullOrBlank()) {
            return
        }
        
        try {
            val avatarPath = getAvatarPath()
            val avatarFile = File(avatarPath, avatarFilename)
            
            if (avatarFile.exists() && avatarFile.isFile) {
                avatarFile.delete()
            }
        } catch (e: Exception) {
            // Log error but don't throw exception for cleanup operations
            // In production, this would be logged properly
        }
    }
    
    /**
     * Resizes image to specified dimensions while maintaining quality.
     * Uses high-quality scaling algorithms for best results.
     * Preserves transparency for PNG images.
     * 
     * @param originalImage Image to resize
     * @param targetWidth Target width in pixels
     * @param targetHeight Target height in pixels
     * @return Resized image
     */
    fun resizeImage(originalImage: BufferedImage, targetWidth: Int, targetHeight: Int): BufferedImage {
        // Determine image type based on original image to preserve transparency
        val imageType = if (originalImage.colorModel.hasAlpha()) {
            BufferedImage.TYPE_INT_ARGB
        } else {
            BufferedImage.TYPE_INT_RGB
        }
        
        val resizedImage = BufferedImage(targetWidth, targetHeight, imageType)
        val graphics = resizedImage.createGraphics()
        
        // Set high-quality rendering hints
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY)
        graphics.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE)
        graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY)
        
        // Draw the scaled image
        graphics.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null)
        graphics.dispose()
        
        return resizedImage
    }
    
    /**
     * Generates unique filename for avatar to prevent conflicts.
     * Preserves original extension and adds timestamp and UUID.
     * 
     * @param originalFilename Original filename from uploaded file
     * @return Unique sanitized filename
     */
    fun generateUniqueAvatarFilename(originalFilename: String?): String {
        val sanitizedFilename = sanitizeFilename(originalFilename)
        val extension = getFileExtension(sanitizedFilename).ifBlank { "jpg" }
        val baseName = getBaseName(sanitizedFilename).ifBlank { "avatar" }
        
        // Generate unique identifier
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val uniqueId = UUID.randomUUID().toString().substring(0, 8)
        val randomSuffix = Random.nextInt(1000, 9999)
        
        return "${baseName}_${timestamp}_${uniqueId}_${randomSuffix}.${extension}"
    }
    
    /**
     * Gets avatar storage path from configuration.
     * 
     * @return Avatar storage path
     * @throws IllegalStateException if avatar path not configured
     */
    private fun getAvatarPath(): String {
        val attachmentsPath = environment.getProperty("receipts.attachments-path")
            ?: throw IllegalStateException("Avatar path not configured")
        
        return "$attachmentsPath/avatars"
    }
    
    /**
     * Sanitizes filename to prevent security issues.
     * Removes dangerous characters and limits length.
     */
    private fun sanitizeFilename(filename: String?): String {
        if (filename.isNullOrBlank()) {
            return ""
        }
        
        val sanitized = filename
            .replace(Regex("[/\\\\:*?\"<>|]"), "_")
            .replace(Regex("\\.\\./"), "")
            .replace(Regex("\\.\\.\\\\"), "")
            .replace(Regex("^\\./"), "")
            .replace(Regex("^\\.\\\\"), "")
            .trim()
        
        return if (sanitized.length > MAX_FILENAME_LENGTH) {
            val extension = getFileExtension(sanitized)
            val baseName = getBaseName(sanitized)
            val maxBaseLength = MAX_FILENAME_LENGTH - extension.length - 1
            if (maxBaseLength > 0) {
                "${baseName.take(maxBaseLength)}.${extension}"
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
     * Gets image format name from MIME type.
     */
    private fun getImageFormat(mimeType: String): String {
        return when (mimeType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/gif" -> "gif"
            "image/webp" -> "webp"
            else -> "jpg"
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