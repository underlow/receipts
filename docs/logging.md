## Logging

### General

1. Use kotlin-logging framework 
2. Use Appropriate Log Levels:
   * ERROR: For critical errors that require immediate attention. The application may not be able to recover from the error.
   * WARN: For potential problems or unexpected situations that are not critical but should be monitored.
   * INFO: For high-level information about the application's state and major events (e.g., application startup, a new request being handled).
   * DEBUG: For fine-grained information that is useful for debugging.
   * TRACE: For even more detailed information than DEBUG, usually for tracing the execution path of a method. 
3. Don't Log Sensitive Information: Be extremely careful not to log sensitive data like passwords, API keys
4. Keep Log Messages Concise and Informative: A good log message should be easy to understand and provide enough information to identify the source of
      the log and the event that occurred.
5. Log the "Why," Not Just the "What": An error message like NullPointerException is useless. A message like ERROR - Failed to process bill 'B-789'
      because the associated User object was null is invaluable.


### INFO: The Story of Your Application's Happy Path
Use INFO for high-level, significant events in the normal operation of the application. These logs should tell a story that someone can follow without
reading the code.

#### When to Log:

* Application Lifecycle: On startup and shutdown. Log the version, active profiles, and critical configuration values (e.g., "Starting application
  v1.2.3 with profiles: prod, aws").
* Request Boundaries: When a request enters and leaves the system. A common practice is to have a filter or interceptor that logs the start and end of
  every API request.
   * Start: INFO - GET /api/bills/123 - Request received from user 'john.doe'
   * End: INFO - GET /api/bills/123 - Responded with status 200 in 45ms
* Major Business Milestones: When a significant business action is completed successfully.
   * INFO - User 'jane.doe' created successfully with id '456'.
   * INFO - Bill 'B-789' for user '456' was paid successfully.
   * INFO - File 'receipt-abc.pdf' was processed and dispatched to BillService.
* Background Job Status: When a scheduled or asynchronous job starts, finishes, or hits a major checkpoint.
   * INFO - Starting daily file cleanup job.
   * INFO - Daily file cleanup job finished. Deleted 15 old files.

### WARN: Unexpected, but Recoverable Situations
Use WARN for events that are not errors but are outside of normal parameters. They indicate a potential problem or a situation that might lead to an
error in the future.

#### When to Log:
* 
* Retries: When an operation fails but you are retrying it. This is common for calls to external services.
   * WARN - Failed to connect to OCR service. Retrying in 5 seconds (Attempt 1 of 3).
* Invalid, but Handled, Input: When a user provides bad input that your application handles gracefully.
   * WARN - User 'guest' attempted to access unauthorized resource '/admin'. Redirecting to login.
* Approaching Resource Limits: If a resource is nearing its capacity.
   * WARN - Database connection pool is at 90% capacity.
* Use of Deprecated Features: When a request uses a deprecated API endpoint or feature.
   * WARN - Request received for deprecated endpoint '/api/v1/receipts'. Client should upgrade to '/api/v2/receipts'.

## ERROR: Problems That Require Attention
Use ERROR for significant failures that prevent a specific operation from completing. These are issues that typically require a developer or operator
to investigate.

#### When to Log:
* Unhandled Exceptions: Your global exception handler should catch any uncaught exceptions and log them as ERROR.
* Operation Failure: When a critical operation fails and cannot be recovered.
   * ERROR - Failed to process payment for bill 'B-789' after 3 retries.
   * ERROR - Could not save user 'new.user' to the database due to a unique constraint violation.
* Loss of Connectivity: When the application loses connection to a critical resource (like a database or message queue) and cannot re-establish it.
   * ERROR - Lost connection to primary database. Switching to read-only mode.
* Security Violations: For clear security-related issues.
   * ERROR - Invalid JWT signature detected from IP 123.45.67.89.

What to include in an ERROR log:
Always include the full stack trace and as much context as possible (from MDC: request ID, user ID, etc.) to make debugging possible.

### DEBUG / TRACE: For Developers Only

Use DEBUG and TRACE for detailed information useful only for developers during active troubleshooting. These logs should generally be disabled in a
production environment but can be enabled temporarily to diagnose a specific issue.

When to Log:


* Method Flow: Entering and exiting complex methods.
   * DEBUG - Entering BillService.calculateTotal() with billId 'B-789'.
* Variable State: Dumping the state of important variables or objects.
   * DEBUG - User object before save: User(name='test', ...)
* External System Payloads: The full request/response body sent to/received from an external service (be careful not to log sensitive data!).
   * DEBUG - Request to OCR service: { 'file': '...' }
