package me.underlow.receipt.config

import me.underlow.receipt.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

/**
 * E2E test security configuration that bypasses OAuth2 for E2E testing.
 * Uses form-based authentication with in-memory test users and CSRF enabled.
 * Active only for e2e-test profile to avoid interfering with production security.
 */
@Configuration
@Profile("e2e-test")
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
     * Replaces OAuth2 login with form-based authentication and keeps CSRF enabled.
     *
     * @param http HttpSecurity to configure
     * @param e2eTestAuthenticationProvider Custom authentication provider for email allowlist validation
     * @return SecurityFilterChain bean for testing
     */
    @Bean
    @Primary
    fun e2eTestSecurityFilterChain(
        http: HttpSecurity,
        e2eTestAuthenticationProvider: E2ETestAuthenticationProvider
    ): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/login", "/error", "/css/**", "/js/**", "/images/**").permitAll()
                    .anyRequest().authenticated()
            }
            .authenticationProvider(e2eTestAuthenticationProvider)
            .formLogin { form ->
                form
                    .loginPage("/login")
                    .defaultSuccessUrl("/dashboard", true)
                    .failureUrl("/login?error=access_denied")
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
            // Keep CSRF enabled with default configuration for E2E testing (same as production)

        return http.build()
    }

    /**
     * Creates password encoder for E2E test users.
     * Uses BCrypt for secure password hashing in tests.
     *
     * @return PasswordEncoder bean for testing
     */
    @Bean
    @Primary
    fun e2eTestPasswordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    /**
     * Creates E2E test user details service with predefined test users.
     * Provides users with allowed and non-allowed email addresses for testing.
     *
     * @param passwordEncoder Password encoder for test users
     * @return UserDetailsService bean for testing
     */
    @Bean
    @Primary
    fun e2eTestUserDetailsService(passwordEncoder: PasswordEncoder): UserDetailsService {
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

    /**
     * Custom authentication provider that validates credentials and email allowlist.
     * Integrates form-based authentication with email allowlist validation from UserService.
     *
     * @param userDetailsService Service for loading user details
     * @param passwordEncoder Password encoder for validation
     * @param userService Service for email allowlist validation
     */
    @Bean
    @Primary
    fun e2eTestAuthenticationProvider(
        userDetailsService: UserDetailsService,
        passwordEncoder: PasswordEncoder,
        userService: UserService
    ): E2ETestAuthenticationProvider {
        return E2ETestAuthenticationProvider(userDetailsService, passwordEncoder, userService)
    }
}

/**
 * Custom authentication provider for E2E test environment.
 * Validates both username/password and email allowlist.
 */
class E2ETestAuthenticationProvider(
    private val userDetailsService: UserDetailsService,
    private val passwordEncoder: PasswordEncoder,
    private val userService: UserService
) : AuthenticationProvider {

    /**
     * Authenticates the user credentials and validates against email allowlist.
     *
     * @param authentication The authentication request
     * @return Authenticated token if successful
     * @throws BadCredentialsException if credentials are invalid or email not allowed
     */
    override fun authenticate(authentication: Authentication): Authentication {
        val username = authentication.name
        val password = authentication.credentials.toString()

        // Load user details
        val userDetails = try {
            userDetailsService.loadUserByUsername(username)
        } catch (e: Exception) {
            throw BadCredentialsException("Invalid credentials")
        }

        // Validate password
        if (!passwordEncoder.matches(password, userDetails.password)) {
            throw BadCredentialsException("Invalid credentials")
        }

        // Validate email against allowlist
        if (!userService.isEmailAllowed(username)) {
            throw BadCredentialsException("Email $username is not in the allowlist")
        }

        // Return authenticated token
        return UsernamePasswordAuthenticationToken(
            userDetails,
            password,
            userDetails.authorities
        )
    }

    /**
     * Indicates whether this provider supports the given authentication type.
     *
     * @param authentication The authentication class
     * @return true if this provider supports the authentication type
     */
    override fun supports(authentication: Class<*>): Boolean {
        return UsernamePasswordAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}
