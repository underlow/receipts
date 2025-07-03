package me.underlow.receipt.config

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy

/**
 * Configuration for WireMock-based OAuth2 testing.
 * Provides realistic OAuth2 flow simulation without external dependencies.
 */
@TestConfiguration
@EnableWebSecurity
@Profile("wiremock-oauth2")
class WireMockOAuth2Config {

    private lateinit var wireMockServer: WireMockServer

    @PostConstruct
    fun startWireMock() {
        wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().port(8888))
        wireMockServer.start()
        setupOAuth2Stubs()
    }

    @PreDestroy
    fun stopWireMock() {
        if (::wireMockServer.isInitialized) {
            wireMockServer.stop()
        }
    }

    /**
     * Configure OAuth2 authorization server endpoints for Google OAuth2 simulation
     */
    private fun setupOAuth2Stubs() {
        // OAuth2 Authorization endpoint
        wireMockServer.stubFor(
            get(urlPathEqualTo("/o/oauth2/auth"))
                .willReturn(
                    aResponse()
                        .withStatus(302)
                        .withHeader("Location", "http://localhost:8080/login/oauth2/code/google?code=test_auth_code&state=test_state")
                )
        )

        // OAuth2 Token endpoint  
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
                                "scope": "openid profile email",
                                "id_token": "eyJhbGciOiJSUzI1NiIsImtpZCI6InRlc3Qta2V5LWlkIiwidHlwIjoiSldUIn0.eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0Ojg4ODgiLCJhdWQiOiJ0ZXN0LWNsaWVudC1pZCIsImV4cCI6OTk5OTk5OTk5OSwiaWF0IjoxNjAwMDAwMDAwLCJzdWIiOiJ0ZXN0LXVzZXItaWQiLCJlbWFpbCI6InRlc3RAZXhhbXBsZS5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibmFtZSI6IlRlc3QgVXNlciIsImdpdmVuX25hbWUiOiJUZXN0IiwiZmFtaWx5X25hbWUiOiJVc2VyIiwicGljdHVyZSI6Imh0dHBzOi8vZXhhbXBsZS5jb20vYXZhdGFyLnBuZyJ9.dummy_signature"
                            }
                        """.trimIndent())
                )
        )

        // OAuth2 UserInfo endpoint
        wireMockServer.stubFor(
            get(urlPathEqualTo("/oauth2/v2/userinfo"))
                .withHeader("Authorization", matching("Bearer test_access_token"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                            {
                                "sub": "test-user-id",
                                "email": "test@example.com",
                                "email_verified": true,
                                "name": "Test User",
                                "given_name": "Test",
                                "family_name": "User",
                                "picture": "https://example.com/avatar.png"
                            }
                        """.trimIndent())
                )
        )

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

        // JWKS endpoint (for token validation)
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
     * Security filter chain for WireMock OAuth2 testing.
     * Allows normal OAuth2 flow but uses WireMock server endpoints.
     */
    @Bean
    @Primary
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .authorizeHttpRequests { requests ->
                requests
                    .requestMatchers("/login", "/error", "/webjars/**", "/css/**", "/js/**", "/images/**").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2Login { oauth2 ->
                oauth2
                    .loginPage("/login")
                    .defaultSuccessUrl("/dashboard", true)
            }
            .logout { logout ->
                logout
                    .logoutSuccessUrl("/login?logout")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
            }
            .build()
    }

    /**
     * Get WireMock server instance for test configuration
     */
    fun getWireMockServer(): WireMockServer = wireMockServer
}