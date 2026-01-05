# Setup Instructions

## Prerequisites

- Java 11 or higher
- Google Cloud Platform account
- Google Calendar and Google Sheets APIs enabled

## Google Cloud Console Setup

### 1. Create a Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the following APIs:
   - Google Calendar API
   - Google Sheets API
   - Google Drive API

### 2. Create OAuth 2.0 Credentials

1. Go to **APIs & Services** → **Credentials**
2. Click **Create Credentials** → **OAuth client ID**
3. Choose **Desktop app** as application type
4. Download the `credentials.json` file

### 3. Configure OAuth Consent Screen

⚠️ **IMPORTANT**: Set to **PRODUCTION** mode (not Testing)
- Testing mode: tokens expire in 7 days
- Production mode: tokens last 6 months

1. Go to **APIs & Services** → **OAuth consent screen**
2. Configure the consent screen
3. Publish the app (move from Testing to Production)

## Application Setup

### Option 1: Automated Import (Recommended)

1. Build and run the application:
   ```bash
   ./gradlew :composeApp:run
   ```

2. On first launch, the app will detect missing credentials
3. Follow the on-screen prompts to select your `credentials.json` file
4. Credentials will be encrypted and stored securely using KSafe

### Option 2: Manual Import

1. Place your `credentials.json` in `~/.payroll-app/`

2. Run the import command:
   ```kotlin
   val credProvider = GoogleCredentialProvider()
   credProvider.importCredentials(File("/path/to/credentials.json"))
   ```

3. The credentials will be encrypted and stored in `~/.payroll-app/credentials/`

## Verification

After setup, verify everything works:

1. Run the application
2. The app should automatically open a browser for Google OAuth
3. Authorize the application
4. You should see "Loaded existing credentials" in the console

## Security Notes

- Credentials are encrypted using **KSafe** and stored in `~/.payroll-app/credentials/`
- OAuth tokens are stored in `~/.payroll-app/tokens/`
- The original `credentials.json` is NOT stored in the codebase
- Never commit credentials to version control

## Troubleshooting

### "Google OAuth credentials not found"
- Run the import process again
- Check that `~/.payroll-app/credentials/` exists and contains encrypted data

### "Token expired" errors
- Delete `~/.payroll-app/tokens/` and re-authorize
- Ensure OAuth Consent Screen is set to PRODUCTION mode

### "Access denied" errors
- Verify all required APIs are enabled in Google Cloud Console
- Check OAuth scopes in the consent screen

## Data Storage Locations

- **Encrypted credentials**: `~/.payroll-app/credentials/`
- **OAuth tokens**: `~/.payroll-app/tokens/`
- **Local database**: `~/.payroll-app/payroll.db`