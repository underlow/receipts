package me.underlow.receipt.service

import me.underlow.receipt.model.BillEntity
import me.underlow.receipt.model.BillState
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Tests for MockBillsService to ensure it provides proper mock data
 * for development and testing scenarios.
 */
class MockBillsServiceTest {

    private lateinit var service: MockBillsService

    @BeforeEach
    fun setUp() {
        service = MockBillsService()
    }

    @Test
    fun `findAll should return non-empty list of bills`() {
        // Given - service is initialized
        
        // When - requesting all bills
        val bills = service.findAll()
        
        // Then - should return bills with proper structure
        assertNotNull(bills)
        assertTrue(bills.isNotEmpty())
        assertTrue(bills.all { it is BillEntity })
    }

    @Test
    fun `findAll should return bills with all required fields populated`() {
        // Given - service is initialized
        
        // When - requesting all bills
        val bills = service.findAll()
        
        // Then - all bills should have proper data structure
        bills.forEach { bill ->
            assertNotNull(bill.id)
            assertTrue(bill.id.isNotBlank())
            assertNotNull(bill.serviceProviderId)
            assertTrue(bill.serviceProviderId.isNotBlank())
            assertNotNull(bill.billDate)
            assertNotNull(bill.amount)
            assertTrue(bill.amount.compareTo(BigDecimal.ZERO) > 0)
            assertNotNull(bill.createdDate)
            assertNotNull(bill.state)
        }
    }

    @Test
    fun `findAll should include bills in different states`() {
        // Given - service is initialized
        
        // When - requesting all bills
        val bills = service.findAll()
        
        // Then - should include bills in both CREATED and REMOVED states
        val createdBills = bills.filter { it.state == BillState.CREATED }
        val removedBills = bills.filter { it.state == BillState.REMOVED }
        
        assertTrue(createdBills.isNotEmpty(), "Should have bills in CREATED state")
        assertTrue(removedBills.isNotEmpty(), "Should have bills in REMOVED state")
    }

    @Test
    fun `findAll should include bills created from inbox and manually`() {
        // Given - service is initialized
        
        // When - requesting all bills
        val bills = service.findAll()
        
        // Then - should include bills with and without inbox entity links
        val billsFromInbox = bills.filter { it.inboxEntityId != null }
        val manualBills = bills.filter { it.inboxEntityId == null }
        
        assertTrue(billsFromInbox.isNotEmpty(), "Should have bills created from inbox")
        assertTrue(manualBills.isNotEmpty(), "Should have manually created bills")
    }

    @Test
    fun `findAll should include bills with different service providers`() {
        // Given - service is initialized
        
        // When - requesting all bills
        val bills = service.findAll()
        
        // Then - should have bills with different service providers
        val serviceProviders = bills.map { it.serviceProviderId }.distinct()
        assertTrue(serviceProviders.size > 1, "Should have multiple different service providers")
    }

    @Test
    fun `findAll should include bills with realistic amounts`() {
        // Given - service is initialized
        
        // When - requesting all bills
        val bills = service.findAll()
        
        // Then - should have bills with realistic monetary amounts
        bills.forEach { bill ->
            assertTrue(bill.amount.compareTo(BigDecimal.ZERO) > 0, "Amount should be positive")
            assertTrue(bill.amount.compareTo(BigDecimal("10000.00")) < 0, "Amount should be realistic")
        }
    }

    @Test
    fun `findAll should include bills with different dates`() {
        // Given - service is initialized
        
        // When - requesting all bills
        val bills = service.findAll()
        
        // Then - should have bills with different dates (recent and older)
        val dates = bills.map { it.billDate }.distinct()
        assertTrue(dates.size > 1, "Should have bills with different dates")
        
        // Should have at least some recent bills
        val recentBills = bills.filter { it.billDate.isAfter(LocalDate.now().minusDays(30)) }
        assertTrue(recentBills.isNotEmpty(), "Should have some recent bills")
    }

    @Test
    fun `findAll with pagination should return correct subset`() {
        // Given - service is initialized
        val totalBills = service.findAll()
        
        // When - requesting first page with size 3
        val firstPage = service.findAll(page = 0, size = 3)
        
        // Then - should return exactly 3 bills
        assertEquals(3, firstPage.size)
        
        // When - requesting second page with size 3
        val secondPage = service.findAll(page = 1, size = 3)
        
        // Then - should return different bills
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
    fun `findAll should support sorting by billDate`() {
        // Given - service is initialized
        
        // When - requesting bills sorted by billDate ascending
        val billsAsc = service.findAll(page = 0, size = 10, sortBy = "billDate", sortDirection = "ASC")
        
        // Then - should be sorted by bill date ascending
        for (i in 0 until billsAsc.size - 1) {
            assertTrue(billsAsc[i].billDate <= billsAsc[i + 1].billDate)
        }
        
        // When - requesting bills sorted by billDate descending
        val billsDesc = service.findAll(page = 0, size = 10, sortBy = "billDate", sortDirection = "DESC")
        
        // Then - should be sorted by bill date descending
        for (i in 0 until billsDesc.size - 1) {
            assertTrue(billsDesc[i].billDate >= billsDesc[i + 1].billDate)
        }
    }

    @Test
    fun `findAll should support sorting by amount`() {
        // Given - service is initialized
        
        // When - requesting bills sorted by amount ascending
        val billsAsc = service.findAll(page = 0, size = 10, sortBy = "amount", sortDirection = "ASC")
        
        // Then - should be sorted by amount ascending
        for (i in 0 until billsAsc.size - 1) {
            assertTrue(billsAsc[i].amount <= billsAsc[i + 1].amount)
        }
        
        // When - requesting bills sorted by amount descending
        val billsDesc = service.findAll(page = 0, size = 10, sortBy = "amount", sortDirection = "DESC")
        
        // Then - should be sorted by amount descending
        for (i in 0 until billsDesc.size - 1) {
            assertTrue(billsDesc[i].amount >= billsDesc[i + 1].amount)
        }
    }

    @Test
    fun `findAll should support sorting by serviceProviderId`() {
        // Given - service is initialized
        
        // When - requesting bills sorted by serviceProviderId ascending
        val billsAsc = service.findAll(page = 0, size = 10, sortBy = "serviceProviderId", sortDirection = "ASC")
        
        // Then - should be sorted by service provider ID ascending
        for (i in 0 until billsAsc.size - 1) {
            assertTrue(billsAsc[i].serviceProviderId <= billsAsc[i + 1].serviceProviderId)
        }
    }

    @Test
    fun `findAll should support sorting by createdDate`() {
        // Given - service is initialized
        
        // When - requesting bills sorted by createdDate ascending
        val billsAsc = service.findAll(page = 0, size = 10, sortBy = "createdDate", sortDirection = "ASC")
        
        // Then - should be sorted by created date ascending
        for (i in 0 until billsAsc.size - 1) {
            assertTrue(billsAsc[i].createdDate <= billsAsc[i + 1].createdDate)
        }
    }

    @Test
    fun `findAll should handle unknown sort field gracefully`() {
        // Given - service is initialized
        
        // When - requesting bills sorted by unknown field
        val bills = service.findAll(page = 0, size = 10, sortBy = "unknownField", sortDirection = "ASC")
        
        // Then - should return bills without sorting error
        assertTrue(bills.isNotEmpty())
    }

    @Test
    fun `getTotalCount should return consistent count with findAll`() {
        // Given - service is initialized
        
        // When - getting total count and all bills
        val totalCount = service.getTotalCount()
        val allBills = service.findAll()
        
        // Then - counts should match
        assertEquals(totalCount, allBills.size)
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
    fun `mock data should include bills with descriptions`() {
        // Given - service is initialized
        
        // When - requesting all bills
        val bills = service.findAll()
        
        // Then - should include bills with descriptions
        val billsWithDescription = bills.filter { it.description != null }
        assertTrue(billsWithDescription.isNotEmpty(), "Should have bills with descriptions")
    }

    @Test
    fun `mock data should include bills without descriptions`() {
        // Given - service is initialized
        
        // When - requesting all bills
        val bills = service.findAll()
        
        // Then - should include bills without descriptions
        val billsWithoutDescription = bills.filter { it.description == null }
        assertTrue(billsWithoutDescription.isNotEmpty(), "Should have bills without descriptions")
    }

    @Test
    fun `mock data should have diverse service provider types`() {
        // Given - service is initialized
        
        // When - requesting all bills
        val bills = service.findAll()
        
        // Then - should have diverse service provider types
        val serviceProviders = bills.map { it.serviceProviderId }.distinct()
        
        // Should have at least utility, internet, and phone providers
        assertTrue(serviceProviders.size >= 3, "Should have at least 3 different service providers")
    }
}