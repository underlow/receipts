package me.underlow.receipt.controller

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

/**
 * Test for upload page controller mapping
 */
@WebMvcTest(LoginController::class)
class UploadPageControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `given authenticated user when accessing upload page then should return upload template`() {
        // When: User accesses upload page with OAuth2 authentication
        val oauth2User = DefaultOAuth2User(
            listOf(SimpleGrantedAuthority("ROLE_USER")),
            mapOf(
                "email" to "test@example.com",
                "name" to "Test User"
            ),
            "email"
        )
        val auth = OAuth2AuthenticationToken(oauth2User, oauth2User.authorities, "google")
        
        val result = mockMvc.perform(
            get("/upload")
                .with(authentication(auth))
        )

        // Then: Should return upload template
        result.andExpect(status().isOk)
            .andExpect(view().name("upload"))
    }

    @Test
    fun `given unauthenticated user when accessing upload page then should return unauthorized`() {
        // When: Unauthenticated user accesses upload page
        mockMvc.perform(get("/upload"))
            // Then: Should return unauthorized (401) since OAuth2 is configured
            .andExpect(status().isUnauthorized)
    }
}
