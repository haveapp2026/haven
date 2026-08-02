package com.bitchat.android.haven

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest

object HavenPreferences {

    private const val PREFS_NAME = "haven_secure_prefs"
    private const val KEY_REAL_PIN_HASH = "real_pin_hash"
    private const val KEY_DURESS_PIN_HASH = "duress_pin_hash"
    private const val KEY_VOUCHER_FINGERPRINTS = "voucher_fingerprints"
    private const val DEFAULT_REAL_PIN = "1984"
    private const val DEFAULT_DURESS_PIN = "0000"

    private fun getPrefs(context: Context) = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private fun hash(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun checkPin(context: Context, input: String): PinResult {
        val prefs = getPrefs(context)
        val realHash = prefs.getString(KEY_REAL_PIN_HASH, hash(DEFAULT_REAL_PIN))
        val duressHash = prefs.getString(KEY_DURESS_PIN_HASH, hash(DEFAULT_DURESS_PIN))
        val inputHash = hash(input)
        return when (inputHash) {
            realHash -> PinResult.REAL
            duressHash -> PinResult.DURESS
            else -> PinResult.WRONG
        }
    }

    fun setRealPin(context: Context, pin: String) {
        getPrefs(context).edit().putString(KEY_REAL_PIN_HASH, hash(pin)).apply()
    }

    fun setDuressPin(context: Context, pin: String) {
        getPrefs(context).edit().putString(KEY_DURESS_PIN_HASH, hash(pin)).apply()
    }

    fun addVouchedFingerprint(context: Context, fingerprint: String) {
        val prefs = getPrefs(context)
        val existing = prefs.getString(KEY_VOUCHER_FINGERPRINTS, "") ?: ""
        val set = existing.split(",").filter { it.isNotBlank() }.toMutableSet()
        set.add(fingerprint)
        prefs.edit().putString(KEY_VOUCHER_FINGERPRINTS, set.joinToString(",")).apply()
    }

    fun getVouchedFingerprints(context: Context): Set<String> {
        val prefs = getPrefs(context)
        val raw = prefs.getString(KEY_VOUCHER_FINGERPRINTS, "") ?: ""
        return raw.split(",").filter { it.isNotBlank() }.toSet()
    }

    enum class PinResult { REAL, DURESS, WRONG }
}
