# 🔒 PayrollDesktop - Desktop/JVM ProGuard Rules
# Optimizes and obfuscates the desktop JAR for security and size reduction

# ==================== GENERAL SETTINGS ====================

-optimizationpasses 5
-repackageclasses ''
-allowaccessmodification
-dontpreverify

# Keep attributes for better debugging (optional - remove for max obfuscation)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ==================== KEEP MAIN ENTRY POINT ====================

# Keep the main application entry point
-keep class com.payroll.app.desktop.MainKt {
    public static void main(java.lang.String[]);
}

# ==================== KOTLIN & COMPOSE ====================

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**

# Keep Compose runtime
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.compose.material3.** { *; }
-dontwarn androidx.compose.**

# Keep lifecycle ViewModels (reflection)
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ==================== SERIALIZATION ====================

# Keep serialized data classes
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    <fields>;
    $$serializer(...);
}

-keepclassmembernames class kotlinx.serialization.json.** {
    *** Companion;
}

-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ==================== OBFUSCATE SENSITIVE LAYERS ====================

# 🔒 Obfuscate database layer
-keep,allowobfuscation class com.payroll.app.desktop.database.** { *; }

# 🔒 Obfuscate Google API integration
-keep,allowobfuscation class com.payroll.app.desktop.google.** {
    public <methods>;
}

# 🔒 Obfuscate repositories
-keep,allowobfuscation class com.payroll.app.desktop.data.repositories.** {
    public <methods>;
}

# 🔒 Obfuscate domain services
-keep,allowobfuscation class com.payroll.app.desktop.domain.service.** {
    public <methods>;
}

# 🔒 Obfuscate security/encryption
-keep,allowobfuscation class com.payroll.app.desktop.core.security.** { *; }

# ==================== THIRD-PARTY LIBRARIES ====================

# SQLDelight
-keep class app.cash.sqldelight.** { *; }
-keep class com.payroll.app.desktop.database.PayrollDatabase** { *; }

# Ktor
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { *; }
-dontwarn io.ktor.**

# Google APIs
-keep class com.google.api.** { *; }
-keep class com.google.api.client.** { *; }
-keep class com.google.api.services.** { *; }
-keep class com.google.gson.** { *; }
-dontwarn com.google.api.**
-dontwarn com.google.gson.**

# Koin DI
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# KSafe
-keep class eu.anifantakis.ksafe.** { *; }
-dontwarn eu.anifantakis.ksafe.**

# ==================== REMOVE DEBUG LOGGING ====================

# 🔒 Strip debug/trace logs
-assumenosideeffects class com.payroll.app.desktop.core.logging.Logger {
    public static *** debug(...);
    public static *** trace(...);
}

# ==================== REFLECTION SUPPORT ====================

-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exception
-keepattributes InnerClasses

# ==================== SUPPRESS WARNINGS ====================

-dontwarn java.lang.management.**
-dontwarn javax.management.**
-dontwarn org.slf4j.**
-dontwarn ch.qos.logback.**
-dontwarn sun.misc.Unsafe
-dontwarn com.sun.jna.**
-dontwarn org.apache.log4j.**

# ==================== NOTES ====================

# To build obfuscated JAR:
# ./gradlew proguardJvm
#
# To verify obfuscation:
# 1. Extract JAR: unzip -d extracted build/proguard/output.jar
# 2. Check class names: find extracted -name "*.class"
# 3. Decompile with jd-gui or similar
