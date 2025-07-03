package me.underlow.receipt.service

import me.underlow.receipt.model.Payment
import me.underlow.receipt.model.Bill
import me.underlow.receipt.model.Receipt
import me.underlow.receipt.model.ServiceProvider
import me.underlow.receipt.model.PaymentMethod
import me.underlow.receipt.repository.PaymentRepository
import me.underlow.receipt.repository.BillRepository
import me.underlow.receipt.repository.ReceiptRepository
import me.underlow.receipt.repository.ServiceProviderRepository
import me.underlow.receipt.repository.PaymentMethodRepository
import me.underlow.receipt.repository.UserRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Business logic service for managing Payment entities and payment creation
 */
@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val billRepository: BillRepository,
    private val receiptRepository: ReceiptRepository,
    private val serviceProviderRepository: ServiceProviderRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val userRepository: UserRepository
) {

    /**
     * Finds a Payment by ID and verifies user ownership via email
     */
    fun findByIdAndUserEmail(paymentId: Long, userEmail: String): Payment? {
        val user = userRepository.findByEmail(userEmail) ?: return null
        val payment = paymentRepository.findById(paymentId) ?: return null
        
        return if (payment.userId == user.id) payment else null
    }

    /**
     * Finds all Payments for a user by email
     */
    fun findByUserEmail(userEmail: String): List<Payment> {
        val user = userRepository.findByEmail(userEmail) ?: return emptyList()
        return paymentRepository.findByUserId(user.id!!)
    }

    /**
     * Creates a new Payment from an approved Bill
     */
    fun createPaymentFromBill(
        billId: Long,
        userEmail: String,
        serviceProviderId: Long,
        paymentMethodId: Long,
        amount: BigDecimal,
        currency: String,
        invoiceDate: LocalDate,
        paymentDate: LocalDate,
        comment: String? = null
    ): Payment? {
        val user = userRepository.findByEmail(userEmail) ?: return null
        val bill = billRepository.findById(billId) ?: return null
        
        // Verify the bill belongs to the user
        if (bill.userId != user.id) {
            return null
        }
        
        // Verify the service provider and payment method exist
        val serviceProvider = serviceProviderRepository.findById(serviceProviderId) ?: return null
        val paymentMethod = paymentMethodRepository.findById(paymentMethodId) ?: return null
        
        val payment = Payment(
            serviceProviderId = serviceProviderId,
            paymentMethodId = paymentMethodId,
            amount = amount,
            currency = currency,
            invoiceDate = invoiceDate,
            paymentDate = paymentDate,
            billId = billId,
            userId = user.id!!,
            comment = comment
        )
        
        return paymentRepository.save(payment)
    }

    /**
     * Creates a new Payment from a standalone Receipt
     */
    fun createPaymentFromReceipt(
        receiptId: Long,
        userEmail: String,
        serviceProviderId: Long,
        paymentMethodId: Long,
        amount: BigDecimal,
        currency: String,
        invoiceDate: LocalDate,
        paymentDate: LocalDate,
        comment: String? = null
    ): Payment? {
        val user = userRepository.findByEmail(userEmail) ?: return null
        val receipt = receiptRepository.findById(receiptId) ?: return null
        
        // Verify the receipt belongs to the user
        if (receipt.userId != user.id) {
            return null
        }
        
        // Verify the service provider and payment method exist
        val serviceProvider = serviceProviderRepository.findById(serviceProviderId) ?: return null
        val paymentMethod = paymentMethodRepository.findById(paymentMethodId) ?: return null
        
        val payment = Payment(
            serviceProviderId = serviceProviderId,
            paymentMethodId = paymentMethodId,
            amount = amount,
            currency = currency,
            invoiceDate = invoiceDate,
            paymentDate = paymentDate,
            billId = receipt.billId,
            userId = user.id!!,
            comment = comment
        )
        
        return paymentRepository.save(payment)
    }

    /**
     * Creates a new standalone Payment
     */
    fun createPayment(
        userEmail: String,
        serviceProviderId: Long,
        paymentMethodId: Long,
        amount: BigDecimal,
        currency: String,
        invoiceDate: LocalDate,
        paymentDate: LocalDate,
        comment: String? = null
    ): Payment? {
        val user = userRepository.findByEmail(userEmail) ?: return null
        
        // Verify the service provider and payment method exist
        val serviceProvider = serviceProviderRepository.findById(serviceProviderId) ?: return null
        val paymentMethod = paymentMethodRepository.findById(paymentMethodId) ?: return null
        
        val payment = Payment(
            serviceProviderId = serviceProviderId,
            paymentMethodId = paymentMethodId,
            amount = amount,
            currency = currency,
            invoiceDate = invoiceDate,
            paymentDate = paymentDate,
            billId = null,
            userId = user.id!!,
            comment = comment
        )
        
        return paymentRepository.save(payment)
    }

    /**
     * Updates an existing Payment
     */
    fun updatePayment(
        paymentId: Long,
        userEmail: String,
        serviceProviderId: Long,
        paymentMethodId: Long,
        amount: BigDecimal,
        currency: String,
        invoiceDate: LocalDate,
        paymentDate: LocalDate,
        comment: String? = null
    ): Payment? {
        val existingPayment = findByIdAndUserEmail(paymentId, userEmail) ?: return null
        
        // Verify the service provider and payment method exist
        val serviceProvider = serviceProviderRepository.findById(serviceProviderId) ?: return null
        val paymentMethod = paymentMethodRepository.findById(paymentMethodId) ?: return null
        
        val updatedPayment = existingPayment.copy(
            serviceProviderId = serviceProviderId,
            paymentMethodId = paymentMethodId,
            amount = amount,
            currency = currency,
            invoiceDate = invoiceDate,
            paymentDate = paymentDate,
            comment = comment
        )
        
        return paymentRepository.save(updatedPayment)
    }

    /**
     * Finds all Payments for a specific service provider
     */
    fun findByServiceProviderAndUserEmail(serviceProviderId: Long, userEmail: String): List<Payment> {
        val user = userRepository.findByEmail(userEmail) ?: return emptyList()
        val allPayments = paymentRepository.findByServiceProviderId(serviceProviderId)
        
        return allPayments.filter { it.userId == user.id }
    }

    /**
     * Gets payment statistics for a user
     */
    fun getPaymentStatistics(userEmail: String): Map<String, Any> {
        val user = userRepository.findByEmail(userEmail) ?: return emptyMap()
        val payments = paymentRepository.findByUserId(user.id!!)
        
        val totalAmount = payments.sumOf { it.amount }
        val totalCount = payments.size
        val uniqueProviders = payments.map { it.serviceProviderId }.distinct().size
        val uniquePaymentMethods = payments.map { it.paymentMethodId }.distinct().size
        
        return mapOf(
            "totalAmount" to totalAmount,
            "totalCount" to totalCount,
            "uniqueProviders" to uniqueProviders,
            "uniquePaymentMethods" to uniquePaymentMethods
        )
    }

    /**
     * Finds Payments within a date range
     */
    fun findByDateRange(
        userEmail: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Payment> {
        val user = userRepository.findByEmail(userEmail) ?: return emptyList()
        val payments = paymentRepository.findByUserId(user.id!!)
        
        return payments.filter { 
            (it.paymentDate.isAfter(startDate) || it.paymentDate.isEqual(startDate)) &&
            (it.paymentDate.isBefore(endDate) || it.paymentDate.isEqual(endDate))
        }
    }

    /**
     * Deletes a Payment
     */
    fun deletePayment(paymentId: Long, userEmail: String): Boolean {
        val payment = findByIdAndUserEmail(paymentId, userEmail) ?: return false
        return paymentRepository.delete(paymentId)
    }

    /**
     * Gets the Bill associated with a Payment
     */
    fun getAssociatedBill(paymentId: Long, userEmail: String): Bill? {
        val payment = findByIdAndUserEmail(paymentId, userEmail) ?: return null
        val billId = payment.billId ?: return null
        
        val bill = billRepository.findById(billId) ?: return null
        
        // Verify the bill belongs to the same user
        val user = userRepository.findByEmail(userEmail) ?: return null
        return if (bill.userId == user.id) bill else null
    }

    /**
     * Gets the ServiceProvider associated with a Payment
     */
    fun getAssociatedServiceProvider(paymentId: Long, userEmail: String): ServiceProvider? {
        val payment = findByIdAndUserEmail(paymentId, userEmail) ?: return null
        return serviceProviderRepository.findById(payment.serviceProviderId)
    }

    /**
     * Gets the PaymentMethod associated with a Payment
     */
    fun getAssociatedPaymentMethod(paymentId: Long, userEmail: String): PaymentMethod? {
        val payment = findByIdAndUserEmail(paymentId, userEmail) ?: return null
        return paymentMethodRepository.findById(payment.paymentMethodId)
    }
}