package me.underlow.receipt.repository

import me.underlow.receipt.model.PaymentMethod

interface PaymentMethodRepository {
    fun save(paymentMethod: PaymentMethod): PaymentMethod
    fun findById(id: Long): PaymentMethod?
    fun findAll(): List<PaymentMethod>
    fun delete(id: Long): Boolean
}