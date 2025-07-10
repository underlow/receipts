package me.underlow.receipt.dashboard

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Unit tests for Services tab navigation functionality.
 * Tests Services tab integration with NavigationPanel component.
 */
@ExtendWith(MockitoExtension::class)
class ServicesTabNavigationTest {

    @Test
    fun `given navigation panel when getting navigation data then should contain Services tab as 4th tab`() {
        // given - navigation panel component
        val navigationPanel = NavigationPanel()
        
        // when - getting navigation data
        val navigationData = navigationPanel.getNavigationData()
        
        // then - should contain Services tab as 4th tab
        assertNotNull(navigationData)
        assertEquals(4, navigationData.tabs.size)
        assertTrue(navigationData.tabs.any { it.name == "Services" })
        
        // Verify correct tab order
        val tabNames = navigationData.tabs.map { it.name }
        assertEquals(listOf("Inbox", "Bills", "Receipts", "Services"), tabNames)
    }

    @Test
    fun `given navigation panel when getting data with Services tab active then should highlight Services tab`() {
        // given - navigation panel component
        val navigationPanel = NavigationPanel()
        
        // when - getting navigation data with Services tab active
        val navigationData = navigationPanel.getNavigationData(activeTab = "Services")
        
        // then - should highlight Services tab as active
        assertNotNull(navigationData)
        assertEquals("Services", navigationData.activeTab)
        val servicesTab = navigationData.tabs.find { it.name == "Services" }
        assertNotNull(servicesTab)
        assertTrue(servicesTab!!.active)
        
        // Verify other tabs are not active
        val inboxTab = navigationData.tabs.find { it.name == "Inbox" }
        assertFalse(inboxTab!!.active)
        val billsTab = navigationData.tabs.find { it.name == "Bills" }
        assertFalse(billsTab!!.active)
        val receiptsTab = navigationData.tabs.find { it.name == "Receipts" }
        assertFalse(receiptsTab!!.active)
    }

    @Test
    fun `given navigation panel when getting data then should include Services tab icon`() {
        // given - navigation panel component
        val navigationPanel = NavigationPanel()
        
        // when - getting navigation data
        val navigationData = navigationPanel.getNavigationData()
        
        // then - should include appropriate icon for Services tab
        assertNotNull(navigationData)
        val servicesTab = navigationData.tabs.find { it.name == "Services" }
        assertNotNull(servicesTab)
        assertEquals("cog", servicesTab!!.icon)
    }

    @Test
    fun `given navigation panel when validating tab names then should validate Services tab correctly`() {
        // given - navigation panel component
        val navigationPanel = NavigationPanel()
        
        // when - checking if Services is valid tab name
        val isServicesValid = navigationPanel.isValidTab("Services")
        
        // then - should validate Services tab as valid
        assertTrue(isServicesValid)
    }

    @Test
    fun `given navigation panel when getting tab count then should return 4 tabs including Services`() {
        // given - navigation panel component
        val navigationPanel = NavigationPanel()
        
        // when - getting tab count
        val tabCount = navigationPanel.getTabCount()
        
        // then - should return 4 tabs including Services
        assertEquals(4, tabCount)
    }

    @Test
    fun `given navigation panel when getting tab names then should include Services tab`() {
        // given - navigation panel component
        val navigationPanel = NavigationPanel()
        
        // when - getting all tab names
        val tabNames = navigationPanel.getTabNames()
        
        // then - should include Services tab
        assertNotNull(tabNames)
        assertEquals(4, tabNames.size)
        assertTrue(tabNames.contains("Services"))
        assertTrue(tabNames.contains("Inbox"))
        assertTrue(tabNames.contains("Bills"))
        assertTrue(tabNames.contains("Receipts"))
    }

    @Test
    fun `given navigation panel when getting data with Services tab active then should maintain single active state`() {
        // given - navigation panel component
        val navigationPanel = NavigationPanel()
        
        // when - getting navigation data with Services tab active
        val navigationData = navigationPanel.getNavigationData(activeTab = "Services")
        
        // then - should have only one active tab
        assertEquals(1, navigationData.tabs.count { it.active })
        assertTrue(navigationData.tabs.find { it.name == "Services" }!!.active)
    }

    @Test
    fun `given navigation panel when getting data with default state then should not activate Services tab`() {
        // given - navigation panel component
        val navigationPanel = NavigationPanel()
        
        // when - getting navigation data with default state
        val navigationData = navigationPanel.getNavigationData()
        
        // then - should default to Inbox tab, not Services
        assertEquals("Inbox", navigationData.activeTab)
        val servicesTab = navigationData.tabs.find { it.name == "Services" }
        assertFalse(servicesTab!!.active)
    }
}