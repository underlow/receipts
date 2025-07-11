package me.underlow.receipt.controller

import com.fasterxml.jackson.databind.ObjectMapper
import me.underlow.receipt.model.ServiceProvider
import me.underlow.receipt.model.ServiceProviderState
import me.underlow.receipt.model.RegularFrequency
import me.underlow.receipt.service.ServiceProviderService
import me.underlow.receipt.service.AvatarService
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import java.time.LocalDateTime

/**
 * Unit tests for ServiceProviderController.
 * Tests all REST endpoints with proper authentication, validation, and error handling.
 */
@WebMvcTest(ServiceProviderController::class)
class ServiceProviderControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var serviceProviderService: ServiceProviderService

    @MockBean
    private lateinit var avatarService: AvatarService

    private val sampleServiceProvider = ServiceProvider(
        id = 1L,
        name = "Test Provider",
        avatar = null,
        comment = "Test comment",
        commentForOcr = "Test OCR comment",
        regular = RegularFrequency.MONTHLY,
        customFields = """{"department": "IT", "contactEmail": "it@test.com"}""",
        state = ServiceProviderState.ACTIVE,
        createdDate = LocalDateTime.now(),
        modifiedDate = LocalDateTime.now()
    )

    @Test
    @WithMockUser
    fun `given authenticated user when GET service providers then returns all providers`() {
        // given - service returns list of providers
        val providers = listOf(sampleServiceProvider)
        `when`(serviceProviderService.findAll()).thenReturn(providers)

        // when - GET request to list all service providers
        mockMvc.perform(get("/api/service-providers"))
            // then - returns success response with providers list
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Test Provider"))
            .andExpect(jsonPath("$[0].state").value("ACTIVE"))
    }

    @Test
    fun `given unauthenticated user when GET service providers then redirects to login`() {
        // given - no authentication

        // when - GET request to list service providers
        mockMvc.perform(get("/api/service-providers"))
            // then - redirects to OAuth2 login
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("http://localhost/oauth2/authorization/google"))
    }

    @Test
    @WithMockUser
    fun `given service throws exception when GET service providers then returns internal server error`() {
        // given - service throws exception
        `when`(serviceProviderService.findAll()).thenThrow(RuntimeException("Database error"))

        // when - GET request to list service providers
        mockMvc.perform(get("/api/service-providers"))
            // then - returns internal server error
            .andExpect(status().isInternalServerError)
    }

    @Test
    @WithMockUser
    fun `given existing provider when GET service provider by id then returns provider`() {
        // given - service returns provider for given ID
        `when`(serviceProviderService.findById(1L)).thenReturn(sampleServiceProvider)

        // when - GET request for specific provider
        mockMvc.perform(get("/api/service-providers/1"))
            // then - returns success response with provider data
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Test Provider"))
            .andExpect(jsonPath("$.state").value("ACTIVE"))
    }

    @Test
    @WithMockUser
    fun `given non-existing provider when GET service provider by id then returns not found`() {
        // given - service returns null for non-existing ID
        `when`(serviceProviderService.findById(999L)).thenReturn(null)

        // when - GET request for non-existing provider
        mockMvc.perform(get("/api/service-providers/999"))
            // then - returns not found status
            .andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser
    fun `given valid request when POST service provider then creates and returns provider`() {
        // given - valid creation request and service creates provider
        val request = CreateServiceProviderRequest("New Provider")
        `when`(serviceProviderService.createServiceProvider("New Provider")).thenReturn(sampleServiceProvider)

        // when - POST request to create service provider
        mockMvc.perform(post("/api/service-providers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .with(csrf()))
            // then - returns success response with created provider
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.name").value("Test Provider"))
    }

    @Test
    @WithMockUser
    fun `given blank name when POST service provider then returns validation error`() {
        // given - request with blank name
        val request = CreateServiceProviderRequest("")

        // when - POST request with invalid data
        mockMvc.perform(post("/api/service-providers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .with(csrf()))
            // then - returns bad request status
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser
    fun `given duplicate name when POST service provider then returns error response`() {
        // given - service throws exception for duplicate name
        val request = CreateServiceProviderRequest("Duplicate Name")
        `when`(serviceProviderService.createServiceProvider("Duplicate Name"))
            .thenThrow(IllegalArgumentException("Service provider with name 'Duplicate Name' already exists"))

        // when - POST request with duplicate name
        mockMvc.perform(post("/api/service-providers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .with(csrf()))
            // then - returns bad request with error message
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("Service provider with name 'Duplicate Name' already exists"))
    }

    @Test
    @WithMockUser
    fun `given valid request when PUT service provider then updates and returns provider`() {
        // given - valid update request and service updates provider
        val request = UpdateServiceProviderRequest(
            name = "Updated Provider",
            comment = "Updated comment",
            commentForOcr = "Updated OCR comment",
            regular = RegularFrequency.WEEKLY,
            customFields = """{"updated": true}"""
        )
        `when`(serviceProviderService.updateServiceProvider(
            id = 1L,
            name = "Updated Provider",
            comment = "Updated comment",
            commentForOcr = "Updated OCR comment",
            regular = RegularFrequency.WEEKLY,
            customFields = """{"updated": true}"""
        )).thenReturn(sampleServiceProvider)

        // when - PUT request to update service provider
        mockMvc.perform(put("/api/service-providers/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .with(csrf()))
            // then - returns success response with updated provider
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(1))
    }

    @Test
    @WithMockUser
    fun `given non-existing provider when PUT service provider then returns error`() {
        // given - service throws exception for non-existing provider
        val request = UpdateServiceProviderRequest("Updated Provider")
        `when`(serviceProviderService.updateServiceProvider(
            id = 999L,
            name = "Updated Provider",
            comment = null,
            commentForOcr = null,
            regular = RegularFrequency.NOT_REGULAR,
            customFields = null
        )).thenThrow(IllegalArgumentException("Service provider with ID 999 not found"))

        // when - PUT request for non-existing provider
        mockMvc.perform(put("/api/service-providers/999")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .with(csrf()))
            // then - returns bad request with error message
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("Service provider with ID 999 not found"))
    }

    @Test
    @WithMockUser
    fun `given valid state change when PATCH service provider state then updates state`() {
        // given - valid state change request and service hides provider
        val request = ChangeStateRequest(ServiceProviderState.HIDDEN)
        val hiddenProvider = sampleServiceProvider.copy(state = ServiceProviderState.HIDDEN)
        `when`(serviceProviderService.hideServiceProvider(1L)).thenReturn(hiddenProvider)

        // when - PATCH request to change state
        mockMvc.perform(patch("/api/service-providers/1/state")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .with(csrf()))
            // then - returns success response with updated provider
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.state").value("HIDDEN"))
    }

    @Test
    @WithMockUser
    fun `given already hidden provider when PATCH to hide then returns error`() {
        // given - service throws exception for invalid state transition
        val request = ChangeStateRequest(ServiceProviderState.HIDDEN)
        `when`(serviceProviderService.hideServiceProvider(1L))
            .thenThrow(IllegalStateException("Cannot hide service provider from state HIDDEN"))

        // when - PATCH request to hide already hidden provider
        mockMvc.perform(patch("/api/service-providers/1/state")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .with(csrf()))
            // then - returns bad request with error message
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("Cannot hide service provider from state HIDDEN"))
    }

    @Test
    @WithMockUser
    fun `given existing provider when DELETE service provider then hides provider`() {
        // given - service hides provider successfully
        val hiddenProvider = sampleServiceProvider.copy(state = ServiceProviderState.HIDDEN)
        `when`(serviceProviderService.hideServiceProvider(1L)).thenReturn(hiddenProvider)

        // when - DELETE request to soft delete provider
        mockMvc.perform(delete("/api/service-providers/1")
            .with(csrf()))
            // then - returns success response with hidden provider
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.state").value("HIDDEN"))
    }

    @Test
    @WithMockUser
    fun `given non-existing provider when DELETE service provider then returns error`() {
        // given - service throws exception for non-existing provider
        `when`(serviceProviderService.hideServiceProvider(999L))
            .thenThrow(IllegalArgumentException("Service provider with ID 999 not found"))

        // when - DELETE request for non-existing provider
        mockMvc.perform(delete("/api/service-providers/999")
            .with(csrf()))
            // then - returns bad request with error message
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("Service provider with ID 999 not found"))
    }

    @Test
    @WithMockUser
    fun `given valid avatar file when POST avatar then uploads and returns success`() {
        // given - valid avatar file, existing provider, and successful upload
        val avatarFile = MockMultipartFile(
            "avatar",
            "test-avatar.png",
            "image/png",
            "fake-image-data".toByteArray()
        )
        val avatarFilename = "avatar_20240101_12345678_abcd.png"
        val updatedProvider = sampleServiceProvider.copy(avatar = avatarFilename)

        `when`(avatarService.validateAvatarFile(avatarFile)).thenReturn(true)
        `when`(serviceProviderService.findById(1L)).thenReturn(sampleServiceProvider)
        `when`(avatarService.uploadAndResizeAvatar(avatarFile)).thenReturn(avatarFilename)
        `when`(serviceProviderService.updateAvatar(1L, avatarFilename)).thenReturn(updatedProvider)

        // when - POST request to upload avatar
        mockMvc.perform(multipart("/api/service-providers/1/avatar")
            .file(avatarFile)
            .with(csrf()))
            // then - returns success response with avatar path
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.avatarPath").value(avatarFilename))
            .andExpect(jsonPath("$.data.avatar").value(avatarFilename))
    }

    @Test
    @WithMockUser
    fun `given no file when POST avatar then returns error`() {
        // given - no file provided

        // when - POST request without file
        mockMvc.perform(multipart("/api/service-providers/1/avatar")
            .with(csrf()))
            // then - returns bad request with error message
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("No file provided"))
    }

    @Test
    @WithMockUser
    fun `given invalid avatar file when POST avatar then returns validation error`() {
        // given - invalid avatar file format
        val invalidFile = MockMultipartFile(
            "avatar",
            "invalid.txt",
            "text/plain",
            "invalid-content".toByteArray()
        )
        `when`(avatarService.validateAvatarFile(invalidFile)).thenReturn(false)

        // when - POST request with invalid file
        mockMvc.perform(multipart("/api/service-providers/1/avatar")
            .file(invalidFile)
            .with(csrf()))
            // then - returns bad request with validation error
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("Invalid avatar file format or size"))
    }

    @Test
    @WithMockUser
    fun `given non-existing provider when POST avatar then returns not found`() {
        // given - valid file but non-existing provider
        val avatarFile = MockMultipartFile(
            "avatar",
            "test-avatar.png",
            "image/png",
            "fake-image-data".toByteArray()
        )
        `when`(avatarService.validateAvatarFile(avatarFile)).thenReturn(true)
        `when`(serviceProviderService.findById(999L)).thenReturn(null)

        // when - POST request to upload avatar for non-existing provider
        mockMvc.perform(multipart("/api/service-providers/999/avatar")
            .file(avatarFile)
            .with(csrf()))
            // then - returns not found status
            .andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser
    fun `given upload failure when POST avatar then returns error`() {
        // given - valid file but upload service throws exception
        val avatarFile = MockMultipartFile(
            "avatar",
            "test-avatar.png",
            "image/png",
            "fake-image-data".toByteArray()
        )
        `when`(avatarService.validateAvatarFile(avatarFile)).thenReturn(true)
        `when`(serviceProviderService.findById(1L)).thenReturn(sampleServiceProvider)
        `when`(avatarService.uploadAndResizeAvatar(avatarFile))
            .thenThrow(RuntimeException("Upload failed"))

        // when - POST request with upload failure
        mockMvc.perform(multipart("/api/service-providers/1/avatar")
            .file(avatarFile)
            .with(csrf()))
            // then - returns internal server error
            .andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("Upload failed. Please try again"))
    }

    @Test
    @WithMockUser
    fun `given successful avatar upload when POST avatar then cleans up old avatar`() {
        // given - provider with existing avatar and successful upload
        val oldAvatar = "old_avatar.png"
        val existingProvider = sampleServiceProvider.copy(avatar = oldAvatar)
        val avatarFile = MockMultipartFile(
            "avatar",
            "new-avatar.png",
            "image/png",
            "fake-image-data".toByteArray()
        )
        val newAvatarFilename = "new_avatar_20240101_12345678_abcd.png"
        val updatedProvider = sampleServiceProvider.copy(avatar = newAvatarFilename)

        `when`(avatarService.validateAvatarFile(avatarFile)).thenReturn(true)
        `when`(serviceProviderService.findById(1L)).thenReturn(existingProvider)
        `when`(avatarService.uploadAndResizeAvatar(avatarFile)).thenReturn(newAvatarFilename)
        `when`(serviceProviderService.updateAvatar(1L, newAvatarFilename)).thenReturn(updatedProvider)

        // when - POST request to upload new avatar
        mockMvc.perform(multipart("/api/service-providers/1/avatar")
            .file(avatarFile)
            .with(csrf()))
            // then - returns success and cleanup is called
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))

        // verify cleanup was called with old avatar
        verify(avatarService).cleanupOldAvatar(oldAvatar)
    }

    @Test
    @WithMockUser
    fun `given request validation error when POST service provider then returns proper error response`() {
        // given - request with validation constraint violation
        val request = CreateServiceProviderRequest("   ") // blank name after trim

        // when - POST request with validation failure
        mockMvc.perform(post("/api/service-providers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .with(csrf()))
            // then - returns bad request with validation error
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser
    fun `given service throws general exception when POST service provider then returns internal server error`() {
        // given - service throws unexpected exception
        val request = CreateServiceProviderRequest("Test Provider")
        `when`(serviceProviderService.createServiceProvider("Test Provider"))
            .thenThrow(RuntimeException("Unexpected error"))

        // when - POST request with service exception
        mockMvc.perform(post("/api/service-providers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .with(csrf()))
            // then - returns internal server error
            .andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("Internal server error"))
    }
}
