package me.underlow.receipt.dashboard

import org.springframework.stereotype.Component

/**
 * NavigationPanel component for providing tabbed navigation between document types.
 * This component displays tabs for Inbox, Bills, and Receipts with proper tab selection
 * and highlighting functionality.
 */
@Component
class NavigationPanel {

    private val validTabs = listOf("Inbox", "Bills", "Receipts")
    private val defaultTab = "Inbox"

    /**
     * Renders the navigation panel HTML with tabs for different document types.
     * 
     * @param activeTab the currently active tab name, defaults to "Inbox"
     * @return HTML string containing the navigation panel with tabs
     */
    fun render(activeTab: String = defaultTab): String {
        val currentActiveTab = if (isValidTab(activeTab)) activeTab else defaultTab
        
        return buildString {
            append("""
                <nav class="nav-panel" role="navigation" aria-label="Document type navigation">
                    <div class="nav nav-tabs" id="nav-tab" role="tablist">
            """.trimIndent())
            
            validTabs.forEach { tab ->
                val isActive = tab == currentActiveTab
                val activeClass = if (isActive) "active" else ""
                val ariaSelected = if (isActive) "true" else "false"
                val tabindex = if (isActive) "0" else "-1"
                
                append("""
                        <button class="nav-link tab-link $activeClass" 
                                id="nav-${tab.lowercase()}-tab" 
                                data-bs-toggle="tab" 
                                data-bs-target="#nav-${tab.lowercase()}" 
                                data-tab="$tab"
                                type="button" 
                                role="tab" 
                                aria-controls="nav-${tab.lowercase()}" 
                                aria-selected="$ariaSelected"
                                tabindex="$tabindex">
                            <i class="fas fa-${getTabIcon(tab)} me-2"></i>
                            $tab
                        </button>
                """.trimIndent())
            }
            
            append("""
                    </div>
                </nav>
            """.trimIndent())
        }
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