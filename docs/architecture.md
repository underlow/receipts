# Architecture Overview

## 1. Introduction
This document provides a high-level overview of the system architecture for the household-expense tracker application. It outlines the main components, data flow, deployment topology, and key technical considerations.

## 2. Authentication & Authorization Architecture

### 2.1 Authentication Flow
The system uses Google OAuth 2.0 for user authentication with email-based authorization:

### 2.2 Security Components
- **SecurityConfiguration**: Spring Security configuration with OAuth2 client setup
- **CustomOAuth2UserService**: Handles Google OAuth user info processing and email validation
- **UserService**: Manages user data and allowlist validation logic
- **AuthenticationSuccessHandler**: Handles post-login redirects
- **AuthenticationFailureHandler**: Handles authentication errors

### 2.3 Session Management
- HTTP sessions stored server-side
- Session timeout configured via Spring Security
- Secure logout endpoint clears session and redirects to login

### 2.4 Environment Configuration
- `ALLOWED_EMAILS`: Comma-separated list of authorized email addresses
- Google OAuth credentials configured in application.yaml
