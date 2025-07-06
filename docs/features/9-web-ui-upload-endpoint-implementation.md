# Feature Implementation: Web-UI Upload Endpoint

## Motivation
Users need the ability to upload receipt and bill files directly through the web interface without relying solely on the file system folder watcher. This provides a more convenient and immediate way to add documents to the system, especially for users who prefer web-based interactions over file system operations.

## Feature Description
A REST API endpoint that allows authenticated users to upload files via multipart HTTP requests. The endpoint integrates with the existing file processing infrastructure to maintain consistency with folder-watcher uploaded files. Files are validated, deduplicated, stored securely, and processed through the same workflow as automatically detected files.

### Key Capabilities:
- **Multipart File Upload**: Accepts files via `POST /api/files/upload`
- **File Validation**: Validates file size (10MB limit), type (PDF, images), and content
- **Duplicate Detection**: Uses SHA-256 checksums to prevent duplicate uploads
- **User Authentication**: Requires OAuth2 authentication for all uploads
- **Structured Response**: Returns JSON responses with success/error details
- **Security**: CSRF protection and proper authentication context
- **Integration**: Seamlessly integrates with existing `FileProcessingService`

## Definition of Done

### API Functionality Tests:
- ✅ **File Upload Success**: Valid files uploaded by authenticated users are processed correctly
- ✅ **Authentication Required**: Unauthenticated requests return 401 Unauthorized
- ✅ **File Size Validation**: Files over 10MB are rejected with appropriate error
- ✅ **File Type Validation**: Unsupported file types (e.g., .txt) are rejected
- ✅ **Duplicate Prevention**: Files with identical content are detected and rejected
- ✅ **Error Handling**: Various error scenarios return proper HTTP status codes and messages

### Integration Tests:
- ✅ **End-to-End Workflow**: Complete file upload through processing pipeline
- ✅ **Database Persistence**: Uploaded files create proper `IncomingFile` records
- ✅ **File Storage**: Files are moved to configured attachments directory
- ✅ **User Context**: Files are associated with correct authenticated user
- ✅ **Cleanup**: Temporary files are properly cleaned up on errors

### Security Tests:
- ✅ **CSRF Protection**: API endpoints have appropriate CSRF configuration
- ✅ **Authentication Context**: User identification works correctly
- ✅ **Authorization**: Only authenticated users can upload files
- ✅ **File Path Security**: Generated file paths are safe and organized

## Implementation Steps

### Step 1: Create FileUploadController ✅
**Status: COMPLETED**
- Created `/src/main/kotlin/me/underlow/receipt/controller/FileUploadController.kt`
- Implemented `POST /api/files/upload` endpoint with multipart file support
- Added comprehensive file validation (size, type, content)
- Integrated with existing `FileProcessingService` for consistent file handling
- Created structured DTOs for request/response handling

### Step 2: Update Security Configuration ✅
**Status: COMPLETED**
- Modified `SecurityConfig.kt` to allow `/api/files/**` endpoints for authenticated users
- Configured CSRF protection to ignore file upload endpoints
- Ensured proper authentication context for user identification

### Step 3: Add File Upload Configuration ✅
**Status: COMPLETED**
- Updated `application.yaml` with multipart file settings:
  - Maximum file size: 10MB
  - Maximum request size: 10MB
  - File size threshold: 2KB
- Configuration allows for easy adjustment of limits

### Step 4: Create DTOs and Error Handling ✅
**Status: COMPLETED**
- Created `FileUploadResponse.kt` for successful upload responses
- Created `ErrorResponse.kt` for structured error information
- Implemented comprehensive error handling for:
  - File size exceeded (`FILE_TOO_LARGE`)
  - Unsupported file types (`UNSUPPORTED_FILE_TYPE`)
  - Empty files (`EMPTY_FILE`)
  - Duplicate files (`DUPLICATE_FILE`)
  - Internal server errors (`INTERNAL_ERROR`)

### Step 5: Integrate User Authentication ✅
**Status: COMPLETED**
- Created `UserService.kt` for user lookup operations
- Implemented user ID extraction from OAuth2 authentication token
- Integrated user context into file processing workflow
- Ensured proper user isolation for uploaded files

### Step 6: Comprehensive Testing ✅
**Status: COMPLETED**

#### Unit Tests (`FileUploadControllerTest.kt`):
- File upload success scenarios
- Authentication and authorization testing
- File validation error scenarios
- Duplicate detection testing
- Error handling verification
- Various file type support testing

#### Integration Tests (`FileUploadControllerIntegrationTest.kt`):
- End-to-end workflow testing
- Database integration verification
- File storage validation
- Multiple file format support
- Error scenario handling with cleanup
- Large file processing within limits

#### E2E Tests (`FileUploadE2ETest.kt`):
- API endpoint testing with TestContainers PostgreSQL
- Authentication flow testing
- File processing verification
- Database schema validation
- Complete user journey simulation
- Error scenario testing in realistic environment

### Step 7: Update Documentation ✅
**Status: COMPLETED**
- Created comprehensive feature documentation
- Updated implementation details and test coverage
- Documented API specifications and integration points

## Technical Specifications

### API Endpoint
- **URL**: `POST /api/files/upload`
- **Content-Type**: `multipart/form-data`
- **Authentication**: Required (OAuth2)
- **Parameter**: `file` (multipart file)

### Supported File Types
- PDF: `.pdf`
- Images: `.jpg`, `.jpeg`, `.png`, `.gif`, `.bmp`, `.tiff`

### File Size Limits
- Maximum file size: 10MB (configurable)
- Maximum request size: 10MB
- File size threshold: 2KB

### Response Format
**Success Response (HTTP 200):**
```json
{
  "id": 123,
  "filename": "receipt.pdf",
  "uploadDate": "2025-01-01T12:00:00",
  "status": "PENDING",
  "checksum": "abc123...",
  "success": true,
  "message": "File uploaded successfully"
}
```

**Error Response (HTTP 4xx/5xx):**
```json
{
  "success": false,
  "message": "Error description",
  "code": "ERROR_CODE",
  "details": {
    "additionalInfo": "value"
  }
}
```

## Integration Points

### Existing Components Used:
- **FileProcessingService**: Reused for consistent file handling, checksum calculation, and storage organization
- **IncomingFile Entity**: Used same data model as folder watcher for consistency
- **UserService**: Created for user lookup and authentication integration
- **Security Configuration**: Extended existing OAuth2 setup for API authentication
- **Database Schema**: Uses existing `incoming_files` table structure

### File Processing Workflow:
1. **Authentication**: Extract user from OAuth2 token
2. **Validation**: Check file size, type, and content
3. **Temporary Storage**: Save multipart file to temp location
4. **Processing**: Use `FileProcessingService.processFile()`
5. **Deduplication**: Check SHA-256 checksum against existing files
6. **Storage**: Move file to organized attachments directory
7. **Database**: Create `IncomingFile` record in PENDING status
8. **Cleanup**: Remove temporary files
9. **Response**: Return structured success/error response

### File Organization:
- Files stored in same directory structure as folder watcher
- Filename format: `yyyy-MM-dd-originalname.ext`
- Duplicate handling with incremental suffixes (`-1`, `-2`, etc.)
- Proper directory creation and file permissions

## Testing Coverage

### Test Categories:
1. **Unit Tests**: Controller logic, validation, error handling
2. **Integration Tests**: Service integration, database operations, file handling
3. **E2E Tests**: API endpoints, authentication, complete workflows

### Test Scenarios Covered:
- ✅ Valid file upload with authentication
- ✅ Unauthenticated access attempts
- ✅ File size validation (empty, oversized, within limits)
- ✅ File type validation (supported/unsupported formats)
- ✅ Duplicate detection and rejection
- ✅ Error handling and proper HTTP status codes
- ✅ Database schema and constraints verification
- ✅ File storage and cleanup operations
- ✅ Multiple file format support
- ✅ User context and isolation
- ✅ Temporary file cleanup on errors

## Security Considerations

### Authentication & Authorization:
- OAuth2 token-based authentication required
- User context properly extracted and validated
- Files associated with authenticated user only

### File Security:
- File type validation prevents executable uploads
- File size limits prevent resource exhaustion
- Checksum-based duplicate detection
- Organized storage prevents path traversal

### API Security:
- CSRF protection configured for file upload endpoints
- Structured error responses don't leak sensitive information
- Proper HTTP status codes for different scenarios

## Future Enhancements

### Potential Improvements:
1. **Progress Tracking**: WebSocket-based upload progress for large files
2. **Batch Upload**: Support for multiple file uploads in single request
3. **File Preview**: Thumbnail generation and preview capabilities
4. **Upload Resumption**: Support for resumable uploads for reliability
5. **Drag & Drop UI**: Enhanced web interface for file uploads
6. **Cloud Storage**: Integration with cloud storage providers
7. **Virus Scanning**: Integration with antivirus scanning services

### Configuration Options:
1. **Configurable Limits**: Make file size and type restrictions configurable per user/role
2. **Storage Options**: Support for different storage backends
3. **Processing Options**: Configurable OCR processing triggers

## Conclusion

The Web-UI Upload Endpoint feature has been successfully implemented with comprehensive testing and documentation. It provides a robust, secure, and user-friendly way for authenticated users to upload files directly through the web interface while maintaining full integration with the existing file processing infrastructure. The implementation includes extensive error handling, validation, and security measures to ensure reliable operation in production environments.