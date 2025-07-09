package me.underlow.receipt.controller

import me.underlow.receipt.config.SecurityConfiguration
import me.underlow.receipt.config.TestSecurityConfiguration
import me.underlow.receipt.service.CustomAuthenticationFailureHandler
import me.underlow.receipt.service.CustomAuthenticationSuccessHandler
import me.underlow.receipt.service.CustomOAuth2UserService
import me.underlow.receipt.service.FileUploadService
import me.underlow.receipt.service.UserService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

/**
 * Integration tests for FileUploadController.
 * Tests file upload REST API endpoint with various scenarios including validation and error handling.
 */
@ExtendWith(MockitoExtension::class)
@WebMvcTest(FileUploadController::class)
@Import(SecurityConfiguration::class, TestSecurityConfiguration::class)
@ActiveProfiles("test")
class FileUploadControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var fileUploadService: FileUploadService

    @MockitoBean
    private lateinit var customOAuth2UserService: CustomOAuth2UserService

    @MockitoBean
    private lateinit var customAuthenticationSuccessHandler: CustomAuthenticationSuccessHandler

    @MockitoBean
    private lateinit var customAuthenticationFailureHandler: CustomAuthenticationFailureHandler

    @MockitoBean
    private lateinit var userService: UserService

    @Test
    @WithMockUser
    fun `given valid image file when POST to upload endpoint then should return success response`() {
        // given - valid JPEG file and service configured to accept it
        val validJpegFile = MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            "fake-jpeg-content".toByteArray()
        )
        whenever(fileUploadService.validateFile(any())).thenReturn(true)
        whenever(fileUploadService.saveFile(any())).thenReturn("test_unique_filename.jpg")

        // when - POST request to /api/upload with valid file
        mockMvc.perform(
            multipart("/api/upload")
                .file(validJpegFile)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            // then - returns success response with 200 status
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.filePath").value("test_unique_filename.jpg"))
            .andExpect(jsonPath("$.message").value("File uploaded successfully"))
            .andExpect(jsonPath("$.error").doesNotExist())
    }

    @Test
    @WithMockUser
    fun `given invalid file type when POST to upload endpoint then should return error response`() {
        // given - invalid file type rejected by service
        val invalidFile = MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "invalid-content".toByteArray()
        )
        whenever(fileUploadService.validateFile(any())).thenReturn(false)

        // when - POST request to /api/upload with invalid file
        mockMvc.perform(
            multipart("/api/upload")
                .file(invalidFile)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            // then - returns error response with 400 status
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.filePath").doesNotExist())
            .andExpect(jsonPath("$.message").value("Invalid file"))
            .andExpect(jsonPath("$.error").value("Only JPEG, PNG, GIF, WebP files are allowed"))
    }

    @Test
    @WithMockUser
    fun `given empty file when POST to upload endpoint then should return error response`() {
        // given - empty file with no content
        val emptyFile = MockMultipartFile(
            "file",
            "empty.jpg",
            "image/jpeg",
            ByteArray(0)
        )
        whenever(fileUploadService.validateFile(any())).thenReturn(false)

        // when - POST request to /api/upload with empty file
        mockMvc.perform(
            multipart("/api/upload")
                .file(emptyFile)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            // then - returns error response with 400 status
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.filePath").doesNotExist())
            .andExpect(jsonPath("$.message").value("Invalid file"))
            .andExpect(jsonPath("$.error").value("Only JPEG, PNG, GIF, WebP files are allowed"))
    }

    @Test
    @WithMockUser
    fun `given file save failure when POST to upload endpoint then should return error response`() {
        // given - valid file but filesystem error during save
        val validFile = MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            "fake-jpeg-content".toByteArray()
        )
        whenever(fileUploadService.validateFile(any())).thenReturn(true)
        whenever(fileUploadService.saveFile(any())).thenThrow(RuntimeException("Filesystem error"))

        // when - POST request to /api/upload with valid file but save failure
        mockMvc.perform(
            multipart("/api/upload")
                .file(validFile)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            // then - returns error response with 500 status
            .andExpect(status().isInternalServerError)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.filePath").doesNotExist())
            .andExpect(jsonPath("$.message").value("File upload failed"))
            .andExpect(jsonPath("$.error").value("Filesystem error"))
    }

    @Test
    @WithMockUser
    fun `given missing file parameter when POST to upload endpoint then should return error response`() {
        // given - request without file parameter
        // when - POST request to /api/upload without file parameter
        mockMvc.perform(
            multipart("/api/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            // then - returns error response with 400 status
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.filePath").doesNotExist())
            .andExpect(jsonPath("$.message").value("Missing file parameter"))
            .andExpect(jsonPath("$.error").value("File parameter is required"))
    }

    @Test
    @WithMockUser
    fun `given valid PNG file when POST to upload endpoint then should return success response`() {
        // given - valid PNG file accepted by service
        val validPngFile = MockMultipartFile(
            "file",
            "test.png",
            "image/png",
            "fake-png-content".toByteArray()
        )
        whenever(fileUploadService.validateFile(any())).thenReturn(true)
        whenever(fileUploadService.saveFile(any())).thenReturn("test_unique_filename.png")

        // when - POST request to /api/upload with valid PNG file
        mockMvc.perform(
            multipart("/api/upload")
                .file(validPngFile)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            // then - returns success response with 200 status
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.filePath").value("test_unique_filename.png"))
            .andExpect(jsonPath("$.message").value("File uploaded successfully"))
            .andExpect(jsonPath("$.error").doesNotExist())
    }

    @Test
    @WithMockUser
    fun `given valid WebP file when POST to upload endpoint then should return success response`() {
        // given - valid WebP file accepted by service
        val validWebpFile = MockMultipartFile(
            "file",
            "test.webp",
            "image/webp",
            "fake-webp-content".toByteArray()
        )
        whenever(fileUploadService.validateFile(any())).thenReturn(true)
        whenever(fileUploadService.saveFile(any())).thenReturn("test_unique_filename.webp")

        // when - POST request to /api/upload with valid WebP file
        mockMvc.perform(
            multipart("/api/upload")
                .file(validWebpFile)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            // then - returns success response with 200 status
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.filePath").value("test_unique_filename.webp"))
            .andExpect(jsonPath("$.message").value("File uploaded successfully"))
            .andExpect(jsonPath("$.error").doesNotExist())
    }

    @Test
    fun `given unauthenticated user when POST to upload endpoint then should require authentication`() {
        // given - unauthenticated user attempting file upload
        val validFile = MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            "fake-jpeg-content".toByteArray()
        )

        // when - POST request to /api/upload without authentication
        mockMvc.perform(
            multipart("/api/upload")
                .file(validFile)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            // then - redirects to authentication (form login in test profile)
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("http://localhost/login"))
    }
}