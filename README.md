# Smart Dosage

Smart Dosage is a patient‑centric Android application that helps users manage medications, receive dependable reminders, track adherence and supplies, ask questions through a built‑in chat assistant, and share a doctor‑ready PDF (“Doctor Pack”) summarizing the current regimen and recent adherence.

## Features
- Medication manager with full regimen details: name, strength, dose amount, schedule (specific times, every‑X‑hours, weekdays), instructions, start/end dates, refills, initial supply, doses/day, optional photo
- Reliable reminders with actionable notifications: Taken, Snooze, and quick Pharmacy link
- Adherence history: timeline and calendar heat‑map with day/week/month filters
- Supply tracking: decrement on Taken, add refills, days‑left and run‑out estimates
- Doctor Pack PDF: one‑tap generation, robust line wrapping/pagination, open in external app or built‑in viewer, easy sharing
- Chat assistant: local medicine‑aware answers and general Q&A via configurable AI endpoints (Gemini/OpenAI/OpenRouter/local)
- Caretaker tools: store contacts and quick actions (call, SMS, WhatsApp, share)

## Architecture
- Presentation: Activities and RecyclerViews using Material UI components
- Domain: reminder scheduling, notification receivers, action handling, missed‑dose checks, supply management, PDF generation, chat logic
- Data: Room database for `Medicine`, `DoseEvent`, `Supply`, `Caretaker` entities and DAOs
- Integration: AlarmManager + PendingIntent, Notification channels, FileProvider and MediaStore for PDF sharing, HttpURLConnection for AI calls, PdfDocument/PdfRenderer for PDF

## Tech Stack
- Language: Java 11
- Android: `compileSdk=36`, `targetSdk=36`, `minSdk=24`
- Build: Gradle Kotlin DSL with Version Catalogs (AGP 8.13.1)
- Core libraries:
  - AndroidX AppCompat, Core, Activity, ConstraintLayout, RecyclerView
  - Material Components
  - Room (runtime + annotation processor)
  - LiveData (Lifecycle)
  - WorkManager (present for background tasks)
  - Glide (images)
  - ML Kit Text Recognition (present)
  - Play Services Location (pharmacy navigation and future features)
  - GridLayout, CardView
- Testing: JUnit 4, AndroidX Test Ext JUnit, Espresso

## Permissions
- Notifications: `POST_NOTIFICATIONS` (Android 13+)
- Exact alarms: `SCHEDULE_EXACT_ALARM` (Android 12+ permission must be enabled by the user)
- Boot: `RECEIVE_BOOT_COMPLETED` for rescheduling alarms at device restart
- Media/Storage: `READ_MEDIA_IMAGES` (Android 13+), legacy `READ_EXTERNAL_STORAGE` (≤ Android 12) for photo selection
- Network: `INTERNET` for AI endpoints
- Location: `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION` (pharmacy intent)

## Setup
1. Prerequisites
   - Android Studio (Giraffe or newer)
   - JDK 11
   - Android SDK with API level 36
2. Clone and open
   - Open the project in Android Studio and let it sync dependencies
3. Configure run device
   - Create an emulator (Android 12/13/14) or connect a physical device

## Build & Run
- Android Studio: Use Run ▶ to install on the selected device
- CLI:
  - `./gradlew assembleDebug`
  - `./gradlew installDebug`

## Configuration (Chat Assistant)
- Open Settings in the app and set:
  - Base URL (e.g., Gemini: `https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash-latest:generateContent`)
  - API Key (Provider key when required)
  - Model ID (for OpenAI/OpenRouter, defaults to `gpt-4o-mini` or `openai/gpt-4o-mini`)
- Notes:
  - Gemini endpoints require the key in the query string; the app handles this format
  - OpenAI/OpenRouter use `Authorization: Bearer <key>`
  - Local endpoints are supported via `inputs` payload and do not require a key

## Usage
- Add Medicine
  - Enter name, strength, dose amount, schedule (times or intervals or weekdays), instructions, dates, refills, initial supply, doses/day; optionally add a photo
- Reminders
  - Receive high‑priority notifications at exact times; mark Taken or Snooze; missed doses are detected after a grace period
- History
  - View timeline of events and a calendar heat‑map of adherence; filter by day/week/month
- Supply
  - Remaining supply decrements on Taken; add refills; see days‑left and run‑out date
- Doctor Pack PDF
  - Generate a consolidated regimen + 30‑day adherence PDF; open in external viewer when available or in the built‑in viewer; share via standard intents
- Chat
  - Ask questions: medicine‑specific queries show local summaries; general Q&A uses configured AI endpoint with cleaned, readable responses

## Testing
- Unit: `./gradlew test`
- Instrumentation/UI: `./gradlew connectedAndroidTest` (requires a running emulator/device)
- Manual Scenarios
  - Create multiple medicines with different schedules; verify notifications at the exact minute
  - Mark Taken and Snooze; confirm history entries and supply changes
  - Ignore a reminder to confirm missed‑dose logging and optional catch‑up
  - Generate Doctor Pack with long content; verify pagination, open and share behavior without external viewer
  - Configure AI settings; validate chat responses and fallback behavior when offline

## Troubleshooting
- Notifications not arriving exactly on time (Android 12+)
  - Ensure Exact Alarm permission is enabled in system settings for the app
- “No PDF viewer installed”
  - Use the built‑in viewer; share still works via a public Downloads URI or FileProvider
- Share errors
  - On Android 10+, the app copies the PDF to `Downloads/SmartDosage` via MediaStore; recipients should open without additional permissions
- AI failures
  - Verify Base URL, API key, and model ID; the app falls back to local answers when endpoints fail

## Privacy & Security
- All medical data is stored locally using Room; no automatic cloud sync
- PDF sharing uses scoped URIs (MediaStore/FileProvider) with read grants
- AI keys are stored in app preferences; keys are not committed to source control

## Roadmap
- Enhanced PDF styling (tables, headers, branding)
- Rich adherence analytics (streaks, trends, notification engagement)
- Backup/restore and optional secure cloud sync
- Localization and accessibility improvements

## License
- Copyright © 2025. All rights reserved.

