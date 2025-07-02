# Detailed Development Plan for OAuth2 Login Flow

## Motivation:
To provide secure and convenient user authentication using Google as an OAuth2 provider, ensuring that only authenticated users can access protected resources while maintaining a minimal user profile and logging login events for auditing.

## What is planning:
This plan outlines the steps to integrate Google OAuth2 with Spring Security, define access rules for different routes, handle user provisioning on first login, and record login events. It also includes creating a basic login page.

## Steps:

1.  **Configure Google OAuth2 in `application.yaml`:**
    *   Add necessary properties for Google OAuth2 client ID, client secret, and redirect URI.
    *   Ensure these can be loaded from environment variables for different environments.

2.  **Configure Spring Security:**
    *   Create a Spring Security configuration class (e.g., `SecurityConfig.kt`).
    *   Enable OAuth2 login.
    *   Define URL authorization rules:
        *   `/login`, `/static/**` (for CSS/JS/images) should be publicly accessible.
        *   All other routes should require authentication.
    *   Configure a custom `OAuth2UserService` to handle user details.

3.  **Implement Custom `OAuth2UserService`:**
    *   Create a service (e.g., `CustomOAuth2UserService.kt`) that extends `DefaultOAuth2UserService`.
    *   Override the `loadUser` method to:
        *   Fetch user attributes from the OAuth2 provider.
        *   Check if the user already exists in the database based on their email.
        *   If it's a new user, persist their minimal profile (email, name) to a `User` entity.
        *   Return a `UserDetails` object.

4.  **Create `User` Entity and Repository:**
    *   Define a simple `User` entity (e.g., `User.kt`) with fields for `id`, `email`, `name`, `createdAt`, `lastLoginAt`.
    *   Create a corresponding Spring Data JPA repository (e.g., `UserRepository.kt`).

5.  **Implement Login Event Logging:**
    *   Create a `LoginEvent` entity (e.g., `LoginEvent.kt`) with fields for `id`, `userId`, `timestamp`, `ipAddress`.
    *   Create a `LoginEventRepository.kt`.
    *   In the `CustomOAuth2UserService` or a separate event listener, record a `LoginEvent` after successful authentication, including the user ID and timestamp.

6.  **Create a Minimal Login Web Page:**
    *   Create a Thymeleaf template (e.g., `login.html`) with a link or button to initiate the Google OAuth2 login flow.
    *   Ensure it's accessible via the `/login` route.

7.  **Testing:**
    *   Write integration tests to verify the OAuth2 flow, user persistence, and login event logging.
    *   Test access to public and secured routes.
