package me.underlow.receipt.dao

import me.underlow.receipt.model.User
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Integration tests for UserDao.
 * Tests database operations with actual PostgreSQL database using TestContainers.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Testcontainers
class UserDaoIntegrationTest {
    
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
    private lateinit var userDao: UserDao
    
    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate
    
    @BeforeEach
    fun setUp() {
        // Clean up database before each test
        jdbcTemplate.execute("DELETE FROM users")
    }
    
    @Test
    fun `given empty database when findByEmail then returns null`() {
        // given - empty database
        
        // when - finding user by email
        val result = userDao.findByEmail("test@example.com")
        
        // then - returns null
        assertNull(result)
    }
    
    @Test
    fun `given user in database when findByEmail then returns user`() {
        // given - user inserted in database
        val testUser = User(
            email = "test@example.com",
            name = "Test User",
            avatar = "https://example.com/avatar.jpg"
        )
        val savedUser = userDao.save(testUser)
        
        // when - finding user by email
        val result = userDao.findByEmail("test@example.com")
        
        // then - returns saved user
        assertNotNull(result)
        assertEquals(savedUser.id, result.id)
        assertEquals(savedUser.email, result.email)
        assertEquals(savedUser.name, result.name)
        assertEquals(savedUser.avatar, result.avatar)
    }
    
    @Test
    fun `given empty database when existsByEmail then returns false`() {
        // given - empty database
        
        // when - checking if email exists
        val result = userDao.existsByEmail("test@example.com")
        
        // then - returns false
        assertFalse(result)
    }
    
    @Test
    fun `given user in database when existsByEmail then returns true`() {
        // given - user inserted in database
        val testUser = User(
            email = "existing@example.com",
            name = "Existing User"
        )
        userDao.save(testUser)
        
        // when - checking if email exists
        val result = userDao.existsByEmail("existing@example.com")
        
        // then - returns true
        assertTrue(result)
    }
    
    @Test
    fun `given new user when save then inserts user with generated ID and timestamps`() {
        // given - new user without ID
        val newUser = User(
            email = "new@example.com",
            name = "New User",
            avatar = "https://example.com/new.jpg"
        )
        val beforeSave = LocalDateTime.now()
        
        // when - saving new user
        val result = userDao.save(newUser)
        
        // then - user is saved with generated ID and timestamps
        assertNotNull(result.id)
        assertTrue(result.id!! > 0)
        assertEquals(newUser.email, result.email)
        assertEquals(newUser.name, result.name)
        assertEquals(newUser.avatar, result.avatar)
        assertTrue(result.createdAt.isAfter(beforeSave) || result.createdAt.isEqual(beforeSave))
        assertTrue(result.updatedAt.isAfter(beforeSave) || result.updatedAt.isEqual(beforeSave))
    }
    
    @Test
    fun `given existing user when save then updates user and timestamp`() {
        // given - existing user in database
        val originalUser = User(
            email = "update@example.com",
            name = "Original Name",
            avatar = "https://example.com/original.jpg"
        )
        val savedUser = userDao.save(originalUser)
        
        // Wait a bit to ensure timestamp difference
        Thread.sleep(10)
        
        val updatedUser = savedUser.copy(
            name = "Updated Name",
            avatar = "https://example.com/updated.jpg"
        )
        
        // when - saving updated user
        val result = userDao.save(updatedUser)
        
        // then - user is updated with new timestamp
        assertEquals(savedUser.id, result.id)
        assertEquals(updatedUser.name, result.name)
        assertEquals(updatedUser.avatar, result.avatar)
        assertEquals(savedUser.createdAt, result.createdAt) // Created timestamp unchanged
        assertTrue(result.updatedAt.isAfter(savedUser.updatedAt)) // Updated timestamp changed
    }
    
    @Test
    fun `given existing user ID when findById then returns user`() {
        // given - user inserted in database
        val testUser = User(
            email = "findbyid@example.com",
            name = "Find By ID User",
            avatar = null
        )
        val savedUser = userDao.save(testUser)
        
        // when - finding user by ID
        val result = userDao.findById(savedUser.id!!)
        
        // then - returns saved user
        assertNotNull(result)
        assertEquals(savedUser.id, result.id)
        assertEquals(savedUser.email, result.email)
        assertEquals(savedUser.name, result.name)
        assertEquals(savedUser.avatar, result.avatar)
    }
    
    @Test
    fun `given non-existing ID when findById then returns null`() {
        // given - empty database
        
        // when - finding user by non-existing ID
        val result = userDao.findById(999L)
        
        // then - returns null
        assertNull(result)
    }
    
    @Test
    fun `given duplicate email when save then throws DataIntegrityViolationException`() {
        // given - user with email already in database
        val firstUser = User(
            email = "duplicate@example.com",
            name = "First User"
        )
        userDao.save(firstUser)
        
        val duplicateUser = User(
            email = "duplicate@example.com",
            name = "Duplicate User"
        )
        
        // when/then - DataIntegrityViolationException is thrown
        assertThrows<DataIntegrityViolationException> {
            userDao.save(duplicateUser)
        }
    }
    
    @Test
    fun `given user with null avatar when save then saves successfully`() {
        // given - user with null avatar
        val userWithNullAvatar = User(
            email = "noavatar@example.com",
            name = "No Avatar User",
            avatar = null
        )
        
        // when - saving user with null avatar
        val result = userDao.save(userWithNullAvatar)
        
        // then - user is saved successfully
        assertNotNull(result.id)
        assertEquals(userWithNullAvatar.email, result.email)
        assertEquals(userWithNullAvatar.name, result.name)
        assertNull(result.avatar)
    }
    
    @Test
    fun `given concurrent save operations when saving same user then handles correctly`() {
        // given - user saved initially
        val user = User(
            email = "concurrent@example.com",
            name = "Concurrent User"
        )
        val savedUser = userDao.save(user)
        
        // when - updating same user concurrently (simulated)
        val update1 = savedUser.copy(name = "Update 1")
        val update2 = savedUser.copy(name = "Update 2")
        
        userDao.save(update1)
        val finalResult = userDao.save(update2)
        
        // then - final update is applied
        assertEquals(savedUser.id, finalResult.id)
        assertEquals("Update 2", finalResult.name)
    }
}