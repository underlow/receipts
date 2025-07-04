package me.underlow.receipt.repository

import me.underlow.receipt.model.Receipt
import me.underlow.receipt.model.ItemStatus

interface ReceiptRepository {
    fun save(receipt: Receipt): Receipt
    fun findById(id: Long): Receipt?
    fun findByUserId(userId: Long): List<Receipt>
    fun findByBillId(billId: Long): List<Receipt>
    fun findByStatus(status: ItemStatus): List<Receipt>
    fun findByUserIdAndStatus(userId: Long, status: ItemStatus): List<Receipt>
    fun findAll(): List<Receipt>
    fun delete(id: Long): Boolean
}