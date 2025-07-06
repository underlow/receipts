package me.underlow.receipt.repository

import me.underlow.receipt.model.Bill
import me.underlow.receipt.model.ItemStatus

interface BillRepository {
    fun save(bill: Bill): Bill
    fun findById(id: Long): Bill?
    fun findByUserId(userId: Long): List<Bill>
    fun findByStatus(status: ItemStatus): List<Bill>
    fun findByUserIdAndStatus(userId: Long, status: ItemStatus): List<Bill>
    fun findAll(): List<Bill>
    fun delete(id: Long): Boolean
}