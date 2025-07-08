package me.underlow.receipt.dashboard

import org.springframework.stereotype.Component

/**
 * Data class representing a navigation tab
 */
data class NavigationTab(
    val name: String,
    val icon: String,
    val active: Boolean = false
)

/**
 * Data class representing navigation panel data
 */
data class NavigationPanelData(
    val tabs: List<NavigationTab>,
    val activeTab: String
)

/**
 * NavigationPanel component for providing tabbed navigation between document types.
 * This component provides data for tabs for Inbox, Bills, and Receipts with proper tab selection
 * and highlighting functionality.
 */
@Component
class NavigationPanel {

    private val validTabs = listOf("Inbox", "Bills", "Receipts")
    private val defaultTab = "Inbox"

    /**
     * Creates navigation panel data with tabs for different document types.
     * 
     * @param activeTab the currently active tab name, defaults to "Inbox"
     * @return NavigationPanelData containing tab information
     */
    fun getNavigationData(activeTab: String = defaultTab): NavigationPanelData {
        val currentActiveTab = if (isValidTab(activeTab)) activeTab else defaultTab
        
        val tabs = validTabs.map { tab ->
            NavigationTab(
                name = tab,
                icon = getTabIcon(tab),
                active = tab == currentActiveTab
            )
        }
        
        return NavigationPanelData(
            tabs = tabs,
            activeTab = currentActiveTab
        )
    }

    /**
     * Checks if the provided tab name is valid.
     * 
     * @param tabName the tab name to validate
     * @return true if the tab name is valid, false otherwise
     */
    fun isValidTab(tabName: String): Boolean {
        return validTabs.contains(tabName)
    }

    /**
     * Gets the total number of available tabs.
     * 
     * @return the number of tabs in the navigation panel
     */
    fun getTabCount(): Int {
        return validTabs.size
    }

    /**
     * Gets all available tab names.
     * 
     * @return list of all tab names
     */
    fun getTabNames(): List<String> {
        return validTabs.toList()
    }

    /**
     * Gets the appropriate icon for each tab type.
     * 
     * @param tab the tab name
     * @return the FontAwesome icon class for the tab
     */
    private fun getTabIcon(tab: String): String {
        return when (tab) {
            "Inbox" -> "inbox"
            "Bills" -> "file-invoice"
            "Receipts" -> "receipt"
            else -> "file"
        }
    }
}