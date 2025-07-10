package me.underlow.receipt.e2e.helpers

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.awt.Color
import java.awt.Graphics2D

/**
 * Helper class for managing test files and upload operations in e2e tests.
 * Provides utilities for creating, managing, and cleaning up test files.
 * Follows best practices for file management and test isolation.
 */
class UploadHelper {
    
    companion object {
        private val TEST_FILES_DIR = Paths.get("src/test/resources/testfiles")
        private val TEMP_FILES_DIR = Paths.get(System.getProperty("java.io.tmpdir"), "receipts-test-files")
        
        // Standard test file names
        const val TEST_JPEG_FILE = "test-image.jpg"
        const val TEST_PNG_FILE = "test-image.png"
        const val TEST_GIF_FILE = "test-image.gif"
        const val TEST_WEBP_FILE = "test-image.webp"
        const val LARGE_TEST_FILE = "large-test-image.jpg"
        const val SECURE_TEST_FILE = "secure-test-image.jpg"
        
        // File size constants
        const val SMALL_FILE_SIZE = 1024 * 10 // 10KB
        const val MEDIUM_FILE_SIZE = 1024 * 100 // 100KB
        const val LARGE_FILE_SIZE = 1024 * 1024 * 15 // 15MB
        
        init {
            // Ensure temp directory exists
            if (!Files.exists(TEMP_FILES_DIR)) {
                try {
                    Files.createDirectories(TEMP_FILES_DIR)
                } catch (e: Exception) {
                    // Ignore directory creation errors
                }
            }
        }
    }
    
    /**
     * Creates a test JPEG file with specified dimensions and size
     */
    fun createTestJpegFile(fileName: String = TEST_JPEG_FILE, 
                          width: Int = 100, 
                          height: Int = 100): File {
        val file = TEMP_FILES_DIR.resolve(fileName).toFile()
        val image = createTestImage(width, height)
        
        ImageIO.write(image, "JPEG", file)
        return file
    }
    
    /**
     * Creates a test PNG file with specified dimensions and size
     */
    fun createTestPngFile(fileName: String = TEST_PNG_FILE, 
                         width: Int = 100, 
                         height: Int = 100): File {
        val file = TEMP_FILES_DIR.resolve(fileName).toFile()
        val image = createTestImage(width, height)
        
        ImageIO.write(image, "PNG", file)
        return file
    }
    
    /**
     * Creates a test GIF file with specified dimensions and size
     */
    fun createTestGifFile(fileName: String = TEST_GIF_FILE, 
                         width: Int = 100, 
                         height: Int = 100): File {
        val file = TEMP_FILES_DIR.resolve(fileName).toFile()
        val image = createTestImage(width, height)
        
        ImageIO.write(image, "GIF", file)
        return file
    }
    
    /**
     * Creates a test WebP file with specified dimensions and size
     * Note: WebP support might require additional libraries
     */
    fun createTestWebPFile(fileName: String = TEST_WEBP_FILE, 
                          width: Int = 100, 
                          height: Int = 100): File {
        // For now, create a PNG file as WebP support is complex
        // In production, this would use a WebP encoder
        val file = TEMP_FILES_DIR.resolve(fileName).toFile()
        val image = createTestImage(width, height)
        
        ImageIO.write(image, "PNG", file)
        return file
    }
    
    /**
     * Creates a large test file for testing file size limits
     */
    fun createLargeTestFile(fileName: String = LARGE_TEST_FILE, 
                           targetSizeKB: Int = LARGE_FILE_SIZE): File {
        val file = TEMP_FILES_DIR.resolve(fileName).toFile()
        
        // Calculate dimensions to achieve target file size
        val estimatedDimension = kotlin.math.sqrt(targetSizeKB.toDouble() * 50).toInt()
        val image = createTestImage(estimatedDimension, estimatedDimension)
        
        ImageIO.write(image, "JPEG", file)
        return file
    }
    
    /**
     * Creates a secure test file with valid image headers and content
     */
    fun createSecureTestFile(fileName: String = SECURE_TEST_FILE): File {
        val file = TEMP_FILES_DIR.resolve(fileName).toFile()
        val image = createTestImage(200, 200)
        
        // Add some metadata to make it more realistic
        ImageIO.write(image, "JPEG", file)
        return file
    }
    
    /**
     * Gets an existing test file from resources or creates it if it doesn't exist
     */
    fun getTestFile(fileName: String): File {
        // First try to get from resources
        val resourceFile = getResourceFile(fileName)
        if (resourceFile?.exists() == true) {
            return resourceFile
        }
        
        // If not found, create it based on file extension
        return when (fileName.lowercase()) {
            TEST_JPEG_FILE, "test-image1.jpg", "test-image2.jpg", "test-image3.jpg" -> 
                createTestJpegFile(fileName)
            TEST_PNG_FILE, "test-image1.png", "test-image2.png" -> 
                createTestPngFile(fileName)
            TEST_GIF_FILE, "test-image1.gif", "test-image2.gif" -> 
                createTestGifFile(fileName)
            TEST_WEBP_FILE -> 
                createTestWebPFile(fileName)
            LARGE_TEST_FILE -> 
                createLargeTestFile(fileName)
            SECURE_TEST_FILE -> 
                createSecureTestFile(fileName)
            else -> 
                createTestJpegFile(fileName)
        }
    }
    
    /**
     * Gets multiple test files for batch upload testing
     */
    fun getMultipleTestFiles(fileNames: List<String>): List<File> {
        return fileNames.map { getTestFile(it) }
    }
    
    /**
     * Validates that a file exists and is readable
     */
    fun validateTestFile(file: File): Boolean {
        return file.exists() && file.canRead() && file.length() > 0
    }
    
    /**
     * Gets the MIME type for a test file based on its extension
     */
    fun getMimeType(fileName: String): String {
        return when (fileName.lowercase().substringAfterLast('.')) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            else -> "image/jpeg"
        }
    }
    
    /**
     * Cleans up temporary test files
     */
    fun cleanupTestFiles() {
        try {
            if (Files.exists(TEMP_FILES_DIR)) {
                Files.walk(TEMP_FILES_DIR)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach { file ->
                        try {
                            file.delete()
                        } catch (e: Exception) {
                            // Ignore cleanup errors
                        }
                    }
            }
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }
    
    /**
     * Cleans up specific test files
     */
    fun cleanupTestFile(fileName: String) {
        try {
            val file = TEMP_FILES_DIR.resolve(fileName).toFile()
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }
    
    /**
     * Creates a test image with specified dimensions
     */
    private fun createTestImage(width: Int, height: Int): BufferedImage {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()
        
        // Create a simple pattern
        graphics.color = Color.WHITE
        graphics.fillRect(0, 0, width, height)
        
        graphics.color = Color.BLUE
        graphics.fillRect(10, 10, width - 20, height - 20)
        
        graphics.color = Color.RED
        graphics.fillRect(20, 20, width - 40, height - 40)
        
        graphics.color = Color.GREEN
        graphics.fillRect(30, 30, width - 60, height - 60)
        
        // Add some text
        graphics.color = Color.BLACK
        graphics.drawString("Test Image", 40, 50)
        graphics.drawString("${width}x${height}", 40, 70)
        
        graphics.dispose()
        return image
    }
    
    /**
     * Gets a file from test resources
     */
    private fun getResourceFile(fileName: String): File? {
        val resourcePath = TEST_FILES_DIR.resolve(fileName)
        return if (Files.exists(resourcePath)) {
            resourcePath.toFile()
        } else {
            null
        }
    }
    
    /**
     * Ensures a directory exists
     */
    private fun ensureDirectoryExists(path: Path) {
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path)
            } catch (e: Exception) {
                // Ignore directory creation errors
            }
        }
    }
}