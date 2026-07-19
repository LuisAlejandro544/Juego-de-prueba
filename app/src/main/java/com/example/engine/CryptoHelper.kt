package com.example.engine

import android.util.Log
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoHelper {
    private val aesKey = "FAFI_MANAGER_KEY_FAFI_MANAGER_K3" // 32-bit key (256-bit AES)
    private val keySpec = SecretKeySpec(aesKey.toByteArray(StandardCharsets.UTF_8), "AES")
    private val secureRandom = SecureRandom()

    fun encrypt(data: String): String {
        return try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val iv = ByteArray(16)
            secureRandom.nextBytes(iv)
            val ivSpec = IvParameterSpec(iv)
            
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
            val encryptedBytes = cipher.doFinal(data.toByteArray(StandardCharsets.UTF_8))
            
            // Combine IV and Ciphertext
            val combinedBytes = ByteArray(iv.size + encryptedBytes.size)
            System.arraycopy(iv, 0, combinedBytes, 0, iv.size)
            System.arraycopy(encryptedBytes, 0, combinedBytes, iv.size, encryptedBytes.size)
            
            android.util.Base64.encodeToString(combinedBytes, android.util.Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e("CryptoHelper", "AES/CBC encryption failed, falling back to base64", e)
            try {
                android.util.Base64.encodeToString(data.toByteArray(StandardCharsets.UTF_8), android.util.Base64.NO_WRAP)
            } catch (ex: Exception) {
                data
            }
        }
    }

    fun decrypt(encryptedData: String): String {
        return try {
            val decodedBytes = android.util.Base64.decode(encryptedData, android.util.Base64.DEFAULT)
            if (decodedBytes.size < 16) {
                return tryLegacyDecrypt(encryptedData)
            }
            
            val iv = ByteArray(16)
            System.arraycopy(decodedBytes, 0, iv, 0, 16)
            val ivSpec = IvParameterSpec(iv)
            
            val ciphertext = ByteArray(decodedBytes.size - 16)
            System.arraycopy(decodedBytes, 16, ciphertext, 0, ciphertext.size)
            
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
            String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8)
        } catch (e: Exception) {
            Log.e("CryptoHelper", "AES/CBC decryption failed, trying legacy decrypt", e)
            tryLegacyDecrypt(encryptedData)
        }
    }

    private fun tryLegacyDecrypt(encryptedData: String): String {
        val decodedBytes = try {
            android.util.Base64.decode(encryptedData, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            return encryptedData
        }

        // We will try various potential keys to find a valid decryption
        val potentialKeys = listOf(
            "FAFI_MANAGER_KEY_FAFI_MANAGER_K3",
            "FAFI_MANAGER_KEY_FAFI_MANAGER_K2",
            "FAFI_MANAGER_KEY_FAFI_MANAGER_K1",
            "FAFI_MANAGER_KEY_FAFI_MANAGER_K".padEnd(32, '0'),
            "FAFI_MANAGER_KEY_FAFI_MANAGER_K".padEnd(32, '\u0000'),
            "FAFI_MANAGER_KEY"
        )

        for (key in potentialKeys) {
            try {
                val keySpec = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "AES")
                
                // Try ECB first (as legacy might have used ECB)
                try {
                    val cipherECB = Cipher.getInstance("AES/ECB/PKCS5Padding")
                    cipherECB.init(Cipher.DECRYPT_MODE, keySpec)
                    val decrypted = cipherECB.doFinal(decodedBytes)
                    return String(decrypted, StandardCharsets.UTF_8)
                } catch (e: Exception) {
                    // Ignore and try CBC fallback
                }

                // Try CBC fallback if legacy used CBC but failed earlier
                if (decodedBytes.size >= 16) {
                    val iv = ByteArray(16)
                    System.arraycopy(decodedBytes, 0, iv, 0, 16)
                    val ivSpec = IvParameterSpec(iv)
                    val ciphertext = ByteArray(decodedBytes.size - 16)
                    System.arraycopy(decodedBytes, 16, ciphertext, 0, ciphertext.size)

                    val cipherCBC = Cipher.getInstance("AES/CBC/PKCS5Padding")
                    cipherCBC.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
                    val decrypted = cipherCBC.doFinal(ciphertext)
                    return String(decrypted, StandardCharsets.UTF_8)
                }
            } catch (e: Exception) {
                // Continue trying other keys
            }
        }

        // Last resort: plain decode
        return try {
            String(decodedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            encryptedData
        }
    }
}
