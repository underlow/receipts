package me.underlow.receipt.repository

import me.underlow.receipt.model.Payment

interface PaymentRepository {
    fun save(payment: Payment): Payment
    fun findById(id: Long): Payment?
    fun findByUserId(userId: Long): List<Payment>
    fun findByServiceProviderId(serviceProviderId: Long): List<Payment>
    fun findAll(): List<Payment>
    fun delete(id: Long): Boolean
}