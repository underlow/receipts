package me.underlow.receipt.service

import me.underlow.receipt.model.ReceiptEntity
import me.underlow.receipt.model.ReceiptState
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/**
 * Mock service that provides sample receipts data for development and testing.
 * This service generates realistic test data with receipts in all possible states,
 * various payment types, and different merchant scenarios to support
 * UI development and testing scenarios.
 */
@Service
class MockReceiptsService {

    private val mockData: List<ReceiptEntity> = generateMockData()

    /**
     * Returns all mock receipt items.
     * 
     * @return list of all mock receipt entities
     */
    fun findAll(): List<ReceiptEntity> {
        return mockData
    }

    /**
     * Returns paginated mock receipt items with optional sorting.
     * 
     * @param page page number (0-based)
     * @param size page size
     * @param sortBy field to sort by ("paymentDate", "amount", "paymentTypeId", "createdDate")
     * @param sortDirection sort direction ("ASC" or "DESC")
     * @return paginated list of receipt entities
     */
    fun findAll(page: Int, size: Int, sortBy: String = "paymentDate", sortDirection: String = "DESC"): List<ReceiptEntity> {
        val sortedData = when (sortBy) {
            "paymentDate" -> if (sortDirection == "ASC") mockData.sortedBy { it.paymentDate } else mockData.sortedByDescending { it.paymentDate }
            "amount" -> if (sortDirection == "ASC") mockData.sortedBy { it.amount } else mockData.sortedByDescending { it.amount }
            "paymentTypeId" -> if (sortDirection == "ASC") mockData.sortedBy { it.paymentTypeId } else mockData.sortedByDescending { it.paymentTypeId }
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
     * Returns the total count of mock receipt items.
     * 
     * @return total number of items
     */
    fun getTotalCount(): Int {
        return mockData.size
    }

    /**
     * Generates realistic mock data for receipt items in various states and scenarios.
     * 
     * @return list of mock receipt entities
     */
    private fun generateMockData(): List<ReceiptEntity> {
        val receipts = mutableListOf<ReceiptEntity>()
        val baseTime = LocalDateTime.now()

        // CREATED receipts - grocery and food expenses (created from inbox)
        receipts.add(
            ReceiptEntity(
                id = UUID.randomUUID().toString(),
                paymentTypeId = "credit_card",
                paymentDate = LocalDate.now().minusDays(2),
                amount = BigDecimal("87.43"),
                inboxEntityId = "inbox_001",
                state = ReceiptState.CREATED,
                createdDate = baseTime.minusDays(1),
                description = "Weekly grocery shopping",
                merchantName = "Whole Foods Market"
            )
        )

        receipts.add(
            ReceiptEntity(
                id = UUID.randomUUID().toString(),
                paymentTypeId = "debit_card",
                paymentDate = LocalDate.now().minusDays(5),
                amount = BigDecimal("23.67"),
                inboxEntityId = "inbox_002",
                state = ReceiptState.CREATED,
                createdDate = baseTime.minusDays(2),
                description = "Lunch with colleagues",
                merchantName = "Subway"
            )
        )

        receipts.add(
            ReceiptEntity(
                id = UUID.randomUUID().toString(),
                paymentTypeId = "cash",
                paymentDate = LocalDate.now().minusDays(7),
                amount = BigDecimal("45.20"),
                inboxEntityId = "inbox_003",
                state = ReceiptState.CREATED,
                createdDate = baseTime.minusDays(3),
                description = "Coffee shop visit",
                merchantName = "Starbucks"
            )
        )

        // CREATED receipts - retail and shopping (created from inbox)
        receipts.add(
            ReceiptEntity(
                id = UUID.randomUUID().toString(),
                paymentTypeId = "credit_card",
                paymentDate = LocalDate.now().minusDays(10),
                amount = BigDecimal("156.99"),
                inboxEntityId = "inbox_004",
                state = ReceiptState.CREATED,
                createdDate = baseTime.minusDays(4),
                description = "New running shoes",
                merchantName = "Nike Store"
            )
        )

        receipts.add(
            ReceiptEntity(
                id = UUID.randomUUID().toString(),
                paymentTypeId = "debit_card",
                paymentDate = LocalDate.now().minusDays(12),
                amount = BigDecimal("89.50"),
                inboxEntityId = "inbox_005",
                state = ReceiptState.CREATED,
                createdDate = baseTime.minusDays(5),
                description = "Office supplies",
                merchantName = "Staples"
            )
        )

        receipts.add(
            ReceiptEntity(
                id = UUID.randomUUID().toString(),
                paymentTypeId = "credit_card",
                paymentDate = LocalDate.now().minusDays(15),
                amount = BigDecimal("234.75"),
                inboxEntityId = "inbox_006",
                state = ReceiptState.CREATED,
                createdDate = baseTime.minusDays(6),
                description = "Electronics purchase",
                merchantName = "Best Buy"
            )
        )

        // CREATED receipts - manually created (no inbox link)
        receipts.add(
            ReceiptEntity(
                id = UUID.randomUUID().toString(),
                paymentTypeId = "cash",
                paymentDate = LocalDate.now().minusDays(18),
                amount = BigDecimal("15.75"),
                inboxEntityId = null,
                state = ReceiptState.CREATED,
                createdDate = baseTime.minusDays(7),
                description = "Bus fare",
                merchantName = "Metro Transit"
            )
        )

        receipts.add(
            ReceiptEntity(
                id = UUID.randomUUID().toString(),
                paymentTypeId = "check",
                paymentDate = LocalDate.now().minusDays(20),
                amount = BigDecimal("350.00"),
                inboxEntityId = null,
                state = ReceiptState.CREATED,
                createdDate = baseTime.minusDays(8),
                description = "Monthly parking permit",
                merchantName = "Downtown Parking Authority"
            )
        )

        receipts.add(
            ReceiptEntity(
                id = UUID.randomUUID().toString(),
                paymentTypeId = "credit_card",
                paymentDate = LocalDate.now().minusDays(22),
                amount = BigDecimal("67.89"),
                inboxEntityId = null,
                state = ReceiptState.CREATED,
                createdDate = baseTime.minusDays(9),
                description = "Pharmacy prescription",
                merchantName = "CVS Pharmacy"
            )
        )

        // CREATED receipts - services and utilities (mix of inbox and manual)
        receipts.add(
            ReceiptEntity(
                id = UUID.randomUUID().toString(),
                paymentTypeId = "debit_card",
                paymentDate = LocalDate.now().minusDays(25),
                amount = BigDecimal("125.40"),
                inboxEntityId = "inbox_007",
                state = ReceiptState.CREATED,
                createdDate = baseTime.minusDays(10),
                description = "Car repair service",
                merchantName = "Quick Lube Auto"
            )
        )

        receipts.add(
            ReceiptEntity(
                id = UUID.randomUUID().toString(),
                paymentTypeId = "credit_card",
                paymentDate = LocalDate.now().minusDays(28),
                amount = BigDecimal("45.00"),
                inboxEntityId = null,
                state = ReceiptState.CREATED,
                createdDate = baseTime.minusDays(11),
                description = "Haircut",
                merchantName = "The Barber Shop"
            )
        )

        receipts.add(
            ReceiptEntity(
                id = UUID.randomUUID().toString(),
                paymentTypeId = "cash",
                paymentDate = LocalDate.now().minusDays(30),
                amount = BigDecimal("28.50"),
                inboxEntityId = "inbox_008",
                state = ReceiptState.CREATED,
                createdDate = baseTime.minusDays(12),
                description = "Dry cleaning",
                merchantName = "Express Cleaners"
            )
        )

        // CREATED receipts - entertainment and dining (mix of inbox and manual)
        receipts.add(
            ReceiptEntity(
                id = UUID.randomUUID().toString(),
                paymentTypeId = "credit_card",
                paymentDate = LocalDate.now().minusDays(35),
                amount = BigDecimal("78.90"),
                inboxEntityId = "inbox_009",
                state = ReceiptState.CREATED,
                createdDate = baseTime.minusDays(14),
                description = "Dinner with friends",
                merchantName = "Olive Garden"
            )
        )

        receipts.add(
            ReceiptEntity(
                id = UUID.randomUUID().toString(),
                paymentTypeId = "debit_card",
                paymentDate = LocalDate.now().minusDays(38),
                amount = BigDecimal("24.00"),
                inboxEntityId = null,
                state = ReceiptState.CREATED,
                createdDate = baseTime.minusDays(15),
                description = "Movie tickets",
                merchantName = "AMC Theaters"
            )
        )

        receipts.add(
            ReceiptEntity(
                id = UUID.randomUUID().toString(),
                paymentTypeId = "cash",
                paymentDate = LocalDate.now().minusDays(40),
                amount = BigDecimal("12.50"),
                inboxEntityId = "inbox_010",
                state = ReceiptState.CREATED,
                createdDate = baseTime.minusDays(16),
                description = "Coffee and pastry",
                merchantName = "Local Coffee Shop"
            )
        )

        // REMOVED receipts - receipts that have been deleted
        receipts.add(
            ReceiptEntity(
                id = UUID.randomUUID().toString(),
                paymentTypeId = "credit_card",
                paymentDate = LocalDate.now().minusDays(45),
                amount = BigDecimal("199.99"),
                inboxEntityId = "inbox_011",
                state = ReceiptState.REMOVED,
                createdDate = baseTime.minusDays(18),
                description = "Electronics return (cancelled)",
                merchantName = "Amazon"
            )
        )

        receipts.add(
            ReceiptEntity(
                id = UUID.randomUUID().toString(),
                paymentTypeId = "debit_card",
                paymentDate = LocalDate.now().minusDays(50),
                amount = BigDecimal("65.00"),
                inboxEntityId = null,
                state = ReceiptState.REMOVED,
                createdDate = baseTime.minusDays(20),
                description = "Duplicate entry (cancelled)",
                merchantName = "Target"
            )
        )

        receipts.add(
            ReceiptEntity(
                id = UUID.randomUUID().toString(),
                paymentTypeId = "cash",
                paymentDate = LocalDate.now().minusDays(55),
                amount = BigDecimal("18.75"),
                inboxEntityId = "inbox_012",
                state = ReceiptState.REMOVED,
                createdDate = baseTime.minusDays(22),
                description = "Refunded purchase (cancelled)",
                merchantName = "Walmart"
            )
        )

        // Additional receipts with various scenarios
        receipts.add(
            ReceiptEntity(
                id = UUID.randomUUID().toString(),
                paymentTypeId = "credit_card",
                paymentDate = LocalDate.now().minusDays(60),
                amount = BigDecimal("456.78"),
                inboxEntityId = null,
                state = ReceiptState.CREATED,
                createdDate = baseTime.minusDays(24),
                description = "Monthly gym membership",
                merchantName = "Fitness First"
            )
        )

        receipts.add(
            ReceiptEntity(
                id = UUID.randomUUID().toString(),
                paymentTypeId = "check",
                paymentDate = LocalDate.now().minusDays(65),
                amount = BigDecimal("89.99"),
                inboxEntityId = "inbox_013",
                state = ReceiptState.CREATED,
                createdDate = baseTime.minusDays(26),
                description = "Annual subscription",
                merchantName = "Magazine Publisher"
            )
        )

        // Receipts without descriptions (some receipts may not have descriptions)
        receipts.add(
            ReceiptEntity(
                id = UUID.randomUUID().toString(),
                paymentTypeId = "cash",
                paymentDate = LocalDate.now().minusDays(70),
                amount = BigDecimal("35.40"),
                inboxEntityId = "inbox_014",
                state = ReceiptState.CREATED,
                createdDate = baseTime.minusDays(28),
                description = null,
                merchantName = "Gas Station"
            )
        )

        receipts.add(
            ReceiptEntity(
                id = UUID.randomUUID().toString(),
                paymentTypeId = "debit_card",
                paymentDate = LocalDate.now().minusDays(75),
                amount = BigDecimal("22.30"),
                inboxEntityId = null,
                state = ReceiptState.CREATED,
                createdDate = baseTime.minusDays(30),
                description = null,
                merchantName = "Convenience Store"
            )
        )

        // Receipts without merchant names (some receipts may not have merchant info)
        receipts.add(
            ReceiptEntity(
                id = UUID.randomUUID().toString(),
                paymentTypeId = "credit_card",
                paymentDate = LocalDate.now().minusDays(80),
                amount = BigDecimal("75.60"),
                inboxEntityId = "inbox_015",
                state = ReceiptState.CREATED,
                createdDate = baseTime.minusDays(32),
                description = "Online purchase",
                merchantName = null
            )
        )

        receipts.add(
            ReceiptEntity(
                id = UUID.randomUUID().toString(),
                paymentTypeId = "cash",
                paymentDate = LocalDate.now().minusDays(85),
                amount = BigDecimal("8.95"),
                inboxEntityId = null,
                state = ReceiptState.CREATED,
                createdDate = baseTime.minusDays(34),
                description = "Cash purchase",
                merchantName = null
            )
        )

        receipts.add(
            ReceiptEntity(
                id = UUID.randomUUID().toString(),
                paymentTypeId = "debit_card",
                paymentDate = LocalDate.now().minusDays(90),
                amount = BigDecimal("15.25"),
                inboxEntityId = "inbox_016",
                state = ReceiptState.REMOVED,
                createdDate = baseTime.minusDays(36),
                description = null,
                merchantName = null
            )
        )

        return receipts
    }
}