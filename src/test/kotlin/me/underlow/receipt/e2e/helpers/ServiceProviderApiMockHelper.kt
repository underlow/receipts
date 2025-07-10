package me.underlow.receipt.e2e.helpers

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import me.underlow.receipt.e2e.testdata.ServiceProviderTestDataFactory

/**
 * Helper class for mocking Service Provider API endpoints in e2e tests.
 * Provides consistent API mocking functionality across all service provider tests.
 */
class ServiceProviderApiMockHelper(private val wireMockServer: WireMockServer) {

    companion object {
        private const val SERVICE_PROVIDERS_ENDPOINT = "/api/service-providers"
        private const val CONTENT_TYPE_JSON = "application/json"
    }

    /**
     * Sets up successful API response with standard providers
     */
    fun setupSuccessfulResponse() {
        setupSuccessfulResponse(ServiceProviderTestDataFactory.createStandardProviders())
    }

    /**
     * Sets up successful API response with custom provider data
     */
    fun setupSuccessfulResponse(providersJson: String) {
        wireMockServer.stubFor(
            get(urlEqualTo(SERVICE_PROVIDERS_ENDPOINT))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", CONTENT_TYPE_JSON)
                        .withBody(providersJson)
                )
        )
    }

    /**
     * Sets up API response with providers having mixed avatar configurations
     */
    fun setupProvidersWithMixedAvatars() {
        setupSuccessfulResponse(ServiceProviderTestDataFactory.createProvidersWithMixedAvatars())
    }

    /**
     * Sets up API response with providers having mixed states (active/hidden)
     */
    fun setupProvidersWithMixedStates() {
        setupSuccessfulResponse(ServiceProviderTestDataFactory.createProvidersWithMixedStates())
    }

    /**
     * Sets up API response with many providers for testing scrolling
     */
    fun setupManyProviders(count: Int = 15) {
        setupSuccessfulResponse(ServiceProviderTestDataFactory.createManyProviders(count))
    }

    /**
     * Sets up API response with providers for search testing
     */
    fun setupProvidersForSearch() {
        setupSuccessfulResponse(ServiceProviderTestDataFactory.createProvidersForSearch())
    }

    /**
     * Sets up API error response with 500 status
     */
    fun setupServerError() {
        setupErrorResponse(500, ServiceProviderTestDataFactory.createErrorResponse())
    }

    /**
     * Sets up API error response with 404 status
     */
    fun setupNotFoundError() {
        setupErrorResponse(404, """{"error": "Service providers not found"}""")
    }

    /**
     * Sets up API error response with custom status and body
     */
    fun setupErrorResponse(status: Int, errorJson: String) {
        wireMockServer.stubFor(
            get(urlEqualTo(SERVICE_PROVIDERS_ENDPOINT))
                .willReturn(
                    aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", CONTENT_TYPE_JSON)
                        .withBody(errorJson)
                )
        )
    }

    /**
     * Sets up slow API response for testing loading states
     */
    fun setupSlowResponse(delayMs: Int = 2000) {
        wireMockServer.stubFor(
            get(urlEqualTo(SERVICE_PROVIDERS_ENDPOINT))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", CONTENT_TYPE_JSON)
                        .withBody(ServiceProviderTestDataFactory.createStandardProviders())
                        .withFixedDelay(delayMs)
                )
        )
    }

    /**
     * Verifies that the API was called exactly once
     */
    fun verifyApiCalledOnce() {
        wireMockServer.verify(
            exactly(1),
            getRequestedFor(urlEqualTo(SERVICE_PROVIDERS_ENDPOINT))
        )
    }

    /**
     * Verifies that the API was called a specific number of times
     */
    fun verifyApiCalledTimes(times: Int) {
        wireMockServer.verify(
            exactly(times),
            getRequestedFor(urlEqualTo(SERVICE_PROVIDERS_ENDPOINT))
        )
    }

    /**
     * Verifies that the API was never called
     */
    fun verifyApiNeverCalled() {
        wireMockServer.verify(
            exactly(0),
            getRequestedFor(urlEqualTo(SERVICE_PROVIDERS_ENDPOINT))
        )
    }

    /**
     * Resets all mock configurations
     */
    fun reset() {
        wireMockServer.resetAll()
    }
}