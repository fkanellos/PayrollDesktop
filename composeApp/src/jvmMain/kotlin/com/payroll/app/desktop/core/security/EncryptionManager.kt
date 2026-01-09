package com.payroll.app.desktop.core.security

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Encryption manager for securing sensitive configuration and data
 *
 * 🔒 Security Features:
 * - AES-256-CBC encryption
 * - PBKDF2 key derivation (100,000 iterations)
 * - Random IV for each encryption
 * - Machine-specific key generation
 *
 * Use cases:
 * - Config file encryption (Google Sheets IDs, API URLs)
 * - Token storage encryption
 * - Sensitive data at rest
 */
object EncryptionManager {

    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private const val KEY_SIZE = 256
    private const val IV_SIZE = 16
    private const val PBKDF2_ITERATIONS = 100_000
    private const val SALT = "PayrollDesktop_Encryption_Salt_v1"

    /**
     * Generate encryption key based on machine + user identity
     * Same approach as DatabaseKeyManager for consistency
     */
    private fun generateEncryptionKey(): SecretKeySpec {
        val userHome = System.getProperty("user.home")
        val userName = System.getProperty("user.name")
        val hostname = try {
            java.net.InetAddress.getLocalHost().hostName
        } catch (e: Exception) {
            "unknown-host"
        }

        // Combine all sources
        val keyMaterial = "$SALT:$userHome:$userName:$hostname"

        // Use PBKDF2 for key derivation (stronger than simple SHA-256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(
            keyMaterial.toCharArray(),
            SALT.toByteArray(StandardCharsets.UTF_8),
            PBKDF2_ITERATIONS,
            KEY_SIZE
        )
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, ALGORITHM)
    }

    /**
     * Encrypt string data
     *
     * @param plaintext The data to encrypt
     * @return Base64-encoded encrypted data with IV prepended
     *
     * Format: IV (16 bytes) + Encrypted Data
     * Both are Base64-encoded together for easy storage
     */
    fun encrypt(plaintext: String): String {
        val key = generateEncryptionKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)

        // Generate random IV for this encryption
        val iv = ByteArray(IV_SIZE)
        SecureRandom().nextBytes(iv)
        val ivSpec = IvParameterSpec(iv)

        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)
        val encrypted = cipher.doFinal(plaintext.toByteArray(StandardCharsets.UTF_8))

        // Prepend IV to encrypted data
        val combined = iv + encrypted

        // Encode to Base64 for storage
        return Base64.getEncoder().encodeToString(combined)
    }

    /**
     * Decrypt string data
     *
     * @param encryptedBase64 Base64-encoded encrypted data (with IV prepended)
     * @return Decrypted plaintext
     * @throws Exception if decryption fails (wrong key, corrupted data)
     */
    fun decrypt(encryptedBase64: String): String {
        val key = generateEncryptionKey()
        val combined = Base64.getDecoder().decode(encryptedBase64)

        // Extract IV (first 16 bytes)
        val iv = combined.copyOfRange(0, IV_SIZE)
        val encrypted = combined.copyOfRange(IV_SIZE, combined.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)

        val decrypted = cipher.doFinal(encrypted)
        return String(decrypted, StandardCharsets.UTF_8)
    }

    /**
     * Check if a string is encrypted (Base64 format check)
     */
    fun isEncrypted(value: String): Boolean {
        return try {
            // Valid Base64 should have length multiple of 4 and proper characters
            Base64.getDecoder().decode(value)
            // If we can decode it, it's likely encrypted
            value.length >= 32 && value.matches(Regex("^[A-Za-z0-9+/=]+$"))
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Encrypt a file in-place
     * Useful for config files
     */
    fun encryptFile(filePath: String, plaintext: String) {
        val encrypted = encrypt(plaintext)
        java.io.File(filePath).writeText(encrypted)
    }

    /**
     * Decrypt a file
     */
    fun decryptFile(filePath: String): String {
        val encrypted = java.io.File(filePath).readText()
        return decrypt(encrypted)
    }
}
