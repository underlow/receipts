package me.underlow.receipt.service

import me.underlow.receipt.dao.ServiceProviderDao
import me.underlow.receipt.model.ServiceProvider
import me.underlow.receipt.model.ServiceProviderState
import me.underlow.receipt.model.RegularFrequency
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith
import java.time.LocalDateTime

/**
 * Unit tests for ServiceProviderService.
 * Tests business logic for service provider management including validation and state transitions.
 */
@ExtendWith(MockitoExtension::class)
class ServiceProviderServiceTest {
    
    @Mock
    private lateinit var serviceProviderDao: ServiceProviderDao
    
    private lateinit var serviceProviderService: ServiceProviderService
    
    @BeforeEach
    fun setUp() {
        serviceProviderService = ServiceProviderService(serviceProviderDao)
    }
    
    @Test
    fun `given valid service provider when createServiceProvider then creates service provider`() {
        // given - valid service provider data
        val name = "Test Provider"
        val expectedServiceProvider = ServiceProvider.createNew(name)
        val savedServiceProvider = expectedServiceProvider.copy(id = 1L)
        
        whenever(serviceProviderDao.existsByName(name)).thenReturn(false)
        whenever(serviceProviderDao.save(any())).thenReturn(savedServiceProvider)
        
        // when - creating service provider
        val result = serviceProviderService.createServiceProvider(name)
        
        // then - service provider is created
        assertEquals(savedServiceProvider, result)
        verify(serviceProviderDao).existsByName(name)
        verify(serviceProviderDao).save(any())
    }
    
    @Test
    fun `given duplicate name when createServiceProvider then throws exception`() {
        // given - duplicate name exists
        val name = "Duplicate Provider"
        
        whenever(serviceProviderDao.existsByName(name)).thenReturn(true)
        
        // when - creating service provider with duplicate name
        // then - throws exception
        assertFailsWith<IllegalArgumentException> {
            serviceProviderService.createServiceProvider(name)
        }
        
        verify(serviceProviderDao).existsByName(name)
        verify(serviceProviderDao, never()).save(any())
    }
    
    @Test
    fun `given blank name when createServiceProvider then throws exception`() {
        // given - blank name
        val name = "   "
        
        // when - creating service provider with blank name
        // then - throws exception
        assertFailsWith<IllegalArgumentException> {
            serviceProviderService.createServiceProvider(name)
        }
        
        verify(serviceProviderDao, never()).existsByName(any())
        verify(serviceProviderDao, never()).save(any())
    }
    
    @Test
    fun `given existing service provider when updateServiceProvider then updates service provider`() {
        // given - existing service provider
        val existingProvider = ServiceProvider.createNew("Original Name").copy(id = 1L)
        val newName = "Updated Name"
        val comment = "Updated comment"
        val commentForOcr = "Updated OCR comment"
        val regular = RegularFrequency.MONTHLY
        val customFields = "{\"field1\": \"value1\"}"
        val updatedProvider = existingProvider
            .updateName(newName)
            .updateComment(comment)
            .updateCommentForOcr(commentForOcr)
            .updateRegular(regular)
            .updateCustomFields(customFields)
        
        whenever(serviceProviderDao.findById(existingProvider.id)).thenReturn(existingProvider)
        whenever(serviceProviderDao.existsByName(newName)).thenReturn(false)
        whenever(serviceProviderDao.save(any())).thenReturn(updatedProvider)
        
        // when - updating service provider
        val result = serviceProviderService.updateServiceProvider(
            existingProvider.id, newName, comment, commentForOcr, regular, customFields
        )
        
        // then - service provider is updated
        assertEquals(updatedProvider, result)
        verify(serviceProviderDao).findById(existingProvider.id)
        verify(serviceProviderDao).existsByName(newName)
        verify(serviceProviderDao).save(any())
    }
    
    @Test
    fun `given non-existent service provider when updateServiceProvider then throws exception`() {
        // given - non-existent service provider
        val nonExistentId = 999L
        
        whenever(serviceProviderDao.findById(nonExistentId)).thenReturn(null)
        
        // when - updating non-existent service provider
        // then - throws exception
        assertFailsWith<IllegalArgumentException> {
            serviceProviderService.updateServiceProvider(
                nonExistentId, "New Name", null, null, RegularFrequency.NOT_REGULAR, null
            )
        }
        
        verify(serviceProviderDao).findById(nonExistentId)
        verify(serviceProviderDao, never()).save(any())
    }
    
    @Test
    fun `given existing service provider when hideServiceProvider then hides service provider`() {
        // given - existing active service provider
        val existingProvider = ServiceProvider.createNew("Test Provider").copy(id = 1L)
        val hiddenProvider = existingProvider.hide()
        
        whenever(serviceProviderDao.findById(existingProvider.id)).thenReturn(existingProvider)
        whenever(serviceProviderDao.save(any())).thenReturn(hiddenProvider)
        
        // when - hiding service provider
        val result = serviceProviderService.hideServiceProvider(existingProvider.id)
        
        // then - service provider is hidden
        assertEquals(hiddenProvider, result)
        verify(serviceProviderDao).findById(existingProvider.id)
        verify(serviceProviderDao).save(any())
    }
    
    @Test
    fun `given existing service provider when showServiceProvider then shows service provider`() {
        // given - existing hidden service provider
        val hiddenProvider = ServiceProvider.createNew("Test Provider").copy(id = 1L, state = ServiceProviderState.HIDDEN)
        val activeProvider = hiddenProvider.show()
        
        whenever(serviceProviderDao.findById(hiddenProvider.id)).thenReturn(hiddenProvider)
        whenever(serviceProviderDao.save(any())).thenReturn(activeProvider)
        
        // when - showing service provider
        val result = serviceProviderService.showServiceProvider(hiddenProvider.id)
        
        // then - service provider is shown
        assertEquals(activeProvider, result)
        verify(serviceProviderDao).findById(hiddenProvider.id)
        verify(serviceProviderDao).save(any())
    }
    
    @Test
    fun `given service provider id when findById then returns service provider`() {
        // given - existing service provider
        val existingProvider = ServiceProvider.createNew("Test Provider").copy(id = 1L)
        
        whenever(serviceProviderDao.findById(existingProvider.id)).thenReturn(existingProvider)
        
        // when - finding service provider by id
        val result = serviceProviderService.findById(existingProvider.id)
        
        // then - returns service provider
        assertEquals(existingProvider, result)
        verify(serviceProviderDao).findById(existingProvider.id)
    }
    
    @Test
    fun `given active state when findByState then returns active service providers`() {
        // given - active service providers
        val activeProviders = listOf(
            ServiceProvider.createNew("Provider 1").copy(id = 1L),
            ServiceProvider.createNew("Provider 2").copy(id = 2L)
        )
        
        whenever(serviceProviderDao.findByState(ServiceProviderState.ACTIVE)).thenReturn(activeProviders)
        
        // when - finding active service providers
        val result = serviceProviderService.findByState(ServiceProviderState.ACTIVE)
        
        // then - returns active service providers
        assertEquals(activeProviders, result)
        verify(serviceProviderDao).findByState(ServiceProviderState.ACTIVE)
    }
    
    @Test
    fun `given all providers when findAll then returns all service providers`() {
        // given - all service providers
        val allProviders = listOf(
            ServiceProvider.createNew("Provider 1").copy(id = 1L),
            ServiceProvider.createNew("Provider 2").copy(id = 2L, state = ServiceProviderState.HIDDEN)
        )
        
        whenever(serviceProviderDao.findAll()).thenReturn(allProviders)
        
        // when - finding all service providers
        val result = serviceProviderService.findAll()
        
        // then - returns all service providers
        assertEquals(allProviders, result)
        verify(serviceProviderDao).findAll()
    }
    
    @Test
    fun `given valid custom fields when validateCustomFields then validates correctly`() {
        // given - valid JSON custom fields
        val validJson = "{\"field1\": \"value1\", \"field2\": \"value2\"}"
        
        // when - validating custom fields
        val result = serviceProviderService.validateCustomFields(validJson)
        
        // then - returns true for valid JSON
        assertTrue(result)
    }
    
    @Test
    fun `given invalid custom fields when validateCustomFields then returns false`() {
        // given - invalid JSON custom fields
        val invalidJson = "{field1: value1}"
        
        // when - validating custom fields
        val result = serviceProviderService.validateCustomFields(invalidJson)
        
        // then - returns false for invalid JSON
        assertFalse(result)
    }
    
    @Test
    fun `given null custom fields when validateCustomFields then returns true`() {
        // given - null custom fields
        val nullFields = null
        
        // when - validating custom fields
        val result = serviceProviderService.validateCustomFields(nullFields)
        
        // then - returns true for null fields
        assertTrue(result)
    }
    
    @Test
    fun `given service provider with duplicate name when updateServiceProvider then throws exception`() {
        // given - service provider exists with different name that conflicts
        val existingProvider = ServiceProvider.createNew("Original Name").copy(id = 1L)
        val newName = "Duplicate Name"
        
        whenever(serviceProviderDao.findById(existingProvider.id)).thenReturn(existingProvider)
        whenever(serviceProviderDao.existsByName(newName)).thenReturn(true)
        
        // when - updating service provider with duplicate name
        // then - throws exception
        assertFailsWith<IllegalArgumentException> {
            serviceProviderService.updateServiceProvider(
                existingProvider.id, newName, null, null, RegularFrequency.NOT_REGULAR, null
            )
        }
        
        verify(serviceProviderDao).findById(existingProvider.id)
        verify(serviceProviderDao).existsByName(newName)
        verify(serviceProviderDao, never()).save(any())
    }
    
    @Test
    fun `given invalid custom fields when updateServiceProvider then throws exception`() {
        // given - service provider exists with invalid custom fields
        val existingProvider = ServiceProvider.createNew("Test Provider").copy(id = 1L)
        val invalidCustomFields = "{invalid json}"
        
        whenever(serviceProviderDao.findById(existingProvider.id)).thenReturn(existingProvider)
        
        // when - updating service provider with invalid custom fields
        // then - throws exception
        assertFailsWith<IllegalArgumentException> {
            serviceProviderService.updateServiceProvider(
                existingProvider.id, existingProvider.name, null, null, RegularFrequency.NOT_REGULAR, invalidCustomFields
            )
        }
        
        verify(serviceProviderDao).findById(existingProvider.id)
        verify(serviceProviderDao, never()).save(any())
    }
}