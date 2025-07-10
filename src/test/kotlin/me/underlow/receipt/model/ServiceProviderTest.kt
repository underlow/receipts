package me.underlow.receipt.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull
import java.time.LocalDateTime

/**
 * Unit tests for ServiceProvider entity class.
 * Tests entity creation, state transitions, validation methods, and business logic.
 */
@ExtendWith(MockitoExtension::class)
class ServiceProviderTest {

    @Test
    fun `given service provider parameters when created then should have correct initial state`() {
        // given - parameters for new service provider entity
        val id = 1L
        val name = "ABC Electric Company"
        val avatar = "avatars/abc-electric.png"
        val comment = "Monthly electricity bills"
        val commentForOcr = "Look for 'ABC Electric' logo"
        val regular = RegularFrequency.MONTHLY
        val customFields = """[{"name": "Account Number", "value": "12345", "comment": "Primary account"}]"""
        val state = ServiceProviderState.ACTIVE
        val createdDate = LocalDateTime.now()
        val modifiedDate = LocalDateTime.now()
        
        // when - creating new service provider entity
        val entity = ServiceProvider(
            id = id,
            name = name,
            avatar = avatar,
            comment = comment,
            commentForOcr = commentForOcr,
            regular = regular,
            customFields = customFields,
            state = state,
            createdDate = createdDate,
            modifiedDate = modifiedDate
        )
        
        // then - should have correct initial state
        assertNotNull(entity)
        assertEquals(id, entity.id)
        assertEquals(name, entity.name)
        assertEquals(avatar, entity.avatar)
        assertEquals(comment, entity.comment)
        assertEquals(commentForOcr, entity.commentForOcr)
        assertEquals(regular, entity.regular)
        assertEquals(customFields, entity.customFields)
        assertEquals(state, entity.state)
        assertEquals(createdDate, entity.createdDate)
        assertEquals(modifiedDate, entity.modifiedDate)
    }

    @Test
    fun `given service provider in ACTIVE state when hide called then should transition to HIDDEN state`() {
        // given - service provider in ACTIVE state
        val entity = ServiceProvider(
            id = 1L,
            name = "ABC Electric Company",
            avatar = null,
            comment = null,
            commentForOcr = null,
            regular = RegularFrequency.NOT_REGULAR,
            customFields = null,
            state = ServiceProviderState.ACTIVE,
            createdDate = LocalDateTime.now(),
            modifiedDate = LocalDateTime.now()
        )
        
        // when - hiding service provider
        val hiddenEntity = entity.hide()
        
        // then - should transition to HIDDEN state
        assertNotNull(hiddenEntity)
        assertEquals(ServiceProviderState.HIDDEN, hiddenEntity.state)
        assertEquals(entity.id, hiddenEntity.id)
        assertEquals(entity.name, hiddenEntity.name)
        assertEquals(entity.avatar, hiddenEntity.avatar)
        assertEquals(entity.comment, hiddenEntity.comment)
        assertEquals(entity.commentForOcr, hiddenEntity.commentForOcr)
        assertEquals(entity.regular, hiddenEntity.regular)
        assertEquals(entity.customFields, hiddenEntity.customFields)
        assertTrue(hiddenEntity.modifiedDate.isAfter(entity.modifiedDate) || hiddenEntity.modifiedDate.isEqual(entity.modifiedDate))
    }

    @Test
    fun `given service provider in HIDDEN state when show called then should transition to ACTIVE state`() {
        // given - service provider in HIDDEN state
        val entity = ServiceProvider(
            id = 1L,
            name = "ABC Electric Company",
            avatar = null,
            comment = null,
            commentForOcr = null,
            regular = RegularFrequency.NOT_REGULAR,
            customFields = null,
            state = ServiceProviderState.HIDDEN,
            createdDate = LocalDateTime.now(),
            modifiedDate = LocalDateTime.now()
        )
        
        // when - showing service provider
        val activeEntity = entity.show()
        
        // then - should transition to ACTIVE state
        assertNotNull(activeEntity)
        assertEquals(ServiceProviderState.ACTIVE, activeEntity.state)
        assertEquals(entity.id, activeEntity.id)
        assertEquals(entity.name, activeEntity.name)
        assertEquals(entity.avatar, activeEntity.avatar)
        assertEquals(entity.comment, activeEntity.comment)
        assertEquals(entity.commentForOcr, activeEntity.commentForOcr)
        assertEquals(entity.regular, activeEntity.regular)
        assertEquals(entity.customFields, activeEntity.customFields)
        assertTrue(activeEntity.modifiedDate.isAfter(entity.modifiedDate) || activeEntity.modifiedDate.isEqual(entity.modifiedDate))
    }

    @Test
    fun `given service provider in ACTIVE state when isActive called then should return true`() {
        // given - service provider in ACTIVE state
        val entity = ServiceProvider(
            id = 1L,
            name = "ABC Electric Company",
            avatar = null,
            comment = null,
            commentForOcr = null,
            regular = RegularFrequency.NOT_REGULAR,
            customFields = null,
            state = ServiceProviderState.ACTIVE,
            createdDate = LocalDateTime.now(),
            modifiedDate = LocalDateTime.now()
        )
        
        // when - checking if is active
        val isActive = entity.isActive()
        
        // then - should return true
        assertTrue(isActive)
    }

    @Test
    fun `given service provider in HIDDEN state when isActive called then should return false`() {
        // given - service provider in HIDDEN state
        val entity = ServiceProvider(
            id = 1L,
            name = "ABC Electric Company",
            avatar = null,
            comment = null,
            commentForOcr = null,
            regular = RegularFrequency.NOT_REGULAR,
            customFields = null,
            state = ServiceProviderState.HIDDEN,
            createdDate = LocalDateTime.now(),
            modifiedDate = LocalDateTime.now()
        )
        
        // when - checking if is active
        val isActive = entity.isActive()
        
        // then - should return false
        assertFalse(isActive)
    }

    @Test
    fun `given service provider when updateName called then should update name correctly`() {
        // given - service provider with original name
        val entity = ServiceProvider(
            id = 1L,
            name = "ABC Electric Company",
            avatar = null,
            comment = null,
            commentForOcr = null,
            regular = RegularFrequency.NOT_REGULAR,
            customFields = null,
            state = ServiceProviderState.ACTIVE,
            createdDate = LocalDateTime.now(),
            modifiedDate = LocalDateTime.now()
        )
        val newName = "XYZ Electric Company"
        
        // when - updating name
        val updatedEntity = entity.updateName(newName)
        
        // then - should update name correctly
        assertNotNull(updatedEntity)
        assertEquals(newName, updatedEntity.name)
        assertEquals(entity.id, updatedEntity.id)
        assertEquals(entity.avatar, updatedEntity.avatar)
        assertEquals(entity.comment, updatedEntity.comment)
        assertEquals(entity.commentForOcr, updatedEntity.commentForOcr)
        assertEquals(entity.regular, updatedEntity.regular)
        assertEquals(entity.customFields, updatedEntity.customFields)
        assertEquals(entity.state, updatedEntity.state)
        assertTrue(updatedEntity.modifiedDate.isAfter(entity.modifiedDate) || updatedEntity.modifiedDate.isEqual(entity.modifiedDate))
    }

    @Test
    fun `given service provider when updateCustomFields called then should update custom fields correctly`() {
        // given - service provider with original custom fields
        val entity = ServiceProvider(
            id = 1L,
            name = "ABC Electric Company",
            avatar = null,
            comment = null,
            commentForOcr = null,
            regular = RegularFrequency.NOT_REGULAR,
            customFields = """[{"name": "Account Number", "value": "12345", "comment": "Primary account"}]""",
            state = ServiceProviderState.ACTIVE,
            createdDate = LocalDateTime.now(),
            modifiedDate = LocalDateTime.now()
        )
        val newCustomFields = """[{"name": "Customer ID", "value": "67890", "comment": "Secondary ID"}]"""
        
        // when - updating custom fields
        val updatedEntity = entity.updateCustomFields(newCustomFields)
        
        // then - should update custom fields correctly
        assertNotNull(updatedEntity)
        assertEquals(newCustomFields, updatedEntity.customFields)
        assertEquals(entity.id, updatedEntity.id)
        assertEquals(entity.name, updatedEntity.name)
        assertEquals(entity.avatar, updatedEntity.avatar)
        assertEquals(entity.comment, updatedEntity.comment)
        assertEquals(entity.commentForOcr, updatedEntity.commentForOcr)
        assertEquals(entity.regular, updatedEntity.regular)
        assertEquals(entity.state, updatedEntity.state)
        assertTrue(updatedEntity.modifiedDate.isAfter(entity.modifiedDate) || updatedEntity.modifiedDate.isEqual(entity.modifiedDate))
    }

    @Test
    fun `given service provider when updateName called with blank name then should throw exception`() {
        // given - service provider and blank name
        val entity = ServiceProvider(
            id = 1L,
            name = "ABC Electric Company",
            avatar = null,
            comment = null,
            commentForOcr = null,
            regular = RegularFrequency.NOT_REGULAR,
            customFields = null,
            state = ServiceProviderState.ACTIVE,
            createdDate = LocalDateTime.now(),
            modifiedDate = LocalDateTime.now()
        )
        val blankName = ""
        
        // when/then - should throw exception
        try {
            entity.updateName(blankName)
            assert(false) { "Should have thrown exception" }
        } catch (e: IllegalArgumentException) {
            assertEquals("Service provider name cannot be blank", e.message)
        }
    }

    @Test
    fun `given service provider when hide called from HIDDEN state then should throw exception`() {
        // given - service provider in HIDDEN state
        val entity = ServiceProvider(
            id = 1L,
            name = "ABC Electric Company",
            avatar = null,
            comment = null,
            commentForOcr = null,
            regular = RegularFrequency.NOT_REGULAR,
            customFields = null,
            state = ServiceProviderState.HIDDEN,
            createdDate = LocalDateTime.now(),
            modifiedDate = LocalDateTime.now()
        )
        
        // when/then - should throw exception
        try {
            entity.hide()
            assert(false) { "Should have thrown exception" }
        } catch (e: IllegalStateException) {
            assertEquals("Cannot hide service provider from state HIDDEN", e.message)
        }
    }

    @Test
    fun `given service provider when show called from ACTIVE state then should throw exception`() {
        // given - service provider in ACTIVE state
        val entity = ServiceProvider(
            id = 1L,
            name = "ABC Electric Company",
            avatar = null,
            comment = null,
            commentForOcr = null,
            regular = RegularFrequency.NOT_REGULAR,
            customFields = null,
            state = ServiceProviderState.ACTIVE,
            createdDate = LocalDateTime.now(),
            modifiedDate = LocalDateTime.now()
        )
        
        // when/then - should throw exception
        try {
            entity.show()
            assert(false) { "Should have thrown exception" }
        } catch (e: IllegalStateException) {
            assertEquals("Cannot show service provider from state ACTIVE", e.message)
        }
    }

    @Test
    fun `given service provider when createNew called then should create new entity with defaults`() {
        // given - parameters for new service provider creation
        val name = "New Electric Company"
        val createdDate = LocalDateTime.now()
        
        // when - creating new service provider
        val entity = ServiceProvider.createNew(
            name = name,
            createdDate = createdDate
        )
        
        // then - should create new entity with defaults
        assertNotNull(entity)
        assertEquals(0L, entity.id) // new entity has 0 as placeholder ID
        assertEquals(name, entity.name)
        assertNull(entity.avatar)
        assertNull(entity.comment)
        assertNull(entity.commentForOcr)
        assertEquals(RegularFrequency.NOT_REGULAR, entity.regular)
        assertNull(entity.customFields)
        assertEquals(ServiceProviderState.ACTIVE, entity.state)
        assertEquals(createdDate, entity.createdDate)
        assertEquals(createdDate, entity.modifiedDate)
    }

    @Test
    fun `given service provider when equals and hashCode called then should work correctly`() {
        // given - two identical service provider entities
        val createdDate = LocalDateTime.now()
        val modifiedDate = LocalDateTime.now()
        
        val entity1 = ServiceProvider(
            id = 1L,
            name = "ABC Electric Company",
            avatar = null,
            comment = null,
            commentForOcr = null,
            regular = RegularFrequency.MONTHLY,
            customFields = null,
            state = ServiceProviderState.ACTIVE,
            createdDate = createdDate,
            modifiedDate = modifiedDate
        )
        val entity2 = ServiceProvider(
            id = 1L,
            name = "ABC Electric Company",
            avatar = null,
            comment = null,
            commentForOcr = null,
            regular = RegularFrequency.MONTHLY,
            customFields = null,
            state = ServiceProviderState.ACTIVE,
            createdDate = createdDate,
            modifiedDate = modifiedDate
        )
        val entity3 = ServiceProvider(
            id = 2L,
            name = "ABC Electric Company",
            avatar = null,
            comment = null,
            commentForOcr = null,
            regular = RegularFrequency.MONTHLY,
            customFields = null,
            state = ServiceProviderState.ACTIVE,
            createdDate = createdDate,
            modifiedDate = modifiedDate
        )
        
        // when/then - should work correctly
        assertEquals(entity1, entity2)
        assertEquals(entity1.hashCode(), entity2.hashCode())
        assertFalse(entity1 == entity3)
        assertFalse(entity1.hashCode() == entity3.hashCode())
    }

    @Test
    fun `given service provider enums when used then should have correct values`() {
        // given/when - testing ServiceProviderState enum
        val activeState = ServiceProviderState.ACTIVE
        val hiddenState = ServiceProviderState.HIDDEN
        
        // then - should have correct enum values
        assertEquals("ACTIVE", activeState.name)
        assertEquals("HIDDEN", hiddenState.name)
        
        // given/when - testing RegularFrequency enum
        val yearly = RegularFrequency.YEARLY
        val monthly = RegularFrequency.MONTHLY
        val weekly = RegularFrequency.WEEKLY
        val notRegular = RegularFrequency.NOT_REGULAR
        
        // then - should have correct enum values
        assertEquals("YEARLY", yearly.name)
        assertEquals("MONTHLY", monthly.name)
        assertEquals("WEEKLY", weekly.name)
        assertEquals("NOT_REGULAR", notRegular.name)
    }

    @Test
    fun `given service provider when created with all parameters then should set all fields correctly`() {
        // given - all parameters for service provider entity
        val id = 1L
        val name = "ABC Electric Company"
        val avatar = "avatars/abc-electric.png"
        val comment = "Monthly electricity bills"
        val commentForOcr = "Look for 'ABC Electric' logo"
        val regular = RegularFrequency.MONTHLY
        val customFields = """[{"name": "Account Number", "value": "12345", "comment": "Primary account"}]"""
        val state = ServiceProviderState.ACTIVE
        val createdDate = LocalDateTime.now()
        val modifiedDate = LocalDateTime.now()
        
        // when - creating entity with all parameters
        val entity = ServiceProvider(
            id = id,
            name = name,
            avatar = avatar,
            comment = comment,
            commentForOcr = commentForOcr,
            regular = regular,
            customFields = customFields,
            state = state,
            createdDate = createdDate,
            modifiedDate = modifiedDate
        )
        
        // then - should set all fields correctly
        assertNotNull(entity)
        assertEquals(id, entity.id)
        assertEquals(name, entity.name)
        assertEquals(avatar, entity.avatar)
        assertEquals(comment, entity.comment)
        assertEquals(commentForOcr, entity.commentForOcr)
        assertEquals(regular, entity.regular)
        assertEquals(customFields, entity.customFields)
        assertEquals(state, entity.state)
        assertEquals(createdDate, entity.createdDate)
        assertEquals(modifiedDate, entity.modifiedDate)
    }
}