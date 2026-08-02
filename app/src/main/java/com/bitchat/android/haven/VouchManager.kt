package com.bitchat.android.haven

import android.content.Context
import android.util.Log

object VouchManager {

    private const val TAG = "VouchManager"
    private const val PREFS = "haven_vouch"
    private const val KEY = "vouched_fingerprints"

    fun vouchForPeer(context: Context, fingerprint: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val existing = prefs.getString(KEY, "") ?: ""
        val set = existing.split(",").filter { it.isNotBlank() }.toMutableSet()
        set.add(fingerprint)
        prefs.edit().putString(KEY, set.joinToString(",")).apply()
        Log.i(TAG, "Vouched: ${fingerprint.take(8)}")
    }

    fun isVouched(context: Context, fingerprint: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY, "") ?: ""
        return raw.split(",").contains(fingerprint)
    }

    fun getVouchedPeers(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY, "") ?: ""
        return raw.split(",").filter { it.isNotBlank() }.toSet()
    }

    fun revokeVouch(context: Context, fingerprint: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY, "") ?: ""
        val updated = raw.split(",").filter { it.isNotBlank() && it != fingerprint }
        prefs.edit().putString(KEY, updated.joinToString(",")).apply()
        Log.i(TAG, "Revoked: ${fingerprint.take(8)}")
    }
}
