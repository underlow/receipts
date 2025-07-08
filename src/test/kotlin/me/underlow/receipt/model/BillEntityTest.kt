package me.underlow.receipt.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Unit tests for BillEntity class.
 * Tests entity creation, state transitions, validation methods, and business logic.
 */
@ExtendWith(MockitoExtension::class)
class BillEntityTest {

    @Test
    fun `given bill entity parameters when created manually then should have correct initial state`() {
        // given - parameters for new bill entity
        val id = "bill-123"
        val serviceProviderId = "provider-456"
        val billDate = LocalDate.of(2024, 1, 15)
        val amount = BigDecimal("100.00")
        val description = "Electric bill"
        val createdDate = LocalDateTime.now()
        
        // when - creating new bill entity manually
        val entity = BillEntity(
            id = id,
            serviceProviderId = serviceProviderId,
            billDate = billDate,
            amount = amount,
            description = description,
            createdDate = createdDate
        )
        
        // then - should have correct initial state
        assertNotNull(entity)
        assertEquals(id, entity.id)
        assertEquals(serviceProviderId, entity.serviceProviderId)
        assertEquals(billDate, entity.billDate)
        assertEquals(amount, entity.amount)
        assertEquals(description, entity.description)
        assertEquals(createdDate, entity.createdDate)
        assertEquals(BillState.CREATED, entity.state)
        assertNull(entity.inboxEntityId)
    }

    @Test
    fun `given bill entity parameters when created from inbox then should have correct initial state with inbox link`() {
        // given - parameters for new bill entity created from inbox
        val id = "bill-123"
        val serviceProviderId = "provider-456"
        val billDate = LocalDate.of(2024, 1, 15)
        val amount = BigDecimal("100.00")
        val inboxEntityId = "inbox-789"
        val description = "Electric bill"
        val createdDate = LocalDateTime.now()
        
        // when - creating new bill entity from inbox
        val entity = BillEntity(
            id = id,
            serviceProviderId = serviceProviderId,
            billDate = billDate,
            amount = amount,
            inboxEntityId = inboxEntityId,
            description = description,
            createdDate = createdDate
        )
        
        // then - should have correct initial state with inbox link
        assertNotNull(entity)
        assertEquals(id, entity.id)
        assertEquals(serviceProviderId, entity.serviceProviderId)
        assertEquals(billDate, entity.billDate)
        assertEquals(amount, entity.amount)
        assertEquals(inboxEntityId, entity.inboxEntityId)
        assertEquals(description, entity.description)
        assertEquals(createdDate, entity.createdDate)
        assertEquals(BillState.CREATED, entity.state)
    }

    @Test
    fun `given bill entity in CREATED state when remove called then should transition to REMOVED state`() {
        // given - bill entity in CREATED state
        val entity = BillEntity(
            id = "bill-123",
            serviceProviderId = "provider-456",
            billDate = LocalDate.of(2024, 1, 15),
            amount = BigDecimal("100.00"),
            createdDate = LocalDateTime.now()
        )
        
        // when - removing bill entity
        val removedEntity = entity.remove()
        
        // then - should transition to REMOVED state
        assertNotNull(removedEntity)
        assertEquals(BillState.REMOVED, removedEntity.state)
        assertEquals(entity.id, removedEntity.id)
        assertEquals(entity.serviceProviderId, removedEntity.serviceProviderId)
        assertEquals(entity.billDate, removedEntity.billDate)
        assertEquals(entity.amount, removedEntity.amount)
    }

    @Test
    fun `given bill entity in CREATED state when canRemove called then should return true`() {
        // given - bill entity in CREATED state
        val entity = BillEntity(
            id = "bill-123",
            serviceProviderId = "provider-456",
            billDate = LocalDate.of(2024, 1, 15),
            amount = BigDecimal("100.00"),
            createdDate = LocalDateTime.now()
        )
        
        // when - checking if can remove
        val canRemove = entity.canRemove()
        
        // then - should return true
        assertTrue(canRemove)
    }

    @Test
    fun `given bill entity in REMOVED state when canRemove called then should return false`() {
        // given - bill entity in REMOVED state
        val entity = BillEntity(
            id = "bill-123",
            serviceProviderId = "provider-456",
            billDate = LocalDate.of(2024, 1, 15),
            amount = BigDecimal("100.00"),
            state = BillState.REMOVED,
            createdDate = LocalDateTime.now()
        )
        
        // when - checking if can remove
        val canRemove = entity.canRemove()
        
        // then - should return false
        assertFalse(canRemove)
    }

    @Test
    fun `given bill entity in CREATED state when isActive called then should return true`() {
        // given - bill entity in CREATED state
        val entity = BillEntity(
            id = "bill-123",
            serviceProviderId = "provider-456",
            billDate = LocalDate.of(2024, 1, 15),
            amount = BigDecimal("100.00"),
            createdDate = LocalDateTime.now()
        )
        
        // when - checking if is active
        val isActive = entity.isActive()
        
        // then - should return true
        assertTrue(isActive)
    }

    @Test
    fun `given bill entity in REMOVED state when isActive called then should return false`() {
        // given - bill entity in REMOVED state
        val entity = BillEntity(
            id = "bill-123",
            serviceProviderId = "provider-456",
            billDate = LocalDate.of(2024, 1, 15),
            amount = BigDecimal("100.00"),
            state = BillState.REMOVED,
            createdDate = LocalDateTime.now()
        )
        
        // when - checking if is active
        val isActive = entity.isActive()
        
        // then - should return false
        assertFalse(isActive)
    }

    @Test
    fun `given bill entity when updateAmount called then should update amount correctly`() {
        // given - bill entity with original amount
        val entity = BillEntity(
            id = "bill-123",
            serviceProviderId = "provider-456",
            billDate = LocalDate.of(2024, 1, 15),
            amount = BigDecimal("100.00"),
            createdDate = LocalDateTime.now()
        )
        val newAmount = BigDecimal("150.00")
        
        // when - updating amount
        val updatedEntity = entity.updateAmount(newAmount)
        
        // then - should update amount correctly
        assertNotNull(updatedEntity)
        assertEquals(newAmount, updatedEntity.amount)
        assertEquals(entity.id, updatedEntity.id)
        assertEquals(entity.serviceProviderId, updatedEntity.serviceProviderId)
        assertEquals(entity.billDate, updatedEntity.billDate)
        assertEquals(entity.state, updatedEntity.state)
    }

    @Test
    fun `given bill entity when updateServiceProvider called then should update service provider correctly`() {
        // given - bill entity with original service provider
        val entity = BillEntity(
            id = "bill-123",
            serviceProviderId = "provider-456",
            billDate = LocalDate.of(2024, 1, 15),
            amount = BigDecimal("100.00"),
            createdDate = LocalDateTime.now()
        )
        val newServiceProviderId = "provider-789"
        
        // when - updating service provider
        val updatedEntity = entity.updateServiceProvider(newServiceProviderId)
        
        // then - should update service provider correctly
        assertNotNull(updatedEntity)
        assertEquals(newServiceProviderId, updatedEntity.serviceProviderId)
        assertEquals(entity.id, updatedEntity.id)
        assertEquals(entity.billDate, updatedEntity.billDate)
        assertEquals(entity.amount, updatedEntity.amount)
        assertEquals(entity.state, updatedEntity.state)
    }

    @Test
    fun `given bill entity when remove called from REMOVED state then should throw exception`() {
        // given - bill entity in REMOVED state
        val entity = BillEntity(
            id = "bill-123",
            serviceProviderId = "provider-456",
            billDate = LocalDate.of(2024, 1, 15),
            amount = BigDecimal("100.00"),
            state = BillState.REMOVED,
            createdDate = LocalDateTime.now()
        )
        
        // when/then - should throw exception
        try {
            entity.remove()
            assert(false) { "Should have thrown exception" }
        } catch (e: IllegalStateException) {
            assertEquals("Cannot remove bill from state REMOVED", e.message)
        }
    }

    @Test
    fun `given bill entity when updateAmount called with negative amount then should throw exception`() {
        // given - bill entity and negative amount
        val entity = BillEntity(
            id = "bill-123",
            serviceProviderId = "provider-456",
            billDate = LocalDate.of(2024, 1, 15),
            amount = BigDecimal("100.00"),
            createdDate = LocalDateTime.now()
        )
        val negativeAmount = BigDecimal("-50.00")
        
        // when/then - should throw exception
        try {
            entity.updateAmount(negativeAmount)
            assert(false) { "Should have thrown exception" }
        } catch (e: IllegalArgumentException) {
            assertEquals("Bill amount cannot be negative", e.message)
        }
    }

    @Test
    fun `given bill entity when updateAmount called with zero amount then should throw exception`() {
        // given - bill entity and zero amount
        val entity = BillEntity(
            id = "bill-123",
            serviceProviderId = "provider-456",
            billDate = LocalDate.of(2024, 1, 15),
            amount = BigDecimal("100.00"),
            createdDate = LocalDateTime.now()
        )
        val zeroAmount = BigDecimal("0.00")
        
        // when/then - should throw exception
        try {
            entity.updateAmount(zeroAmount)
            assert(false) { "Should have thrown exception" }
        } catch (e: IllegalArgumentException) {
            assertEquals("Bill amount must be greater than zero", e.message)
        }
    }

    @Test
    fun `given bill entity when updateServiceProvider called with blank provider then should throw exception`() {
        // given - bill entity and blank service provider
        val entity = BillEntity(
            id = "bill-123",
            serviceProviderId = "provider-456",
            billDate = LocalDate.of(2024, 1, 15),
            amount = BigDecimal("100.00"),
            createdDate = LocalDateTime.now()
        )
        val blankProviderId = ""
        
        // when/then - should throw exception
        try {
            entity.updateServiceProvider(blankProviderId)
            assert(false) { "Should have thrown exception" }
        } catch (e: IllegalArgumentException) {
            assertEquals("Service provider ID cannot be blank", e.message)
        }
    }

    @Test
    fun `given bill entity when createFromInbox called then should create bill with inbox link`() {
        // given - parameters for bill creation from inbox
        val id = "bill-123"
        val serviceProviderId = "provider-456"
        val billDate = LocalDate.of(2024, 1, 15)
        val amount = BigDecimal("100.00")
        val inboxEntityId = "inbox-789"
        val description = "Electric bill"
        val createdDate = LocalDateTime.now()
        
        // when - creating bill from inbox
        val entity = BillEntity.createFromInbox(
            id = id,
            serviceProviderId = serviceProviderId,
            billDate = billDate,
            amount = amount,
            inboxEntityId = inboxEntityId,
            description = description,
            createdDate = createdDate
        )
        
        // then - should create bill with inbox link
        assertNotNull(entity)
        assertEquals(id, entity.id)
        assertEquals(serviceProviderId, entity.serviceProviderId)
        assertEquals(billDate, entity.billDate)
        assertEquals(amount, entity.amount)
        assertEquals(inboxEntityId, entity.inboxEntityId)
        assertEquals(description, entity.description)
        assertEquals(createdDate, entity.createdDate)
        assertEquals(BillState.CREATED, entity.state)
    }

    @Test
    fun `given bill entity when createManually called then should create bill without inbox link`() {
        // given - parameters for manual bill creation
        val id = "bill-123"
        val serviceProviderId = "provider-456"
        val billDate = LocalDate.of(2024, 1, 15)
        val amount = BigDecimal("100.00")
        val description = "Electric bill"
        val createdDate = LocalDateTime.now()
        
        // when - creating bill manually
        val entity = BillEntity.createManually(
            id = id,
            serviceProviderId = serviceProviderId,
            billDate = billDate,
            amount = amount,
            description = description,
            createdDate = createdDate
        )
        
        // then - should create bill without inbox link
        assertNotNull(entity)
        assertEquals(id, entity.id)
        assertEquals(serviceProviderId, entity.serviceProviderId)
        assertEquals(billDate, entity.billDate)
        assertEquals(amount, entity.amount)
        assertNull(entity.inboxEntityId)
        assertEquals(description, entity.description)
        assertEquals(createdDate, entity.createdDate)
        assertEquals(BillState.CREATED, entity.state)
    }

    @Test
    fun `given bill entity when equals and hashCode called then should work correctly`() {
        // given - two identical bill entities
        val billDate = LocalDate.of(2024, 1, 15)
        val amount = BigDecimal("100.00")
        val createdDate = LocalDateTime.now()
        
        val entity1 = BillEntity(
            id = "bill-123",
            serviceProviderId = "provider-456",
            billDate = billDate,
            amount = amount,
            createdDate = createdDate
        )
        val entity2 = BillEntity(
            id = "bill-123",
            serviceProviderId = "provider-456",
            billDate = billDate,
            amount = amount,
            createdDate = createdDate
        )
        val entity3 = BillEntity(
            id = "bill-456",
            serviceProviderId = "provider-456",
            billDate = billDate,
            amount = amount,
            createdDate = createdDate
        )
        
        // when/then - should work correctly
        assertEquals(entity1, entity2)
        assertEquals(entity1.hashCode(), entity2.hashCode())
        assertFalse(entity1 == entity3)
        assertFalse(entity1.hashCode() == entity3.hashCode())
    }

    @Test
    fun `given bill entity when created with all parameters then should set all fields correctly`() {
        // given - all parameters for bill entity
        val id = "bill-123"
        val serviceProviderId = "provider-456"
        val billDate = LocalDate.of(2024, 1, 15)
        val amount = BigDecimal("100.00")
        val inboxEntityId = "inbox-789"
        val state = BillState.REMOVED
        val createdDate = LocalDateTime.now()
        val description = "Electric bill"
        
        // when - creating entity with all parameters
        val entity = BillEntity(
            id = id,
            serviceProviderId = serviceProviderId,
            billDate = billDate,
            amount = amount,
            inboxEntityId = inboxEntityId,
            state = state,
            createdDate = createdDate,
            description = description
        )
        
        // then - should set all fields correctly
        assertNotNull(entity)
        assertEquals(id, entity.id)
        assertEquals(serviceProviderId, entity.serviceProviderId)
        assertEquals(billDate, entity.billDate)
        assertEquals(amount, entity.amount)
        assertEquals(inboxEntityId, entity.inboxEntityId)
        assertEquals(state, entity.state)
        assertEquals(createdDate, entity.createdDate)
        assertEquals(description, entity.description)
    }
}