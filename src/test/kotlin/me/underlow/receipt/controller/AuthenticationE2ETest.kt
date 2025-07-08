package me.underlow.receipt.controller

import com.codeborne.selenide.Selenide
import com.codeborne.selenide.Condition
import me.underlow.receipt.config.BaseE2ETest
import me.underlow.receipt.config.SelenideConfiguration
import me.underlow.receipt.config.TestSecurityConfiguration
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestPropertySource

/**
 * End-to-end tests for authentication flow using test security configuration.
 * Tests complete login/logout flow using Selenide browser automation.
 * Uses TestSecurityConfiguration to bypass OAuth2 for testing.
 */
@TestPropertySource(
    properties = [
        "ALLOWED_EMAILS=allowed1@example.com,allowed2@example.com"
    ]
)
class AuthenticationE2ETest : BaseE2ETest() {

    @Test
    @DisplayName("Given user visits root URL when unauthenticated then should redirect to login page")
    fun `given user visits root URL when unauthenticated then should redirect to login page`() {
        // Given: unauthenticated user
        // When: user visits root URL
        Selenide.open("/")

        // Then: should redirect to login page
        waitForPageLoad()
        assert(isOnLoginPage()) { "User should be redirected to login page" }

        // And: login page should contain required elements
        Selenide.`$`("input[name='username']").shouldBe(Condition.visible)
        Selenide.`$`("input[name='password']").shouldBe(Condition.visible)
        Selenide.`$`("button[type='submit']").shouldBe(Condition.visible)
    }

    @Test
    @DisplayName("Given user enters valid credentials when logging in then should redirect to dashboard")
    fun `given user enters valid credentials when logging in then should redirect to dashboard`() {
        // Given: user is on login page
        Selenide.open("/login")
        waitForPageLoad()

        // When: user enters valid credentials
        performLogin(TestSecurityConfiguration.ALLOWED_EMAIL_1, TestSecurityConfiguration.TEST_PASSWORD)

        // Then: should redirect to dashboard
        waitForPageLoad()
        assert(isOnDashboardPage()) { "User should be redirected to dashboard after successful login" }
    }

    @Test
    @DisplayName("Given user enters invalid credentials when logging in then should show error message")
    fun `given user enters invalid credentials when logging in then should show error message`() {
        // Given: user is on login page
        Selenide.open("/login")
        waitForPageLoad()

        // When: user enters invalid credentials
        performLogin("invalid@example.com", "wrongpassword")

        // Then: should remain on login page with error
        waitForPageLoad()
        assert(isOnLoginPage()) { "User should remain on login page after failed login" }

        // And: should show error message
        Selenide.`$`(".alert-danger").shouldBe(Condition.visible)
    }

    @Test
    @DisplayName("Given user with non-allowed email when logging in then should show access denied error")
    fun `given user with non-allowed email when logging in then should show access denied error`() {
        // Given: user is on login page
        Selenide.open("/login")
        waitForPageLoad()

        // When: user enters credentials with non-allowed email
        performLogin(TestSecurityConfiguration.NOT_ALLOWED_EMAIL, TestSecurityConfiguration.TEST_PASSWORD)

        // Then: should remain on login page with error
        waitForPageLoad()
        assert(isOnLoginPage()) { "User should remain on login page after access denied" }

        // And: should show appropriate error message
        Selenide.`$`(".alert-danger").shouldBe(Condition.visible)
    }

    @Test
    @DisplayName("Given authenticated user when accessing dashboard then should show user profile")
    fun `given authenticated user when accessing dashboard then should show user profile`() {
        // Given: user is authenticated
        performLoginWithAllowedUser()

        // When: user accesses dashboard
        Selenide.open("/dashboard")
        waitForPageLoad()

        // Then: should show dashboard with user information
        assert(isOnDashboardPage()) { "User should be on dashboard page" }

        // And: user profile information should be visible
        Selenide.`$`("body").shouldHave(Condition.text(TestSecurityConfiguration.ALLOWED_EMAIL_1))
    }

    @Test
    @DisplayName("Given authenticated user when logging out then should redirect to login page")
    fun `given authenticated user when logging out then should redirect to login page`() {
        // Given: user is authenticated
        performLoginWithAllowedUser()

        // When: user clicks logout
        performLogout()

        // Then: should redirect to login page
        waitForPageLoad()
        assert(isOnLoginPage()) { "User should be redirected to login page after logout" }
    }

    @Test
    @DisplayName("Given user logs out when accessing protected page then should redirect to login")
    fun `given user logs out when accessing protected page then should redirect to login`() {
        // Given: user is authenticated
        performLoginWithAllowedUser()

        // When: user logs out
        performLogout()

        // And: tries to access protected page
        Selenide.open("/dashboard")
        waitForPageLoad()

        // Then: should redirect to login page
        assert(isOnLoginPage()) { "User should be redirected to login page when accessing protected page after logout" }
    }

    @Test
    @DisplayName("Given authenticated user when accessing login page then should redirect to dashboard")
    fun `given authenticated user when accessing login page then should redirect to dashboard`() {
        // Given: user is authenticated
        performLoginWithAllowedUser()

        // When: user tries to access login page
        Selenide.open("/login")
        waitForPageLoad()

        // Then: should redirect to dashboard
        assert(isOnDashboardPage()) { "Authenticated user should be redirected to dashboard when accessing login page" }
    }

    @Test
    @DisplayName("Given user session when navigating between pages then should maintain authentication state")
    fun `given user session when navigating between pages then should maintain authentication state`() {
        // Given: user is authenticated
        performLoginWithAllowedUser()

        // When: user navigates to different pages
        Selenide.open("/dashboard")
        waitForPageLoad()
        assert(isOnDashboardPage()) { "User should be on dashboard page" }

        Selenide.open("/profile")
        waitForPageLoad()
        Selenide.`$`("h1").shouldHave(Condition.text("User Profile"))

        Selenide.open("/settings")
        waitForPageLoad()
        Selenide.`$`("h1").shouldHave(Condition.text("Settings"))

        // Then: user should remain authenticated throughout navigation
        // And: should be able to access dashboard again
        Selenide.open("/dashboard")
        waitForPageLoad()
        assert(isOnDashboardPage()) { "User should still be authenticated and able to access dashboard" }
    }

    @Test
    @DisplayName("Given multiple users when logging in with different accounts then should handle user switching")
    fun `given multiple users when logging in with different accounts then should handle user switching`() {
        // Given: first user is authenticated
        performLogin(TestSecurityConfiguration.ALLOWED_EMAIL_1, TestSecurityConfiguration.TEST_PASSWORD)
        waitForPageLoad()
        assert(isOnDashboardPage()) { "First user should be authenticated" }

        // When: user logs out and second user logs in
        performLogout()
        performLogin(TestSecurityConfiguration.ALLOWED_EMAIL_2, TestSecurityConfiguration.TEST_PASSWORD)
        waitForPageLoad()

        // Then: second user should be authenticated
        assert(isOnDashboardPage()) { "Second user should be authenticated" }

        // And: should show second user's email
        Selenide.`$`("body").shouldHave(Condition.text(TestSecurityConfiguration.ALLOWED_EMAIL_2))
    }

    @Test
    @DisplayName("Given browser back button when user is authenticated then should handle navigation correctly")
    fun `given browser back button when user is authenticated then should handle navigation correctly`() {
        // Given: user is authenticated and on dashboard
        performLoginWithAllowedUser()
        Selenide.open("/dashboard")
        waitForPageLoad()

        // When: user navigates to profile
        Selenide.open("/profile")
        waitForPageLoad()

        // And: uses browser back button
        Selenide.back()
        waitForPageLoad()

        // Then: should be back on dashboard and still authenticated
        assert(isOnDashboardPage()) { "User should be back on dashboard after browser back button" }
    }
}
