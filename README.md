# Smart Dosage

**Smart Dosage** is a comprehensive, patient-centric Android application designed to simplify medication management. It goes beyond simple reminders by integrating supply tracking, adherence analytics, AI-powered assistance, doctor-ready reporting, and safety nets like caretaker escalation and interaction checking.

---

## 🌟 Key Features

### 💊 Medication Management
- **Detailed Regimens**: Store comprehensive details including name, strength, form (tablet, syrup, etc.), dose amount, and specific instructions.
- **Flexible Scheduling**: Support for various schedules:
  - Specific times (e.g., 8:00 AM, 8:00 PM)
  - Intervals (e.g., Every 8 hours)
  - Specific days of the week (e.g., Mondays and Thursdays only)
- **Prescription Scanning (OCR)**: Built-in camera feature uses ML Kit to scan physical prescriptions and auto-fill medicine details.
- **Photo Attachments**: Visually identify pills by attaching photos to each medicine entry.

### 🔔 Smart Reminders & Notifications
- **Reliable Alarms**: Uses `AlarmManager` for exact-time notifications, ensuring reliability even on newer Android versions.
- **Actionable Notifications**: Mark doses as "Taken" or "Snooze" directly from the notification shade.
- **Missed Dose Detection**: Automatically flags doses as "Missed" if not acted upon within a grace period.
- **Escalation Protocols**: If a critical dose is missed, the app can automatically notify configured caretakers via SMS or Call (configurable in Settings).

### 📊 Adherence & History
- **Visual Timeline**: A chronological view of all dose events (Taken, Missed, Skipped).
- **Calendar Heatmap**: Color-coded monthly view to instantly visualize adherence patterns (Green = All Taken, Red = Missed, Orange = Partial).
- **Streak Tracking**: Motivational streak counter based on consecutive days of perfect adherence.
- **Weekly Stats**: Percentage-based adherence score for the last 7 days.

### 📦 Supply & Budget Tracking
- **Inventory Management**: Automatically decrements supply when doses are marked as taken.
- **Refill Alerts**: Notifications when supply drops below a custom threshold.
- **Run-out Estimates**: Predicts exactly when current stock will be depleted.
- **Budgeting**: Track medication costs to manage monthly healthcare expenses (`BudgetActivity`).

### 🤖 AI Health Assistant
- **Context-Aware Chat**: A built-in chatbot that knows your medication list. Ask "What are the side effects of my morning pill?" and it understands which pill you mean.
- **Multi-Provider Support**: Configure your preferred AI backend:
  - **Google Gemini** (Recommended/Free)
  - **OpenAI** (GPT-3.5/4)
  - **OpenRouter**
  - **Local LLMs** (via local endpoints)
- **Privacy-First**: AI keys are stored locally; personal health data is anonymized where possible during queries.

### 📄 Doctor Pack
- **One-Tap Reporting**: Generates a professional PDF report summarizing current medications, recent adherence history, and missed dose frequency.
- **Easy Sharing**: Share the PDF directly with healthcare providers via email, WhatsApp, or print it.
- **Built-in Viewer**: Preview reports within the app without needing external PDF readers.

### 🛡️ Safety & Caretakers
- **Caretaker Management**: Store contact details for family members or caregivers.
- **Interaction Checking**: Basic checks for potential drug-drug interactions (`InteractionChecker`).
- **Side Effects Log**: Record and monitor adverse reactions (`SideEffectsActivity`).

---

## 🛠️ Tech Stack & Architecture

### **Core Android**
- **Language**: Java 11
- **SDK Levels**:
  - `minSdk`: 24 (Android 7.0 Nougat)
  - `compileSdk` / `targetSdk`: 36 (Android 15)
- **Build System**: Gradle with Kotlin DSL (`build.gradle.kts`) and Version Catalogs.

### **Libraries & Dependencies**
- **UI/UX**:
  - Material Components (MDC) for modern design implementation.
  - `ConstraintLayout` & `GridLayout` for responsive layouts.
  - `RecyclerView` with custom Adapters for lists.
- **Data Persistence**:
  - **Room Database**: Robust SQLite abstraction for local offline-first data storage.
  - **DAOs**: Type-safe database queries.
- **Background Processing**:
  - `WorkManager`: For deferrable background tasks.
  - `AlarmManager`: For precise time-based scheduling.
  - `BroadcastReceiver`: For handling system events (Boot, Notifications).
- **Machine Learning**:
  - **Google ML Kit (Text Recognition)**: On-device OCR for reading prescriptions.
- **Networking & Image Loading**:
  - `HttpURLConnection`: Lightweight raw networking for AI API calls.
  - **Glide**: Efficient image loading and caching for medicine photos.
- **Utilities**:
  - `Gson` / `org.json`: JSON parsing for AI responses.
  - `PDFDocument`: Native Android PDF generation.

### **Architecture Pattern**
The app follows a modular architecture inspired by **MVVM** principles:
- **Data Layer**: Room Database (`AppDatabase`), Entities (`Medicine`, `DoseEvent`), and DAOs handle all data operations.
- **Domain/Business Logic**: Specialized Managers and Receivers (`SupplyManager`, `ReminderScheduler`, `NotificationHelper`) encapsulate core logic.
- **UI Layer**: Activities act as controllers, observing data (often via direct database queries on background threads for simplicity in this iteration) and updating the UI.

---

## 🚀 Scalability & Future Roadmap

The current architecture is designed to be extensible. Planned future improvements include:

1.  **Cloud Synchronization**:
    - Migration from local-only Room database to a synchronized solution (e.g., Firebase or Supabase) to allow multi-device usage and cloud backup.
2.  **Wear OS Companion App**:
    - Quick actions (Take/Snooze) directly from a smartwatch.
3.  **Advanced Telemedicine Integration**:
    - Direct appointment booking.
    - Real-time dashboard for doctors to view patient adherence.
4.  **Enhanced AI Analytics**:
    - Predictive insights (e.g., "You tend to miss doses on weekends") using on-device TensorFlow Lite models.
5.  **Drug Interaction API**:
    - Integration with professional medical APIs (like RxNorm) for comprehensive interaction checking.
6.  **Localization (i18n)**:
    - Support for multiple languages to serve a global user base.

---

## 🔒 Privacy & Security

- **Local First**: All sensitive medical data is stored strictly on the user's device in the Room database. It is **not** uploaded to any external server by default.
- **AI Privacy**: When using the AI chat, only the user's query is sent. Users can choose to use local LLMs for 100% offline privacy.
- **Scoped Storage**: The app respects Android's scoped storage rules, ensuring it only accesses files it created or was granted permission to see.

---

## ⚙️ Setup & Configuration

### Prerequisites
- **Android Studio**: Ladybug or newer (recommended).
- **JDK**: Java 11 or higher.
- **Android Device/Emulator**: Running Android 7.0 (API 24) or higher.

### Installation
1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/your-username/smart-dosage.git
    ```
2.  **Open in Android Studio**: Allow Gradle to sync dependencies.
3.  **Build**:
    ```bash
    ./gradlew assembleDebug
    ```

### Configuring AI (Optional)
To use the Chat Assistant:
1.  Go to **Settings** > **AI Configuration**.
2.  **Base URL**:
    - Gemini: `https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent`
    - OpenAI: `https://api.openai.com/v1/chat/completions`
3.  **API Key**: Enter your provider's API key.

---

## © License
Copyright © 2025 Smart Dosage Team. All rights reserved.
