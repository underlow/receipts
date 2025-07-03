import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.filter.OncePerRequestFilter

@Configuration
@EnableWebSecurity
@Profile("e2e")
class TestSecurityConfig {

    @Bean
    @Primary
    fun testSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { requests ->
                requests.anyRequest().permitAll()
            }
            .oauth2Login { it.disable() }  // Disable OAuth2 login entirely
            .addFilterBefore(MockOAuth2AuthenticationFilter(), UsernamePasswordAuthenticationFilter::class.java)
            .csrf { it.disable() }
        return http.build()
    }

    /**
     * Filter that sets up mock OAuth2 authentication for all requests in e2e tests
     */
    class MockOAuth2AuthenticationFilter : OncePerRequestFilter() {
        override fun doFilterInternal(
            request: HttpServletRequest,
            response: HttpServletResponse,
            filterChain: FilterChain
        ) {
            // Extract email from request parameter or use default
            val email = request.getParameter("userEmail") ?: "testuser@example.com"
            val name = email.substringBefore("@").replaceFirstChar { it.uppercase() }

            // Create mock OAuth2 user
            val mockUser = DefaultOAuth2User(
                listOf(SimpleGrantedAuthority("ROLE_USER")),
                mapOf(
                    "sub" to "test-user-id",
                    "email" to email,
                    "name" to name
                ),
                "sub"
            )

            // Create OAuth2 authentication token
            val authToken = OAuth2AuthenticationToken(
                mockUser,
                listOf(SimpleGrantedAuthority("ROLE_USER")),
                "google"
            )

            // Set authentication in security context
            SecurityContextHolder.getContext().authentication = authToken

            filterChain.doFilter(request, response)
        }
    }
}
