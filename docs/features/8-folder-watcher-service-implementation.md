# Feature Implementation: Folder-Watcher Service (Item 8)

## Motivation

The folder-watcher service provides seamless, automated file ingestion for the receipt processing application. This eliminates manual file uploads for users who prefer to drop files in a watched directory, enabling a more streamlined workflow for bulk receipt processing.

## Feature Description

Implemented a comprehensive background service that automatically monitors the inbox directory for new receipt/bill files and processes them for subsequent OCR analysis and user review.

### Core Components

1. **FileWatcherService**: Scheduled task that polls the inbox directory every 30 seconds
2. **FileProcessingService**: Core file operations including checksum calculation, duplicate detection, file movement
3. **IncomingFile Entity**: Data model for files detected in inbox with metadata tracking
4. **SchedulingConfig**: Spring configuration enabling scheduled task support

## Definition of Done

### Automated File Detection ✅
- ✅ Service automatically scans inbox directory every 30 seconds using Spring's `@Scheduled` annotation
- ✅ Detects new files while filtering out hidden files, directories, and unsupported formats
- ✅ Validates file readability and format (PDF, JPG, JPEG, PNG, GIF, BMP, TIFF)
- ✅ Graceful handling of locked files and permission issues

### File Processing Pipeline ✅
- ✅ SHA-256 checksum calculation for robust duplicate detection
- ✅ Organized storage with date-prefixed naming: `/attachments/yyyy-MM-dd-filename`
- ✅ Automatic conflict resolution with incremental suffixes (-1, -2, etc.)
- ✅ Atomic file operations ensuring data integrity during processing
- ✅ Comprehensive error handling and logging for monitoring

### Database Integration ✅
- ✅ IncomingFile entity with validation annotations and Spring Data JDBC compatibility
- ✅ Repository layer following existing JdbcTemplate patterns
- ✅ Database migration (003-incoming-files.sql) with proper constraints and indexes
- ✅ Integration with Spring configuration via JdbcConfig

### Testing Coverage ✅
- ✅ Unit tests for IncomingFile entity creation and validation
- ✅ Service tests for FileProcessingService functionality
- ✅ Integration tests for FileWatcherService workflow
- ✅ Edge case testing for duplicates, file conflicts, and error conditions
- ✅ Test-driven development approach ensuring reliability

## Implementation Details

### Entity Model

```kotlin
data class IncomingFile(
    val id: Long? = null,
    val filename: String,
    val filePath: String, 
    val uploadDate: LocalDateTime,
    val status: BillStatus,
    val checksum: String,
    val userId: Long
)
```

### Service Architecture

**FileWatcherService**:
- Scheduled execution every 30 seconds
- Directory scanning with file filtering
- Integration with FileProcessingService
- Manual trigger capability for testing

**FileProcessingService**:
- File validation and readiness checking
- SHA-256 checksum calculation
- Unique filename generation with conflict resolution
- Atomic file movement operations
- IncomingFile entity creation

### Database Schema

```sql
CREATE TABLE incoming_files (
    id          BIGSERIAL PRIMARY KEY,
    filename    VARCHAR(500) NOT NULL,
    file_path   VARCHAR(1000) NOT NULL,
    upload_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status      VARCHAR(20)  NOT NULL CHECK (status IN ('PENDING', 'PROCESSING', 'APPROVED', 'REJECTED')),
    checksum    VARCHAR(64)  NOT NULL UNIQUE,
    user_id     BIGINT       NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id)
);
```

### File Storage Strategy

**Path Format**: `/attachments/yyyy-MM-dd-filename`
- Examples: 
  - `2025-07-02-receipt.pdf`
  - `2025-07-02-receipt-1.pdf` (duplicate)
  - `2025-07-02-receipt-2.pdf` (second duplicate)

**Benefits**:
- Flat directory structure for easy browsing
- Date organization for chronological sorting
- No file overwrites with automatic conflict resolution
- Consistent naming convention across the application

## Workflow Integration

### File Ingestion Pipeline
1. **Detection**: FileWatcherService scans inbox directory
2. **Validation**: Check file format, readability, and size
3. **Deduplication**: Calculate SHA-256 checksum and check for existing files
4. **Processing**: Move file to storage with organized naming
5. **Registration**: Create IncomingFile entity in PENDING status
6. **Handoff**: File ready for OCR processing (Item 10-12) and user review (Item 13-14)

### Status Workflow
- **PENDING**: Initial status after file ingestion
- **PROCESSING**: During OCR analysis (future implementation)
- **APPROVED**: After user review and acceptance
- **REJECTED**: If file is invalid or rejected by user

## Technical Specifications

### Configuration
- **Inbox Path**: Configurable via `receipts.inbox-path` property
- **Attachments Path**: Configurable via `receipts.attachments-path` property
- **Scan Interval**: Fixed 30-second interval (configurable if needed)

### Error Handling
- **File Lock Detection**: Skip files that are currently being written
- **Permission Issues**: Log errors and continue with other files
- **Storage Full**: Graceful handling with appropriate error messages
- **Duplicate Files**: Skip processing and log duplicate detection

### Performance Considerations
- **Efficient Scanning**: Only processes new files, skips previously processed
- **Memory Management**: Streams file content for checksum calculation
- **Database Optimization**: Indexes on checksum and user_id for fast lookups
- **Concurrent Safety**: Thread-safe operations for multiple file processing

## Testing Strategy

### Unit Tests
- IncomingFile entity validation and constraints
- FileProcessingService checksum calculation
- Path generation and conflict resolution
- File validation logic

### Integration Tests
- End-to-end file processing workflow
- Database transaction handling
- Error scenarios and recovery
- Concurrent file processing

### Test Coverage
- All core functionality covered with positive and negative test cases
- Edge cases including empty files, unsupported formats, permission issues
- Performance testing with multiple files
- Mock-based testing for external dependencies

## Future Enhancements

### Planned Improvements
1. **Real-time Monitoring**: Consider file system watchers for immediate detection
2. **Batch Processing**: Optimize for large file volumes
3. **Retry Logic**: Automatic retry for transient failures
4. **Metrics**: Monitoring and alerting for file processing rates
5. **Archive Cleanup**: Automatic cleanup of old processed files

### Integration Points
- **OCR Services** (Items 10-12): IncomingFiles will be input for OCR processing
- **Web Upload** (Item 9): Unified workflow with manual uploads
- **User Interface** (Items 13-14): Display IncomingFiles in inbox for review
- **Audit Logging** (Item 5): Track all file operations for compliance

## Conclusion

The folder-watcher service provides a robust, automated foundation for file ingestion in the receipt processing application. With comprehensive error handling, duplicate detection, and organized storage, it ensures reliable operation while maintaining data integrity. The service is ready for integration with the next phases of the OCR pipeline and user interface development.