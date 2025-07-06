package me.underlow.receipt.service

import me.underlow.receipt.model.PaymentMethod
import me.underlow.receipt.model.PaymentMethodType
import me.underlow.receipt.repository.PaymentMethodRepository
import org.springframework.stereotype.Service

/**
 * Business logic service for managing PaymentMethod entities
 */
@Service
class PaymentMethodService(
    private val paymentMethodRepository: PaymentMethodRepository
) {

    /**
     * Finds a PaymentMethod by ID
     */
    fun findById(id: Long): PaymentMethod? {
        return paymentMethodRepository.findById(id)
    }

    /**
     * Finds all PaymentMethods
     */
    fun findAll(): List<PaymentMethod> {
        return paymentMethodRepository.findAll()
    }

    /**
     * Finds PaymentMethods by type
     */
    fun findByType(type: PaymentMethodType): List<PaymentMethod> {
        return paymentMethodRepository.findAll().filter { it.type == type }
    }

    /**
     * Creates a new PaymentMethod
     */
    fun createPaymentMethod(
        name: String,
        type: PaymentMethodType,
        comment: String? = null
    ): PaymentMethod {
        val paymentMethod = PaymentMethod(
            name = name,
            type = type,
            comment = comment
        )
        
        return paymentMethodRepository.save(paymentMethod)
    }

    /**
     * Updates an existing PaymentMethod
     */
    fun updatePaymentMethod(
        id: Long,
        name: String,
        type: PaymentMethodType,
        comment: String? = null
    ): PaymentMethod? {
        val existingMethod = paymentMethodRepository.findById(id) ?: return null
        
        val updatedMethod = existingMethod.copy(
            name = name,
            type = type,
            comment = comment
        )
        
        return paymentMethodRepository.save(updatedMethod)
    }

    /**
     * Deletes a PaymentMethod
     */
    fun deletePaymentMethod(id: Long): Boolean {
        return paymentMethodRepository.delete(id)
    }

    /**
     * Searches PaymentMethods by name
     */
    fun searchByName(searchTerm: String): List<PaymentMethod> {
        return paymentMethodRepository.findAll().filter { 
            it.name.contains(searchTerm, ignoreCase = true) 
        }
    }

    /**
     * Gets all PaymentMethods grouped by type
     */
    fun findAllGroupedByType(): Map<PaymentMethodType, List<PaymentMethod>> {
        return paymentMethodRepository.findAll().groupBy { it.type }
    }

    /**
     * Gets statistics about PaymentMethods
     */
    fun getStatistics(): Map<String, Any> {
        val methods = paymentMethodRepository.findAll()
        val typeCounts = methods.groupingBy { it.type }.eachCount()
        
        return mapOf(
            "totalMethods" to methods.size,
            "typeCounts" to typeCounts.mapKeys { it.key.name }
        )
    }

    /**
     * Gets all available PaymentMethodTypes
     */
    fun getAllTypes(): List<PaymentMethodType> {
        return PaymentMethodType.values().toList()
    }

    /**
     * Finds the default PaymentMethod for a given type
     */
    fun findDefaultForType(type: PaymentMethodType): PaymentMethod? {
        return findByType(type).firstOrNull()
    }
}