package me.underlow.receipt.repository

import me.underlow.receipt.model.Receipt

interface ReceiptRepository {
    fun save(receipt: Receipt): Receipt
    fun findById(id: Long): Receipt?
    fun findByUserId(userId: Long): List<Receipt>
    fun findByBillId(billId: Long): List<Receipt>
    fun findAll(): List<Receipt>
    fun delete(id: Long): Boolean
}