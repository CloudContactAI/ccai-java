# Release Notes

## Version 1.0.1 (2026-02-02)

### New Features

#### MMS Support
- Added complete MMS (Multimedia Messaging Service) functionality
- `MMSService` class with methods for sending MMS with images
- `sendWithImage()` - Upload and send MMS in one step
- `getSignedUploadUrl()` - Get S3 signed URL for image upload
- `uploadImageToSignedUrl()` - Upload image to S3
- `send()` - Send MMS to multiple recipients
- `sendSingle()` - Send MMS to a single recipient
- Support for JPEG, PNG, GIF, and other image formats
- Automatic image upload to S3 with signed URLs

#### Enhanced Webhook Support
- Added `WebhookEvent` model for parsing webhook payloads
- `parseWebhookEvent()` - Parse incoming webhook JSON
- `validateWebhookSignature()` - HMAC SHA-256 signature validation
- Constant-time signature comparison for security
- Support for all event types: SMS/Email sent/delivered/failed, contact unsubscribed

#### Configuration Enhancements
- Added `debugMode` flag for detailed logging
- Added `maxRetries` configuration for automatic retry logic
- Added `timeoutMs` configuration for request timeouts
- Improved validation with helpful error messages

### Improvements

#### Test Environment Support
- Already present: `useTestEnvironment` configuration option
- Automatic URL switching between production and test environments
- Environment variable overrides for custom URLs

### Technical Changes
- Created `MMSService`, `MMSRequest`, `MMSCampaign` classes
- Created `Account` class for MMS recipients
- Created `SignedUploadUrlRequest` and `SignedUploadUrlResponse` classes
- Enhanced `WebhookService` with event parsing and signature validation
- Updated `CCAIClient` to include `mms` service
- All implementations in Kotlin for consistency

### Breaking Changes
None - This release is backward compatible with 1.0.0

### Migration Guide
If upgrading from 1.0.0:
1. Update dependency version to 1.0.1
2. Optionally add `CCAI_USE_TEST_ENVIRONMENT=true` to use test environment
3. No code changes required - all existing SMS and Email code continues to work
4. New MMS functionality available via `ccai.mms`

### Dependencies
- Java 11+
- Kotlin 1.9.0
- OkHttp 4.x
- Jackson 2.x

---

## Version 1.0.0 (2025-01-XX)

### Initial Release
- SMS messaging support
- Email campaign support
- Webhook management
- Spring Boot integration
- Comprehensive error handling
