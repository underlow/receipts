package me.underlow.receipt.service

import me.underlow.receipt.model.InboxEntity
import me.underlow.receipt.model.InboxState
import me.underlow.receipt.model.EntityType
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

/**
 * Mock service that provides sample inbox data for development and testing.
 * This service generates realistic test data with items in all possible states
 * to support UI development and testing scenarios.
 */
@Service
class MockInboxService {

    private val mockData: List<InboxEntity> = generateMockData()

    /**
     * Returns all mock inbox items.
     * 
     * @return list of all mock inbox entities
     */
    fun findAll(): List<InboxEntity> {
        return mockData
    }

    /**
     * Returns paginated mock inbox items with optional sorting.
     * 
     * @param page page number (0-based)
     * @param size page size
     * @param sortBy field to sort by ("uploadDate", "state", "id")
     * @param sortDirection sort direction ("ASC" or "DESC")
     * @return paginated list of inbox entities
     */
    fun findAll(page: Int, size: Int, sortBy: String = "uploadDate", sortDirection: String = "DESC"): List<InboxEntity> {
        val sortedData = when (sortBy) {
            "uploadDate" -> if (sortDirection == "ASC") mockData.sortedBy { it.uploadDate } else mockData.sortedByDescending { it.uploadDate }
            "state" -> if (sortDirection == "ASC") mockData.sortedBy { it.state } else mockData.sortedByDescending { it.state }
            "id" -> if (sortDirection == "ASC") mockData.sortedBy { it.id } else mockData.sortedByDescending { it.id }
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
     * Returns the total count of mock inbox items.
     * 
     * @return total number of items
     */
    fun getTotalCount(): Int {
        return mockData.size
    }

    /**
     * Generates realistic mock data for inbox items in various states.
     * 
     * @return list of mock inbox entities
     */
    private fun generateMockData(): List<InboxEntity> {
        val items = mutableListOf<InboxEntity>()
        val baseTime = LocalDateTime.now()

        // CREATED items (recently uploaded, OCR pending)
        items.add(
            InboxEntity(
                id = UUID.randomUUID().toString(),
                uploadedImage = "/images/receipts/grocery_receipt_001.jpg",
                uploadDate = baseTime.minusMinutes(5),
                state = InboxState.CREATED
            )
        )
        
        items.add(
            InboxEntity(
                id = UUID.randomUUID().toString(),
                uploadedImage = "/images/receipts/gas_receipt_001.jpg",
                uploadDate = baseTime.minusMinutes(15),
                state = InboxState.CREATED
            )
        )

        // PROCESSED items (OCR completed, ready for approval)
        items.add(
            InboxEntity(
                id = UUID.randomUUID().toString(),
                uploadedImage = "/images/receipts/restaurant_receipt_001.jpg",
                uploadDate = baseTime.minusHours(2),
                ocrResults = "Restaurant ABC\nDate: 2024-01-15\nTotal: $45.67\nTax: $3.67\nTip: $9.00",
                state = InboxState.PROCESSED
            )
        )

        items.add(
            InboxEntity(
                id = UUID.randomUUID().toString(),
                uploadedImage = "/images/receipts/office_supplies_receipt_001.jpg",
                uploadDate = baseTime.minusHours(3),
                ocrResults = "Office Depot\nDate: 2024-01-15\nPaper: $12.99\nPens: $8.50\nTotal: $21.49",
                state = InboxState.PROCESSED
            )
        )

        items.add(
            InboxEntity(
                id = UUID.randomUUID().toString(),
                uploadedImage = "/images/receipts/pharmacy_receipt_001.jpg",
                uploadDate = baseTime.minusHours(4),
                ocrResults = "CVS Pharmacy\nDate: 2024-01-14\nMedicine: $24.99\nVitamins: $15.99\nTotal: $40.98",
                state = InboxState.PROCESSED
            )
        )

        // FAILED items (OCR failed, can be retried)
        items.add(
            InboxEntity(
                id = UUID.randomUUID().toString(),
                uploadedImage = "/images/receipts/blurry_receipt_001.jpg",
                uploadDate = baseTime.minusHours(6),
                state = InboxState.FAILED,
                failureReason = "Image too blurry for OCR processing"
            )
        )

        items.add(
            InboxEntity(
                id = UUID.randomUUID().toString(),
                uploadedImage = "/images/receipts/damaged_receipt_001.jpg",
                uploadDate = baseTime.minusHours(8),
                state = InboxState.FAILED,
                failureReason = "Receipt image is damaged or corrupted"
            )
        )

        items.add(
            InboxEntity(
                id = UUID.randomUUID().toString(),
                uploadedImage = "/images/receipts/low_quality_receipt_001.jpg",
                uploadDate = baseTime.minusHours(10),
                state = InboxState.FAILED,
                failureReason = "Image resolution too low for text extraction"
            )
        )

        // APPROVED items (processed and converted to bills/receipts)
        items.add(
            InboxEntity(
                id = UUID.randomUUID().toString(),
                uploadedImage = "/images/receipts/utility_bill_001.jpg",
                uploadDate = baseTime.minusDays(1),
                ocrResults = "Electric Company\nAccount: 123456789\nDate: 2024-01-10\nAmount Due: $125.43\nDue Date: 2024-02-10",
                linkedEntityId = "bill_001",
                linkedEntityType = EntityType.BILL,
                state = InboxState.APPROVED
            )
        )

        items.add(
            InboxEntity(
                id = UUID.randomUUID().toString(),
                uploadedImage = "/images/receipts/grocery_receipt_002.jpg",
                uploadDate = baseTime.minusDays(2),
                ocrResults = "Safeway\nDate: 2024-01-12\nGroceries: $87.45\nTax: $7.02\nTotal: $94.47",
                linkedEntityId = "receipt_001",
                linkedEntityType = EntityType.RECEIPT,
                state = InboxState.APPROVED
            )
        )

        items.add(
            InboxEntity(
                id = UUID.randomUUID().toString(),
                uploadedImage = "/images/receipts/car_maintenance_receipt_001.jpg",
                uploadDate = baseTime.minusDays(3),
                ocrResults = "AutoCare Center\nDate: 2024-01-11\nOil Change: $45.00\nFilter: $15.00\nTotal: $60.00",
                linkedEntityId = "receipt_002",
                linkedEntityType = EntityType.RECEIPT,
                state = InboxState.APPROVED
            )
        )

        items.add(
            InboxEntity(
                id = UUID.randomUUID().toString(),
                uploadedImage = "/images/receipts/internet_bill_001.jpg",
                uploadDate = baseTime.minusDays(5),
                ocrResults = "Internet Provider\nAccount: 987654321\nDate: 2024-01-08\nMonthly Service: $79.99\nTax: $6.40\nTotal: $86.39",
                linkedEntityId = "bill_002",
                linkedEntityType = EntityType.BILL,
                state = InboxState.APPROVED
            )
        )

        // Additional edge cases
        items.add(
            InboxEntity(
                id = UUID.randomUUID().toString(),
                uploadedImage = "/images/receipts/handwritten_receipt_001.jpg",
                uploadDate = baseTime.minusHours(12),
                state = InboxState.FAILED,
                failureReason = "Handwritten text not supported by OCR engine"
            )
        )

        items.add(
            InboxEntity(
                id = UUID.randomUUID().toString(),
                uploadedImage = "/images/receipts/foreign_language_receipt_001.jpg",
                uploadDate = baseTime.minusHours(14),
                state = InboxState.FAILED,
                failureReason = "Language not supported by OCR engine"
            )
        )

        items.add(
            InboxEntity(
                id = UUID.randomUUID().toString(),
                uploadedImage = "/images/receipts/medical_receipt_001.jpg",
                uploadDate = baseTime.minusDays(7),
                ocrResults = "Medical Center\nDate: 2024-01-05\nConsultation: $150.00\nLab Tests: $75.00\nTotal: $225.00",
                linkedEntityId = "receipt_003",
                linkedEntityType = EntityType.RECEIPT,
                state = InboxState.APPROVED
            )
        )

        return items
    }
}