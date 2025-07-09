package me.underlow.receipt.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.mockito.kotlin.*
import org.springframework.core.env.Environment
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.*

/**
 * Unit tests for FileUploadService.
 * Tests file validation, storage, and naming functionality for uploaded receipt images.
 */
@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FileUploadServiceTest {
    
    @Mock
    private lateinit var environment: Environment
    
    @Mock
    private lateinit var multipartFile: MultipartFile
    
    private lateinit var fileUploadService: FileUploadService
    
    @TempDir
    private lateinit var tempDir: Path
    
    private val validJpegBytes = byteArrayOf(
        0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte(), // JPEG header
        0x00.toByte(), 0x10.toByte(), 0x4A.toByte(), 0x46.toByte(), // JFIF marker
        0x49.toByte(), 0x46.toByte(), 0x00.toByte(), 0x01.toByte()
    )
    
    private val validPngBytes = byteArrayOf(
        0x89.toByte(), 0x50.toByte(), 0x4E.toByte(), 0x47.toByte(), // PNG header
        0x0D.toByte(), 0x0A.toByte(), 0x1A.toByte(), 0x0A.toByte()
    )
    
    private val validGifBytes = byteArrayOf(
        0x47.toByte(), 0x49.toByte(), 0x46.toByte(), 0x38.toByte(), // GIF header
        0x39.toByte(), 0x61.toByte() // GIF89a
    )
    
    private val validWebpBytes = byteArrayOf(
        0x52.toByte(), 0x49.toByte(), 0x46.toByte(), 0x46.toByte(), // RIFF header
        0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), // file size
        0x57.toByte(), 0x45.toByte(), 0x42.toByte(), 0x50.toByte()  // WEBP
    )
    
    private val invalidFileBytes = byteArrayOf(
        0x7F.toByte(), 0x45.toByte(), 0x4C.toByte(), 0x46.toByte() // ELF header (executable)
    )
    
    @BeforeEach
    fun setUp() {
        fileUploadService = FileUploadService(environment)
    }
    
    // Tests for validateFile method
    
    @Test
    fun `given valid JPEG file when validateFile then returns true`() {
        // given - valid JPEG file with correct MIME type and size
        whenever(multipartFile.contentType).thenReturn("image/jpeg")
        whenever(multipartFile.originalFilename).thenReturn("test.jpg")
        whenever(multipartFile.size).thenReturn(1024L)
        whenever(multipartFile.bytes).thenReturn(validJpegBytes)
        
        // when - validating file
        val result = fileUploadService.validateFile(multipartFile)
        
        // then - returns true for valid JPEG
        assertTrue(result)
    }
    
    @Test
    fun `given valid PNG file when validateFile then returns true`() {
        // given - valid PNG file with correct MIME type and size
        whenever(multipartFile.contentType).thenReturn("image/png")
        whenever(multipartFile.originalFilename).thenReturn("test.png")
        whenever(multipartFile.size).thenReturn(2048L)
        whenever(multipartFile.bytes).thenReturn(validPngBytes)
        
        // when - validating file
        val result = fileUploadService.validateFile(multipartFile)
        
        // then - returns true for valid PNG
        assertTrue(result)
    }
    
    @Test
    fun `given valid GIF file when validateFile then returns true`() {
        // given - valid GIF file with correct MIME type and size
        whenever(multipartFile.contentType).thenReturn("image/gif")
        whenever(multipartFile.originalFilename).thenReturn("test.gif")
        whenever(multipartFile.size).thenReturn(1536L)
        whenever(multipartFile.bytes).thenReturn(validGifBytes)
        
        // when - validating file
        val result = fileUploadService.validateFile(multipartFile)
        
        // then - returns true for valid GIF
        assertTrue(result)
    }
    
    @Test
    fun `given valid WebP file when validateFile then returns true`() {
        // given - valid WebP file with correct MIME type and size
        whenever(multipartFile.contentType).thenReturn("image/webp")
        whenever(multipartFile.originalFilename).thenReturn("test.webp")
        whenever(multipartFile.size).thenReturn(1024L)
        whenever(multipartFile.bytes).thenReturn(validWebpBytes)
        
        // when - validating file
        val result = fileUploadService.validateFile(multipartFile)
        
        // then - returns true for valid WebP
        assertTrue(result)
    }
    
    @Test
    fun `given invalid MIME type when validateFile then returns false`() {
        // given - file with invalid MIME type
        whenever(multipartFile.contentType).thenReturn("application/pdf")
        whenever(multipartFile.originalFilename).thenReturn("test.pdf")
        whenever(multipartFile.size).thenReturn(1024L)
        
        // when - validating file
        val result = fileUploadService.validateFile(multipartFile)
        
        // then - returns false for invalid MIME type
        assertFalse(result)
    }
    
    @Test
    fun `given null MIME type when validateFile then returns false`() {
        // given - file with null MIME type
        whenever(multipartFile.contentType).thenReturn(null)
        whenever(multipartFile.originalFilename).thenReturn("test.jpg")
        whenever(multipartFile.size).thenReturn(1024L)
        
        // when - validating file
        val result = fileUploadService.validateFile(multipartFile)
        
        // then - returns false for null MIME type
        assertFalse(result)
    }
    
    @Test
    fun `given file larger than 20MB when validateFile then returns false`() {
        // given - file larger than 20MB limit
        whenever(multipartFile.contentType).thenReturn("image/jpeg")
        whenever(multipartFile.originalFilename).thenReturn("large.jpg")
        whenever(multipartFile.size).thenReturn(21 * 1024 * 1024L) // 21MB
        
        // when - validating file
        val result = fileUploadService.validateFile(multipartFile)
        
        // then - returns false for oversized file
        assertFalse(result)
    }
    
    @Test
    fun `given file with mismatched MIME type and header when validateFile then returns false`() {
        // given - file claiming to be JPEG but with PNG header
        whenever(multipartFile.contentType).thenReturn("image/jpeg")
        whenever(multipartFile.originalFilename).thenReturn("fake.jpg")
        whenever(multipartFile.size).thenReturn(1024L)
        whenever(multipartFile.bytes).thenReturn(validPngBytes)
        
        // when - validating file
        val result = fileUploadService.validateFile(multipartFile)
        
        // then - returns false for mismatched MIME type and header
        assertFalse(result)
    }
    
    @Test
    fun `given file with executable header when validateFile then returns false`() {
        // given - file with executable header but claiming to be image
        whenever(multipartFile.contentType).thenReturn("image/jpeg")
        whenever(multipartFile.originalFilename).thenReturn("malicious.jpg")
        whenever(multipartFile.size).thenReturn(1024L)
        whenever(multipartFile.bytes).thenReturn(invalidFileBytes)
        
        // when - validating file
        val result = fileUploadService.validateFile(multipartFile)
        
        // then - returns false for executable header
        assertFalse(result)
    }
    
    @Test
    fun `given null filename when validateFile then returns false`() {
        // given - file with null filename
        whenever(multipartFile.contentType).thenReturn("image/jpeg")
        whenever(multipartFile.originalFilename).thenReturn(null)
        whenever(multipartFile.size).thenReturn(1024L)
        
        // when - validating file
        val result = fileUploadService.validateFile(multipartFile)
        
        // then - returns false for null filename
        assertFalse(result)
    }
    
    @Test
    fun `given empty file when validateFile then returns false`() {
        // given - empty file
        whenever(multipartFile.contentType).thenReturn("image/jpeg")
        whenever(multipartFile.originalFilename).thenReturn("empty.jpg")
        whenever(multipartFile.size).thenReturn(0L)
        whenever(multipartFile.isEmpty).thenReturn(true)
        
        // when - validating file
        val result = fileUploadService.validateFile(multipartFile)
        
        // then - returns false for empty file
        assertFalse(result)
    }
    
    // Tests for saveFile method
    
    @Test
    fun `given valid file when saveFile then saves file and returns path`() {
        // given - valid file ready for saving
        whenever(environment.getProperty("receipts.inbox-path")).thenReturn(tempDir.toString())
        whenever(multipartFile.contentType).thenReturn("image/jpeg")
        whenever(multipartFile.originalFilename).thenReturn("test.jpg")
        whenever(multipartFile.size).thenReturn(1024L)
        whenever(multipartFile.bytes).thenReturn(validJpegBytes)
        
        // Mock transferTo to create the file
        doAnswer { invocation ->
            val file = invocation.arguments[0] as File
            file.createNewFile()
            null
        }.whenever(multipartFile).transferTo(any<File>())
        
        // when - saving file
        val result = fileUploadService.saveFile(multipartFile)
        
        // then - returns file path and file exists
        assertNotNull(result)
        assertTrue(result.endsWith(".jpg"))
        assertTrue(File(tempDir.toString(), result).exists())
    }
    
    @Test
    fun `given file with special characters in filename when saveFile then sanitizes filename`() {
        // given - file with special characters in filename
        whenever(environment.getProperty("receipts.inbox-path")).thenReturn(tempDir.toString())
        whenever(multipartFile.contentType).thenReturn("image/jpeg")
        whenever(multipartFile.originalFilename).thenReturn("../../../etc/passwd.jpg")
        whenever(multipartFile.size).thenReturn(1024L)
        whenever(multipartFile.bytes).thenReturn(validJpegBytes)
        
        // Mock transferTo to create the file
        doAnswer { invocation ->
            val file = invocation.arguments[0] as File
            file.createNewFile()
            null
        }.whenever(multipartFile).transferTo(any<File>())
        
        // when - saving file
        val result = fileUploadService.saveFile(multipartFile)
        
        // then - filename is sanitized and file is saved safely
        assertNotNull(result)
        assertFalse(result.contains("../"))
        assertFalse(result.contains("/etc/"))
        assertTrue(result.endsWith(".jpg"))
        assertTrue(File(tempDir.toString(), result).exists())
    }
    
    @Test
    fun `given multiple files with same name when saveFile then generates unique filenames`() {
        // given - multiple files with same original name
        whenever(environment.getProperty("receipts.inbox-path")).thenReturn(tempDir.toString())
        whenever(multipartFile.contentType).thenReturn("image/jpeg")
        whenever(multipartFile.originalFilename).thenReturn("duplicate.jpg")
        whenever(multipartFile.size).thenReturn(1024L)
        whenever(multipartFile.bytes).thenReturn(validJpegBytes)
        
        // Mock transferTo to create the file
        doAnswer { invocation ->
            val file = invocation.arguments[0] as File
            file.createNewFile()
            null
        }.whenever(multipartFile).transferTo(any<File>())
        
        // when - saving multiple files with same name
        val result1 = fileUploadService.saveFile(multipartFile)
        val result2 = fileUploadService.saveFile(multipartFile)
        
        // then - generates unique filenames for both files
        assertNotNull(result1)
        assertNotNull(result2)
        assertNotEquals(result1, result2)
        assertTrue(File(tempDir.toString(), result1).exists())
        assertTrue(File(tempDir.toString(), result2).exists())
    }
    
    @Test
    fun `given file transfer error when saveFile then throws exception`() {
        // given - file that will cause transfer error
        whenever(environment.getProperty("receipts.inbox-path")).thenReturn(tempDir.toString())
        whenever(multipartFile.contentType).thenReturn("image/jpeg")
        whenever(multipartFile.originalFilename).thenReturn("test.jpg")
        whenever(multipartFile.size).thenReturn(1024L)
        whenever(multipartFile.bytes).thenReturn(validJpegBytes)
        doThrow(IOException("Transfer failed")).whenever(multipartFile).transferTo(any<File>())
        
        // when - saving file that causes transfer error
        // then - throws exception
        assertFailsWith<IOException> {
            fileUploadService.saveFile(multipartFile)
        }
    }
    
    // Tests for generateUniqueFilename method
    
    @Test
    fun `given filename with extension when generateUniqueFilename then preserves extension`() {
        // given - filename with extension
        val originalFilename = "test.jpg"
        
        // when - generating unique filename
        val result = fileUploadService.generateUniqueFilename(originalFilename)
        
        // then - preserves extension
        assertNotNull(result)
        assertTrue(result.endsWith(".jpg"))
        assertNotEquals(originalFilename, result)
    }
    
    @Test
    fun `given filename without extension when generateUniqueFilename then handles gracefully`() {
        // given - filename without extension
        val originalFilename = "test"
        
        // when - generating unique filename
        val result = fileUploadService.generateUniqueFilename(originalFilename)
        
        // then - handles filename without extension
        assertNotNull(result)
        assertNotEquals(originalFilename, result)
    }
    
    @Test
    fun `given multiple calls with same filename when generateUniqueFilename then generates unique names`() {
        // given - same filename used multiple times
        val originalFilename = "duplicate.png"
        
        // when - generating unique filenames multiple times
        val result1 = fileUploadService.generateUniqueFilename(originalFilename)
        val result2 = fileUploadService.generateUniqueFilename(originalFilename)
        val result3 = fileUploadService.generateUniqueFilename(originalFilename)
        
        // then - generates unique names each time
        assertNotNull(result1)
        assertNotNull(result2)
        assertNotNull(result3)
        assertNotEquals(result1, result2)
        assertNotEquals(result2, result3)
        assertNotEquals(result1, result3)
        assertTrue(result1.endsWith(".png"))
        assertTrue(result2.endsWith(".png"))
        assertTrue(result3.endsWith(".png"))
    }
    
    @Test
    fun `given filename with special characters when generateUniqueFilename then sanitizes filename`() {
        // given - filename with special characters
        val originalFilename = "../../../etc/passwd.jpg"
        
        // when - generating unique filename
        val result = fileUploadService.generateUniqueFilename(originalFilename)
        
        // then - sanitizes filename
        assertNotNull(result)
        assertFalse(result.contains("../"))
        assertFalse(result.contains("/etc/"))
        assertTrue(result.endsWith(".jpg"))
    }
    
    @Test
    fun `given filename with Unicode characters when generateUniqueFilename then handles Unicode safely`() {
        // given - filename with Unicode characters
        val originalFilename = "тест_файл_🎉.jpg"
        
        // when - generating unique filename
        val result = fileUploadService.generateUniqueFilename(originalFilename)
        
        // then - handles Unicode characters safely
        assertNotNull(result)
        assertTrue(result.endsWith(".jpg"))
        assertNotEquals(originalFilename, result)
    }
    
    @Test
    fun `given null filename when generateUniqueFilename then generates fallback name`() {
        // given - null filename
        val originalFilename = null
        
        // when - generating unique filename
        val result = fileUploadService.generateUniqueFilename(originalFilename)
        
        // then - generates fallback name
        assertNotNull(result)
        assertTrue(result.isNotBlank())
    }
    
    @Test
    fun `given empty filename when generateUniqueFilename then generates fallback name`() {
        // given - empty filename
        val originalFilename = ""
        
        // when - generating unique filename
        val result = fileUploadService.generateUniqueFilename(originalFilename)
        
        // then - generates fallback name
        assertNotNull(result)
        assertTrue(result.isNotBlank())
    }
    
    @Test
    fun `given very long filename when generateUniqueFilename then truncates appropriately`() {
        // given - very long filename
        val originalFilename = "a".repeat(300) + ".jpg"
        
        // when - generating unique filename
        val result = fileUploadService.generateUniqueFilename(originalFilename)
        
        // then - truncates filename to reasonable length
        assertNotNull(result)
        assertTrue(result.length < 255) // Common filesystem limit
        assertTrue(result.endsWith(".jpg"))
    }
}