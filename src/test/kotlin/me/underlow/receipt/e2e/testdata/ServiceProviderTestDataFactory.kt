package me.underlow.receipt.e2e.testdata

/**
 * Factory for creating service provider test data.
 * Provides consistent test data for service provider related tests.
 */
object ServiceProviderTestDataFactory {

    /**
     * Creates a standard list of service providers for testing
     */
    fun createStandardProviders(): String {
        return createProviderList(
            ServiceProviderTestData(
                id = 1,
                name = "Electric Company",
                avatar = "/uploads/avatar1.jpg",
                comment = "Monthly electricity bills",
                commentForOcr = "Electric utility provider",
                regular = "MONTHLY",
                customFields = mapOf(
                    "account" to "123456789",
                    "customerService" to "+1-800-123-4567"
                ),
                state = "ACTIVE"
            ),
            ServiceProviderTestData(
                id = 2,
                name = "Water Utility",
                avatar = null,
                comment = "Quarterly water bills",
                commentForOcr = "Water utility provider",
                regular = "NOT_REGULAR",
                customFields = emptyMap(),
                state = "ACTIVE"
            )
        )
    }

    /**
     * Creates providers with different avatar configurations
     */
    fun createProvidersWithMixedAvatars(): String {
        return createProviderList(
            ServiceProviderTestData(
                id = 1,
                name = "Electric Company",
                avatar = "/uploads/avatar1.jpg",
                state = "ACTIVE"
            ),
            ServiceProviderTestData(
                id = 2,
                name = "Water Utility",
                avatar = null,
                state = "ACTIVE"
            )
        )
    }

    /**
     * Creates providers with different states (active and hidden)
     */
    fun createProvidersWithMixedStates(): String {
        return createProviderList(
            ServiceProviderTestData(
                id = 1,
                name = "Electric Company",
                avatar = "/uploads/avatar1.jpg",
                state = "ACTIVE"
            ),
            ServiceProviderTestData(
                id = 2,
                name = "Old Provider",
                avatar = null,
                state = "HIDDEN"
            )
        )
    }

    /**
     * Creates a large number of providers for testing scrolling
     */
    fun createManyProviders(count: Int = 15): String {
        val providers = (1..count).map { i ->
            ServiceProviderTestData(
                id = i,
                name = "Provider $i",
                avatar = null,
                state = "ACTIVE"
            )
        }
        return createProviderList(*providers.toTypedArray())
    }

    /**
     * Creates providers with different names for search testing
     */
    fun createProvidersForSearch(): String {
        return createProviderList(
            ServiceProviderTestData(
                id = 1,
                name = "Electric Company",
                avatar = null,
                state = "ACTIVE"
            ),
            ServiceProviderTestData(
                id = 2,
                name = "Water Utility",
                avatar = null,
                state = "ACTIVE"
            ),
            ServiceProviderTestData(
                id = 3,
                name = "Internet Provider",
                avatar = null,
                state = "ACTIVE"
            )
        )
    }

    /**
     * Creates error response JSON
     */
    fun createErrorResponse(): String {
        return """{"error": "Internal server error"}"""
    }

    /**
     * Creates a list of providers as JSON string
     */
    private fun createProviderList(vararg providers: ServiceProviderTestData): String {
        val providerJsonList = providers.map { provider ->
            createProviderJson(provider)
        }
        return "[${providerJsonList.joinToString(",")}]"
    }

    /**
     * Creates JSON representation of a single provider
     */
    private fun createProviderJson(provider: ServiceProviderTestData): String {
        val customFieldsJson = if (provider.customFields.isNotEmpty()) {
            provider.customFields.entries.joinToString(",") { (key, value) ->
                "\"$key\": \"$value\""
            }
        } else {
            ""
        }

        return """
            {
                "id": ${provider.id},
                "name": "${provider.name}",
                "avatar": ${provider.avatar?.let { "\"$it\"" } ?: "null"},
                ${if (provider.comment != null) "\"comment\": \"${provider.comment}\"," else ""}
                ${if (provider.commentForOcr != null) "\"commentForOcr\": \"${provider.commentForOcr}\"," else ""}
                ${if (provider.regular != null) "\"regular\": \"${provider.regular}\"," else ""}
                "customFields": {$customFieldsJson},
                "state": "${provider.state}",
                "createdDate": "${provider.createdDate}",
                "modifiedDate": "${provider.modifiedDate}"
            }
        """.trimIndent()
    }

    /**
     * Data class representing service provider test data
     */
    data class ServiceProviderTestData(
        val id: Int,
        val name: String,
        val avatar: String? = null,
        val comment: String? = null,
        val commentForOcr: String? = null,
        val regular: String? = null,
        val customFields: Map<String, String> = emptyMap(),
        val state: String,
        val createdDate: String = "2025-01-01T10:00:00Z",
        val modifiedDate: String = "2025-01-01T10:00:00Z"
    )
}