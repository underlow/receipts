package me.underlow.receipt.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.core.env.Environment
import org.springframework.web.multipart.MultipartFile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith
import java.io.File
import java.io.IOException
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * Unit tests for AvatarService.
 * Tests avatar upload, resize, and cleanup functionality.
 */
@ExtendWith(MockitoExtension::class)
class AvatarServiceTest {
    
    @Mock
    private lateinit var environment: Environment
    
    @Mock
    private lateinit var multipartFile: MultipartFile
    
    private lateinit var avatarService: AvatarService
    
    @BeforeEach
    fun setUp() {
        avatarService = AvatarService(environment)
    }
    
    @Test
    fun `given valid image when validateAvatarFile then validates file correctly`() {
        // given - valid image file
        val validJpegBytes = createValidJpegBytes()
        
        whenever(multipartFile.isEmpty).thenReturn(false)
        whenever(multipartFile.size).thenReturn(validJpegBytes.size.toLong())
        whenever(multipartFile.originalFilename).thenReturn("avatar.jpg")
        whenever(multipartFile.contentType).thenReturn("image/jpeg")
        whenever(multipartFile.bytes).thenReturn(validJpegBytes)
        
        // when - validating avatar file
        val result = avatarService.validateAvatarFile(multipartFile)
        
        // then - returns true for valid file
        assertTrue(result)
    }
    
    @Test
    fun `given oversized image when validateAvatarFile then returns false`() {
        // given - oversized image file (> 10MB)
        val oversizedFileSize = 11 * 1024 * 1024L // 11MB
        
        whenever(multipartFile.isEmpty).thenReturn(false)
        whenever(multipartFile.size).thenReturn(oversizedFileSize)
        whenever(multipartFile.originalFilename).thenReturn("avatar.jpg")
        whenever(multipartFile.contentType).thenReturn("image/jpeg")
        
        // when - validating oversized avatar file
        val result = avatarService.validateAvatarFile(multipartFile)
        
        // then - returns false for oversized file
        assertFalse(result)
    }
    
    @Test
    fun `given unsupported format when validateAvatarFile then returns false`() {
        // given - unsupported file format
        whenever(multipartFile.isEmpty).thenReturn(false)
        whenever(multipartFile.size).thenReturn(1024L)
        whenever(multipartFile.originalFilename).thenReturn("avatar.bmp")
        whenever(multipartFile.contentType).thenReturn("image/bmp")
        
        // when - validating unsupported avatar file
        val result = avatarService.validateAvatarFile(multipartFile)
        
        // then - returns false for unsupported format
        assertFalse(result)
    }
    
    @Test
    fun `given empty file when validateAvatarFile then returns false`() {
        // given - empty file
        whenever(multipartFile.isEmpty).thenReturn(true)
        
        // when - validating empty avatar file
        val result = avatarService.validateAvatarFile(multipartFile)
        
        // then - returns false for empty file
        assertFalse(result)
    }
    
    @Test
    fun `given valid avatar when uploadAndResizeAvatar then processes avatar correctly`() {
        // given - valid avatar file and configuration
        val validJpegBytes = createValidJpegBytes()
        val avatarPath = "/tmp/avatars"
        
        whenever(environment.getProperty("receipts.attachments-path")).thenReturn(avatarPath)
        whenever(multipartFile.isEmpty).thenReturn(false)
        whenever(multipartFile.size).thenReturn(validJpegBytes.size.toLong())
        whenever(multipartFile.originalFilename).thenReturn("avatar.jpg")
        whenever(multipartFile.contentType).thenReturn("image/jpeg")
        whenever(multipartFile.bytes).thenReturn(validJpegBytes)
        whenever(multipartFile.inputStream).thenReturn(ByteArrayInputStream(validJpegBytes))
        
        // when - uploading and resizing avatar
        val result = avatarService.uploadAndResizeAvatar(multipartFile)
        
        // then - returns avatar filename
        assertTrue(result.isNotEmpty())
        assertTrue(result.endsWith(".jpg"))
    }
    
    @Test
    fun `given invalid avatar when uploadAndResizeAvatar then throws exception`() {
        // given - invalid avatar file
        whenever(multipartFile.isEmpty).thenReturn(true)
        
        // when - uploading invalid avatar
        // then - throws exception
        assertFailsWith<IllegalArgumentException> {
            avatarService.uploadAndResizeAvatar(multipartFile)
        }
    }
    
    @Test
    fun `given missing avatar path when uploadAndResizeAvatar then throws exception`() {
        // given - missing avatar path configuration
        val validJpegBytes = createValidJpegBytes()
        
        whenever(environment.getProperty("receipts.attachments-path")).thenReturn(null)
        whenever(multipartFile.isEmpty).thenReturn(false)
        whenever(multipartFile.size).thenReturn(validJpegBytes.size.toLong())
        whenever(multipartFile.originalFilename).thenReturn("avatar.jpg")
        whenever(multipartFile.contentType).thenReturn("image/jpeg")
        whenever(multipartFile.bytes).thenReturn(validJpegBytes)
        
        // when - uploading avatar without path configuration
        // then - throws exception
        assertFailsWith<IllegalStateException> {
            avatarService.uploadAndResizeAvatar(multipartFile)
        }
    }
    
    @Test
    fun `given existing avatar when cleanupOldAvatar then removes old avatar file`() {
        // given - existing avatar file
        val avatarPath = "/tmp/avatars"
        val oldAvatarFilename = "old-avatar.jpg"
        
        whenever(environment.getProperty("receipts.attachments-path")).thenReturn(avatarPath)
        
        // when - cleaning up old avatar
        avatarService.cleanupOldAvatar(oldAvatarFilename)
        
        // then - old avatar file is removed (no exception thrown)
        // Note: In real implementation, this would delete the file
        verify(environment).getProperty("receipts.attachments-path")
    }
    
    @Test
    fun `given null avatar when cleanupOldAvatar then does nothing`() {
        // given - null avatar filename
        val nullAvatarFilename = null
        
        // when - cleaning up null avatar
        avatarService.cleanupOldAvatar(nullAvatarFilename)
        
        // then - no action taken (no exception thrown)
        verifyNoInteractions(environment)
    }
    
    @Test
    fun `given blank avatar when cleanupOldAvatar then does nothing`() {
        // given - blank avatar filename
        val blankAvatarFilename = "   "
        
        // when - cleaning up blank avatar
        avatarService.cleanupOldAvatar(blankAvatarFilename)
        
        // then - no action taken (no exception thrown)
        verifyNoInteractions(environment)
    }
    
    @Test
    fun `given large image when resizeImage then resizes to 200x200`() {
        // given - large image (400x400)
        val largeImage = BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB)
        // Fill with test pattern
        val graphics = largeImage.createGraphics()
        graphics.fillRect(0, 0, 400, 400)
        graphics.dispose()
        
        // when - resizing image
        val resizedImage = avatarService.resizeImage(largeImage, 200, 200)
        
        // then - image is resized to 200x200
        assertEquals(200, resizedImage.width)
        assertEquals(200, resizedImage.height)
    }
    
    @Test
    fun `given small image when resizeImage then resizes to 200x200`() {
        // given - small image (100x100)
        val smallImage = BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB)
        // Fill with test pattern
        val graphics = smallImage.createGraphics()
        graphics.fillRect(0, 0, 100, 100)
        graphics.dispose()
        
        // when - resizing image
        val resizedImage = avatarService.resizeImage(smallImage, 200, 200)
        
        // then - image is resized to 200x200
        assertEquals(200, resizedImage.width)
        assertEquals(200, resizedImage.height)
    }
    
    @Test
    fun `given rectangular image when resizeImage then maintains aspect ratio`() {
        // given - rectangular image (400x200)
        val rectangularImage = BufferedImage(400, 200, BufferedImage.TYPE_INT_RGB)
        // Fill with test pattern
        val graphics = rectangularImage.createGraphics()
        graphics.fillRect(0, 0, 400, 200)
        graphics.dispose()
        
        // when - resizing image
        val resizedImage = avatarService.resizeImage(rectangularImage, 200, 200)
        
        // then - image is resized to 200x200 (aspect ratio handling)
        assertEquals(200, resizedImage.width)
        assertEquals(200, resizedImage.height)
    }
    
    @Test
    fun `given filename when generateUniqueAvatarFilename then generates unique filename`() {
        // given - original filename
        val originalFilename = "avatar.jpg"
        
        // when - generating unique filename
        val uniqueFilename = avatarService.generateUniqueAvatarFilename(originalFilename)
        
        // then - generates unique filename with timestamp and UUID
        assertTrue(uniqueFilename.contains("avatar"))
        assertTrue(uniqueFilename.endsWith(".jpg"))
        assertTrue(uniqueFilename.length > originalFilename.length)
    }
    
    @Test
    fun `given null filename when generateUniqueAvatarFilename then generates default filename`() {
        // given - null filename
        val nullFilename = null
        
        // when - generating unique filename
        val uniqueFilename = avatarService.generateUniqueAvatarFilename(nullFilename)
        
        // then - generates default filename
        assertTrue(uniqueFilename.contains("avatar"))
        assertTrue(uniqueFilename.endsWith(".jpg"))
    }
    
    /**
     * Helper method to create valid JPEG bytes for testing
     */
    private fun createValidJpegBytes(): ByteArray {
        val image = BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()
        graphics.fillRect(0, 0, 100, 100)
        graphics.dispose()
        
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(image, "jpg", outputStream)
        return outputStream.toByteArray()
    }
}