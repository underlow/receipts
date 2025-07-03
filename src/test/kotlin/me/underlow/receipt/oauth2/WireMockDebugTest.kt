package me.underlow.receipt.oauth2

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.junit.jupiter.api.*
import org.springframework.web.client.RestTemplate

/**
 * Debug test to understand what's going wrong with WireMock
 */
class WireMockDebugTest {

    private lateinit var wireMockServer: WireMockServer
    private val restTemplate = RestTemplate()

    @BeforeEach
    fun setup() {
        wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().port(8888))
        wireMockServer.start()
        
        // Simple test stub
        wireMockServer.stubFor(
            get(urlEqualTo("/test"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withBody("Hello World")
                )
        )
    }

    @AfterEach
    fun tearDown() {
        wireMockServer.stop()
    }

    @Test
    fun `Test basic WireMock functionality`() {
        println("WireMock server running: ${wireMockServer.isRunning}")
        println("WireMock server port: ${wireMockServer.port()}")
        
        val response = restTemplate.getForObject(
            "http://localhost:8888/test",
            String::class.java
        )
        
        println("Response: $response")
        Assertions.assertEquals("Hello World", response)
    }

    @Test
    fun `Test userinfo endpoint with debug output`() {
        // Setup userinfo endpoint
        wireMockServer.stubFor(
            get(urlPathEqualTo("/oauth2/v2/userinfo"))
                .withHeader("Authorization", equalTo("Bearer test_access_token"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"email\":\"test@example.com\"}")
                )
        )

        // Test userinfo endpoint
        val headers = org.springframework.http.HttpHeaders()
        headers.set("Authorization", "Bearer test_access_token")
        val entity = org.springframework.http.HttpEntity<String>(headers)
        
        try {
            val response = restTemplate.exchange(
                "http://localhost:8888/oauth2/v2/userinfo",
                org.springframework.http.HttpMethod.GET,
                entity,
                String::class.java
            )
            
            println("Response status: ${response.statusCode}")
            println("Response body: ${response.body}")
            
            Assertions.assertEquals(200, response.statusCode.value())
            Assertions.assertTrue(response.body!!.contains("test@example.com"))
        } catch (e: Exception) {
            println("Exception occurred: ${e.message}")
            throw e
        }
    }
}