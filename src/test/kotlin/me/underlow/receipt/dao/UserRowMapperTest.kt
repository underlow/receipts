package me.underlow.receipt.dao

import me.underlow.receipt.model.User
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Unit tests for UserRowMapper.
 * Tests mapping of database rows to User objects.
 */
class UserRowMapperTest {
    
    private lateinit var userRowMapper: UserRowMapper
    private lateinit var resultSet: ResultSet
    
    @BeforeEach
    fun setUp() {
        userRowMapper = UserRowMapper()
        resultSet = mock()
    }
    
    @Test
    fun `given complete user data when mapRow then returns User with all fields`() {
        // given - complete user data in result set
        val createdAt = LocalDateTime.of(2023, 1, 1, 10, 0, 0)
        val updatedAt = LocalDateTime.of(2023, 1, 2, 11, 0, 0)
        
        whenever(resultSet.getLong("id")).thenReturn(1L)
        whenever(resultSet.getString("email")).thenReturn("test@example.com")
        whenever(resultSet.getString("name")).thenReturn("Test User")
        whenever(resultSet.getString("avatar")).thenReturn("https://example.com/avatar.jpg")
        whenever(resultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(createdAt))
        whenever(resultSet.getTimestamp("updated_at")).thenReturn(Timestamp.valueOf(updatedAt))
        
        // when - mapping row to User
        val user = userRowMapper.mapRow(resultSet, 0)
        
        // then - User object contains all expected data
        assertEquals(1L, user.id)
        assertEquals("test@example.com", user.email)
        assertEquals("Test User", user.name)
        assertEquals("https://example.com/avatar.jpg", user.avatar)
        assertEquals(createdAt, user.createdAt)
        assertEquals(updatedAt, user.updatedAt)
    }
    
    @Test
    fun `given user data with null avatar when mapRow then returns User with null avatar`() {
        // given - user data with null avatar
        val createdAt = LocalDateTime.of(2023, 1, 1, 10, 0, 0)
        val updatedAt = LocalDateTime.of(2023, 1, 2, 11, 0, 0)
        
        whenever(resultSet.getLong("id")).thenReturn(2L)
        whenever(resultSet.getString("email")).thenReturn("user@example.com")
        whenever(resultSet.getString("name")).thenReturn("User Without Avatar")
        whenever(resultSet.getString("avatar")).thenReturn(null)
        whenever(resultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(createdAt))
        whenever(resultSet.getTimestamp("updated_at")).thenReturn(Timestamp.valueOf(updatedAt))
        
        // when - mapping row to User
        val user = userRowMapper.mapRow(resultSet, 0)
        
        // then - User object has null avatar
        assertEquals(2L, user.id)
        assertEquals("user@example.com", user.email)
        assertEquals("User Without Avatar", user.name)
        assertNull(user.avatar)
        assertEquals(createdAt, user.createdAt)
        assertEquals(updatedAt, user.updatedAt)
    }
    
    @Test
    fun `given invalid result set when mapRow then throws SQLException`() {
        // given - result set that throws SQLException
        whenever(resultSet.getLong("id")).thenThrow(SQLException("Column not found"))
        
        // when/then - SQLException is thrown
        assertThrows<SQLException> {
            userRowMapper.mapRow(resultSet, 0)
        }
    }
}