package com.example.engine

import android.util.Log
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.Base64
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
            
            Base64.getEncoder().encodeToString(combinedBytes)
        } catch (e: Exception) {
            Log.e("CryptoHelper", "AES/CBC encryption failed, falling back to base64", e)
            Base64.getEncoder().encodeToString(data.toByteArray(StandardCharsets.UTF_8))
        }
    }

    fun decrypt(encryptedData: String): String {
        return try {
            val decodedBytes = Base64.getDecoder().decode(encryptedData)
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
        return try {
            val legacyKey = "FAFI_MANAGER_KEY_FAFI_MANAGER_K"
            val legacyKeySpec = SecretKeySpec(legacyKey.toByteArray(StandardCharsets.UTF_8), "AES")
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, legacyKeySpec)
            val decodedBytes = Base64.getDecoder().decode(encryptedData)
            String(cipher.doFinal(decodedBytes), StandardCharsets.UTF_8)
        } catch (e: Exception) {
            Log.e("CryptoHelper", "Legacy decryption failed, trying plain base64 decode", e)
            try {
                String(Base64.getDecoder().decode(encryptedData), StandardCharsets.UTF_8)
            } catch (ex: Exception) {
                encryptedData
            }
        }
    }
}
