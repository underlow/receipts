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
        val status = BillStatus.PENDING
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
    fun `given BillStatus enum when accessing values then should have correct statuses`() {
        // Given: BillStatus enum
        // When: Accessing enum values
        val values = BillStatus.values()

        // Then: Should contain all expected bill statuses
        assertEquals(4, values.size)
        assertEquals(BillStatus.PENDING, BillStatus.valueOf("PENDING"))
        assertEquals(BillStatus.PROCESSING, BillStatus.valueOf("PROCESSING"))
        assertEquals(BillStatus.APPROVED, BillStatus.valueOf("APPROVED"))
        assertEquals(BillStatus.REJECTED, BillStatus.valueOf("REJECTED"))
    }
}