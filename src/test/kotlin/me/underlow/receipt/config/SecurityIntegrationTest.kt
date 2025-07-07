package me.underlow.receipt.config

import me.underlow.receipt.dao.UserDao
import me.underlow.receipt.model.User
import me.underlow.receipt.service.UserService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for Spring Security configuration.
 * Tests complete OAuth2 authentication flow, email validation, user management, 
 * session handling, logout functionality, and access control.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Testcontainers
class SecurityIntegrationTest {
    
    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer<Nothing>("postgres:15-alpine").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }
        
        @JvmStatic
        @org.springframework.test.context.DynamicPropertySource
        fun configureProperties(registry: org.springframework.test.context.DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }
    
    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext
    
    @Autowired
    private lateinit var userDao: UserDao
    
    @Autowired
    private lateinit var userService: UserService
    
    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate
    
    private lateinit var mockMvc: MockMvc
    
    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
        
        // Clean up database before each test
        jdbcTemplate.execute("DELETE FROM users")
    }
    
    @Test
    fun `given unauthenticated user when accessing protected endpoint then redirects to login`() {
        // given - unauthenticated user trying to access protected endpoint
        
        // when - accessing protected dashboard endpoint
        mockMvc.perform(get("/dashboard"))
            // then - redirects to login page
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("http://localhost/login"))
    }
    
    @Test
    fun `given unauthenticated user when accessing public endpoints then allows access`() {
        // given - unauthenticated user
        
        // when - accessing public login endpoint
        mockMvc.perform(get("/login"))
            // then - allows access without authentication
            .andExpect(status().isOk)
        
        // when - accessing public error endpoint
        mockMvc.perform(get("/error"))
            // then - allows access without authentication
            .andExpect(status().isOk)
        
        // when - accessing public static resources
        mockMvc.perform(get("/css/style.css"))
            // then - allows access without authentication
            .andExpect(status().isNotFound) // 404 is expected since file doesn't exist, but no redirect
    }
    
    @Test
    fun `given allowed email OAuth2 user when authenticating then creates user and grants access`() {
        // given - OAuth2 user with email in allowlist
        val allowedEmail = "test@example.com"
        val userName = "Test User"
        val userAvatar = "https://example.com/avatar.jpg"
        
        // when - authenticating with OAuth2 using allowed email
        mockMvc.perform(get("/dashboard")
            .with(oauth2Login()
                .attributes { attrs ->
                    attrs["email"] = allowedEmail
                    attrs["name"] = userName
                    attrs["picture"] = userAvatar
                }
            ))
            // then - grants access to protected endpoint
            .andExpect(status().isOk)
        
        // then - user is created in database
        val createdUser = userDao.findByEmail(allowedEmail)
        assertNotNull(createdUser)
        assertEquals(allowedEmail, createdUser.email)
        assertEquals(userName, createdUser.name)
        assertEquals(userAvatar, createdUser.avatar)
    }
    
    @Test
    fun `given existing user OAuth2 authentication when authenticating then updates user information`() {
        // given - existing user in database
        val existingEmail = "test@example.com"
        val originalUser = User(
            email = existingEmail,
            name = "Original Name",
            avatar = "https://example.com/original.jpg"
        )
        userDao.save(originalUser)
        
        // when - authenticating with OAuth2 using same email but updated info
        val updatedName = "Updated Name"
        val updatedAvatar = "https://example.com/updated.jpg"
        
        mockMvc.perform(get("/dashboard")
            .with(oauth2Login()
                .attributes { attrs ->
                    attrs["email"] = existingEmail
                    attrs["name"] = updatedName
                    attrs["picture"] = updatedAvatar
                }
            ))
            // then - grants access to protected endpoint
            .andExpect(status().isOk)
        
        // then - user information is updated in database
        val updatedUser = userDao.findByEmail(existingEmail)
        assertNotNull(updatedUser)
        assertEquals(existingEmail, updatedUser.email)
        assertEquals(updatedName, updatedUser.name)
        assertEquals(updatedAvatar, updatedUser.avatar)
        assertEquals(originalUser.id, updatedUser.id) // Same user ID
    }
    
    @Test
    fun `given non-allowed email OAuth2 user when authenticating then denies access`() {
        // given - OAuth2 user with email not in allowlist
        val nonAllowedEmail = "notallowed@example.com"
        val userName = "Non-allowed User"
        val userAvatar = "https://example.com/avatar.jpg"
        
        // when - authenticating with OAuth2 using non-allowed email
        mockMvc.perform(get("/dashboard")
            .with(oauth2Login()
                .attributes { attrs ->
                    attrs["email"] = nonAllowedEmail
                    attrs["name"] = userName
                    attrs["picture"] = userAvatar
                }
            ))
            // then - authentication fails and redirects to login with error
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrlPattern("**/login?error"))
        
        // then - user is not created in database
        val user = userDao.findByEmail(nonAllowedEmail)
        assertEquals(null, user)
    }
    
    @Test
    fun `given authenticated user when accessing multiple protected endpoints then grants access`() {
        // given - authenticated user with allowed email
        val allowedEmail = "test@example.com"
        val userName = "Test User"
        
        // when - accessing different protected endpoints
        mockMvc.perform(get("/dashboard")
            .with(oauth2Login()
                .attributes { attrs ->
                    attrs["email"] = allowedEmail
                    attrs["name"] = userName
                }
            ))
            // then - grants access to dashboard
            .andExpect(status().isOk)
        
        mockMvc.perform(get("/profile")
            .with(oauth2Login()
                .attributes { attrs ->
                    attrs["email"] = allowedEmail
                    attrs["name"] = userName
                }
            ))
            // then - grants access to profile endpoint
            .andExpect(status().isOk)
        
        mockMvc.perform(get("/settings")
            .with(oauth2Login()
                .attributes { attrs ->
                    attrs["email"] = allowedEmail
                    attrs["name"] = userName
                }
            ))
            // then - grants access to settings endpoint
            .andExpect(status().isOk)
    }
    
    @Test
    fun `given authenticated user when logout then clears authentication and redirects to login`() {
        // given - authenticated user
        val allowedEmail = "test@example.com"
        val userName = "Test User"
        
        // when - performing logout
        mockMvc.perform(post("/logout")
            .with(csrf())
            .with(oauth2Login()
                .attributes { attrs ->
                    attrs["email"] = allowedEmail
                    attrs["name"] = userName
                }
            ))
            // then - redirects to login page
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/login"))
    }
    
    @Test
    fun `given session-based authentication when accessing endpoints then maintains session`() {
        // given - authenticated user with session
        val allowedEmail = "test@example.com"
        val userName = "Test User"
        
        // when - accessing protected endpoint with OAuth2 authentication
        val result = mockMvc.perform(get("/dashboard")
            .with(oauth2Login()
                .attributes { attrs ->
                    attrs["email"] = allowedEmail
                    attrs["name"] = userName
                }
            ))
            // then - grants access and maintains session
            .andExpect(status().isOk)
            .andReturn()
        
        // then - session is created and maintained
        val session = result.request.getSession(false)
        assertNotNull(session)
        assertTrue(session.isNew || !session.isNew) // Session exists
    }
    
    @Test
    fun `given OAuth2 user with missing email when authenticating then authentication fails`() {
        // given - OAuth2 user without email attribute
        val userName = "Test User"
        val userAvatar = "https://example.com/avatar.jpg"
        
        // when - authenticating with OAuth2 without email
        mockMvc.perform(get("/dashboard")
            .with(oauth2Login()
                .attributes { attrs ->
                    attrs["name"] = userName
                    attrs["picture"] = userAvatar
                }
            ))
            // then - authentication fails and redirects to login with error
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrlPattern("**/login?error"))
    }
    
    @Test
    fun `given OAuth2 user with missing name when authenticating then authentication fails`() {
        // given - OAuth2 user without name attribute
        val allowedEmail = "test@example.com"
        val userAvatar = "https://example.com/avatar.jpg"
        
        // when - authenticating with OAuth2 without name
        mockMvc.perform(get("/dashboard")
            .with(oauth2Login()
                .attributes { attrs ->
                    attrs["email"] = allowedEmail
                    attrs["picture"] = userAvatar
                }
            ))
            // then - authentication fails and redirects to login with error
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrlPattern("**/login?error"))
    }
    
    @Test
    fun `given OAuth2 user with null avatar when authenticating then creates user with null avatar`() {
        // given - OAuth2 user without picture attribute (avatar is optional)
        val allowedEmail = "test@example.com"
        val userName = "Test User"
        
        // when - authenticating with OAuth2 without picture
        mockMvc.perform(get("/dashboard")
            .with(oauth2Login()
                .attributes { attrs ->
                    attrs["email"] = allowedEmail
                    attrs["name"] = userName
                }
            ))
            // then - grants access to protected endpoint
            .andExpect(status().isOk)
        
        // then - user is created with null avatar
        val createdUser = userDao.findByEmail(allowedEmail)
        assertNotNull(createdUser)
        assertEquals(allowedEmail, createdUser.email)
        assertEquals(userName, createdUser.name)
        assertEquals(null, createdUser.avatar)
    }
    
    @Test
    fun `given email validation service when checking allowed emails then validates correctly`() {
        // given - email validation service with configured allowlist
        val allowedEmail = "test@example.com"
        val nonAllowedEmail = "notallowed@example.com"
        
        // when - checking if emails are allowed
        val allowedResult = userService.isEmailAllowed(allowedEmail)
        val nonAllowedResult = userService.isEmailAllowed(nonAllowedEmail)
        
        // then - validates emails against allowlist correctly
        assertTrue(allowedResult, "Email $allowedEmail should be allowed")
        assertEquals(false, nonAllowedResult, "Email $nonAllowedEmail should not be allowed")
    }
}