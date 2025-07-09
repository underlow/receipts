package me.underlow.receipt.config

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties
import org.springframework.util.unit.DataSize
import org.assertj.core.api.Assertions.assertThat
import org.springframework.context.ApplicationContext
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

/**
 * Tests for Spring Boot multipart configuration.
 * Verifies that multipart file upload settings are properly configured
 * with correct size limits and enabled state for receipt image processing.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Testcontainers
class MultipartConfigTest (
    private val multipartProperties: MultipartProperties,
    private val applicationContext: ApplicationContext
) {

    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer<Nothing>("postgres:15-alpine").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    @Test
    @DisplayName("Given multipart configuration, when application context loads, then multipart properties are loaded")
    fun multipartConfigurationIsLoaded() {
        // Given: Application context with multipart configuration
        val environment = applicationContext.environment

        // When: Application context is loaded
        val multipartEnabled = environment.getProperty("spring.servlet.multipart.enabled")
        val maxFileSize = environment.getProperty("spring.servlet.multipart.max-file-size")
        val maxRequestSize = environment.getProperty("spring.servlet.multipart.max-request-size")

        // Then: Multipart properties should be configured correctly
        assertThat(multipartEnabled).isEqualTo("true")
        assertThat(maxFileSize).isEqualTo("20MB")
        assertThat(maxRequestSize).isEqualTo("20MB")
    }

    @Test
    @DisplayName("Given multipart properties bean, when checking file size limits, then limits are set to 20MB")
    fun multipartFileSizeLimitsAreConfigured() {
        // Given: Multipart properties bean is injected
        val expectedSize = DataSize.ofMegabytes(20)

        // When: Checking file size limits
        val maxFileSize = multipartProperties.maxFileSize
        val maxRequestSize = multipartProperties.maxRequestSize

        // Then: File size limits should be set to 20MB
        assertThat(maxFileSize).isEqualTo(expectedSize)
        assertThat(maxRequestSize).isEqualTo(expectedSize)
    }

    @Test
    @DisplayName("Given multipart properties bean, when checking enabled state, then multipart is enabled")
    fun multipartIsEnabled() {
        // Given: Multipart properties bean is injected
        val expectedEnabled = true

        // When: Checking multipart enabled state
        val isEnabled = multipartProperties.isEnabled

        // Then: Multipart should be enabled
        assertThat(isEnabled).isEqualTo(expectedEnabled)
    }

    @Test
    @DisplayName("Given multipart properties bean, when checking location configuration, then location is properly configured")
    fun multipartLocationIsConfigured() {
        // Given: Multipart properties bean is injected
        val multipartLocation = multipartProperties.location

        // When: Checking multipart location configuration
        // Location can be null (default behavior uses system temp directory)
        val locationIsValid = multipartLocation == null || multipartLocation.isNotBlank()

        // Then: Location should be properly configured (null means default temp directory)
        assertThat(locationIsValid).isTrue()
    }
}