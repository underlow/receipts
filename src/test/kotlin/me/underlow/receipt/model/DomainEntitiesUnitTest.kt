package me.underlow.receipt.model

import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DomainEntitiesUnitTest {

    @Test
    fun `given valid data when creating ServiceProvider then should create successfully`() {
        // Given: Valid service provider data
        val name = "Electricity Company"
        val category = "Utilities"
        val defaultPaymentMethod = "BANK"
        val isActive = true
        val comment = "Monthly electricity provider"

        // When: ServiceProvider is created
        val serviceProvider = ServiceProvider(
            id = null,
            name = name,
            category = category,
            defaultPaymentMethod = defaultPaymentMethod,
            isActive = isActive,
            comment = comment
        )

        // Then: ServiceProvider should be created with correct values
        assertNotNull(serviceProvider)
        assertEquals(name, serviceProvider.name)
        assertEquals(category, serviceProvider.category)
        assertEquals(defaultPaymentMethod, serviceProvider.defaultPaymentMethod)
        assertEquals(isActive, serviceProvider.isActive)
        assertEquals(comment, serviceProvider.comment)
    }

    @Test
    fun `given valid data when creating PaymentMethod then should create successfully`() {
        // Given: Valid payment method data
        val name = "Primary Credit Card"
        val type = PaymentMethodType.CARD
        val comment = "Main payment card"

        // When: PaymentMethod is created
        val paymentMethod = PaymentMethod(
            id = null,
            name = name,
            type = type,
            comment = comment
        )

        // Then: PaymentMethod should be created with correct values
        assertNotNull(paymentMethod)
        assertEquals(name, paymentMethod.name)
        assertEquals(type, paymentMethod.type)
        assertEquals(comment, paymentMethod.comment)
    }

    @Test
    fun `given valid data when creating Bill then should create successfully`() {
        // Given: Valid bill data
        val filename = "electricity_bill_jan_2024.pdf"
        val filePath = "/data/bills/electricity_bill_jan_2024.pdf"
        val uploadDate = LocalDateTime.now()
        val status = ItemStatus.NEW
        val userId = 1L

        // When: Bill is created
        val bill = Bill(
            id = null,
            filename = filename,
            filePath = filePath,
            uploadDate = uploadDate,
            status = status,
            ocrRawJson = null,
            extractedAmount = null,
            extractedDate = null,
            extractedProvider = null,
            userId = userId
        )

        // Then: Bill should be created with correct values
        assertNotNull(bill)
        assertEquals(filename, bill.filename)
        assertEquals(filePath, bill.filePath)
        assertEquals(uploadDate, bill.uploadDate)
        assertEquals(status, bill.status)
        assertEquals(userId, bill.userId)
    }

    @Test
    fun `given valid data when creating Receipt then should create successfully`() {
        // Given: Valid receipt data
        val userId = 1L
        val billId = 2L

        // When: Receipt is created
        val receipt = Receipt(
            id = null,
            userId = userId,
            billId = billId
        )

        // Then: Receipt should be created with correct values
        assertNotNull(receipt)
        assertEquals(userId, receipt.userId)
        assertEquals(billId, receipt.billId)
    }

    @Test
    fun `given valid data when creating Payment then should create successfully`() {
        // Given: Valid payment data
        val serviceProviderId = 1L
        val paymentMethodId = 2L
        val amount = BigDecimal("150.50")
        val currency = "USD"
        val invoiceDate = LocalDate.of(2024, 1, 15)
        val paymentDate = LocalDate.of(2024, 1, 20)
        val userId = 1L

        // When: Payment is created
        val payment = Payment(
            id = null,
            serviceProviderId = serviceProviderId,
            paymentMethodId = paymentMethodId,
            amount = amount,
            currency = currency,
            invoiceDate = invoiceDate,
            paymentDate = paymentDate,
            billId = null,
            userId = userId,
            comment = null
        )

        // Then: Payment should be created with correct values
        assertNotNull(payment)
        assertEquals(serviceProviderId, payment.serviceProviderId)
        assertEquals(paymentMethodId, payment.paymentMethodId)
        assertEquals(amount, payment.amount)
        assertEquals(currency, payment.currency)
        assertEquals(invoiceDate, payment.invoiceDate)
        assertEquals(paymentDate, payment.paymentDate)
        assertEquals(userId, payment.userId)
    }

    @Test
    fun `given PaymentMethodType enum when accessing values then should have correct types`() {
        // Given: PaymentMethodType enum
        // When: Accessing enum values
        val values = PaymentMethodType.values()

        // Then: Should contain all expected payment method types
        assertEquals(4, values.size)
        assertEquals(PaymentMethodType.CARD, PaymentMethodType.valueOf("CARD"))
        assertEquals(PaymentMethodType.BANK, PaymentMethodType.valueOf("BANK"))
        assertEquals(PaymentMethodType.CASH, PaymentMethodType.valueOf("CASH"))
        assertEquals(PaymentMethodType.OTHER, PaymentMethodType.valueOf("OTHER"))
    }

    @Test
    fun `given ItemStatus enum when accessing values then should have correct statuses`() {
        // Given: ItemStatus enum after removing PROCESSING status
        // When: Accessing enum values
        val values = ItemStatus.values()

        // Then: Should contain only NEW, APPROVED, and REJECTED statuses (no PROCESSING)
        assertEquals(3, values.size)
        assertEquals(ItemStatus.NEW, ItemStatus.valueOf("NEW"))
        assertEquals(ItemStatus.APPROVED, ItemStatus.valueOf("APPROVED"))
        assertEquals(ItemStatus.REJECTED, ItemStatus.valueOf("REJECTED"))
    }

    @Test
    fun `given valid data when creating IncomingFile then should create successfully`() {
        // Given: Valid incoming file data
        val filename = "receipt_2024_01_15.pdf"
        val filePath = "/data/attachments/2024/01/15/receipt_2024_01_15.pdf"
        val uploadDate = LocalDateTime.now()
        val status = ItemStatus.NEW
        val checksum = "a1b2c3d4e5f6789012345678901234567890abcdef1234567890abcdef123456"
        val userId = 1L

        // When: IncomingFile is created
        val incomingFile = IncomingFile(
            id = null,
            filename = filename,
            filePath = filePath,
            uploadDate = uploadDate,
            status = status,
            checksum = checksum,
            userId = userId
        )

        // Then: IncomingFile should be created with correct values
        assertNotNull(incomingFile)
        assertEquals(filename, incomingFile.filename)
        assertEquals(filePath, incomingFile.filePath)
        assertEquals(uploadDate, incomingFile.uploadDate)
        assertEquals(status, incomingFile.status)
        assertEquals(checksum, incomingFile.checksum)
        assertEquals(userId, incomingFile.userId)
    }

    @Test
    fun `given IncomingFile with same checksum when comparing then should be considered duplicate`() {
        // Given: Two IncomingFiles with same checksum but different names
        val checksum = "duplicate_checksum_test_12345"
        val userId = 1L
        
        val file1 = IncomingFile(
            id = 1L,
            filename = "receipt1.pdf",
            filePath = "/path/to/receipt1.pdf",
            uploadDate = LocalDateTime.now(),
            status = ItemStatus.NEW,
            checksum = checksum,
            userId = userId
        )
        
        val file2 = IncomingFile(
            id = 2L,
            filename = "receipt2.pdf",
            filePath = "/path/to/receipt2.pdf",
            uploadDate = LocalDateTime.now(),
            status = ItemStatus.APPROVED,
            checksum = checksum,
            userId = userId
        )

        // When: Comparing checksums
        val haveSameChecksum = file1.checksum == file2.checksum

        // Then: Files should be considered duplicates based on checksum
        assertEquals(true, haveSameChecksum)
        assertEquals(checksum, file1.checksum)
        assertEquals(checksum, file2.checksum)
    }
}