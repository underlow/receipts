package me.underlow.receipt.repository

import me.underlow.receipt.model.LoginEvent

interface LoginEventRepository {
    fun save(loginEvent: LoginEvent): LoginEvent
}
