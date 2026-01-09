import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    jvm()
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.datetime)
            implementation(compose.materialIconsExtended)
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

            // Networking
            implementation("io.ktor:ktor-client-core:2.3.4")
            implementation("io.ktor:ktor-client-content-negotiation:2.3.4")
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.4")

            // Koin DI
            implementation("io.insert-koin:koin-core:3.4.3")
            implementation("io.insert-koin:koin-compose:1.0.4")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation("io.ktor:ktor-client-cio:2.3.4")

            // SQLDelight
            implementation(libs.sqldelight.driver)
            implementation(libs.sqldelight.coroutines)

            // Google APIs
            implementation(libs.google.api.client)
            implementation(libs.google.oauth.client.jetty)
            implementation(libs.google.calendar)
            implementation(libs.google.sheets)
            implementation(libs.google.drive)

            // KSafe - Encrypted credential storage
            implementation("eu.anifantakis:ksafe:1.3.0")

            // SLF4J - Logging for Google API client
            implementation("org.slf4j:slf4j-simple:2.0.9")
        }
    }
}

android {
    namespace = "com.payroll.app.desktop"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.payroll.app.desktop"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            // 🔒 SECURITY: Enable code minification and obfuscation
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "com.payroll.app.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.payroll.app.desktop"
            packageVersion = "1.0.0"
        }
    }
}

// SQLDelight configuration
sqldelight {
    databases {
        create("PayrollDatabase") {
            packageName.set("com.payroll.app.desktop.database")
            srcDirs.setFrom("src/commonMain/sqldelight")
            // Enable schema verification and migration support
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/databases"))
            // Verify migrations on build
            verifyMigrations.set(true)
        }
    }
}

// Task to setup credentials
tasks.register<JavaExec>("setupCredentials") {
    group = "application"
    description = "Import Google OAuth credentials from credentials.json"
    mainClass.set("com.payroll.app.desktop.SetupCredentialsKt")
    classpath = sourceSets["jvmMain"].runtimeClasspath
    workingDir = project.projectDir.parentFile
}

// 🔒 SECURITY: JAR Signing Task
// Generates a self-signed certificate and signs the JAR for integrity verification
tasks.register<Exec>("generateKeystore") {
    group = "security"
    description = "Generate self-signed keystore for JAR signing"

    val keystoreFile = file("${project.buildDir}/keystore/payroll-desktop.jks")

    doFirst {
        keystoreFile.parentFile.mkdirs()
    }

    commandLine(
        "keytool",
        "-genkeypair",
        "-alias", "payroll-desktop",
        "-keyalg", "RSA",
        "-keysize", "2048",
        "-validity", "3650",
        "-keystore", keystoreFile.absolutePath,
        "-storepass", "changeit",
        "-keypass", "changeit",
        "-dname", "CN=Payroll Desktop, OU=IT, O=PayrollDesktop, L=Athens, ST=Attica, C=GR"
    )

    onlyIf { !keystoreFile.exists() }
}

// Sign the JAR file after build
tasks.register<Exec>("signJar") {
    group = "security"
    description = "Sign the application JAR with keystore"
    dependsOn("generateKeystore", "jvmJar")

    val keystoreFile = file("${project.buildDir}/keystore/payroll-desktop.jks")
    val jarFile = tasks.named<Jar>("jvmJar").get().archiveFile.get().asFile

    commandLine(
        "jarsigner",
        "-keystore", keystoreFile.absolutePath,
        "-storepass", "changeit",
        "-keypass", "changeit",
        "-signedjar", jarFile.absolutePath,
        jarFile.absolutePath,
        "payroll-desktop"
    )

    doLast {
        println("✅ JAR signed successfully: ${jarFile.name}")
    }
}

// Verify JAR signature
tasks.register<Exec>("verifyJarSignature") {
    group = "security"
    description = "Verify the JAR signature"
    dependsOn("signJar")

    val jarFile = tasks.named<Jar>("jvmJar").get().archiveFile.get().asFile

    commandLine(
        "jarsigner",
        "-verify",
        "-verbose",
        "-certs",
        jarFile.absolutePath
    )
}

// 🔒 SECURITY: Create obfuscated JAR documentation
// Note: For full ProGuard integration, use gradle-proguard-plugin
// This is a documentation task showing how to use ProGuard manually
tasks.register("obfuscateJarInfo") {
    group = "security"
    description = "Show instructions for JAR obfuscation with ProGuard"

    doLast {
        println("""
        ╔════════════════════════════════════════════════════════════════╗
        ║  🔒 JAR OBFUSCATION WITH PROGUARD                              ║
        ╚════════════════════════════════════════════════════════════════╝

        ProGuard configuration: proguard-desktop.pro

        To obfuscate the JAR manually:

        1. Download ProGuard:
           https://github.com/Guardsquare/proguard/releases

        2. Run ProGuard:
           java -jar proguard.jar @proguard-desktop.pro \
             -injars build/libs/composeApp-jvm.jar \
             -outjars build/libs/composeApp-jvm-obfuscated.jar \
             -libraryjars <java.home>/jmods/java.base.jmod

        3. Verify obfuscation:
           unzip -l build/libs/composeApp-jvm-obfuscated.jar | grep "\.class"
           (Class names should be shortened: a.class, b.class, etc.)

        4. Sign the obfuscated JAR:
           ./gradlew signJar

        ════════════════════════════════════════════════════════════════

        🔒 Security Benefits:
        ✓ Obfuscated class/method names (harder to reverse engineer)
        ✓ Removed debug logging
        ✓ Optimized bytecode (smaller JAR size)
        ✓ Protected sensitive business logic

        ════════════════════════════════════════════════════════════════
        """.trimIndent())
    }
}
