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
 * Unit tests for ReceiptEntity class.
 * Tests entity creation, state transitions, validation methods, and business logic.
 */
@ExtendWith(MockitoExtension::class)
class ReceiptEntityTest {

    @Test
    fun `given receipt entity parameters when created manually then should have correct initial state`() {
        // given - parameters for new receipt entity
        val id = "receipt-123"
        val paymentTypeId = "payment-456"
        val paymentDate = LocalDate.of(2024, 1, 15)
        val amount = BigDecimal("100.00")
        val description = "Restaurant bill"
        val merchantName = "Pizza Palace"
        val createdDate = LocalDateTime.now()
        
        // when - creating new receipt entity manually
        val entity = ReceiptEntity(
            id = id,
            paymentTypeId = paymentTypeId,
            paymentDate = paymentDate,
            amount = amount,
            description = description,
            merchantName = merchantName,
            createdDate = createdDate
        )
        
        // then - should have correct initial state
        assertNotNull(entity)
        assertEquals(id, entity.id)
        assertEquals(paymentTypeId, entity.paymentTypeId)
        assertEquals(paymentDate, entity.paymentDate)
        assertEquals(amount, entity.amount)
        assertEquals(description, entity.description)
        assertEquals(merchantName, entity.merchantName)
        assertEquals(createdDate, entity.createdDate)
        assertEquals(ReceiptState.CREATED, entity.state)
        assertNull(entity.inboxEntityId)
    }

    @Test
    fun `given receipt entity parameters when created from inbox then should have correct initial state with inbox link`() {
        // given - parameters for new receipt entity created from inbox
        val id = "receipt-123"
        val paymentTypeId = "payment-456"
        val paymentDate = LocalDate.of(2024, 1, 15)
        val amount = BigDecimal("100.00")
        val inboxEntityId = "inbox-789"
        val description = "Restaurant bill"
        val merchantName = "Pizza Palace"
        val createdDate = LocalDateTime.now()
        
        // when - creating new receipt entity from inbox
        val entity = ReceiptEntity(
            id = id,
            paymentTypeId = paymentTypeId,
            paymentDate = paymentDate,
            amount = amount,
            inboxEntityId = inboxEntityId,
            description = description,
            merchantName = merchantName,
            createdDate = createdDate
        )
        
        // then - should have correct initial state with inbox link
        assertNotNull(entity)
        assertEquals(id, entity.id)
        assertEquals(paymentTypeId, entity.paymentTypeId)
        assertEquals(paymentDate, entity.paymentDate)
        assertEquals(amount, entity.amount)
        assertEquals(inboxEntityId, entity.inboxEntityId)
        assertEquals(description, entity.description)
        assertEquals(merchantName, entity.merchantName)
        assertEquals(createdDate, entity.createdDate)
        assertEquals(ReceiptState.CREATED, entity.state)
    }

    @Test
    fun `given receipt entity in CREATED state when remove called then should transition to REMOVED state`() {
        // given - receipt entity in CREATED state
        val entity = ReceiptEntity(
            id = "receipt-123",
            paymentTypeId = "payment-456",
            paymentDate = LocalDate.of(2024, 1, 15),
            amount = BigDecimal("100.00"),
            createdDate = LocalDateTime.now()
        )
        
        // when - removing receipt entity
        val removedEntity = entity.remove()
        
        // then - should transition to REMOVED state
        assertNotNull(removedEntity)
        assertEquals(ReceiptState.REMOVED, removedEntity.state)
        assertEquals(entity.id, removedEntity.id)
        assertEquals(entity.paymentTypeId, removedEntity.paymentTypeId)
        assertEquals(entity.paymentDate, removedEntity.paymentDate)
        assertEquals(entity.amount, removedEntity.amount)
    }

    @Test
    fun `given receipt entity in CREATED state when isActive called then should return true`() {
        // given - receipt entity in CREATED state
        val entity = ReceiptEntity(
            id = "receipt-123",
            paymentTypeId = "payment-456",
            paymentDate = LocalDate.of(2024, 1, 15),
            amount = BigDecimal("100.00"),
            createdDate = LocalDateTime.now()
        )
        
        // when - checking if is active
        val isActive = entity.isActive()
        
        // then - should return true
        assertTrue(isActive)
    }

    @Test
    fun `given receipt entity in REMOVED state when isActive called then should return false`() {
        // given - receipt entity in REMOVED state
        val entity = ReceiptEntity(
            id = "receipt-123",
            paymentTypeId = "payment-456",
            paymentDate = LocalDate.of(2024, 1, 15),
            amount = BigDecimal("100.00"),
            state = ReceiptState.REMOVED,
            createdDate = LocalDateTime.now()
        )
        
        // when - checking if is active
        val isActive = entity.isActive()
        
        // then - should return false
        assertFalse(isActive)
    }

    @Test
    fun `given receipt entity when updateAmount called then should update amount correctly`() {
        // given - receipt entity with original amount
        val entity = ReceiptEntity(
            id = "receipt-123",
            paymentTypeId = "payment-456",
            paymentDate = LocalDate.of(2024, 1, 15),
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
        assertEquals(entity.paymentTypeId, updatedEntity.paymentTypeId)
        assertEquals(entity.paymentDate, updatedEntity.paymentDate)
        assertEquals(entity.state, updatedEntity.state)
    }

    @Test
    fun `given receipt entity when updatePaymentType called then should update payment type correctly`() {
        // given - receipt entity with original payment type
        val entity = ReceiptEntity(
            id = "receipt-123",
            paymentTypeId = "payment-456",
            paymentDate = LocalDate.of(2024, 1, 15),
            amount = BigDecimal("100.00"),
            createdDate = LocalDateTime.now()
        )
        val newPaymentTypeId = "payment-789"
        
        // when - updating payment type
        val updatedEntity = entity.updatePaymentType(newPaymentTypeId)
        
        // then - should update payment type correctly
        assertNotNull(updatedEntity)
        assertEquals(newPaymentTypeId, updatedEntity.paymentTypeId)
        assertEquals(entity.id, updatedEntity.id)
        assertEquals(entity.paymentDate, updatedEntity.paymentDate)
        assertEquals(entity.amount, updatedEntity.amount)
        assertEquals(entity.state, updatedEntity.state)
    }

    @Test
    fun `given receipt entity when updateMerchant called then should update merchant correctly`() {
        // given - receipt entity with original merchant
        val entity = ReceiptEntity(
            id = "receipt-123",
            paymentTypeId = "payment-456",
            paymentDate = LocalDate.of(2024, 1, 15),
            amount = BigDecimal("100.00"),
            merchantName = "Pizza Palace",
            createdDate = LocalDateTime.now()
        )
        val newMerchantName = "Burger King"
        
        // when - updating merchant
        val updatedEntity = entity.updateMerchant(newMerchantName)
        
        // then - should update merchant correctly
        assertNotNull(updatedEntity)
        assertEquals(newMerchantName, updatedEntity.merchantName)
        assertEquals(entity.id, updatedEntity.id)
        assertEquals(entity.paymentTypeId, updatedEntity.paymentTypeId)
        assertEquals(entity.paymentDate, updatedEntity.paymentDate)
        assertEquals(entity.amount, updatedEntity.amount)
        assertEquals(entity.state, updatedEntity.state)
    }

    @Test
    fun `given receipt entity when remove called from REMOVED state then should throw exception`() {
        // given - receipt entity in REMOVED state
        val entity = ReceiptEntity(
            id = "receipt-123",
            paymentTypeId = "payment-456",
            paymentDate = LocalDate.of(2024, 1, 15),
            amount = BigDecimal("100.00"),
            state = ReceiptState.REMOVED,
            createdDate = LocalDateTime.now()
        )
        
        // when/then - should throw exception
        try {
            entity.remove()
            assert(false) { "Should have thrown exception" }
        } catch (e: IllegalStateException) {
            assertEquals("Cannot remove receipt from state REMOVED", e.message)
        }
    }

    @Test
    fun `given receipt entity when updateAmount called with negative amount then should throw exception`() {
        // given - receipt entity and negative amount
        val entity = ReceiptEntity(
            id = "receipt-123",
            paymentTypeId = "payment-456",
            paymentDate = LocalDate.of(2024, 1, 15),
            amount = BigDecimal("100.00"),
            createdDate = LocalDateTime.now()
        )
        val negativeAmount = BigDecimal("-50.00")
        
        // when/then - should throw exception
        try {
            entity.updateAmount(negativeAmount)
            assert(false) { "Should have thrown exception" }
        } catch (e: IllegalArgumentException) {
            assertEquals("Receipt amount cannot be negative", e.message)
        }
    }

    @Test
    fun `given receipt entity when updateAmount called with zero amount then should throw exception`() {
        // given - receipt entity and zero amount
        val entity = ReceiptEntity(
            id = "receipt-123",
            paymentTypeId = "payment-456",
            paymentDate = LocalDate.of(2024, 1, 15),
            amount = BigDecimal("100.00"),
            createdDate = LocalDateTime.now()
        )
        val zeroAmount = BigDecimal("0.00")
        
        // when/then - should throw exception
        try {
            entity.updateAmount(zeroAmount)
            assert(false) { "Should have thrown exception" }
        } catch (e: IllegalArgumentException) {
            assertEquals("Receipt amount must be greater than zero", e.message)
        }
    }

    @Test
    fun `given receipt entity when updatePaymentType called with blank payment type then should throw exception`() {
        // given - receipt entity and blank payment type
        val entity = ReceiptEntity(
            id = "receipt-123",
            paymentTypeId = "payment-456",
            paymentDate = LocalDate.of(2024, 1, 15),
            amount = BigDecimal("100.00"),
            createdDate = LocalDateTime.now()
        )
        val blankPaymentTypeId = ""
        
        // when/then - should throw exception
        try {
            entity.updatePaymentType(blankPaymentTypeId)
            assert(false) { "Should have thrown exception" }
        } catch (e: IllegalArgumentException) {
            assertEquals("Payment type ID cannot be blank", e.message)
        }
    }

    @Test
    fun `given receipt entity when createFromInbox called then should create receipt with inbox link`() {
        // given - parameters for receipt creation from inbox
        val id = "receipt-123"
        val paymentTypeId = "payment-456"
        val paymentDate = LocalDate.of(2024, 1, 15)
        val amount = BigDecimal("100.00")
        val inboxEntityId = "inbox-789"
        val description = "Restaurant bill"
        val merchantName = "Pizza Palace"
        val createdDate = LocalDateTime.now()
        
        // when - creating receipt from inbox
        val entity = ReceiptEntity.createFromInbox(
            id = id,
            paymentTypeId = paymentTypeId,
            paymentDate = paymentDate,
            amount = amount,
            inboxEntityId = inboxEntityId,
            description = description,
            merchantName = merchantName,
            createdDate = createdDate
        )
        
        // then - should create receipt with inbox link
        assertNotNull(entity)
        assertEquals(id, entity.id)
        assertEquals(paymentTypeId, entity.paymentTypeId)
        assertEquals(paymentDate, entity.paymentDate)
        assertEquals(amount, entity.amount)
        assertEquals(inboxEntityId, entity.inboxEntityId)
        assertEquals(description, entity.description)
        assertEquals(merchantName, entity.merchantName)
        assertEquals(createdDate, entity.createdDate)
        assertEquals(ReceiptState.CREATED, entity.state)
    }

    @Test
    fun `given receipt entity when createManually called then should create receipt without inbox link`() {
        // given - parameters for manual receipt creation
        val id = "receipt-123"
        val paymentTypeId = "payment-456"
        val paymentDate = LocalDate.of(2024, 1, 15)
        val amount = BigDecimal("100.00")
        val description = "Restaurant bill"
        val merchantName = "Pizza Palace"
        val createdDate = LocalDateTime.now()
        
        // when - creating receipt manually
        val entity = ReceiptEntity.createManually(
            id = id,
            paymentTypeId = paymentTypeId,
            paymentDate = paymentDate,
            amount = amount,
            description = description,
            merchantName = merchantName,
            createdDate = createdDate
        )
        
        // then - should create receipt without inbox link
        assertNotNull(entity)
        assertEquals(id, entity.id)
        assertEquals(paymentTypeId, entity.paymentTypeId)
        assertEquals(paymentDate, entity.paymentDate)
        assertEquals(amount, entity.amount)
        assertNull(entity.inboxEntityId)
        assertEquals(description, entity.description)
        assertEquals(merchantName, entity.merchantName)
        assertEquals(createdDate, entity.createdDate)
        assertEquals(ReceiptState.CREATED, entity.state)
    }

    @Test
    fun `given receipt entity when equals and hashCode called then should work correctly`() {
        // given - two identical receipt entities
        val paymentDate = LocalDate.of(2024, 1, 15)
        val amount = BigDecimal("100.00")
        val createdDate = LocalDateTime.now()
        
        val entity1 = ReceiptEntity(
            id = "receipt-123",
            paymentTypeId = "payment-456",
            paymentDate = paymentDate,
            amount = amount,
            createdDate = createdDate
        )
        val entity2 = ReceiptEntity(
            id = "receipt-123",
            paymentTypeId = "payment-456",
            paymentDate = paymentDate,
            amount = amount,
            createdDate = createdDate
        )
        val entity3 = ReceiptEntity(
            id = "receipt-456",
            paymentTypeId = "payment-456",
            paymentDate = paymentDate,
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
    fun `given receipt entity when created with all parameters then should set all fields correctly`() {
        // given - all parameters for receipt entity
        val id = "receipt-123"
        val paymentTypeId = "payment-456"
        val paymentDate = LocalDate.of(2024, 1, 15)
        val amount = BigDecimal("100.00")
        val inboxEntityId = "inbox-789"
        val state = ReceiptState.REMOVED
        val createdDate = LocalDateTime.now()
        val description = "Restaurant bill"
        val merchantName = "Pizza Palace"
        
        // when - creating entity with all parameters
        val entity = ReceiptEntity(
            id = id,
            paymentTypeId = paymentTypeId,
            paymentDate = paymentDate,
            amount = amount,
            inboxEntityId = inboxEntityId,
            state = state,
            createdDate = createdDate,
            description = description,
            merchantName = merchantName
        )
        
        // then - should set all fields correctly
        assertNotNull(entity)
        assertEquals(id, entity.id)
        assertEquals(paymentTypeId, entity.paymentTypeId)
        assertEquals(paymentDate, entity.paymentDate)
        assertEquals(amount, entity.amount)
        assertEquals(inboxEntityId, entity.inboxEntityId)
        assertEquals(state, entity.state)
        assertEquals(createdDate, entity.createdDate)
        assertEquals(description, entity.description)
        assertEquals(merchantName, entity.merchantName)
    }

    @Test
    fun `given receipt entity when updateMerchant called with null merchant then should clear merchant`() {
        // given - receipt entity with original merchant
        val entity = ReceiptEntity(
            id = "receipt-123",
            paymentTypeId = "payment-456",
            paymentDate = LocalDate.of(2024, 1, 15),
            amount = BigDecimal("100.00"),
            merchantName = "Pizza Palace",
            createdDate = LocalDateTime.now()
        )
        
        // when - updating merchant to null
        val updatedEntity = entity.updateMerchant(null)
        
        // then - should clear merchant
        assertNotNull(updatedEntity)
        assertNull(updatedEntity.merchantName)
        assertEquals(entity.id, updatedEntity.id)
        assertEquals(entity.paymentTypeId, updatedEntity.paymentTypeId)
        assertEquals(entity.paymentDate, updatedEntity.paymentDate)
        assertEquals(entity.amount, updatedEntity.amount)
        assertEquals(entity.state, updatedEntity.state)
    }

    @Test
    fun `given receipt entity when updateMerchant called with blank merchant then should clear merchant`() {
        // given - receipt entity with original merchant
        val entity = ReceiptEntity(
            id = "receipt-123",
            paymentTypeId = "payment-456",
            paymentDate = LocalDate.of(2024, 1, 15),
            amount = BigDecimal("100.00"),
            merchantName = "Pizza Palace",
            createdDate = LocalDateTime.now()
        )
        
        // when - updating merchant to blank
        val updatedEntity = entity.updateMerchant("")
        
        // then - should clear merchant
        assertNotNull(updatedEntity)
        assertNull(updatedEntity.merchantName)
        assertEquals(entity.id, updatedEntity.id)
        assertEquals(entity.paymentTypeId, updatedEntity.paymentTypeId)
        assertEquals(entity.paymentDate, updatedEntity.paymentDate)
        assertEquals(entity.amount, updatedEntity.amount)
        assertEquals(entity.state, updatedEntity.state)
    }
}