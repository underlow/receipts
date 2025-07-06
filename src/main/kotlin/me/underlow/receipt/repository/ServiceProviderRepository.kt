package me.underlow.receipt.repository

import me.underlow.receipt.model.ServiceProvider

interface ServiceProviderRepository {
    fun save(serviceProvider: ServiceProvider): ServiceProvider
    fun findById(id: Long): ServiceProvider?
    fun findAll(): List<ServiceProvider>
    fun delete(id: Long): Boolean
}