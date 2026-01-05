# Payroll Desktop

A desktop application for managing psychologist payroll calculations based on Google Calendar appointments.

## Status: Work in Progress

This is an active development project built to solve a real-world problem in my practice. Core functionality is working, but some features are still being developed.

## What It Does

- Syncs client data from Google Sheets
- Reads appointments from Google Calendar
- Calculates payroll based on session attendance
- Tracks matched/unmatched calendar events
- Stores data locally with SQLite

## Tech Stack

- **Kotlin Multiplatform** - Targeting Desktop (JVM) initially
- **Compose Multiplatform** - UI framework
- **SQLDelight** - Type-safe SQL
- **Koin** - Dependency injection
- **Google APIs** - Calendar and Sheets integration
- **KSafe** - Encrypted credential storage

## Current Features

### Working
- Google Calendar integration with OAuth
- Google Sheets data synchronization
- Local SQLite database for offline access
- Client name matching (exact and fuzzy)
- Session counting and payroll calculation
- Client breakdown by employee

### In Development
- Export to PDF/Excel
- Advanced reporting
- Multi-employee support improvements
- Better error handling

## Setup

### Requirements
- Java 11+
- Google Cloud Platform account
- Google Calendar and Sheets APIs enabled

### Quick Start

1. Clone the repository
2. See [SETUP.md](SETUP.md) for detailed Google Cloud configuration
3. Run the application:
   ```bash
   ./gradlew :composeApp:run
   ```

## Security

- OAuth credentials are **encrypted** using [KSafe](https://github.com/ioannisa/KSafe)
- No hardcoded secrets in codebase
- Local data stored in `~/.payroll-app/`

## Architecture

```
composeApp/
├── src/commonMain/     # Shared Kotlin code
│   ├── domain/         # Business logic
│   ├── data/           # Repositories
│   └── ui/             # Compose UI
├── src/jvmMain/        # Desktop-specific code
│   ├── google/         # Google API integration
│   ├── database/       # SQLDelight setup
│   └── di/             # Koin modules
└── src/commonMain/sqldelight/  # SQL schemas
```

## Known Issues

- Calendar event matching sometimes requires manual confirmation
- Spreadsheet format must follow specific structure
- No automated tests yet (planned)

## Why This Project?

I built this to automate payroll calculations for a psychology practice. Previously, this was done manually with spreadsheets, which was error-prone and time-consuming. The app is actively used in production with real data.

## License

Private project - not for distribution

## Contact

For questions about the code or approach, feel free to reach out.