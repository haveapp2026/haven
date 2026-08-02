package com.bitchat.android.haven

import android.content.Context
import android.util.Log

/**
 * Local contact book: maps noise fingerprints to user-chosen display names.
 * Stored in SharedPreferences under "haven_contacts". Intended as a thin overlay
 * on top of the mesh identity system — users scan a QR once, assign a friendly name,
 * and that name persists across sessions even when the peer is offline.
 */
object ContactBook {

    private const val TAG = "ContactBook"
    private const val PREFS = "haven_contacts"
    private const val KEY_PREFIX = "name_"

    fun setName(context: Context, fingerprint: String, name: String) {
        val clean = name.trim()
        if (clean.isBlank()) {
            removeName(context, fingerprint)
            return
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PREFIX + fingerprint.lowercase(), clean)
            .apply()
        Log.i(TAG, "Saved contact name for ${fingerprint.take(8)}: $clean")
    }

    fun getName(context: Context, fingerprint: String): String? {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_PREFIX + fingerprint.lowercase(), null)
    }

    fun removeName(context: Context, fingerprint: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_PREFIX + fingerprint.lowercase())
            .apply()
    }

    fun getAllNames(context: Context): Map<String, String> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.all
            .filterKeys { it.startsWith(KEY_PREFIX) }
            .mapKeys { (k, _) -> k.removePrefix(KEY_PREFIX) }
            .mapValues { (_, v) -> v as? String ?: "" }
            .filterValues { it.isNotBlank() }
    }

    /** Wipe all contact names — called by DuressManager.wipeRealData. */
    fun wipeAll(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().clear().apply()
        Log.i(TAG, "Contact book wiped")
    }
}
