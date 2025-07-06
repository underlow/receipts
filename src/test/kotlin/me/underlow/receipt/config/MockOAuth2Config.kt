package me.underlow.receipt.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User

@TestConfiguration
@Profile("e2e")
class MockOAuth2Config {
    @Bean
    @Primary
    fun mockOAuth2UserService(): OAuth2UserService<OAuth2UserRequest, OAuth2User> =
        OAuth2UserService { _ ->
            DefaultOAuth2User(
                listOf(SimpleGrantedAuthority("ROLE_USER")),
                mapOf(
                    "sub" to "42",
                    "email" to "testuser@example.com",
                    "name" to "Test User"
                ),
                "sub"
            )
        }
}
