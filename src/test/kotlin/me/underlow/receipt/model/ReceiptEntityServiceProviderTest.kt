package me.underlow.receipt.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Unit tests for ReceiptEntity serviceProviderId functionality.
 * Tests receipt entity creation, service provider linking, and business logic.
 */
@ExtendWith(MockitoExtension::class)
class ReceiptEntityServiceProviderTest {

    @Test
    fun `given receipt entity parameters with service provider when created then should have correct service provider link`() {
        // given - parameters for new receipt entity with service provider
        val id = "receipt-123"
        val paymentTypeId = "payment-456"
        val paymentDate = LocalDate.of(2024, 1, 15)
        val amount = BigDecimal("100.00")
        val serviceProviderId = 1L
        val description = "Restaurant bill"
        val merchantName = "Pizza Palace"
        val createdDate = LocalDateTime.now()
        
        // when - creating new receipt entity with service provider
        val entity = ReceiptEntity(
            id = id,
            paymentTypeId = paymentTypeId,
            paymentDate = paymentDate,
            amount = amount,
            serviceProviderId = serviceProviderId,
            description = description,
            merchantName = merchantName,
            createdDate = createdDate
        )
        
        // then - should have correct service provider link
        assertNotNull(entity)
        assertEquals(id, entity.id)
        assertEquals(paymentTypeId, entity.paymentTypeId)
        assertEquals(paymentDate, entity.paymentDate)
        assertEquals(amount, entity.amount)
        assertEquals(serviceProviderId, entity.serviceProviderId)
        assertEquals(description, entity.description)
        assertEquals(merchantName, entity.merchantName)
        assertEquals(createdDate, entity.createdDate)
        assertEquals(ReceiptState.CREATED, entity.state)
        assertNull(entity.inboxEntityId)
    }

    @Test
    fun `given receipt entity parameters without service provider when created then should have null service provider`() {
        // given - parameters for new receipt entity without service provider
        val id = "receipt-123"
        val paymentTypeId = "payment-456"
        val paymentDate = LocalDate.of(2024, 1, 15)
        val amount = BigDecimal("100.00")
        val description = "Restaurant bill"
        val merchantName = "Pizza Palace"
        val createdDate = LocalDateTime.now()
        
        // when - creating new receipt entity without service provider
        val entity = ReceiptEntity(
            id = id,
            paymentTypeId = paymentTypeId,
            paymentDate = paymentDate,
            amount = amount,
            description = description,
            merchantName = merchantName,
            createdDate = createdDate
        )
        
        // then - should have null service provider
        assertNotNull(entity)
        assertEquals(id, entity.id)
        assertEquals(paymentTypeId, entity.paymentTypeId)
        assertEquals(paymentDate, entity.paymentDate)
        assertEquals(amount, entity.amount)
        assertNull(entity.serviceProviderId)
        assertEquals(description, entity.description)
        assertEquals(merchantName, entity.merchantName)
        assertEquals(createdDate, entity.createdDate)
        assertEquals(ReceiptState.CREATED, entity.state)
        assertNull(entity.inboxEntityId)
    }

    @Test
    fun `given receipt entity when updateServiceProvider called then should update service provider correctly`() {
        // given - receipt entity with original service provider
        val entity = ReceiptEntity(
            id = "receipt-123",
            paymentTypeId = "payment-456",
            paymentDate = LocalDate.of(2024, 1, 15),
            amount = BigDecimal("100.00"),
            serviceProviderId = 1L,
            createdDate = LocalDateTime.now()
        )
        val newServiceProviderId = 2L
        
        // when - updating service provider
        val updatedEntity = entity.updateServiceProvider(newServiceProviderId)
        
        // then - should update service provider correctly
        assertNotNull(updatedEntity)
        assertEquals(newServiceProviderId, updatedEntity.serviceProviderId)
        assertEquals(entity.id, updatedEntity.id)
        assertEquals(entity.paymentTypeId, updatedEntity.paymentTypeId)
        assertEquals(entity.paymentDate, updatedEntity.paymentDate)
        assertEquals(entity.amount, updatedEntity.amount)
        assertEquals(entity.state, updatedEntity.state)
    }

    @Test
    fun `given receipt entity when updateServiceProvider called with null then should clear service provider`() {
        // given - receipt entity with original service provider
        val entity = ReceiptEntity(
            id = "receipt-123",
            paymentTypeId = "payment-456",
            paymentDate = LocalDate.of(2024, 1, 15),
            amount = BigDecimal("100.00"),
            serviceProviderId = 1L,
            createdDate = LocalDateTime.now()
        )
        
        // when - updating service provider to null
        val updatedEntity = entity.updateServiceProvider(null)
        
        // then - should clear service provider
        assertNotNull(updatedEntity)
        assertNull(updatedEntity.serviceProviderId)
        assertEquals(entity.id, updatedEntity.id)
        assertEquals(entity.paymentTypeId, updatedEntity.paymentTypeId)
        assertEquals(entity.paymentDate, updatedEntity.paymentDate)
        assertEquals(entity.amount, updatedEntity.amount)
        assertEquals(entity.state, updatedEntity.state)
    }

    @Test
    fun `given receipt entity when createFromInbox called with service provider then should create receipt with service provider link`() {
        // given - parameters for receipt creation from inbox with service provider
        val id = "receipt-123"
        val paymentTypeId = "payment-456"
        val paymentDate = LocalDate.of(2024, 1, 15)
        val amount = BigDecimal("100.00")
        val inboxEntityId = "inbox-789"
        val serviceProviderId = 1L
        val description = "Restaurant bill"
        val merchantName = "Pizza Palace"
        val createdDate = LocalDateTime.now()
        
        // when - creating receipt from inbox with service provider
        val entity = ReceiptEntity.createFromInbox(
            id = id,
            paymentTypeId = paymentTypeId,
            paymentDate = paymentDate,
            amount = amount,
            inboxEntityId = inboxEntityId,
            serviceProviderId = serviceProviderId,
            description = description,
            merchantName = merchantName,
            createdDate = createdDate
        )
        
        // then - should create receipt with service provider link
        assertNotNull(entity)
        assertEquals(id, entity.id)
        assertEquals(paymentTypeId, entity.paymentTypeId)
        assertEquals(paymentDate, entity.paymentDate)
        assertEquals(amount, entity.amount)
        assertEquals(inboxEntityId, entity.inboxEntityId)
        assertEquals(serviceProviderId, entity.serviceProviderId)
        assertEquals(description, entity.description)
        assertEquals(merchantName, entity.merchantName)
        assertEquals(createdDate, entity.createdDate)
        assertEquals(ReceiptState.CREATED, entity.state)
    }

    @Test
    fun `given receipt entity when createFromInbox called without service provider then should create receipt without service provider link`() {
        // given - parameters for receipt creation from inbox without service provider
        val id = "receipt-123"
        val paymentTypeId = "payment-456"
        val paymentDate = LocalDate.of(2024, 1, 15)
        val amount = BigDecimal("100.00")
        val inboxEntityId = "inbox-789"
        val description = "Restaurant bill"
        val merchantName = "Pizza Palace"
        val createdDate = LocalDateTime.now()
        
        // when - creating receipt from inbox without service provider
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
        
        // then - should create receipt without service provider link
        assertNotNull(entity)
        assertEquals(id, entity.id)
        assertEquals(paymentTypeId, entity.paymentTypeId)
        assertEquals(paymentDate, entity.paymentDate)
        assertEquals(amount, entity.amount)
        assertEquals(inboxEntityId, entity.inboxEntityId)
        assertNull(entity.serviceProviderId)
        assertEquals(description, entity.description)
        assertEquals(merchantName, entity.merchantName)
        assertEquals(createdDate, entity.createdDate)
        assertEquals(ReceiptState.CREATED, entity.state)
    }

    @Test
    fun `given receipt entity when createManually called with service provider then should create receipt with service provider link`() {
        // given - parameters for manual receipt creation with service provider
        val id = "receipt-123"
        val paymentTypeId = "payment-456"
        val paymentDate = LocalDate.of(2024, 1, 15)
        val amount = BigDecimal("100.00")
        val serviceProviderId = 1L
        val description = "Restaurant bill"
        val merchantName = "Pizza Palace"
        val createdDate = LocalDateTime.now()
        
        // when - creating receipt manually with service provider
        val entity = ReceiptEntity.createManually(
            id = id,
            paymentTypeId = paymentTypeId,
            paymentDate = paymentDate,
            amount = amount,
            serviceProviderId = serviceProviderId,
            description = description,
            merchantName = merchantName,
            createdDate = createdDate
        )
        
        // then - should create receipt with service provider link
        assertNotNull(entity)
        assertEquals(id, entity.id)
        assertEquals(paymentTypeId, entity.paymentTypeId)
        assertEquals(paymentDate, entity.paymentDate)
        assertEquals(amount, entity.amount)
        assertNull(entity.inboxEntityId)
        assertEquals(serviceProviderId, entity.serviceProviderId)
        assertEquals(description, entity.description)
        assertEquals(merchantName, entity.merchantName)
        assertEquals(createdDate, entity.createdDate)
        assertEquals(ReceiptState.CREATED, entity.state)
    }

    @Test
    fun `given receipt entity when createManually called without service provider then should create receipt without service provider link`() {
        // given - parameters for manual receipt creation without service provider
        val id = "receipt-123"
        val paymentTypeId = "payment-456"
        val paymentDate = LocalDate.of(2024, 1, 15)
        val amount = BigDecimal("100.00")
        val description = "Restaurant bill"
        val merchantName = "Pizza Palace"
        val createdDate = LocalDateTime.now()
        
        // when - creating receipt manually without service provider
        val entity = ReceiptEntity.createManually(
            id = id,
            paymentTypeId = paymentTypeId,
            paymentDate = paymentDate,
            amount = amount,
            description = description,
            merchantName = merchantName,
            createdDate = createdDate
        )
        
        // then - should create receipt without service provider link
        assertNotNull(entity)
        assertEquals(id, entity.id)
        assertEquals(paymentTypeId, entity.paymentTypeId)
        assertEquals(paymentDate, entity.paymentDate)
        assertEquals(amount, entity.amount)
        assertNull(entity.inboxEntityId)
        assertNull(entity.serviceProviderId)
        assertEquals(description, entity.description)
        assertEquals(merchantName, entity.merchantName)
        assertEquals(createdDate, entity.createdDate)
        assertEquals(ReceiptState.CREATED, entity.state)
    }

    @Test
    fun `given receipt entities with different service providers when equals called then should work correctly`() {
        // given - two receipt entities with different service providers
        val paymentDate = LocalDate.of(2024, 1, 15)
        val amount = BigDecimal("100.00")
        val createdDate = LocalDateTime.now()
        
        val entity1 = ReceiptEntity(
            id = "receipt-123",
            paymentTypeId = "payment-456",
            paymentDate = paymentDate,
            amount = amount,
            serviceProviderId = 1L,
            createdDate = createdDate
        )
        val entity2 = ReceiptEntity(
            id = "receipt-123",
            paymentTypeId = "payment-456",
            paymentDate = paymentDate,
            amount = amount,
            serviceProviderId = 1L,
            createdDate = createdDate
        )
        val entity3 = ReceiptEntity(
            id = "receipt-123",
            paymentTypeId = "payment-456",
            paymentDate = paymentDate,
            amount = amount,
            serviceProviderId = 2L,
            createdDate = createdDate
        )
        
        // when/then - should work correctly
        assertEquals(entity1, entity2)
        assertEquals(entity1.hashCode(), entity2.hashCode())
        assertNotNull(entity1 != entity3)
        assertNotNull(entity1.hashCode() != entity3.hashCode())
    }
}