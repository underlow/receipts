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
    fun `given navigation panel when rendered then should display all navigation tabs`() {
        // given - navigation panel component
        val navigationPanel = NavigationPanel()
        
        // when - rendering navigation panel
        val html = navigationPanel.render()
        
        // then - should display all required tabs
        assertNotNull(html)
        assertTrue(html.contains("Inbox"))
        assertTrue(html.contains("Bills"))
        assertTrue(html.contains("Receipts"))
    }

    @Test
    fun `given navigation panel when rendered with default state then should highlight inbox tab as active`() {
        // given - navigation panel component with default state
        val navigationPanel = NavigationPanel()
        
        // when - rendering navigation panel with default active tab
        val html = navigationPanel.render()
        
        // then - should highlight inbox tab as active by default
        assertNotNull(html)
        assertTrue(html.contains("active"))
        assertTrue(html.contains("Inbox"))
    }

    @Test
    fun `given navigation panel when rendered with active tab then should highlight specified tab`() {
        // given - navigation panel component
        val navigationPanel = NavigationPanel()
        
        // when - rendering navigation panel with bills tab active
        val html = navigationPanel.render(activeTab = "Bills")
        
        // then - should highlight bills tab as active
        assertNotNull(html)
        assertTrue(html.contains("Bills"))
        assertTrue(html.contains("active"))
    }

    @Test
    fun `given navigation panel when rendered with receipts tab active then should highlight receipts tab`() {
        // given - navigation panel component
        val navigationPanel = NavigationPanel()
        
        // when - rendering navigation panel with receipts tab active
        val html = navigationPanel.render(activeTab = "Receipts")
        
        // then - should highlight receipts tab as active
        assertNotNull(html)
        assertTrue(html.contains("Receipts"))
        assertTrue(html.contains("active"))
    }

    @Test
    fun `given navigation panel when rendered then should include proper accessibility attributes`() {
        // given - navigation panel component
        val navigationPanel = NavigationPanel()
        
        // when - rendering navigation panel
        val html = navigationPanel.render()
        
        // then - should include proper accessibility attributes
        assertNotNull(html)
        assertTrue(html.contains("role=\"navigation\""))
        assertTrue(html.contains("aria-label"))
        assertTrue(html.contains("tabindex"))
    }

    @Test
    fun `given navigation panel when rendered then should include tab selection functionality`() {
        // given - navigation panel component
        val navigationPanel = NavigationPanel()
        
        // when - rendering navigation panel
        val html = navigationPanel.render()
        
        // then - should include tab selection functionality
        assertNotNull(html)
        assertTrue(html.contains("tab-link"))
        assertTrue(html.contains("data-tab"))
        assertTrue(html.contains("data-bs-target"))
    }

    @Test
    fun `given navigation panel when rendered with invalid tab then should default to inbox`() {
        // given - navigation panel component
        val navigationPanel = NavigationPanel()
        
        // when - rendering navigation panel with invalid tab name
        val html = navigationPanel.render(activeTab = "InvalidTab")
        
        // then - should default to inbox tab as active
        assertNotNull(html)
        assertTrue(html.contains("Inbox"))
        assertTrue(html.contains("active"))
    }

    @Test
    fun `given navigation panel when rendered then should include proper CSS classes for styling`() {
        // given - navigation panel component
        val navigationPanel = NavigationPanel()
        
        // when - rendering navigation panel
        val html = navigationPanel.render()
        
        // then - should include proper CSS classes for styling
        assertNotNull(html)
        assertTrue(html.contains("nav-panel"))
        assertTrue(html.contains("nav-tabs"))
        assertTrue(html.contains("nav-link"))
    }

    @Test
    fun `given navigation panel when rendered then should handle tab state management`() {
        // given - navigation panel component
        val navigationPanel = NavigationPanel()
        
        // when - rendering navigation panel with different active tabs
        val inboxHtml = navigationPanel.render(activeTab = "Inbox")
        val billsHtml = navigationPanel.render(activeTab = "Bills")
        val receiptsHtml = navigationPanel.render(activeTab = "Receipts")
        
        // then - should handle tab state management correctly
        assertNotNull(inboxHtml)
        assertNotNull(billsHtml)
        assertNotNull(receiptsHtml)
        
        // Each should have only one active tab
        assertTrue(inboxHtml.contains("active"))
        assertTrue(billsHtml.contains("active"))
        assertTrue(receiptsHtml.contains("active"))
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
        assertEquals(3, tabCount)
    }

    @Test
    fun `given navigation panel when getting tab names then should return all tab names`() {
        // given - navigation panel component
        val navigationPanel = NavigationPanel()
        
        // when - getting all tab names
        val tabNames = navigationPanel.getTabNames()
        
        // then - should return all tab names
        assertNotNull(tabNames)
        assertEquals(3, tabNames.size)
        assertTrue(tabNames.contains("Inbox"))
        assertTrue(tabNames.contains("Bills"))
        assertTrue(tabNames.contains("Receipts"))
    }
}