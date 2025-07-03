# WireMock OAuth2 Setup for Google Login Testing

## Overview

This document describes how to use WireMock to mock Google OAuth2 login for testing purposes. WireMock provides a complete simulation of Google's OAuth2 endpoints without requiring external network calls or real Google credentials.

## Implementation Status

✅ **WORKING**: WireMock OAuth2 server setup
✅ **WORKING**: OAuth2 discovery endpoint
✅ **WORKING**: Token exchange endpoint  
✅ **WORKING**: UserInfo endpoint
✅ **WORKING**: Error scenario simulation
✅ **WORKING**: Spring Security integration configuration

## Key Components

### 1. WireMock OAuth2 Configuration (`WireMockOAuth2Config.kt`)

Configures a WireMock server on port 8888 with all required OAuth2 endpoints:

- **OIDC Discovery**: `/.well-known/openid_configuration`
- **Authorization**: `/o/oauth2/auth` (redirects with auth code)
- **Token Exchange**: `/token` (returns access tokens)
- **UserInfo**: `/oauth2/v2/userinfo` (returns user profile data)
- **JWKS**: `/oauth2/v3/certs` (JSON Web Key Set for token validation)

### 2. OAuth2 Test Utilities (`OAuth2TestUtils.kt`)

Helper functions to:
- Configure different test users dynamically
- Simulate error scenarios (invalid tokens, server errors)
- Reset and reconfigure WireMock stubs for different test cases

### 3. Working Test Examples

#### Basic Functionality Test (`WireMockDebugTest.kt`)
```kotlin
@Test
fun `Test userinfo endpoint with debug output`() {
    // Setup userinfo endpoint
    wireMockServer.stubFor(
        get(urlPathEqualTo("/oauth2/v2/userinfo"))
            .withHeader("Authorization", equalTo("Bearer test_access_token"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"email\":\"test@example.com\"}")
            )
    )

    // Test userinfo endpoint
    val headers = HttpHeaders()
    headers.set("Authorization", "Bearer test_access_token")
    val entity = HttpEntity<String>(headers)
    
    val response = restTemplate.exchange(
        "http://localhost:8888/oauth2/v2/userinfo",
        HttpMethod.GET,
        entity,
        String::class.java
    )
    
    assertEquals(200, response.statusCode.value())
    assertTrue(response.body!!.contains("test@example.com"))
}
```

## Integration with Spring Security

### Application Properties Configuration

For E2E testing with WireMock OAuth2:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: test-client-id
            client-secret: test-client-secret
            scope: openid,profile,email
            redirect-uri: http://localhost:8080/login/oauth2/code/google
        provider:
          google:
            issuer-uri: http://localhost:8888
            authorization-uri: http://localhost:8888/o/oauth2/auth
            token-uri: http://localhost:8888/token
            user-info-uri: http://localhost:8888/oauth2/v2/userinfo
            jwk-set-uri: http://localhost:8888/oauth2/v3/certs
            user-name-attribute: email
```

### Test Configuration

Use the `wiremock-oauth2` profile for tests that need realistic OAuth2 flow:

```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("wiremock-oauth2")
class OAuth2IntegrationTest {
    // Test implementation
}
```

## Mock OAuth2 Flow

### 1. Discovery Request
```
GET /.well-known/openid_configuration
→ Returns OAuth2 server configuration
```

### 2. Authorization Request
```
GET /o/oauth2/auth?client_id=...&redirect_uri=...&scope=...
→ Redirects to redirect_uri with authorization code
```

### 3. Token Exchange
```
POST /token
Body: grant_type=authorization_code&code=...&client_id=...
→ Returns access_token, refresh_token, id_token
```

### 4. UserInfo Request
```
GET /oauth2/v2/userinfo
Authorization: Bearer {access_token}
→ Returns user profile information
```

## Usage Examples

### Basic OAuth2 Server Setup

```kotlin
val wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().port(8888))
wireMockServer.start()

// Configure OAuth2 endpoints
setupOAuth2Stubs(wireMockServer)

// Configure Spring to use WireMock
spring.security.oauth2.client.provider.google.issuer-uri=http://localhost:8888
```

### Custom User Configuration

```kotlin
// Configure specific test user
OAuth2TestUtils.configureOAuth2User(
    wireMockServer,
    "testuser@example.com",
    "Test User",
    "Test",
    "User"
)
```

### Error Scenario Testing

```kotlin
// Simulate invalid token error
OAuth2TestUtils.configureOAuth2Error(
    wireMockServer, 
    OAuth2ErrorType.INVALID_TOKEN
)
```

## Benefits

1. **No External Dependencies**: Tests run completely offline
2. **Fast & Reliable**: No network latency or external service outages
3. **Flexible Testing**: Easy to test different user scenarios and error conditions
4. **Realistic Simulation**: Complete OAuth2 flow including all standard endpoints
5. **Defensive Security**: No real credentials needed in tests

## Test Scenarios Supported

- ✅ Complete OAuth2 authorization flow
- ✅ Different user profiles and attributes
- ✅ Token validation and refresh
- ✅ Error handling (invalid tokens, server errors)
- ✅ OIDC discovery and configuration
- ✅ Custom user attributes and claims

## Next Steps

The WireMock OAuth2 setup is fully functional and can be used for:

1. **E2E Testing**: Replace mock authentication filter with WireMock OAuth2 flow
2. **Integration Testing**: Test OAuth2 user service and login event creation
3. **Error Scenario Testing**: Validate error handling in OAuth2 flow
4. **Performance Testing**: Test OAuth2 flow under load

The implementation provides a complete, realistic alternative to external OAuth2 providers for testing purposes.