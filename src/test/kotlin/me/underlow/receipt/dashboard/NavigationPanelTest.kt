package me.underlow.receipt.dashboard

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Unit tests for NavigationPanel component.
 * Tests component rendering, tab management, and navigation functionality.
 */
@ExtendWith(MockitoExtension::class)
class NavigationPanelTest {

    @Test
    fun `given navigation panel when getting navigation data then should contain all navigation tabs`() {
        // given - navigation panel component
        val navigationPanel = NavigationPanel()
        
        // when - getting navigation data
        val navigationData = navigationPanel.getNavigationData()
        
        // then - should contain all required tabs
        assertNotNull(navigationData)
        assertEquals(4, navigationData.tabs.size)
        assertTrue(navigationData.tabs.any { it.name == "Inbox" })
        assertTrue(navigationData.tabs.any { it.name == "Bills" })
        assertTrue(navigationData.tabs.any { it.name == "Receipts" })
        assertTrue(navigationData.tabs.any { it.name == "Services" })
    }

    @Test
    fun `given navigation panel when getting data with default state then should highlight inbox tab as active`() {
        // given - navigation panel component with default state
        val navigationPanel = NavigationPanel()
        
        // when - getting navigation data with default active tab
        val navigationData = navigationPanel.getNavigationData()
        
        // then - should highlight inbox tab as active by default
        assertNotNull(navigationData)
        assertEquals("Inbox", navigationData.activeTab)
        val inboxTab = navigationData.tabs.find { it.name == "Inbox" }
        assertNotNull(inboxTab)
        assertTrue(inboxTab!!.active)
    }

    @Test
    fun `given navigation panel when getting data with active tab then should highlight specified tab`() {
        // given - navigation panel component
        val navigationPanel = NavigationPanel()
        
        // when - getting navigation data with bills tab active
        val navigationData = navigationPanel.getNavigationData(activeTab = "Bills")
        
        // then - should highlight bills tab as active
        assertNotNull(navigationData)
        assertEquals("Bills", navigationData.activeTab)
        val billsTab = navigationData.tabs.find { it.name == "Bills" }
        assertNotNull(billsTab)
        assertTrue(billsTab!!.active)
        val inboxTab = navigationData.tabs.find { it.name == "Inbox" }
        assertFalse(inboxTab!!.active)
    }

    @Test
    fun `given navigation panel when getting data with receipts tab active then should highlight receipts tab`() {
        // given - navigation panel component
        val navigationPanel = NavigationPanel()
        
        // when - getting navigation data with receipts tab active
        val navigationData = navigationPanel.getNavigationData(activeTab = "Receipts")
        
        // then - should highlight receipts tab as active
        assertNotNull(navigationData)
        assertEquals("Receipts", navigationData.activeTab)
        val receiptsTab = navigationData.tabs.find { it.name == "Receipts" }
        assertNotNull(receiptsTab)
        assertTrue(receiptsTab!!.active)
    }

    @Test
    fun `given navigation panel when getting data then should include tab icons`() {
        // given - navigation panel component
        val navigationPanel = NavigationPanel()
        
        // when - getting navigation data
        val navigationData = navigationPanel.getNavigationData()
        
        // then - should include appropriate icons for each tab
        assertNotNull(navigationData)
        val inboxTab = navigationData.tabs.find { it.name == "Inbox" }
        assertEquals("inbox", inboxTab?.icon)
        val billsTab = navigationData.tabs.find { it.name == "Bills" }
        assertEquals("file-invoice", billsTab?.icon)
        val receiptsTab = navigationData.tabs.find { it.name == "Receipts" }
        assertEquals("receipt", receiptsTab?.icon)
        val servicesTab = navigationData.tabs.find { it.name == "Services" }
        assertEquals("cog", servicesTab?.icon)
    }

    @Test
    fun `given navigation panel when getting data then should have correct tab structure`() {
        // given - navigation panel component
        val navigationPanel = NavigationPanel()
        
        // when - getting navigation data
        val navigationData = navigationPanel.getNavigationData()
        
        // then - should have correct tab structure
        assertNotNull(navigationData)
        navigationData.tabs.forEach { tab ->
            assertNotNull(tab.name)
            assertNotNull(tab.icon)
            assertTrue(tab.name.isNotEmpty())
            assertTrue(tab.icon.isNotEmpty())
        }
    }

    @Test
    fun `given navigation panel when getting data with invalid tab then should default to inbox`() {
        // given - navigation panel component
        val navigationPanel = NavigationPanel()
        
        // when - getting navigation data with invalid tab name
        val navigationData = navigationPanel.getNavigationData(activeTab = "InvalidTab")
        
        // then - should default to inbox tab as active
        assertNotNull(navigationData)
        assertEquals("Inbox", navigationData.activeTab)
        val inboxTab = navigationData.tabs.find { it.name == "Inbox" }
        assertTrue(inboxTab!!.active)
    }

    @Test
    fun `given navigation panel when getting data then should maintain consistent tab names`() {
        // given - navigation panel component
        val navigationPanel = NavigationPanel()
        
        // when - getting navigation data
        val navigationData = navigationPanel.getNavigationData()
        
        // then - should maintain consistent tab names
        assertNotNull(navigationData)
        val expectedTabs = listOf("Inbox", "Bills", "Receipts", "Services")
        val actualTabNames = navigationData.tabs.map { it.name }
        assertEquals(expectedTabs, actualTabNames)
    }

    @Test
    fun `given navigation panel when getting data then should handle tab state management`() {
        // given - navigation panel component
        val navigationPanel = NavigationPanel()
        
        // when - getting navigation data with different active tabs
        val inboxData = navigationPanel.getNavigationData(activeTab = "Inbox")
        val billsData = navigationPanel.getNavigationData(activeTab = "Bills")
        val receiptsData = navigationPanel.getNavigationData(activeTab = "Receipts")
        
        // then - should handle tab state management correctly
        assertNotNull(inboxData)
        assertNotNull(billsData)
        assertNotNull(receiptsData)
        
        // Each should have only one active tab
        assertEquals(1, inboxData.tabs.count { it.active })
        assertEquals(1, billsData.tabs.count { it.active })
        assertEquals(1, receiptsData.tabs.count { it.active })
        
        // Verify correct active tabs
        assertTrue(inboxData.tabs.find { it.name == "Inbox" }!!.active)
        assertTrue(billsData.tabs.find { it.name == "Bills" }!!.active)
        assertTrue(receiptsData.tabs.find { it.name == "Receipts" }!!.active)
    }

    @Test
    fun `given navigation panel when checking valid tab names then should validate correctly`() {
        // given - navigation panel component
        val navigationPanel = NavigationPanel()
        
        // when - checking valid tab names
        val isInboxValid = navigationPanel.isValidTab("Inbox")
        val isBillsValid = navigationPanel.isValidTab("Bills")
        val isReceiptsValid = navigationPanel.isValidTab("Receipts")
        val isInvalidTab = navigationPanel.isValidTab("InvalidTab")
        
        // then - should validate tab names correctly
        assertTrue(isInboxValid)
        assertTrue(isBillsValid)
        assertTrue(isReceiptsValid)
        assertFalse(isInvalidTab)
    }

    @Test
    fun `given navigation panel when getting tab count then should return correct number of tabs`() {
        // given - navigation panel component
        val navigationPanel = NavigationPanel()
        
        // when - getting tab count
        val tabCount = navigationPanel.getTabCount()
        
        // then - should return correct number of tabs
        assertEquals(4, tabCount)
    }

    @Test
    fun `given navigation panel when getting tab names then should return all tab names`() {
        // given - navigation panel component
        val navigationPanel = NavigationPanel()
        
        // when - getting all tab names
        val tabNames = navigationPanel.getTabNames()
        
        // then - should return all tab names
        assertNotNull(tabNames)
        assertEquals(4, tabNames.size)
        assertTrue(tabNames.contains("Inbox"))
        assertTrue(tabNames.contains("Bills"))
        assertTrue(tabNames.contains("Receipts"))
        assertTrue(tabNames.contains("Services"))
    }
}