package me.underlow.receipt.controller

import me.underlow.receipt.model.ServiceProvider
import me.underlow.receipt.model.ServiceProviderState
import me.underlow.receipt.model.RegularFrequency
import me.underlow.receipt.service.ServiceProviderService
import me.underlow.receipt.service.AvatarService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for avatar upload functionality in ServiceProviderController.
 * Tests avatar upload, validation, error handling, and cleanup.
 */
class AvatarUploadControllerTest {

    @Mock
    private lateinit var serviceProviderService: ServiceProviderService

    @Mock
    private lateinit var avatarService: AvatarService

    private lateinit var controller: ServiceProviderController

    private val testServiceProvider = ServiceProvider(
        id = 1L,
        name = "Test Provider",
        avatar = null,
        comment = null,
        commentForOcr = null,
        regular = RegularFrequency.NOT_REGULAR,
        customFields = null,
        state = ServiceProviderState.ACTIVE,
        createdDate = LocalDateTime.now(),
        modifiedDate = LocalDateTime.now()
    )

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        controller = ServiceProviderController(serviceProviderService, avatarService)
    }

    @Nested
    @DisplayName("Avatar Upload Tests")
    inner class AvatarUploadTests {

        @Test
        @DisplayName("given valid avatar file when upload then returns success response")
        fun `given valid avatar file when upload then returns success response`() {
            // Given
            val serviceProviderId = 1L
            val avatarFile = MockMultipartFile(
                "avatar",
                "test-avatar.jpg",
                "image/jpeg",
                "fake image content".toByteArray()
            )
            val expectedAvatarFilename = "avatar_1_123456789.jpg"
            val updatedProvider = testServiceProvider.copy(avatar = expectedAvatarFilename)

            whenever(avatarService.validateAvatarFile(avatarFile)).thenReturn(true)
            whenever(serviceProviderService.findById(serviceProviderId)).thenReturn(testServiceProvider)
            whenever(avatarService.uploadAndResizeAvatar(avatarFile)).thenReturn(expectedAvatarFilename)
            whenever(serviceProviderService.updateAvatar(serviceProviderId, expectedAvatarFilename))
                .thenReturn(updatedProvider)

            // When
            val response = controller.uploadAvatar(serviceProviderId, avatarFile)

            // Then
            assertEquals(HttpStatus.OK, response.statusCode)
            assertTrue(response.body!!.success)
            assertEquals(updatedProvider, response.body!!.data)
            assertEquals(expectedAvatarFilename, response.body!!.avatarPath)
            assertNotNull(response.body!!.message)

            verify(avatarService).validateAvatarFile(avatarFile)
            verify(serviceProviderService).findById(serviceProviderId)
            verify(avatarService).uploadAndResizeAvatar(avatarFile)
            verify(serviceProviderService).updateAvatar(serviceProviderId, expectedAvatarFilename)
            verify(avatarService).cleanupOldAvatar(null)
        }

        @Test
        @DisplayName("given existing avatar when upload new avatar then cleans up old avatar")
        fun `given existing avatar when upload new avatar then cleans up old avatar`() {
            // Given
            val serviceProviderId = 1L
            val oldAvatarFilename = "old_avatar.jpg"
            val existingProvider = testServiceProvider.copy(avatar = oldAvatarFilename)
            val avatarFile = MockMultipartFile(
                "avatar",
                "new-avatar.jpg",
                "image/jpeg",
                "new image content".toByteArray()
            )
            val newAvatarFilename = "avatar_1_987654321.jpg"
            val updatedProvider = existingProvider.copy(avatar = newAvatarFilename)

            whenever(avatarService.validateAvatarFile(avatarFile)).thenReturn(true)
            whenever(serviceProviderService.findById(serviceProviderId)).thenReturn(existingProvider)
            whenever(avatarService.uploadAndResizeAvatar(avatarFile)).thenReturn(newAvatarFilename)
            whenever(serviceProviderService.updateAvatar(serviceProviderId, newAvatarFilename))
                .thenReturn(updatedProvider)

            // When
            val response = controller.uploadAvatar(serviceProviderId, avatarFile)

            // Then
            assertEquals(HttpStatus.OK, response.statusCode)
            assertTrue(response.body!!.success)
            assertEquals(updatedProvider, response.body!!.data)
            assertEquals(newAvatarFilename, response.body!!.avatarPath)

            verify(avatarService).cleanupOldAvatar(oldAvatarFilename)
        }

        @Test
        @DisplayName("given null file when upload then returns bad request")
        fun `given null file when upload then returns bad request`() {
            // Given
            val serviceProviderId = 1L

            // When
            val response = controller.uploadAvatar(serviceProviderId, null)

            // Then
            assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
            assertFalse(response.body!!.success)
            assertEquals("No file provided", response.body!!.error)

            verifyNoInteractions(avatarService)
            verifyNoInteractions(serviceProviderService)
        }

        @Test
        @DisplayName("given empty file when upload then returns bad request")
        fun `given empty file when upload then returns bad request`() {
            // Given
            val serviceProviderId = 1L
            val emptyFile = MockMultipartFile("avatar", "empty.jpg", "image/jpeg", byteArrayOf())

            // When
            val response = controller.uploadAvatar(serviceProviderId, emptyFile)

            // Then
            assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
            assertFalse(response.body!!.success)
            assertEquals("No file provided", response.body!!.error)

            verifyNoInteractions(avatarService)
            verifyNoInteractions(serviceProviderService)
        }

        @Test
        @DisplayName("given invalid file format when upload then returns bad request")
        fun `given invalid file format when upload then returns bad request`() {
            // Given
            val serviceProviderId = 1L
            val invalidFile = MockMultipartFile(
                "avatar",
                "test.txt",
                "text/plain",
                "not an image".toByteArray()
            )

            whenever(avatarService.validateAvatarFile(invalidFile)).thenReturn(false)

            // When
            val response = controller.uploadAvatar(serviceProviderId, invalidFile)

            // Then
            assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
            assertFalse(response.body!!.success)
            assertEquals("Invalid avatar file format or size", response.body!!.error)

            verify(avatarService).validateAvatarFile(invalidFile)
            verifyNoMoreInteractions(avatarService)
            verifyNoInteractions(serviceProviderService)
        }

        @Test
        @DisplayName("given non-existent service provider when upload then returns not found")
        fun `given non-existent service provider when upload then returns not found`() {
            // Given
            val serviceProviderId = 999L
            val avatarFile = MockMultipartFile(
                "avatar",
                "test-avatar.jpg",
                "image/jpeg",
                "fake image content".toByteArray()
            )

            whenever(avatarService.validateAvatarFile(avatarFile)).thenReturn(true)
            whenever(serviceProviderService.findById(serviceProviderId)).thenReturn(null)

            // When
            val response = controller.uploadAvatar(serviceProviderId, avatarFile)

            // Then
            assertEquals(HttpStatus.NOT_FOUND, response.statusCode)

            verify(avatarService).validateAvatarFile(avatarFile)
            verify(serviceProviderService).findById(serviceProviderId)
            verifyNoMoreInteractions(avatarService)
            verifyNoMoreInteractions(serviceProviderService)
        }

        @Test
        @DisplayName("given avatar upload failure when upload then returns internal server error")
        fun `given avatar upload failure when upload then returns internal server error`() {
            // Given
            val serviceProviderId = 1L
            val avatarFile = MockMultipartFile(
                "avatar",
                "test-avatar.jpg",
                "image/jpeg",
                "fake image content".toByteArray()
            )

            whenever(avatarService.validateAvatarFile(avatarFile)).thenReturn(true)
            whenever(serviceProviderService.findById(serviceProviderId)).thenReturn(testServiceProvider)
            whenever(avatarService.uploadAndResizeAvatar(avatarFile))
                .thenThrow(RuntimeException("Upload failed"))

            // When
            val response = controller.uploadAvatar(serviceProviderId, avatarFile)

            // Then
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
            assertFalse(response.body!!.success)
            assertEquals("Avatar upload failed", response.body!!.error)

            verify(avatarService).validateAvatarFile(avatarFile)
            verify(serviceProviderService).findById(serviceProviderId)
            verify(avatarService).uploadAndResizeAvatar(avatarFile)
            verifyNoMoreInteractions(serviceProviderService)
        }

        @Test
        @DisplayName("given service provider update failure when upload then returns internal server error")
        fun `given service provider update failure when upload then returns internal server error`() {
            // Given
            val serviceProviderId = 1L
            val avatarFile = MockMultipartFile(
                "avatar",
                "test-avatar.jpg",
                "image/jpeg",
                "fake image content".toByteArray()
            )
            val avatarFilename = "avatar_1_123456789.jpg"

            whenever(avatarService.validateAvatarFile(avatarFile)).thenReturn(true)
            whenever(serviceProviderService.findById(serviceProviderId)).thenReturn(testServiceProvider)
            whenever(avatarService.uploadAndResizeAvatar(avatarFile)).thenReturn(avatarFilename)
            whenever(serviceProviderService.updateAvatar(serviceProviderId, avatarFilename))
                .thenThrow(RuntimeException("Database update failed"))

            // When
            val response = controller.uploadAvatar(serviceProviderId, avatarFile)

            // Then
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
            assertFalse(response.body!!.success)
            assertEquals("Avatar upload failed", response.body!!.error)

            verify(avatarService).validateAvatarFile(avatarFile)
            verify(serviceProviderService).findById(serviceProviderId)
            verify(avatarService).uploadAndResizeAvatar(avatarFile)
            verify(serviceProviderService).updateAvatar(serviceProviderId, avatarFilename)
        }
    }

    @Nested
    @DisplayName("Avatar Validation Tests")
    inner class AvatarValidationTests {

        @Test
        @DisplayName("given valid JPEG file when validate then returns true")
        fun `given valid JPEG file when validate then returns true`() {
            // Given
            val jpegFile = MockMultipartFile(
                "avatar",
                "test.jpg",
                "image/jpeg",
                ByteArray(1024) // 1KB file
            )

            whenever(avatarService.validateAvatarFile(jpegFile)).thenReturn(true)

            // When
            val isValid = avatarService.validateAvatarFile(jpegFile)

            // Then
            assertTrue(isValid)
        }

        @Test
        @DisplayName("given valid PNG file when validate then returns true")
        fun `given valid PNG file when validate then returns true`() {
            // Given
            val pngFile = MockMultipartFile(
                "avatar",
                "test.png",
                "image/png",
                ByteArray(2048) // 2KB file
            )

            whenever(avatarService.validateAvatarFile(pngFile)).thenReturn(true)

            // When
            val isValid = avatarService.validateAvatarFile(pngFile)

            // Then
            assertTrue(isValid)
        }

        @Test
        @DisplayName("given oversized file when validate then returns false")
        fun `given oversized file when validate then returns false`() {
            // Given
            val oversizedFile = MockMultipartFile(
                "avatar",
                "huge.jpg",
                "image/jpeg",
                ByteArray(15 * 1024 * 1024) // 15MB file (over 10MB limit)
            )

            whenever(avatarService.validateAvatarFile(oversizedFile)).thenReturn(false)

            // When
            val isValid = avatarService.validateAvatarFile(oversizedFile)

            // Then
            assertFalse(isValid)
        }

        @Test
        @DisplayName("given unsupported format when validate then returns false")
        fun `given unsupported format when validate then returns false`() {
            // Given
            val unsupportedFile = MockMultipartFile(
                "avatar",
                "test.bmp",
                "image/bmp",
                ByteArray(1024)
            )

            whenever(avatarService.validateAvatarFile(unsupportedFile)).thenReturn(false)

            // When
            val isValid = avatarService.validateAvatarFile(unsupportedFile)

            // Then
            assertFalse(isValid)
        }
    }

    @Nested
    @DisplayName("Avatar Processing Tests")
    inner class AvatarProcessingTests {

        @Test
        @DisplayName("given valid image when process then resizes to 200x200")
        fun `given valid image when process then resizes to 200x200`() {
            // Given
            val imageFile = MockMultipartFile(
                "avatar",
                "large-image.jpg",
                "image/jpeg",
                "large image content".toByteArray()
            )
            val expectedFilename = "avatar_1_123456789.jpg"

            whenever(avatarService.uploadAndResizeAvatar(imageFile)).thenReturn(expectedFilename)

            // When
            val filename = avatarService.uploadAndResizeAvatar(imageFile)

            // Then
            assertEquals(expectedFilename, filename)
            verify(avatarService).uploadAndResizeAvatar(imageFile)
        }

        @Test
        @DisplayName("given corrupted image when process then throws exception")
        fun `given corrupted image when process then throws exception`() {
            // Given
            val corruptedFile = MockMultipartFile(
                "avatar",
                "corrupted.jpg",
                "image/jpeg",
                "corrupted data".toByteArray()
            )

            whenever(avatarService.uploadAndResizeAvatar(corruptedFile))
                .thenThrow(RuntimeException("Failed to process image"))

            // When & Then
            try {
                avatarService.uploadAndResizeAvatar(corruptedFile)
                assert(false) { "Expected exception was not thrown" }
            } catch (e: RuntimeException) {
                assertEquals("Failed to process image", e.message)
            }
        }
    }

    @Nested
    @DisplayName("Avatar Cleanup Tests")
    inner class AvatarCleanupTests {

        @Test
        @DisplayName("given old avatar filename when cleanup then deletes file")
        fun `given old avatar filename when cleanup then deletes file`() {
            // Given
            val oldAvatarFilename = "old_avatar_123.jpg"

            doNothing().whenever(avatarService).cleanupOldAvatar(oldAvatarFilename)

            // When
            avatarService.cleanupOldAvatar(oldAvatarFilename)

            // Then
            verify(avatarService).cleanupOldAvatar(oldAvatarFilename)
        }

        @Test
        @DisplayName("given null avatar filename when cleanup then does nothing")
        fun `given null avatar filename when cleanup then does nothing`() {
            // Given
            val nullAvatarFilename: String? = null

            doNothing().whenever(avatarService).cleanupOldAvatar(nullAvatarFilename)

            // When
            avatarService.cleanupOldAvatar(nullAvatarFilename)

            // Then
            verify(avatarService).cleanupOldAvatar(nullAvatarFilename)
        }
    }
}