package me.underlow.receipt.controller

import me.underlow.receipt.config.SecurityConfiguration
import me.underlow.receipt.service.CustomAuthenticationFailureHandler
import me.underlow.receipt.service.CustomAuthenticationSuccessHandler
import me.underlow.receipt.service.CustomOAuth2UserService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

/**
 * Unit tests for WebController.
 * Tests profile and settings endpoints with proper OAuth2 authentication and avatar handling.
 */
@ExtendWith(MockitoExtension::class)
@WebMvcTest(WebController::class)
@Import(SecurityConfiguration::class)
class WebControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var customOAuth2UserService: CustomOAuth2UserService

    @MockitoBean
    private lateinit var customAuthenticationSuccessHandler: CustomAuthenticationSuccessHandler

    @MockitoBean
    private lateinit var customAuthenticationFailureHandler: CustomAuthenticationFailureHandler

    @Test
    @WithAnonymousUser
    fun `given anonymous user when accessing profile then should redirect to login`() {
        // given - anonymous user attempting to access profile page
        // this test is very strange and its behaviour differs from prod

        // when - making GET request to profile endpoint
        mockMvc.perform(get("/profile"))
            // then - should redirect to login due to authentication requirement
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("http://localhost/oauth2/authorization/google"))
    }

    @Test
    @WithAnonymousUser
    fun `given anonymous user when accessing settings then should redirect to login`() {
        // given - anonymous user attempting to access settings page
        // this test is very strange and its behaviour differs from prod
        // when - making GET request to settings endpoint
        mockMvc.perform(get("/settings"))
            // then - should redirect to login due to authentication requirement
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("http://localhost/oauth2/authorization/google"))
    }

    @Test
    fun `given authenticated OAuth2 user with avatar when accessing profile then should include avatar in model`() {
        // given - authenticated OAuth2 user with all profile attributes including avatar
        val userAttributes = mapOf(
            "email" to "test@example.com",
            "name" to "Test User",
            "picture" to "https://example.com/avatar.jpg"
        )

        // when - making GET request to profile endpoint with OAuth2 authentication
        mockMvc.perform(
            get("/profile")
                .with(oauth2Login().attributes { attrs ->
                    attrs.putAll(userAttributes)
                })
        )
            // then - should render profile template with correct user attributes including avatar
            .andExpect(status().isOk)
            .andExpect(view().name("profile"))
            .andExpect(model().attribute("userEmail", "test@example.com"))
            .andExpect(model().attribute("userName", "Test User"))
            .andExpect(model().attribute("userAvatar", "https://example.com/avatar.jpg"))
    }

    @Test
    fun `given authenticated OAuth2 user without avatar when accessing profile then should include empty avatar in model`() {
        // given - authenticated OAuth2 user without picture attribute (avatar missing)
        val userAttributes = mapOf(
            "email" to "test@example.com",
            "name" to "Test User"
            // picture attribute is intentionally omitted
        )

        // when - making GET request to profile endpoint with OAuth2 authentication
        mockMvc.perform(
            get("/profile")
                .with(oauth2Login().attributes { attrs ->
                    attrs.putAll(userAttributes)
                })
        )
            // then - should render profile template with empty avatar for graceful fallback
            .andExpect(status().isOk)
            .andExpect(view().name("profile"))
            .andExpect(model().attribute("userEmail", "test@example.com"))
            .andExpect(model().attribute("userName", "Test User"))
            .andExpect(model().attribute("userAvatar", ""))
    }

    @Test
    fun `given authenticated OAuth2 user with avatar when accessing settings then should include avatar in model`() {
        // given - authenticated OAuth2 user with all profile attributes including avatar
        val userAttributes = mapOf(
            "email" to "test@example.com",
            "name" to "Test User",
            "picture" to "https://example.com/avatar.jpg"
        )

        // when - making GET request to settings endpoint with OAuth2 authentication
        mockMvc.perform(
            get("/settings")
                .with(oauth2Login().attributes { attrs ->
                    attrs.putAll(userAttributes)
                })
        )
            // then - should render settings template with correct user attributes including avatar
            .andExpect(status().isOk)
            .andExpect(view().name("settings"))
            .andExpect(model().attribute("userEmail", "test@example.com"))
            .andExpect(model().attribute("userName", "Test User"))
            .andExpect(model().attribute("userAvatar", "https://example.com/avatar.jpg"))
    }

    @Test
    fun `given authenticated OAuth2 user without avatar when accessing settings then should include empty avatar in model`() {
        // given - authenticated OAuth2 user without picture attribute (avatar missing)
        val userAttributes = mapOf(
            "email" to "test@example.com",
            "name" to "Test User"
            // picture attribute is intentionally omitted
        )

        // when - making GET request to settings endpoint with OAuth2 authentication
        mockMvc.perform(
            get("/settings")
                .with(oauth2Login().attributes { attrs ->
                    attrs.putAll(userAttributes)
                })
        )
            // then - should render settings template with empty avatar for graceful fallback
            .andExpect(status().isOk)
            .andExpect(view().name("settings"))
            .andExpect(model().attribute("userEmail", "test@example.com"))
            .andExpect(model().attribute("userName", "Test User"))
            .andExpect(model().attribute("userAvatar", ""))
    }

    @Test
    @WithMockUser
    fun `given authenticated non-OAuth2 user when accessing profile then should include empty avatar in model`() {
        // given - authenticated user but not OAuth2 (fallback case)

        // when - making GET request to profile endpoint with basic authentication
        mockMvc.perform(get("/profile"))
            // then - should render profile template with empty avatar for non-OAuth2 users
            .andExpect(status().isOk)
            .andExpect(view().name("profile"))
            .andExpect(model().attribute("userAvatar", ""))
    }

    @Test
    @WithMockUser
    fun `given authenticated non-OAuth2 user when accessing settings then should include empty avatar in model`() {
        // given - authenticated user but not OAuth2 (fallback case)

        // when - making GET request to settings endpoint with basic authentication
        mockMvc.perform(get("/settings"))
            // then - should render settings template with empty avatar for non-OAuth2 users
            .andExpect(status().isOk)
            .andExpect(view().name("settings"))
            .andExpect(model().attribute("userAvatar", ""))
    }
}
