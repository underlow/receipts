package me.underlow.receipt.repository

import me.underlow.receipt.model.User

interface UserRepository {
    fun save(user: User): User
    fun findByEmail(email: String): User?
    fun findById(id: Long): User?
}
