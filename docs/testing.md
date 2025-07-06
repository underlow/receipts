# Comprehensive Testing Strategy

## 1. Overview

This document provides a comprehensive guide to the testing strategy for this application. 

## 2. Testing Frameworks and Libraries

The project leverages a comprehensive set of testing tools to cover all layers of the application.

*   **JUnit 5:** The core testing framework for writing unit, integration, and end-to-end tests.
*   **Spring Boot Starter Test:** Provides essential Spring Boot testing infrastructure, including auto-configuration, and brings in libraries like AssertJ.
*   **Kotlin Test:** Provides Kotlin-specific testing utilities, extensions for JUnit 5, and idiomatic assertion functions.
*   **Mockito-Kotlin:** The primary mocking framework used for creating mock objects and isolating components in unit and integration tests with a Kotlin-friendly syntax.
*   **Spring Security Test:** Provides utilities for testing Spring Security-secured applications, allowing for mock authentication (e.g., `@WithMockUser`, `SecurityMockMvcRequestPostProcessors`).
*   **Testcontainers:** Manages lightweight, disposable instances of external dependencies like databases (PostgreSQL) and browsers (Selenium) in Docker containers, ensuring clean and isolated environments for integration and E2E tests.
*   **Selenide:** A concise and powerful UI testing framework built on Selenium WebDriver, used for writing readable and stable E2E tests.
*   **H2 Database:** An in-memory database used for fast-executing unit and integration tests.

## 3. Test Structure and Layers

Tests are organized into a multi-layered structure that mirrors the main source code, ensuring clarity and targeted testing.

### 3.1. Directory Structure

```
src/test/kotlin/
└── me/underlow/receipt/
    ├── ReceiptApplicationTests.kt          # Spring Boot context test
    ├── config/
    │   └── TestSecurityConfig.kt          # Security configuration for E2E tests
    ├── controller/                        # Controller integration tests (@WebMvcTest)
    ├── e2e/                              # End-to-end tests (*E2ETest.kt)
    ├── integration/                      # Full integration tests (*IntegrationTest.kt)
    ├── model/                           # Domain model unit tests
    ├── repository/                      # Repository layer tests
    └── service/                         # Service layer unit tests
```

### 3.2. Test Types

The project employs different types of tests, each with a specific purpose and scope.

*   **Unit Tests (`*Test.kt`):**
    *   **Purpose:** To test individual components (e.g., domain models, services, utility functions) in isolation.
    *   **Characteristics:** Fast, highly isolated, and use Mockito to mock all external dependencies. They typically use the H2 in-memory database if a repository layer is involved.

*   **Controller Integration Tests (`@WebMvcTest`):**
    *   **Purpose:** To test the web layer (controllers), focusing on HTTP request handling, data binding, serialization, and security without loading the full application context.
    *   **Characteristics:** Uses `@WebMvcTest` to load only the web-related components. Dependencies like services are mocked using `@MockBean`.

*   **Integration Tests (`*IntegrationTest.kt`):**
    *   **Purpose:** To test the interaction between multiple components or layers, such as a service interacting with a real database repository.
    *   **Characteristics:** Uses `@SpringBootTest` to load the full Spring context. Can use either the H2 in-memory database or Testcontainers for a more realistic environment.

*   **End-to-End (E2E) Tests (`*E2ETest.kt`):**
    *   **Purpose:** To test the entire application flow from a user's perspective, including UI interactions (via Selenide), backend logic, and database persistence (via Testcontainers).
    *   **Characteristics:** The most comprehensive and slowest tests. They run the full application on a random port (`@SpringBootTest(webEnvironment = RANDOM_PORT)`) and use the `e2e` Spring profile.

## 4. Test Naming and Conventions

All tests adhere to the following principles for clarity and maintainability.

*   **Given-When-Then Structure:** Test methods are structured and commented using the "Given-When-Then" pattern to clearly define the test's setup, action, and expected outcome.

*   **Descriptive Test Naming:** Test method names are written in plain English using backticks and follow the `given [state] when [action] then [outcome]` pattern. This makes test reports highly readable.

    ```kotlin
    @Test
    fun `given valid file upload when authenticated user uploads then should return success response`() {
        // Given: An authenticated user and a valid file
        // ... setup ...

        // When: The user uploads the file
        // ... action ...

        // Then: The system should process the file and return a success response
        // ... assertions ...
    }
    ```

## 5. Test Data and Environment Management

Ensuring a clean, consistent, and predictable test environment is crucial.

### 5.1. Database Setup
*   **Unit/Integration Tests:** Use the **H2 in-memory database**, configured in `application-test.yaml`. This provides fast startup and automatic cleanup.
*   **E2E Tests:** Use **PostgreSQL via Testcontainers**. A fresh, disposable database instance is created for each test run, ensuring complete isolation.

    ```kotlin
    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:15-alpine")

        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }
    ```

### 5.2. Data Seeding and Cleanup
*   **Mocking:** In unit and controller tests, dependencies are mocked with `@MockBean` and their behavior is defined using `whenever()`.
*   **Database Cleanup:** For integration and E2E tests, `JdbcTemplate` is used in a `@BeforeEach` method to delete data from tables, ensuring each test starts with a known, clean state.

    ```kotlin
    @BeforeEach
    fun setUp() {
        cleanupTestData() // Deletes from all relevant tables
        setupTestUser()   // Inserts a standard user for tests
    }
    ```

### 5.3. File System Testing
The `@TempDir` JUnit 5 annotation is used to create temporary directories for tests that involve file system operations, ensuring that test files do not pollute the project directory and are cleaned up automatically.

## 6. Authentication and Security Testing

Authentication is handled differently based on the test type to balance realism and simplicity.

*   **Unit/Controller Tests:** Mock authentication is used to test secured endpoints. A helper function creates a mock `OAuth2AuthenticationToken`, simulating an authenticated user without a full security context.

    ```kotlin
    // Helper function to create a mock principal
    private fun createMockPrincipal(email: String): OAuth2AuthenticationToken {
        // ... implementation to create a mock OidcUser and token
    }
    ```

*   **E2E Tests:** To simplify UI tests, a special `TestSecurityConfig` is activated via the `e2e` Spring profile (`@ActiveProfiles("e2e")`). This configuration disables CSRF and permits all requests, bypassing the real OAuth2 login flow and allowing Selenide to directly access protected pages.

    ```kotlin
    @TestConfiguration
    @Profile("e2e")
    class TestSecurityConfig {
        @Bean
        fun testSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
            http.authorizeHttpRequests { it.anyRequest().permitAll() }
                .csrf { it.disable() }
            return http.build()
        }
    }
    ```

## 7. End-to-End Testing with Selenide

E2E tests provide the highest confidence in the application's functionality.

*   **Browser Configuration:** Selenide browser settings (e.g., headless mode, timeouts) are configured once in a `@BeforeAll` block.
*   **UI Interaction:**
    *   `open("http://localhost:$port/login")` navigates to a page.
    *   `$` is used to select elements (e.g., `$`("[data-testid=login-button]")).
    *   `.should()` is used for assertions (e.g., `.should(appear)`, `.shouldHave(text("..."))`).
*   **Session Management:** `WebDriverRunner.clearBrowserCache()` is called in `@BeforeEach` to ensure a clean browser session for each test. `closeWebDriver()` is called in `@AfterAll` to shut down the browser.

## 8. How to Write and Run Tests

### 8.1. Writing a New Test
1.  **Choose the Right Test Type:** Decide if you need a Unit, Integration, or E2E test based on the component's scope.
2.  **Create the Test File:** Place the new file in the appropriate package (e.g., `src/test/.../service/MyNewServiceTest.kt`).
3.  **Set up the Test Class:** Add necessary annotations like `@SpringBootTest`, `@WebMvcTest`, `@Testcontainers`, or `@ActiveProfiles("e2e")`.
4.  **Write the Test Method:**
    *   Use `@Test` and a descriptive name in backticks.
    *   **Given:** Set up mocks, prepare test data in the database, or create test files.
    *   **When:** Call the method or perform the action being tested.
    *   **Then:** Use assertions (`assertEquals`, `assertTrue`, etc.) to verify the outcome and `verify()` to check mock interactions.
5.  **Manage Dependencies:** Use `@MockBean` for Spring-managed tests or `@DynamicPropertySource` to connect to Testcontainers.

### 8.2. Running Tests
Tests can be executed using the Gradle wrapper from the command line.

```bash
# Run all tests
./gradlew test

# Run a specific test class
./gradlew test --tests "me.underlow.receipt.controller.FileUploadControllerTest"

# Run tests with a specific Spring profile
./gradlew test -Dspring.profiles.active=e2e
```

## 9. Best Practices and Guidelines

### 9.1. What to Test
*   **Business Logic:** Core application functionality and complex algorithms.
*   **Integration Points:** Database operations, file system interactions, and calls to external services.
*   **Error Handling:** Exception scenarios, validation failures, and edge cases.
*   **Data Flow:** Complete end-to-end user journeys and security flows.

### 9.2. What NOT to Test
*   **Trivial Code:** Simple getters, setters, or constructors.
*   **Framework Code:** Spring Boot auto-configuration or library-internal behavior.
*   **Third-Party Libraries:** Assume external libraries are already tested.

### 9.3. Test Quality Standards
*   **Independent & Repeatable:** Tests must not depend on each other and must produce consistent results.
*   **Fast:** Unit tests should execute quickly to provide rapid feedback.
*   **Clear:** Assertions should be specific and test names should be descriptive.
*   **Clean:** Always clean up resources (database state, files) after a test to ensure isolation.


## 10. Notes
* do not put  spring.security.oauth2.provider.google.issuer-uri into properties - app will try to connect to wiremock before it is initialized
