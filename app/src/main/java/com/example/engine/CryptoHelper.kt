package com.example.engine

import android.util.Log
import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object CryptoHelper {
    private val aesKey = "FAFI_MANAGER_KEY_FAFI_MANAGER_K" // 256-bit key (32 bytes)
    private val keySpec = SecretKeySpec(aesKey.toByteArray(StandardCharsets.UTF_8), "AES")

    fun encrypt(data: String): String {
        return try {
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
            val encryptedBytes = cipher.doFinal(data.toByteArray(StandardCharsets.UTF_8))
            Base64.getEncoder().encodeToString(encryptedBytes)
        } catch (e: Exception) {
            Log.e("CryptoHelper", "AES encryption failed, falling back to base64", e)
            Base64.getEncoder().encodeToString(data.toByteArray(StandardCharsets.UTF_8))
        }
    }

    fun decrypt(encryptedData: String): String {
        return try {
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val decodedBytes = Base64.getDecoder().decode(encryptedData)
            String(cipher.doFinal(decodedBytes), StandardCharsets.UTF_8)
        } catch (e: Exception) {
            Log.e("CryptoHelper", "AES decryption failed, falling back to base64 decode", e)
            try {
                String(Base64.getDecoder().decode(encryptedData), StandardCharsets.UTF_8)
            } catch (ex: Exception) {
                encryptedData // fallback raw
            }
        }
    }
}
