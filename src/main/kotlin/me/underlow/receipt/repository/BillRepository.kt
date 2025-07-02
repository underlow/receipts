package me.underlow.receipt.repository

import me.underlow.receipt.model.Bill

interface BillRepository {
    fun save(bill: Bill): Bill
    fun findById(id: Long): Bill?
    fun findByUserId(userId: Long): List<Bill>
    fun findAll(): List<Bill>
    fun delete(id: Long): Boolean
}