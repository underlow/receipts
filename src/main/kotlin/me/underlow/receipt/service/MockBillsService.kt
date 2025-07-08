package me.underlow.receipt.service

import me.underlow.receipt.model.BillEntity
import me.underlow.receipt.model.BillState
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/**
 * Mock service that provides sample bills data for development and testing.
 * This service generates realistic test data with bills in all possible states,
 * various service providers, and different creation scenarios to support
 * UI development and testing scenarios.
 */
@Service
class MockBillsService {

    private val mockData: List<BillEntity> = generateMockData()

    /**
     * Returns all mock bill items.
     * 
     * @return list of all mock bill entities
     */
    fun findAll(): List<BillEntity> {
        return mockData
    }

    /**
     * Returns paginated mock bill items with optional sorting.
     * 
     * @param page page number (0-based)
     * @param size page size
     * @param sortBy field to sort by ("billDate", "amount", "serviceProviderId", "createdDate")
     * @param sortDirection sort direction ("ASC" or "DESC")
     * @return paginated list of bill entities
     */
    fun findAll(page: Int, size: Int, sortBy: String = "billDate", sortDirection: String = "DESC"): List<BillEntity> {
        val sortedData = when (sortBy) {
            "billDate" -> if (sortDirection == "ASC") mockData.sortedBy { it.billDate } else mockData.sortedByDescending { it.billDate }
            "amount" -> if (sortDirection == "ASC") mockData.sortedBy { it.amount } else mockData.sortedByDescending { it.amount }
            "serviceProviderId" -> if (sortDirection == "ASC") mockData.sortedBy { it.serviceProviderId } else mockData.sortedByDescending { it.serviceProviderId }
            "createdDate" -> if (sortDirection == "ASC") mockData.sortedBy { it.createdDate } else mockData.sortedByDescending { it.createdDate }
            else -> mockData
        }

        val startIndex = page * size
        val endIndex = minOf(startIndex + size, sortedData.size)
        
        return if (startIndex >= sortedData.size) {
            emptyList()
        } else {
            sortedData.subList(startIndex, endIndex)
        }
    }

    /**
     * Returns the total count of mock bill items.
     * 
     * @return total number of items
     */
    fun getTotalCount(): Int {
        return mockData.size
    }

    /**
     * Generates realistic mock data for bill items in various states and scenarios.
     * 
     * @return list of mock bill entities
     */
    private fun generateMockData(): List<BillEntity> {
        val bills = mutableListOf<BillEntity>()
        val baseTime = LocalDateTime.now()

        // CREATED bills - utility bills (created from inbox)
        bills.add(
            BillEntity(
                id = UUID.randomUUID().toString(),
                serviceProviderId = "electric_company_001",
                billDate = LocalDate.now().minusDays(5),
                amount = BigDecimal("125.43"),
                inboxEntityId = "inbox_001",
                state = BillState.CREATED,
                createdDate = baseTime.minusDays(2),
                description = "Monthly electricity bill"
            )
        )

        bills.add(
            BillEntity(
                id = UUID.randomUUID().toString(),
                serviceProviderId = "gas_company_001",
                billDate = LocalDate.now().minusDays(8),
                amount = BigDecimal("89.67"),
                inboxEntityId = "inbox_002",
                state = BillState.CREATED,
                createdDate = baseTime.minusDays(3),
                description = "Natural gas service"
            )
        )

        bills.add(
            BillEntity(
                id = UUID.randomUUID().toString(),
                serviceProviderId = "water_utility_001",
                billDate = LocalDate.now().minusDays(10),
                amount = BigDecimal("67.22"),
                inboxEntityId = "inbox_003",
                state = BillState.CREATED,
                createdDate = baseTime.minusDays(4),
                description = "Water and sewer service"
            )
        )

        // CREATED bills - communication bills (created from inbox)
        bills.add(
            BillEntity(
                id = UUID.randomUUID().toString(),
                serviceProviderId = "internet_provider_001",
                billDate = LocalDate.now().minusDays(12),
                amount = BigDecimal("79.99"),
                inboxEntityId = "inbox_004",
                state = BillState.CREATED,
                createdDate = baseTime.minusDays(5),
                description = "High-speed internet service"
            )
        )

        bills.add(
            BillEntity(
                id = UUID.randomUUID().toString(),
                serviceProviderId = "mobile_carrier_001",
                billDate = LocalDate.now().minusDays(15),
                amount = BigDecimal("95.50"),
                inboxEntityId = "inbox_005",
                state = BillState.CREATED,
                createdDate = baseTime.minusDays(6),
                description = "Mobile phone service"
            )
        )

        bills.add(
            BillEntity(
                id = UUID.randomUUID().toString(),
                serviceProviderId = "cable_provider_001",
                billDate = LocalDate.now().minusDays(18),
                amount = BigDecimal("134.99"),
                inboxEntityId = "inbox_006",
                state = BillState.CREATED,
                createdDate = baseTime.minusDays(7),
                description = "Cable TV and internet bundle"
            )
        )

        // CREATED bills - manually created (no inbox link)
        bills.add(
            BillEntity(
                id = UUID.randomUUID().toString(),
                serviceProviderId = "insurance_company_001",
                billDate = LocalDate.now().minusDays(20),
                amount = BigDecimal("245.00"),
                inboxEntityId = null,
                state = BillState.CREATED,
                createdDate = baseTime.minusDays(8),
                description = "Monthly insurance premium"
            )
        )

        bills.add(
            BillEntity(
                id = UUID.randomUUID().toString(),
                serviceProviderId = "rent_property_001",
                billDate = LocalDate.now().minusDays(25),
                amount = BigDecimal("1850.00"),
                inboxEntityId = null,
                state = BillState.CREATED,
                createdDate = baseTime.minusDays(10),
                description = "Monthly rent payment"
            )
        )

        bills.add(
            BillEntity(
                id = UUID.randomUUID().toString(),
                serviceProviderId = "loan_company_001",
                billDate = LocalDate.now().minusDays(30),
                amount = BigDecimal("423.56"),
                inboxEntityId = null,
                state = BillState.CREATED,
                createdDate = baseTime.minusDays(12),
                description = "Auto loan payment"
            )
        )

        // CREATED bills - additional services (mix of inbox and manual)
        bills.add(
            BillEntity(
                id = UUID.randomUUID().toString(),
                serviceProviderId = "streaming_service_001",
                billDate = LocalDate.now().minusDays(35),
                amount = BigDecimal("15.99"),
                inboxEntityId = "inbox_007",
                state = BillState.CREATED,
                createdDate = baseTime.minusDays(14),
                description = "Streaming subscription"
            )
        )

        bills.add(
            BillEntity(
                id = UUID.randomUUID().toString(),
                serviceProviderId = "gym_membership_001",
                billDate = LocalDate.now().minusDays(40),
                amount = BigDecimal("49.99"),
                inboxEntityId = null,
                state = BillState.CREATED,
                createdDate = baseTime.minusDays(16),
                description = "Monthly gym membership"
            )
        )

        bills.add(
            BillEntity(
                id = UUID.randomUUID().toString(),
                serviceProviderId = "cloud_storage_001",
                billDate = LocalDate.now().minusDays(45),
                amount = BigDecimal("9.99"),
                inboxEntityId = "inbox_008",
                state = BillState.CREATED,
                createdDate = baseTime.minusDays(18),
                description = "Cloud storage subscription"
            )
        )

        // REMOVED bills - bills that have been deleted
        bills.add(
            BillEntity(
                id = UUID.randomUUID().toString(),
                serviceProviderId = "old_internet_provider_001",
                billDate = LocalDate.now().minusDays(50),
                amount = BigDecimal("69.99"),
                inboxEntityId = "inbox_009",
                state = BillState.REMOVED,
                createdDate = baseTime.minusDays(20),
                description = "Old internet service (cancelled)"
            )
        )

        bills.add(
            BillEntity(
                id = UUID.randomUUID().toString(),
                serviceProviderId = "old_mobile_carrier_001",
                billDate = LocalDate.now().minusDays(55),
                amount = BigDecimal("85.00"),
                inboxEntityId = null,
                state = BillState.REMOVED,
                createdDate = baseTime.minusDays(22),
                description = "Old mobile service (cancelled)"
            )
        )

        bills.add(
            BillEntity(
                id = UUID.randomUUID().toString(),
                serviceProviderId = "old_subscription_001",
                billDate = LocalDate.now().minusDays(60),
                amount = BigDecimal("29.99"),
                inboxEntityId = "inbox_010",
                state = BillState.REMOVED,
                createdDate = baseTime.minusDays(24),
                description = "Old subscription service (cancelled)"
            )
        )

        // Additional bills with various scenarios
        bills.add(
            BillEntity(
                id = UUID.randomUUID().toString(),
                serviceProviderId = "credit_card_001",
                billDate = LocalDate.now().minusDays(65),
                amount = BigDecimal("567.89"),
                inboxEntityId = null,
                state = BillState.CREATED,
                createdDate = baseTime.minusDays(26),
                description = "Credit card payment"
            )
        )

        bills.add(
            BillEntity(
                id = UUID.randomUUID().toString(),
                serviceProviderId = "software_license_001",
                billDate = LocalDate.now().minusDays(70),
                amount = BigDecimal("199.99"),
                inboxEntityId = "inbox_011",
                state = BillState.CREATED,
                createdDate = baseTime.minusDays(28),
                description = "Annual software license"
            )
        )

        // Bills without descriptions (some bills may not have descriptions)
        bills.add(
            BillEntity(
                id = UUID.randomUUID().toString(),
                serviceProviderId = "misc_service_001",
                billDate = LocalDate.now().minusDays(75),
                amount = BigDecimal("45.00"),
                inboxEntityId = "inbox_012",
                state = BillState.CREATED,
                createdDate = baseTime.minusDays(30),
                description = null
            )
        )

        bills.add(
            BillEntity(
                id = UUID.randomUUID().toString(),
                serviceProviderId = "misc_service_002",
                billDate = LocalDate.now().minusDays(80),
                amount = BigDecimal("33.75"),
                inboxEntityId = null,
                state = BillState.CREATED,
                createdDate = baseTime.minusDays(32),
                description = null
            )
        )

        bills.add(
            BillEntity(
                id = UUID.randomUUID().toString(),
                serviceProviderId = "old_misc_service_001",
                billDate = LocalDate.now().minusDays(85),
                amount = BigDecimal("22.50"),
                inboxEntityId = "inbox_013",
                state = BillState.REMOVED,
                createdDate = baseTime.minusDays(34),
                description = null
            )
        )

        return bills
    }
}