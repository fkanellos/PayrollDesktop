# Payroll Desktop

A production-ready desktop application for managing psychologist payroll calculations based on Google Calendar appointments.

## Status: Production Ready ✅

This application is actively used in a real psychology practice. All core features are stable, secure, and optimized for performance.

## What It Does

- **Google Sheets Sync** - Syncs client and employee data from Google Sheets
- **Calendar Integration** - Reads appointments from Google Calendar (read-only)
- **Smart Matching** - Intelligent client name matching with manual confirmation for edge cases
- **Payroll Calculation** - Calculates payroll based on session attendance with financial breakdown
- **Local Storage** - Stores data locally with SQLite for offline access
- **Auto-Backup** - Automatic database backups on app shutdown
- **Security** - Encrypted config, obfuscated JARs, secure file permissions

## Tech Stack

- **Kotlin Multiplatform** - Targeting Desktop (JVM) initially
- **Compose Multiplatform** - UI framework
- **SQLDelight** - Type-safe SQL
- **Koin** - Dependency injection
- **Google APIs** - Calendar and Sheets integration
- **KSafe** - Encrypted credential storage

## Features

### ✅ Core Functionality
- **Google Calendar Integration** - OAuth-based calendar access (read-only)
- **Google Sheets Sync** - Bidirectional sync for client/employee data
- **Smart Client Matching** - Fuzzy matching with confidence scoring and manual confirmation
- **Payroll Calculation** - Accurate payroll with client breakdown and financial metrics
- **Multi-Employee Support** - Track multiple employees with separate payrolls

### 🚀 Performance
- **Lazy Loading** - Optimized UI rendering with LazyColumn for large lists
- **Virtual Scrolling** - Handles 100+ clients without lag
- **Efficient Recomposition** - Compose best practices with immutable keys

### 💾 Data Management
- **Auto-Backup** - Automatic database backups on app shutdown
- **Manual Backup/Restore** - Create and restore backups on demand
- **Backup History** - Keeps last 10 backups (configurable)
- **Compressed Backups** - ZIP format with secure permissions (0600)

### 🔒 Security
- **Config Encryption** - AES-256-CBC encryption for sensitive config values
- **Secure File Permissions** - Unix 0700 (directories) and 0600 (files)
- **JAR Signing** - Self-signed certificates for integrity verification
- **ProGuard Obfuscation** - Obfuscated bytecode for reverse engineering protection
- **Token Auto-Refresh** - OAuth tokens refresh automatically before expiration

### 📊 Export & Reporting
- Export to PDF (planned)
- Export to Excel (planned)
- Advanced reporting (planned)

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

### Encryption
- **Config Encryption** - AES-256-CBC with PBKDF2 key derivation (100,000 iterations)
- **Secure Credentials** - OAuth credentials encrypted with [KSafe](https://github.com/ioannisa/KSafe)
- **Machine-Specific Keys** - Encryption keys derived from hostname + username
- **No Hardcoded Secrets** - All sensitive data encrypted or stored securely

### File Security
- **Unix Permissions** - Directories: 0700 (owner-only), Files: 0600 (owner read/write)
- **Secure Storage Path** - `~/.payroll-app/` with restricted permissions
- **Token Protection** - OAuth tokens stored in `~/.payroll-app/tokens/` (0700)
- **Backup Security** - Backups stored with 0600 permissions

### Code Security
- **JAR Signing** - Self-signed certificates with RSA 2048-bit keys
- **ProGuard Obfuscation** - Obfuscated class names and removed debug logs
- **Stripped Logging** - Sensitive data never logged in production
- **Resource Leak Prevention** - Proper cleanup of HTTP transports and file handles

### Best Practices
- **Read-Only Calendar** - Google Calendar is never written to
- **Auto Token Refresh** - Tokens refresh 5 minutes before expiration
- **Single Instance Lock** - Prevents data corruption from multiple instances
- **Graceful Shutdown** - Proper cleanup of resources on exit

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

## Gradle Tasks

### Application
```bash
# Run the application
./gradlew :composeApp:run

# Setup Google OAuth credentials
./gradlew setupCredentials

# Test database backup service
./gradlew testBackup
```

### Security
```bash
# Generate self-signed keystore
./gradlew generateKeystore

# Sign the JAR file
./gradlew signJar

# Verify JAR signature
./gradlew verifyJarSignature

# Show ProGuard obfuscation instructions
./gradlew obfuscateJarInfo
```

### Build
```bash
# Build JVM JAR
./gradlew :composeApp:jvmJar

# Compile Kotlin (faster than full build)
./gradlew compileKotlinJvm

# Clean build
./gradlew clean build
```

## Known Issues

- Calendar event matching sometimes requires manual confirmation
- Spreadsheet format must follow specific structure (see SETUP.md)
- ProGuard obfuscation requires manual setup (see build.gradle.kts)

## Why This Project?

I built this to automate payroll calculations for a psychology practice. Previously, this was done manually with spreadsheets, which was error-prone and time-consuming. The app is actively used in production with real data.

## License

Private project - not for distribution

## Contact

For questions about the code or approach, feel free to reach out.