# E2E Integration Test Plan with TestContainers & Real Browser

## Motivation
Create true end-to-end tests that verify the complete OAuth2 login flow using a real browser, real PostgreSQL database, and mock OAuth2 server - simulating actual user interaction with the application.

## Test Strategy
1. **TestContainers PostgreSQL** for real database operations
2. **Mock OAuth2 Server Container** to simulate Google OAuth2 endpoints
3. **Selenide** for simplified browser automation (easier than Selenium)
4. **Full user journey**: Visit app → Click login → OAuth2 flow → Dashboard access
5. **Database verification** after each user action

## Implementation Steps

### 1. Add Dependencies
- TestContainers for PostgreSQL
- TestContainers generic container for mock OAuth2 server
- Selenide for browser automation (simpler API than Selenium)
- WireMock or custom mock OAuth2 server

### 2. Set Up Test Infrastructure
- PostgreSQL TestContainer with proper schema
- Mock OAuth2 Server TestContainer (or WireMock)
- Selenide configuration (Chrome headless)
- Dynamic property configuration for OAuth2 endpoints

### 3. Create E2E Test Class
- `@SpringBootTest(webEnvironment = RANDOM_PORT)`
- `@Testcontainers` with PostgreSQL and OAuth2 mock server
- Configure Selenide with proper timeouts and capabilities
- Use `@DynamicPropertySource` to point OAuth2 config to mock server

### 4. Mock OAuth2 Server Setup
- Container serving OAuth2 endpoints: `/authorize`, `/token`, `/userinfo`
- Mock successful authentication flow with test user data
- Return proper OIDC tokens and user attributes
- Handle redirect back to application with authorization code

### 5. E2E Test Scenarios
- **Full login flow**: Visit app → Click "Login with Google" → Complete OAuth2 → Access dashboard
- **User persistence**: Verify new user created in PostgreSQL database
- **Login events**: Verify login event recorded with correct timestamp
- **Session management**: Verify user stays logged in across page navigation
- **Logout flow**: Test logout functionality and session cleanup

### 6. Database Verification
- Query PostgreSQL container directly after each test step
- Verify user records with correct email, name, timestamps
- Verify login event records with proper foreign key relationships
- Assert database state matches expected user journey

## Expected Outcomes
- Complete browser-based E2E test covering real user interaction
- Real PostgreSQL database operations with actual schema
- Mocked OAuth2 provider that behaves like Google
- High confidence that production deployment will work correctly
- Automated verification of database persistence and user management

## Selenide Advantages over Selenium
- Simpler API: `$(selector).click()` vs complex WebDriver setup
- Built-in waits: Automatically waits for elements to appear
- Better error messages: Clear screenshots and stack traces
- Less boilerplate: No need for explicit WebDriver management
- Fluent API: More readable test code