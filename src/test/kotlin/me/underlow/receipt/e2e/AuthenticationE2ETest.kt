package me.underlow.receipt.e2e

import com.codeborne.selenide.Selenide
import me.underlow.receipt.config.BaseE2ETest
import me.underlow.receipt.config.TestSecurityConfiguration
import me.underlow.receipt.e2e.pages.DashboardPage
import me.underlow.receipt.e2e.pages.LoginPage
import me.underlow.receipt.e2e.pages.ProfilePage
import me.underlow.receipt.e2e.pages.SettingsPage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.test.context.TestPropertySource

/**
 * End-to-end tests for authentication flow using test security configuration.
 * Tests complete login/logout flow using Selenide browser automation and Page Objects.
 * Uses TestSecurityConfiguration to bypass OAuth2 for testing.
 */
@TestPropertySource(
    properties = [
        "ALLOWED_EMAILS=allowed1@example.com,allowed2@example.com"
    ]
)
class AuthenticationE2ETest : BaseE2ETest() {

    private lateinit var loginPage: LoginPage
    private lateinit var dashboardPage: DashboardPage
    private lateinit var profilePage: ProfilePage
    private lateinit var settingsPage: SettingsPage

    @BeforeEach
    fun setupPages() {
        loginPage = LoginPage()
        dashboardPage = DashboardPage()
        profilePage = ProfilePage()
        settingsPage = SettingsPage()
    }

    @AfterEach
    fun cleanupTestState() {
        try {
            // Ensure user is logged out after each test for proper isolation
            dashboardPage.logout()
        } catch (e: Exception) {
            // Ignore logout errors during cleanup
        }
    }

    @Test
    @DisplayName("Should redirect unauthenticated user to login page")
    fun shouldRedirectUnauthenticatedUserToLoginPage() {
        // Given: unauthenticated user
        // When: user visits root URL
        Selenide.open("/")

        // Then: should redirect to login page with all required elements
        loginPage.shouldBeDisplayed()
            .shouldBeOnLoginUrl()
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    fun shouldLoginSuccessfullyWithValidCredentials() {
        // Given: user is on login page
        // When: user enters valid credentials and logs in
        loginPage.open()
            .loginWith(TestSecurityConfiguration.ALLOWED_EMAIL_1, TestSecurityConfiguration.TEST_PASSWORD)

        // Then: should redirect to dashboard
        dashboardPage.shouldBeDisplayed()
            .shouldBeOnDashboardUrl()
    }

    @Test
    @DisplayName("Should show error message with invalid credentials")
    fun shouldShowErrorMessageWithInvalidCredentials() {
        // Given: user is on login page
        // When: user enters invalid credentials and attempts login
        loginPage.open()
            .loginWith("invalid@example.com", "wrongpassword")

        // Then: should remain on login page with error message
        loginPage.shouldStillBeOnLoginPage()
            .shouldShowErrorMessage()
    }

    @Test
    @DisplayName("Should deny access for non-allowed email")
    fun shouldDenyAccessForNonAllowedEmail() {
        // Given: user is on login page
        // When: user enters credentials with non-allowed email
        loginPage.open()
            .loginWith(TestSecurityConfiguration.NOT_ALLOWED_EMAIL, TestSecurityConfiguration.TEST_PASSWORD)

        // Then: should remain on login page with error message
        loginPage.shouldStillBeOnLoginPage()
            .shouldShowErrorMessage()
    }

    @Test
    @DisplayName("Should show user profile when authenticated user accesses dashboard")
    fun shouldShowUserProfileWhenAuthenticatedUserAccessesDashboard() {
        // Given: user is authenticated
        loginPage.open()
            .loginWith(TestSecurityConfiguration.ALLOWED_EMAIL_1, TestSecurityConfiguration.TEST_PASSWORD)

        // When: user accesses dashboard
        dashboardPage.open()

        // Then: should show dashboard with user information
        dashboardPage.shouldBeDisplayed()
            .shouldShowUserEmail(TestSecurityConfiguration.ALLOWED_EMAIL_1)
    }

    @Test
    @DisplayName("Should redirect to login page after logout")
    fun shouldRedirectToLoginPageAfterLogout() {
        // Given: user is authenticated
        loginPage.open()
            .loginWith(TestSecurityConfiguration.ALLOWED_EMAIL_1, TestSecurityConfiguration.TEST_PASSWORD)

        // When: user logs out
        dashboardPage.logout()

        // Then: should redirect to login page
        loginPage.shouldBeDisplayed()
            .shouldBeOnLoginUrl()
    }

    @Test
    @DisplayName("Should redirect to login when accessing protected page after logout")
    fun shouldRedirectToLoginWhenAccessingProtectedPageAfterLogout() {
        // Given: user is authenticated and then logs out
        loginPage.open()
            .loginWith(TestSecurityConfiguration.ALLOWED_EMAIL_1, TestSecurityConfiguration.TEST_PASSWORD)
        dashboardPage.logout()

        // When: user tries to access protected page
        dashboardPage.open()

        // Then: should redirect to login page
        loginPage.shouldBeDisplayed()
            .shouldBeOnLoginUrl()
    }

    @Test
    @DisplayName("Should redirect authenticated user to dashboard when accessing login page")
    fun shouldRedirectAuthenticatedUserToDashboardWhenAccessingLoginPage() {
        // Given: user is authenticated
        loginPage.open()
            .loginWith(TestSecurityConfiguration.ALLOWED_EMAIL_1, TestSecurityConfiguration.TEST_PASSWORD)

        // When: user tries to access login page
        loginPage.open()

        // Then: should redirect to dashboard
        dashboardPage.shouldBeDisplayed()
            .shouldBeOnDashboardUrl()
    }

    @Test
    @DisplayName("Should maintain authentication state when navigating between pages")
    fun shouldMaintainAuthenticationStateWhenNavigatingBetweenPages() {
        // Given: user is authenticated
        loginPage.open()
            .loginWith(TestSecurityConfiguration.ALLOWED_EMAIL_1, TestSecurityConfiguration.TEST_PASSWORD)

        // When: user navigates to different pages
        dashboardPage.open()
            .shouldBeDisplayed()

        profilePage.open()
            .shouldBeDisplayed()

        settingsPage.open()
            .shouldBeDisplayed()

        // Then: user should remain authenticated and able to access dashboard again
        dashboardPage.open()
            .shouldBeDisplayed()
            .shouldBeOnDashboardUrl()
    }

    @Test
    @DisplayName("Should handle user switching between different accounts")
    fun shouldHandleUserSwitchingBetweenDifferentAccounts() {
        // Given: first user is authenticated
        loginPage.open()
            .loginWith(TestSecurityConfiguration.ALLOWED_EMAIL_1, TestSecurityConfiguration.TEST_PASSWORD)
        dashboardPage.shouldBeDisplayed()

        // When: user logs out and second user logs in
        dashboardPage.logout()
        loginPage.loginWith(TestSecurityConfiguration.ALLOWED_EMAIL_2, TestSecurityConfiguration.TEST_PASSWORD)

        // Then: second user should be authenticated with their email displayed
        dashboardPage.shouldBeDisplayed()
            .shouldShowUserEmail(TestSecurityConfiguration.ALLOWED_EMAIL_2)
    }

    @Test
    @DisplayName("Should handle browser back button navigation correctly")
    fun shouldHandleBrowserBackButtonNavigationCorrectly() {
        // Given: user is authenticated and on dashboard
        loginPage.open()
            .loginWith(TestSecurityConfiguration.ALLOWED_EMAIL_1, TestSecurityConfiguration.TEST_PASSWORD)
        dashboardPage.shouldBeDisplayed()

        // When: user navigates to profile and uses browser back button
        dashboardPage.navigateToProfile()
        profilePage.shouldBeDisplayed()
        
        dashboardPage.goBack()

        // Then: should be back on dashboard and still authenticated
        dashboardPage.shouldBeDisplayed()
            .shouldBeOnDashboardUrl()
    }
}
