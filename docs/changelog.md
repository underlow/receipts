# Changelog

## 2025-07-02
- Implemented OAuth2 login flow with Google:
    - Configured `application.yaml` for Google OAuth2.
    - Created `SecurityConfig.kt` for Spring Security setup.
    - Implemented `User` and `LoginEvent` entities and their repositories.
    - Developed `CustomOAuth2UserService` to handle user provisioning and login event logging.
    - Created `login.html` for the login page.
- Replaced Spring Data JPA with Spring Data JDBC and integrated Liquibase:
    - Updated `build.gradle.kts` to include `liquibase-core`.
    - Configured Liquibase in `application.yaml` and `application-test.yaml`.
    - Created initial Liquibase changelog files for `users` and `login_events` tables.
    - Removed `schema.sql` for the main application.
- Rewrote Liquibase changelog from YAML to SQL.
- Reworked repositories to use JdbcTemplate instead of Spring Data JDBC:
    - Modified `UserRepository` and `LoginEventRepository` interfaces.
    - Created `UserRepositoryImpl` and `LoginEventRepositoryImpl` using `JdbcTemplate`.
    - Removed `@Repository` annotations from implementation classes.
    - Removed `@Table` and `@Id` annotations from model classes.
    - Updated `build.gradle.kts` to use `spring-boot-starter-jdbc`.
    - Created `JdbcConfig` to define repository beans.
    - Configured `ReceiptApplication` and `ReceiptApplicationTests` to import `JdbcConfig`.