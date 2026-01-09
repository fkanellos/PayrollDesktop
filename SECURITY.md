# Security Documentation

This document outlines the security measures implemented in Payroll Desktop to protect sensitive payroll and client data.

## Table of Contents

1. [Encryption](#encryption)
2. [File Security](#file-security)
3. [Code Security](#code-security)
4. [OAuth Security](#oauth-security)
5. [Database Security](#database-security)
6. [Backup Security](#backup-security)
7. [Best Practices](#best-practices)
8. [Threat Model](#threat-model)

---

## Encryption

### Config File Encryption (AES-256-CBC)

Sensitive configuration values (spreadsheet IDs, API URLs) are encrypted using AES-256-CBC:

- **Algorithm**: AES/CBC/PKCS5Padding
- **Key Derivation**: PBKDF2 with 100,000 iterations
- **Key Source**: Machine-specific (hostname + username + "user.home")
- **IV**: Random 16-byte IV per encryption (stored with ciphertext)
- **Format**: Base64-encoded `IV:CipherText`

**Auto-Encryption**: Plaintext values are automatically encrypted on first load.

**Files Encrypted**:
- `~/.payroll-app/app.config` - Spreadsheet IDs, API URLs

**Implementation**: `com.payroll.app.desktop.core.security.EncryptionManager`

---

## File Security

### Unix File Permissions

All sensitive files and directories use restrictive Unix permissions:

| Path | Permission | Meaning |
|------|------------|---------|
| `~/.payroll-app/` | `0700` (drwx------) | Owner-only directory access |
| `~/.payroll-app/payroll.db` | `0600` (-rw-------) | Owner read/write only |
| `~/.payroll-app/tokens/` | `0700` (drwx------) | Owner-only token directory |
| `~/.payroll-app/backups/*.zip` | `0600` (-rw-------) | Owner read/write only |
| `~/.payroll-app/app.config` | `0600` (-rw-------) | Owner read/write only |

**Enforcement**: Permissions are set programmatically on file creation and validated on app startup.

**Verification**:
```bash
ls -la ~/.payroll-app/
# Expected: drwx------ (0700) for directories
# Expected: -rw------- (0600) for files
```

**Implementation**: `com.payroll.app.desktop.database.DriverFactory`, `DatabaseBackupService`

---

## Code Security

### JAR Signing

All production JARs are signed with self-signed certificates to ensure integrity:

- **Algorithm**: SHA384withRSA
- **Key Size**: RSA 2048-bit
- **Validity**: 10 years
- **Keystore**: `build/keystore/payroll-desktop.jks`

**Generate Keystore**:
```bash
./gradlew generateKeystore
```

**Sign JAR**:
```bash
./gradlew signJar
```

**Verify Signature**:
```bash
./gradlew verifyJarSignature
# or manually:
jarsigner -verify -verbose -certs build/libs/composeApp-jvm.jar
```

**Implementation**: `composeApp/build.gradle.kts` (lines 160-227)

---

### ProGuard Obfuscation

Production builds are obfuscated to protect business logic from reverse engineering:

**Obfuscated Layers**:
- Database access code (SQLDelight)
- Google API integration
- Repositories and domain services
- Security/encryption classes

**Debug Logging Removed**:
- All `Logger.debug()` and `Logger.trace()` calls are stripped from production builds

**Configuration Files**:
- Android: `composeApp/proguard-rules.pro`
- Desktop: `composeApp/proguard-desktop.pro`

**Manual Setup Required**: See `obfuscateJarInfo` task for instructions:
```bash
./gradlew obfuscateJarInfo
```

**Implementation**: ProGuard rules in `composeApp/proguard-desktop.pro`

---

## OAuth Security

### Google API Credentials

OAuth credentials are stored securely using KSafe encrypted storage:

- **Storage**: `~/.payroll-app/.ksafe-encrypted-credentials`
- **Encryption**: AES-256 provided by [KSafe](https://github.com/ioannisa/KSafe)
- **Scope Limitation**: Calendar read-only, Sheets read/write, Drive file access only

**Setup**:
1. Download `credentials.json` from Google Cloud Console
2. Run: `./gradlew setupCredentials`
3. Credentials are encrypted and stored securely
4. Original `credentials.json` can be deleted

**Token Management**:
- **Access Tokens**: Auto-refresh 5 minutes before expiration
- **Refresh Tokens**: Valid for 6 months (if used within that period)
- **Token Storage**: `~/.payroll-app/tokens/` with 0700 permissions
- **Production Consent Screen**: Set OAuth consent screen to PRODUCTION mode (not Testing) to avoid 7-day token expiration

**Implementation**: `com.payroll.app.desktop.google.GoogleCredentialProvider`

---

## Database Security

### SQLite Database

Local database stored in user directory with secure permissions:

- **Path**: `~/.payroll-app/payroll.db`
- **Permissions**: `0600` (owner read/write only)
- **Encryption**: None (use OS-level encryption - see below)

**Recommended OS-Level Encryption**:
- **macOS**: FileVault 2 (System Preferences → Security & Privacy → FileVault)
- **Windows**: BitLocker (Control Panel → System and Security → BitLocker Drive Encryption)
- **Linux**: LUKS/dm-crypt (full disk encryption)

**Why not SQLCipher?**
- SQLCipher JVM support is unreliable on JitPack
- OS-level encryption is more robust and transparent
- FileVault/BitLocker provide full disk encryption without application changes

**Implementation**: `com.payroll.app.desktop.database.DriverFactory`

---

## Backup Security

### Auto-Backup System

Database is automatically backed up on app shutdown:

- **Location**: `~/.payroll-app/backups/`
- **Format**: ZIP compression
- **Naming**: `payroll_backup_[reason]_YYYY-MM-DD_HH-MM-SS.zip`
- **Permissions**: `0600` (owner read/write only)
- **Retention**: Keeps last 10 backups (configurable)

**Backup Reasons**:
- `auto-shutdown` - Automatic backup on app exit
- `manual` - User-triggered backup
- `before-restore` - Safety backup before restore operation
- `test-manual` - Test backup (from `./gradlew testBackup`)

**Manual Backup**:
```bash
./gradlew testBackup
```

**Backup Location**:
```bash
ls -lh ~/.payroll-app/backups/
```

**Restore Process**:
1. Safety backup of current database is created
2. Database is replaced with backup file content
3. Permissions are restored to 0600

**Implementation**: `com.payroll.app.desktop.data.services.DatabaseBackupService`

---

## Best Practices

### Application Design

1. **Read-Only Calendar** - Google Calendar is NEVER written to (read-only scope)
2. **Single Instance Lock** - Prevents data corruption from multiple app instances
3. **Graceful Shutdown** - Proper cleanup of HTTP transports, file handles, and auto-backup
4. **Resource Leak Prevention** - All resources (files, network connections) are properly closed
5. **Fail-Fast Credentials** - App fails immediately if Google credentials are missing (no silent failures)

### Development

1. **No Hardcoded Secrets** - All credentials stored in encrypted config or secure storage
2. **No Sensitive Logging** - Sensitive data (spreadsheet IDs, OAuth tokens) never logged
3. **Secure Defaults** - File permissions, encryption enabled by default
4. **Error Handling** - Clear error messages without exposing sensitive details

### Deployment

1. **JAR Signing** - Sign all production JARs
2. **ProGuard Obfuscation** - Obfuscate all production builds
3. **OS Encryption** - Enable FileVault/BitLocker on deployment machines
4. **Production OAuth** - Set Google OAuth consent screen to PRODUCTION mode

---

## Threat Model

### Protected Against

✅ **Unauthorized File Access** - Unix permissions prevent other users from reading data
✅ **Config Theft** - Sensitive config values encrypted with AES-256
✅ **OAuth Token Theft** - Tokens stored in restricted directory (0700)
✅ **JAR Tampering** - Signed JARs detect modifications
✅ **Reverse Engineering** - ProGuard obfuscation protects business logic
✅ **Data Corruption** - Single instance lock + auto-backups
✅ **Resource Exhaustion** - Proper cleanup prevents file handle leaks

### NOT Protected Against

❌ **Root/Admin Access** - Root users can bypass Unix permissions
❌ **Malware on Host** - Malware running as same user can access files
❌ **Physical Access** - Physical access to unlocked machine bypasses all security
❌ **Keyloggers** - Cannot prevent keyloggers from capturing passwords
❌ **Screen Recording** - Cannot prevent screen capture of sensitive data
❌ **Memory Dumps** - Encryption keys stored in memory can be extracted

### Recommended Mitigations

1. **Enable OS-Level Encryption** - FileVault/BitLocker protects against physical theft
2. **Lock Screen** - Always lock screen when leaving machine unattended
3. **Antivirus Software** - Protect against malware and keyloggers
4. **Regular Backups** - Store backups on separate encrypted drive/cloud storage
5. **Least Privilege** - Don't run app as root/administrator

---

## Security Checklist

### Initial Setup
- [ ] Enable FileVault (macOS) or BitLocker (Windows)
- [ ] Set Google OAuth consent screen to PRODUCTION mode
- [ ] Run `./gradlew setupCredentials` to encrypt OAuth credentials
- [ ] Verify file permissions: `ls -la ~/.payroll-app/`

### Development
- [ ] Never commit `credentials.json` or `app.config`
- [ ] Never log sensitive data (spreadsheet IDs, tokens)
- [ ] Use `Logger.debug()` for sensitive logs (stripped in production)
- [ ] Test with `./gradlew testBackup` before deployment

### Deployment
- [ ] Generate keystore: `./gradlew generateKeystore`
- [ ] Sign JAR: `./gradlew signJar`
- [ ] Verify signature: `./gradlew verifyJarSignature`
- [ ] Obfuscate code: Follow `./gradlew obfuscateJarInfo` instructions
- [ ] Test auto-backup by closing app and checking `~/.payroll-app/backups/`

### Ongoing
- [ ] Monitor backup size: `du -sh ~/.payroll-app/backups/`
- [ ] Verify token refresh logs (should happen every ~1 hour)
- [ ] Review backup history: Old backups auto-deleted after 10
- [ ] Update dependencies regularly for security patches

---

## Security Contact

For security issues or questions about this documentation:
- Review code: `com.payroll.app.desktop.core.security.*`
- Check implementation: Search codebase for "🔒 SECURITY" comments

**DO NOT** publicly disclose security vulnerabilities. Report privately first.

---

## License

This security documentation is part of Payroll Desktop (private project).
