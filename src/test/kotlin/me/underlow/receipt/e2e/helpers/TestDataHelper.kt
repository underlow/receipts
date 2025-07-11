package me.underlow.receipt.e2e.helpers

/**
 * Helper class for managing test data in e2e tests.
 * Provides methods for creating, cleaning up, and managing test data.
 */
class TestDataHelper {

    /**
     * Data class representing a test service provider
     */
    data class TestServiceProvider(
        val name: String,
        val state: String,
        val description: String? = null
    )

    /**
     * Creates a test service provider with the given parameters
     */
    fun createTestServiceProvider(
        name: String,
        state: String,
        description: String? = null
    ): TestServiceProvider {
        // In a real implementation, this would create the provider via API or database
        // For now, return the test data object
        return TestServiceProvider(name, state, description)
    }

    /**
     * Clears all service providers from the system
     */
    fun clearAllServiceProviders() {
        // In a real implementation, this would clear the database
        // For now, this is a placeholder
    }

    /**
     * Cleans up all test data created during the test
     */
    fun cleanupTestData() {
        // In a real implementation, this would clean up test data
        // For now, this is a placeholder
    }

    /**
     * Simulates service being unavailable for error testing
     */
    fun simulateServiceUnavailable() {
        // In a real implementation, this would disable the service
        // For now, this is a placeholder
    }

    /**
     * Restores service availability after error testing
     */
    fun restoreServiceAvailability() {
        // In a real implementation, this would restore the service
        // For now, this is a placeholder
    }
}