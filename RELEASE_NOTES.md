# Release Notes

## Version 1.0.1 (2026-01-21)

### New Features

#### MMS Support
- Added complete MMS (Multimedia Messaging Service) functionality
- `MMSService` class with methods for sending MMS with images
- `sendWithImage()` - Upload and send MMS in one step
- `getSignedUploadUrl()` - Get S3 signed URL for image upload
- `uploadImageToSignedUrl()` - Upload image to S3
- `send()` - Send MMS to multiple recipients
- `sendSingle()` - Send MMS to a single recipient
- Support for JPEG, PNG, and other image formats
- Automatic image upload to S3 with signed URLs

#### Test Environment Support
- Added `useTestEnvironment` configuration option
- Automatic URL switching between production and test environments
- Test URLs:
  - Core API: `https://core-test-cloudcontactai.allcode.com/api`
  - Email: `https://email-campaigns-test-cloudcontactai.allcode.com`
  - Auth: `https://auth-test-cloudcontactai.allcode.com`
- Environment variable overrides for custom test URLs

### Improvements

#### SMS Service
- Fixed SMS endpoint to use `/clients/{clientId}/campaigns/direct`
- Updated payload structure to use `SMSCampaign` with `accounts` array
- Improved compatibility with CloudContactAI API
- Better error messages and debugging

#### Configuration
- Simplified configuration - URLs now managed internally
- Added `getEffectiveBaseUrl()`, `getEffectiveEmailBaseUrl()`, `getEffectiveAuthBaseUrl()` methods
- Automatic environment-based URL selection
- Removed need for manual URL configuration in most cases

#### Documentation
- Added comprehensive MMS usage examples to README
- Updated configuration documentation
- Removed redundant URL configuration references
- Added step-by-step MMS workflow examples

### Bug Fixes
- Fixed SMS API endpoint path
- Fixed SMS request payload structure to match API expectations
- Fixed MMS image upload URL encoding issues with S3 signed URLs
- Fixed resource loading from JAR files for MMS images

### Technical Changes
- Created `SMSCampaign` class for proper API payload structure
- Created `MMSService`, `MMSRequest`, `MMSCampaign` classes
- Created `Account` class for MMS recipients
- Created `SignedUploadUrlRequest` and `SignedUploadUrlResponse` classes
- Updated `CCAIClient` to include `getMmsService()` method
- Improved HTTP client handling for S3 uploads using `HttpURLConnection`

### Breaking Changes
None - This release is backward compatible with 1.0.0

### Migration Guide
If upgrading from 1.0.0:
1. Update dependency version to 1.0.1
2. Optionally add `CCAI_USE_TEST_ENVIRONMENT=true` to use test environment
3. Remove manual URL configurations (now handled automatically)
4. No code changes required - all existing SMS and Email code continues to work

### Dependencies
- Java 17+
- Spring Boot 3.2.0
- Jackson 2.15.2
- OkHttp 4.12.0

### Contributors
- CloudContactAI Development Team

---

## Version 1.0.0 (2025-01-XX)

### Initial Release
- SMS messaging support
- Email campaign support
- Webhook handling
- Spring Boot integration
- Async/await support
- Progress tracking
- Comprehensive error handling
