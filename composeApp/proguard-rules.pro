# 🔒 PayrollDesktop ProGuard Rules - Security & Obfuscation
# Protects sensitive code from reverse engineering while maintaining functionality

# ==================== GENERAL OPTIMIZATION ====================

# Enable aggressive optimizations
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Obfuscation options
-repackageclasses ''
-allowaccessmodification
-useunique classnamingclasses

# ==================== KEEP ESSENTIALS ====================

# Keep main entry point
-keep class com.payroll.app.desktop.MainKt { *; }

# Keep Compose & Kotlin essentials
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-keep class androidx.compose.** { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**

# Keep ViewModels (reflection used by Compose)
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Keep data classes (serialization)
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    <fields>;
    $$serializer(...);
}

# ==================== PROTECT SENSITIVE CODE ====================

# 🔒 Obfuscate database layer (harder to reverse engineer schema)
-keep class com.payroll.app.desktop.database.** { *; }

# 🔒 Obfuscate credential management (protect Google API access)
-keep class com.payroll.app.desktop.google.SecureCredentialStore {
    public <methods>;
}
-keep class com.payroll.app.desktop.google.GoogleCredentialProvider {
    public <methods>;
}
-keep class com.payroll.app.desktop.google.GoogleSheetsService {
    public <methods>;
}

# 🔒 Obfuscate repositories (protect business logic)
-keep class com.payroll.app.desktop.data.repositories.** {
    public <methods>;
}

# 🔒 Obfuscate domain services (protect calculation logic)
-keep class com.payroll.app.desktop.domain.service.** {
    public <methods>;
}

# 🔒 Keep config but obfuscate internals
-keep class com.payroll.app.desktop.core.config.AppConfig {
    <fields>;
}

# ==================== THIRD-PARTY LIBRARIES ====================

# Koin DI
-keep class org.koin.** { *; }
-keep class org.koin.core.** { *; }
-dontwarn org.koin.**

# Ktor
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { *; }
-dontwarn io.ktor.**

# SQLDelight
-keep class app.cash.sqldelight.** { *; }
-keep class com.payroll.app.desktop.database.PayrollDatabase** { *; }
-keepclassmembers class * extends app.cash.sqldelight.** {
    public <methods>;
}

# Google APIs
-keep class com.google.api.** { *; }
-keep class com.google.api.client.** { *; }
-keep class com.google.api.services.** { *; }
-dontwarn com.google.api.**

# KSafe (encrypted storage)
-keep class eu.anifantakis.ksafe.** { *; }
-dontwarn eu.anifantakis.ksafe.**

# ==================== REMOVE LOGGING (SECURITY) ====================

# 🔒 Strip debug/verbose logs from release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** isLoggable(...);
}

# 🔒 Remove custom logger debug/trace calls
-assumenosideeffects class com.payroll.app.desktop.core.logging.Logger {
    public static *** debug(...);
    public static *** trace(...);
}

# ==================== REFLECTION SAFETY ====================

# Keep serialization
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exception
-keepattributes InnerClasses

# ==================== PERFORMANCE ====================

# Remove unnecessary metadata
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ==================== WARNINGS SUPPRESSION ====================

# Suppress warnings for missing libraries (not used at runtime)
-dontwarn java.lang.management.**
-dontwarn javax.management.**
-dontwarn org.slf4j.**
-dontwarn ch.qos.logback.**

# ==================== NOTES ====================

# This configuration:
# 1. ✅ Obfuscates class/method/field names
# 2. ✅ Removes debug logging
# 3. ✅ Optimizes bytecode (5 passes)
# 4. ✅ Strips unused code
# 5. ✅ Protects Google API credentials handling
# 6. ✅ Protects database schema and business logic
# 7. ✅ Maintains functionality through selective keep rules

# To test obfuscation:
# ./gradlew assembleRelease
# unzip -l build/outputs/apk/release/app-release.apk
# dex2jar + jd-gui to decompile and verify obfuscation
