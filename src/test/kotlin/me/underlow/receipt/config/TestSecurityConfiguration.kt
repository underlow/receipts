package me.underlow.receipt.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

/**
 * Test security configuration that bypasses OAuth2 for E2E testing.
 * Uses form-based authentication with in-memory test users.
 * Active only for test profile to avoid interfering with production security.
 */
@Configuration
@Profile("test")
class TestSecurityConfiguration {

    companion object {
        // Test users with different email addresses for E2E testing
        const val ALLOWED_EMAIL_1 = "allowed1@example.com"
        const val ALLOWED_EMAIL_2 = "allowed2@example.com"
        const val NOT_ALLOWED_EMAIL = "notallowed@example.com"

        // Test user passwords
        const val TEST_PASSWORD = "testpassword"

        // Test user names
        const val TEST_USER_NAME_1 = "Allowed User 1"
        const val TEST_USER_NAME_2 = "Allowed User 2"
        const val TEST_USER_NAME_NOT_ALLOWED = "Not Allowed User"
    }

    /**
     * Configures the security filter chain for E2E testing.
     * Replaces OAuth2 login with form-based authentication.
     *
     * @param http HttpSecurity to configure
     * @return SecurityFilterChain bean for testing
     */
    @Bean
    @Primary
    fun testSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/login", "/error", "/css/**", "/js/**", "/images/**").permitAll()
                    .anyRequest().authenticated()
            }
            .formLogin { form ->
                form
                    .loginPage("/login")
                    .defaultSuccessUrl("/dashboard", true)
                    .failureUrl("/login?error=true")
                    .usernameParameter("username")
                    .passwordParameter("password")
                    .permitAll()
            }
            .logout { logout ->
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login")
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .deleteCookies("JSESSIONID")
                    .permitAll()
            }
            .csrf { csrf ->
                csrf.disable() // Disable CSRF for E2E testing simplicity
            }

        return http.build()
    }

    /**
     * Creates password encoder for test users.
     * Uses BCrypt for secure password hashing in tests.
     *
     * @return PasswordEncoder bean for testing
     */
    @Bean
    @Primary
    fun testPasswordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    /**
     * Creates test user details service with predefined test users.
     * Provides users with allowed and non-allowed email addresses for testing.
     *
     * @param passwordEncoder Password encoder for test users
     * @return UserDetailsService bean for testing
     */
    @Bean
    @Primary
    fun testUserDetailsService(passwordEncoder: PasswordEncoder): UserDetailsService {
        val users = mutableListOf<UserDetails>()

        // Test user with allowed email 1
        val allowedUser1 = User.builder()
            .username(ALLOWED_EMAIL_1)
            .password(passwordEncoder.encode(TEST_PASSWORD))
            .roles("USER")
            .build()
        users.add(allowedUser1)

        // Test user with allowed email 2
        val allowedUser2 = User.builder()
            .username(ALLOWED_EMAIL_2)
            .password(passwordEncoder.encode(TEST_PASSWORD))
            .roles("USER")
            .build()
        users.add(allowedUser2)

        // Test user with not allowed email
        val notAllowedUser = User.builder()
            .username(NOT_ALLOWED_EMAIL)
            .password(passwordEncoder.encode(TEST_PASSWORD))
            .roles("USER")
            .build()
        users.add(notAllowedUser)

        return InMemoryUserDetailsManager(users)
    }
}
