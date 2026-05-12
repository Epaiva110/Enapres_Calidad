package com.minedu.gob.pe.enaprescalidad.utils

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class CryptoManager @Inject constructor() {

    private val ALGORITHM = "AES"

    private fun getKey(): SecretKey {
        val keyBytes = "1234567890123456".toByteArray() // demo
        return SecretKeySpec(keyBytes, ALGORITHM)
    }

    fun encrypt(text: String): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, getKey())
        val encrypted = cipher.doFinal(text.toByteArray())
        return Base64.encodeToString(encrypted, Base64.DEFAULT)
    }

    fun decrypt(text: String): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, getKey())
        val decoded = Base64.decode(text, Base64.DEFAULT)
        return String(cipher.doFinal(decoded))
    }
}