package me.underlow.receipt.util

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import org.springframework.http.MediaType

/**
 * Utility class for OAuth2 test scenarios.
 * Provides methods to configure WireMock for different OAuth2 user profiles.
 */
object OAuth2TestUtils {

    /**
     * Configure WireMock to return specific user information for OAuth2 userinfo endpoint
     */
    fun configureOAuth2User(
        wireMockServer: WireMockServer,
        email: String,
        name: String,
        givenName: String? = null,
        familyName: String? = null,
        picture: String? = null
    ) {
        // Reset all userinfo stubs
        wireMockServer.resetMappings()
        
        // Reconfigure basic OAuth2 endpoints
        setupBasicOAuth2Stubs(wireMockServer)
        
        // Configure custom userinfo response
        wireMockServer.stubFor(
            get(urlPathEqualTo("/oauth2/v2/userinfo"))
                .withHeader("Authorization", equalTo("Bearer test_access_token"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(createUserInfoJson(email, name, givenName, familyName, picture))
                )
        )
    }

    /**
     * Create userinfo JSON response for OAuth2 testing
     */
    private fun createUserInfoJson(
        email: String,
        name: String,
        givenName: String?,
        familyName: String?,
        picture: String?
    ): String {
        val userInfo = mutableMapOf(
            "sub" to "test-user-id",
            "email" to email,
            "email_verified" to true,
            "name" to name
        )
        
        givenName?.let { userInfo["given_name"] = it }
        familyName?.let { userInfo["family_name"] = it }
        picture?.let { userInfo["picture"] = it }
        
        return userInfo.entries.joinToString(
            separator = ",\n                                ",
            prefix = "{\n                                ",
            postfix = "\n                            }"
        ) { (key, value) ->
            when (value) {
                is String -> "\"$key\": \"$value\""
                is Boolean -> "\"$key\": $value"
                else -> "\"$key\": \"$value\""
            }
        }
    }

    /**
     * Set up basic OAuth2 stubs (OIDC config, token endpoint, JWKS)
     */
    private fun setupBasicOAuth2Stubs(wireMockServer: WireMockServer) {
        // OIDC Configuration endpoint
        wireMockServer.stubFor(
            get(urlPathEqualTo("/.well-known/openid_configuration"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                            {
                                "issuer": "http://localhost:8888",
                                "authorization_endpoint": "http://localhost:8888/o/oauth2/auth",
                                "token_endpoint": "http://localhost:8888/token",
                                "userinfo_endpoint": "http://localhost:8888/oauth2/v2/userinfo",
                                "jwks_uri": "http://localhost:8888/oauth2/v3/certs",
                                "response_types_supported": ["code", "token", "id_token"],
                                "subject_types_supported": ["public"],
                                "id_token_signing_alg_values_supported": ["RS256"],
                                "scopes_supported": ["openid", "profile", "email"]
                            }
                        """.trimIndent())
                )
        )

        // Token endpoint
        wireMockServer.stubFor(
            post(urlPathEqualTo("/token"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                            {
                                "access_token": "test_access_token",
                                "token_type": "Bearer",
                                "expires_in": 3600,
                                "refresh_token": "test_refresh_token",
                                "scope": "openid profile email"
                            }
                        """.trimIndent())
                )
        )

        // JWKS endpoint
        wireMockServer.stubFor(
            get(urlPathEqualTo("/oauth2/v3/certs"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                            {
                                "keys": [
                                    {
                                        "kty": "RSA",
                                        "kid": "test-key-id",
                                        "use": "sig",
                                        "alg": "RS256",
                                        "n": "dummy_n_value",
                                        "e": "AQAB"
                                    }
                                ]
                            }
                        """.trimIndent())
                )
        )
    }

    /**
     * Configure OAuth2 error responses for testing error scenarios
     */
    fun configureOAuth2Error(wireMockServer: WireMockServer, errorType: OAuth2ErrorType) {
        // Reset mappings and setup basic stubs
        wireMockServer.resetMappings()
        setupBasicOAuth2Stubs(wireMockServer)
        
        when (errorType) {
            OAuth2ErrorType.INVALID_TOKEN -> {
                wireMockServer.stubFor(
                    get(urlPathEqualTo("/oauth2/v2/userinfo"))
                        .willReturn(
                            aResponse()
                                .withStatus(401)
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody("""
                                    {
                                        "error": "invalid_token",
                                        "error_description": "The access token provided is invalid"
                                    }
                                """.trimIndent())
                        )
                )
            }
            OAuth2ErrorType.TOKEN_ENDPOINT_ERROR -> {
                // Override the token endpoint with error response
                wireMockServer.stubFor(
                    post(urlPathEqualTo("/token"))
                        .willReturn(
                            aResponse()
                                .withStatus(400)
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody("""
                                    {
                                        "error": "invalid_grant",
                                        "error_description": "The provided authorization grant is invalid"
                                    }
                                """.trimIndent())
                        )
                )
            }
            OAuth2ErrorType.USERINFO_ENDPOINT_ERROR -> {
                wireMockServer.stubFor(
                    get(urlPathEqualTo("/oauth2/v2/userinfo"))
                        .willReturn(
                            aResponse()
                                .withStatus(500)
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody("""
                                    {
                                        "error": "server_error",
                                        "error_description": "Internal server error"
                                    }
                                """.trimIndent())
                        )
                )
            }
        }
    }

    enum class OAuth2ErrorType {
        INVALID_TOKEN,
        TOKEN_ENDPOINT_ERROR,
        USERINFO_ENDPOINT_ERROR
    }
}