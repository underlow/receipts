package me.underlow.receipt.service

import me.underlow.receipt.model.ReceiptEntity
import me.underlow.receipt.model.ReceiptState
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Tests for MockReceiptsService to ensure it provides proper mock data
 * for development and testing scenarios.
 */
class MockReceiptsServiceTest {

    private lateinit var service: MockReceiptsService

    @BeforeEach
    fun setUp() {
        service = MockReceiptsService()
    }

    @Test
    fun `findAll should return non-empty list of receipts`() {
        // Given - service is initialized
        
        // When - requesting all receipts
        val receipts = service.findAll()
        
        // Then - should return receipts with proper structure
        assertNotNull(receipts)
        assertTrue(receipts.isNotEmpty())
        assertTrue(receipts.all { it is ReceiptEntity })
    }

    @Test
    fun `findAll should return receipts with all required fields populated`() {
        // Given - service is initialized
        
        // When - requesting all receipts
        val receipts = service.findAll()
        
        // Then - all receipts should have proper data structure
        receipts.forEach { receipt ->
            assertNotNull(receipt.id)
            assertTrue(receipt.id.isNotBlank())
            assertNotNull(receipt.paymentTypeId)
            assertTrue(receipt.paymentTypeId.isNotBlank())
            assertNotNull(receipt.paymentDate)
            assertNotNull(receipt.amount)
            assertTrue(receipt.amount.compareTo(BigDecimal.ZERO) > 0)
            assertNotNull(receipt.createdDate)
            assertNotNull(receipt.state)
        }
    }

    @Test
    fun `findAll should include receipts in different states`() {
        // Given - service is initialized
        
        // When - requesting all receipts
        val receipts = service.findAll()
        
        // Then - should include receipts in both CREATED and REMOVED states
        val createdReceipts = receipts.filter { it.state == ReceiptState.CREATED }
        val removedReceipts = receipts.filter { it.state == ReceiptState.REMOVED }
        
        assertTrue(createdReceipts.isNotEmpty(), "Should have receipts in CREATED state")
        assertTrue(removedReceipts.isNotEmpty(), "Should have receipts in REMOVED state")
    }

    @Test
    fun `findAll should include receipts created from inbox and manually`() {
        // Given - service is initialized
        
        // When - requesting all receipts
        val receipts = service.findAll()
        
        // Then - should include receipts with and without inbox entity links
        val receiptsFromInbox = receipts.filter { it.inboxEntityId != null }
        val manualReceipts = receipts.filter { it.inboxEntityId == null }
        
        assertTrue(receiptsFromInbox.isNotEmpty(), "Should have receipts created from inbox")
        assertTrue(manualReceipts.isNotEmpty(), "Should have manually created receipts")
    }

    @Test
    fun `findAll should include receipts with different payment types`() {
        // Given - service is initialized
        
        // When - requesting all receipts
        val receipts = service.findAll()
        
        // Then - should have receipts with different payment types
        val paymentTypes = receipts.map { it.paymentTypeId }.distinct()
        assertTrue(paymentTypes.size > 1, "Should have multiple different payment types")
    }

    @Test
    fun `findAll should include receipts with realistic amounts`() {
        // Given - service is initialized
        
        // When - requesting all receipts
        val receipts = service.findAll()
        
        // Then - should have receipts with realistic monetary amounts
        receipts.forEach { receipt ->
            assertTrue(receipt.amount.compareTo(BigDecimal.ZERO) > 0, "Amount should be positive")
            assertTrue(receipt.amount.compareTo(BigDecimal("10000.00")) < 0, "Amount should be realistic")
        }
    }

    @Test
    fun `findAll should include receipts with different dates`() {
        // Given - service is initialized
        
        // When - requesting all receipts
        val receipts = service.findAll()
        
        // Then - should have receipts with different dates (recent and older)
        val dates = receipts.map { it.paymentDate }.distinct()
        assertTrue(dates.size > 1, "Should have receipts with different dates")
        
        // Should have at least some recent receipts
        val recentReceipts = receipts.filter { it.paymentDate.isAfter(LocalDate.now().minusDays(30)) }
        assertTrue(recentReceipts.isNotEmpty(), "Should have some recent receipts")
    }

    @Test
    fun `findAll with pagination should return correct subset`() {
        // Given - service is initialized
        val totalReceipts = service.findAll()
        
        // When - requesting first page with size 3
        val firstPage = service.findAll(page = 0, size = 3)
        
        // Then - should return exactly 3 receipts
        assertEquals(3, firstPage.size)
        
        // When - requesting second page with size 3
        val secondPage = service.findAll(page = 1, size = 3)
        
        // Then - should return different receipts
        assertTrue(firstPage.intersect(secondPage.toSet()).isEmpty(), "Pages should not overlap")
    }

    @Test
    fun `findAll with pagination should handle empty pages`() {
        // Given - service is initialized
        val totalCount = service.getTotalCount()
        
        // When - requesting page beyond available data
        val emptyPage = service.findAll(page = 100, size = 10)
        
        // Then - should return empty list
        assertTrue(emptyPage.isEmpty())
    }

    @Test
    fun `findAll should support sorting by paymentDate`() {
        // Given - service is initialized
        
        // When - requesting receipts sorted by paymentDate ascending
        val receiptsAsc = service.findAll(page = 0, size = 10, sortBy = "paymentDate", sortDirection = "ASC")
        
        // Then - should be sorted by payment date ascending
        for (i in 0 until receiptsAsc.size - 1) {
            assertTrue(receiptsAsc[i].paymentDate <= receiptsAsc[i + 1].paymentDate)
        }
        
        // When - requesting receipts sorted by paymentDate descending
        val receiptsDesc = service.findAll(page = 0, size = 10, sortBy = "paymentDate", sortDirection = "DESC")
        
        // Then - should be sorted by payment date descending
        for (i in 0 until receiptsDesc.size - 1) {
            assertTrue(receiptsDesc[i].paymentDate >= receiptsDesc[i + 1].paymentDate)
        }
    }

    @Test
    fun `findAll should support sorting by amount`() {
        // Given - service is initialized
        
        // When - requesting receipts sorted by amount ascending
        val receiptsAsc = service.findAll(page = 0, size = 10, sortBy = "amount", sortDirection = "ASC")
        
        // Then - should be sorted by amount ascending
        for (i in 0 until receiptsAsc.size - 1) {
            assertTrue(receiptsAsc[i].amount <= receiptsAsc[i + 1].amount)
        }
        
        // When - requesting receipts sorted by amount descending
        val receiptsDesc = service.findAll(page = 0, size = 10, sortBy = "amount", sortDirection = "DESC")
        
        // Then - should be sorted by amount descending
        for (i in 0 until receiptsDesc.size - 1) {
            assertTrue(receiptsDesc[i].amount >= receiptsDesc[i + 1].amount)
        }
    }

    @Test
    fun `findAll should support sorting by paymentTypeId`() {
        // Given - service is initialized
        
        // When - requesting receipts sorted by paymentTypeId ascending
        val receiptsAsc = service.findAll(page = 0, size = 10, sortBy = "paymentTypeId", sortDirection = "ASC")
        
        // Then - should be sorted by payment type ID ascending
        for (i in 0 until receiptsAsc.size - 1) {
            assertTrue(receiptsAsc[i].paymentTypeId <= receiptsAsc[i + 1].paymentTypeId)
        }
    }

    @Test
    fun `findAll should support sorting by createdDate`() {
        // Given - service is initialized
        
        // When - requesting receipts sorted by createdDate ascending
        val receiptsAsc = service.findAll(page = 0, size = 10, sortBy = "createdDate", sortDirection = "ASC")
        
        // Then - should be sorted by created date ascending
        for (i in 0 until receiptsAsc.size - 1) {
            assertTrue(receiptsAsc[i].createdDate <= receiptsAsc[i + 1].createdDate)
        }
    }

    @Test
    fun `findAll should handle unknown sort field gracefully`() {
        // Given - service is initialized
        
        // When - requesting receipts sorted by unknown field
        val receipts = service.findAll(page = 0, size = 10, sortBy = "unknownField", sortDirection = "ASC")
        
        // Then - should return receipts without sorting error
        assertTrue(receipts.isNotEmpty())
    }

    @Test
    fun `getTotalCount should return consistent count with findAll`() {
        // Given - service is initialized
        
        // When - getting total count and all receipts
        val totalCount = service.getTotalCount()
        val allReceipts = service.findAll()
        
        // Then - counts should match
        assertEquals(totalCount, allReceipts.size)
    }

    @Test
    fun `getTotalCount should return positive number`() {
        // Given - service is initialized
        
        // When - getting total count
        val totalCount = service.getTotalCount()
        
        // Then - should be positive
        assertTrue(totalCount > 0)
    }

    @Test
    fun `mock data should include receipts with descriptions`() {
        // Given - service is initialized
        
        // When - requesting all receipts
        val receipts = service.findAll()
        
        // Then - should include receipts with descriptions
        val receiptsWithDescription = receipts.filter { it.description != null }
        assertTrue(receiptsWithDescription.isNotEmpty(), "Should have receipts with descriptions")
    }

    @Test
    fun `mock data should include receipts without descriptions`() {
        // Given - service is initialized
        
        // When - requesting all receipts
        val receipts = service.findAll()
        
        // Then - should include receipts without descriptions
        val receiptsWithoutDescription = receipts.filter { it.description == null }
        assertTrue(receiptsWithoutDescription.isNotEmpty(), "Should have receipts without descriptions")
    }

    @Test
    fun `mock data should include receipts with merchant names`() {
        // Given - service is initialized
        
        // When - requesting all receipts
        val receipts = service.findAll()
        
        // Then - should include receipts with merchant names
        val receiptsWithMerchant = receipts.filter { it.merchantName != null }
        assertTrue(receiptsWithMerchant.isNotEmpty(), "Should have receipts with merchant names")
    }

    @Test
    fun `mock data should include receipts without merchant names`() {
        // Given - service is initialized
        
        // When - requesting all receipts
        val receipts = service.findAll()
        
        // Then - should include receipts without merchant names
        val receiptsWithoutMerchant = receipts.filter { it.merchantName == null }
        assertTrue(receiptsWithoutMerchant.isNotEmpty(), "Should have receipts without merchant names")
    }

    @Test
    fun `mock data should have diverse payment types`() {
        // Given - service is initialized
        
        // When - requesting all receipts
        val receipts = service.findAll()
        
        // Then - should have diverse payment types
        val paymentTypes = receipts.map { it.paymentTypeId }.distinct()
        
        // Should have at least cash, credit card, and debit card
        assertTrue(paymentTypes.size >= 3, "Should have at least 3 different payment types")
    }
}