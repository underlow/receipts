package me.underlow.receipt.controller

import me.underlow.receipt.dashboard.BillsView
import me.underlow.receipt.dashboard.InboxView
import me.underlow.receipt.dashboard.NavigationPanel
import me.underlow.receipt.dashboard.PaginationConfig
import me.underlow.receipt.dashboard.ReceiptsView
import me.underlow.receipt.dashboard.SortDirection
import me.underlow.receipt.dashboard.TableViewData
import me.underlow.receipt.service.MockBillsService
import me.underlow.receipt.service.MockInboxService
import me.underlow.receipt.service.MockReceiptsService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

/**
 * Dashboard controller for authenticated users.
 * Provides dashboard page access with authentication requirements.
 */
@Controller
class DashboardController(
    private val mockInboxService: MockInboxService,
    private val mockBillsService: MockBillsService,
    private val mockReceiptsService: MockReceiptsService,
    private val inboxView: InboxView,
    private val billsView: BillsView,
    private val receiptsView: ReceiptsView,
    private val navigationPanel: NavigationPanel
) {

    /**
     * Displays the dashboard page for authenticated users.
     * Extracts user profile information from OAuth2User principal and adds to model.
     * Requires authentication to access.
     * 
     * @param model the model to add user profile attributes to
     * @param authentication the authentication object containing user principal
     * @return dashboard template name
     */
    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    fun dashboard(model: Model, authentication: Authentication): String {
        extractUserProfileToModel(model, authentication)
        
        // Add navigation data to model
        val navigationData = navigationPanel.getNavigationData("Inbox") // Default to Inbox
        model.addAttribute("navigationData", navigationData)
        
        return "dashboard"
    }

    /**
     * API endpoint for inbox data with pagination and sorting support.
     * Returns table view data for template rendering.
     * 
     * @param page page number (0-based, default 0)
     * @param size page size (default 10)
     * @param sortBy sort field (default "uploadDate")
     * @param sortDirection sort direction (default "DESC")
     * @param search optional search term
     * @return table view data response
     */
    @GetMapping("/api/inbox")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    fun getInboxData(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "uploadDate") sortBy: String,
        @RequestParam(defaultValue = "DESC") sortDirection: String,
        @RequestParam(required = false) search: String?
    ): ResponseEntity<TableViewData> {
        // Get paginated data from mock service
        val inboxData = mockInboxService.findAll(page, size, sortBy, sortDirection)
        val totalCount = mockInboxService.getTotalCount()
        
        // Apply search filter if provided
        val filteredData = if (!search.isNullOrBlank()) {
            inboxData.filter { entity ->
                entity.ocrResults?.contains(search, ignoreCase = true) == true ||
                entity.uploadedImage.contains(search, ignoreCase = true) ||
                entity.state.name.contains(search, ignoreCase = true) ||
                entity.failureReason?.contains(search, ignoreCase = true) == true
            }
        } else {
            inboxData
        }
        
        // Create pagination config
        val paginationConfig = PaginationConfig(
            pageSize = size,
            currentPage = page + 1, // Convert to 1-based
            totalItems = totalCount
        )
        
        // Convert sort direction
        val sortDir = if (sortDirection.uppercase() == "ASC") SortDirection.ASC else SortDirection.DESC
        
        // Prepare table view data
        val tableViewData = inboxView.prepareTableViewData(
            inboxData = filteredData,
            paginationConfig = paginationConfig,
            searchEnabled = true,
            sortKey = sortBy,
            sortDirection = sortDir
        )
        
        return ResponseEntity.ok(tableViewData)
    }

    /**
     * API endpoint for bills data with pagination and sorting support.
     * Returns table view data for template rendering.
     * 
     * @param page page number (0-based, default 0)
     * @param size page size (default 10)
     * @param sortBy sort field (default "billDate")
     * @param sortDirection sort direction (default "DESC")
     * @param search optional search term
     * @return table view data response
     */
    @GetMapping("/api/bills")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    fun getBillsData(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "billDate") sortBy: String,
        @RequestParam(defaultValue = "DESC") sortDirection: String,
        @RequestParam(required = false) search: String?
    ): ResponseEntity<TableViewData> {
        // Get paginated data from mock service
        val billsData = mockBillsService.findAll(page, size, sortBy, sortDirection)
        val totalCount = mockBillsService.getTotalCount()
        
        // Apply search filter if provided
        val filteredData = if (!search.isNullOrBlank()) {
            billsData.filter { entity ->
                entity.serviceProviderId.contains(search, ignoreCase = true) ||
                entity.description?.contains(search, ignoreCase = true) == true ||
                entity.amount.toString().contains(search, ignoreCase = true) ||
                entity.state.name.contains(search, ignoreCase = true)
            }
        } else {
            billsData
        }
        
        // Create pagination config
        val paginationConfig = PaginationConfig(
            pageSize = size,
            currentPage = page + 1, // Convert to 1-based
            totalItems = totalCount
        )
        
        // Convert sort direction
        val sortDir = if (sortDirection.uppercase() == "ASC") SortDirection.ASC else SortDirection.DESC
        
        // Prepare table view data
        val tableViewData = billsView.prepareTableViewData(
            billsData = filteredData,
            paginationConfig = paginationConfig,
            searchEnabled = true,
            sortKey = sortBy,
            sortDirection = sortDir
        )
        
        return ResponseEntity.ok(tableViewData)
    }

    /**
     * API endpoint for receipts data with pagination and sorting support.
     * Returns table view data for template rendering.
     * 
     * @param page page number (0-based, default 0)
     * @param size page size (default 10)
     * @param sortBy sort field (default "paymentDate")
     * @param sortDirection sort direction (default "DESC")
     * @param search optional search term
     * @return table view data response
     */
    @GetMapping("/api/receipts")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    fun getReceiptsData(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "paymentDate") sortBy: String,
        @RequestParam(defaultValue = "DESC") sortDirection: String,
        @RequestParam(required = false) search: String?
    ): ResponseEntity<TableViewData> {
        // Get paginated data from mock service
        val receiptsData = mockReceiptsService.findAll(page, size, sortBy, sortDirection)
        val totalCount = mockReceiptsService.getTotalCount()
        
        // Apply search filter if provided
        val filteredData = if (!search.isNullOrBlank()) {
            receiptsData.filter { entity ->
                entity.merchantName?.contains(search, ignoreCase = true) == true ||
                entity.description?.contains(search, ignoreCase = true) == true ||
                entity.amount.toString().contains(search, ignoreCase = true) ||
                entity.paymentTypeId.contains(search, ignoreCase = true) ||
                entity.state.name.contains(search, ignoreCase = true)
            }
        } else {
            receiptsData
        }
        
        // Create pagination config
        val paginationConfig = PaginationConfig(
            pageSize = size,
            currentPage = page + 1, // Convert to 1-based
            totalItems = totalCount
        )
        
        // Convert sort direction
        val sortDir = if (sortDirection.uppercase() == "ASC") SortDirection.ASC else SortDirection.DESC
        
        // Prepare table view data
        val tableViewData = receiptsView.prepareTableViewData(
            receiptsData = filteredData,
            paginationConfig = paginationConfig,
            searchEnabled = true,
            sortKey = sortBy,
            sortDirection = sortDir
        )
        
        return ResponseEntity.ok(tableViewData)
    }

    /**
     * Extracts user profile information from OAuth2User principal and adds to model.
     * Handles missing attributes gracefully with fallback values.
     * Also handles regular User principal for test environments.
     * 
     * @param model the model to add user profile attributes to
     * @param authentication the authentication object containing user principal
     */
    private fun extractUserProfileToModel(model: Model, authentication: Authentication) {
        val userName = when (val principal = authentication.principal) {
            is OAuth2User -> principal.getAttribute<String>("name") ?: "Unknown User"
            is org.springframework.security.core.userdetails.User -> principal.username
            else -> "Unknown User"
        }
        
        val userEmail = when (val principal = authentication.principal) {
            is OAuth2User -> principal.getAttribute<String>("email") ?: ""
            is org.springframework.security.core.userdetails.User -> principal.username
            else -> ""
        }
        
        val userAvatar = when (val principal = authentication.principal) {
            is OAuth2User -> principal.getAttribute<String>("picture") ?: ""
            else -> ""
        }
        
        model.addAttribute("userName", userName)
        model.addAttribute("userEmail", userEmail)
        model.addAttribute("userAvatar", userAvatar)
    }
}