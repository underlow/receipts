import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.SecurityFilterChain

// In src/test/kotlin/me/underlow/receipt/config/TestSecurityConfig.kt
//@TestConfiguration
//@EnableWebSecurity
//@Profile("e2e")
//class TestSecurityConfig(private val mockOAuth2UserService: OAuth2UserService<OAuth2UserRequest, OAuth2User>) {
//
//    @Bean
//    @Primary
//    fun testSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
//        http
//            .authorizeHttpRequests { requests ->
//                // Secure all endpoints, except for the login page and static assets
//                requests
//                    .requestMatchers("/login", "/static/**", "/error").permitAll()
//                    .anyRequest().authenticated()
//            }
//            .oauth2Login { oauth2Login ->
//                // Enable the login flow but use the mock user service
//                oauth2Login
//                    .loginPage("/login")
//                    .userInfoEndpoint { userInfo ->
//                        userInfo.userService(mockOAuth2UserService) // Use the mock service from MockOAuth2Config
//                    }
//                    .defaultSuccessUrl("/dashboard", true)
//            }
//            .csrf { it.disable() } // CSRF can be disabled for simplicity in tests
//        return http.build()
//    }
//}
