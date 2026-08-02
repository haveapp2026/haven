package com.bitchat.android.haven

import android.content.Context
import android.util.Log

object VouchManager {

    private const val TAG = "VouchManager"

    // Called when user scans another peer's QR code - vouches for them
    fun vouchForPeer(context: Context, peerFingerprint: String) {
        HavenPreferences.addVouchedFingerprint(context, peerFingerprint)
        Log.i(TAG, "Vouched for peer: ${peerFingerprint.take(8)}...")
    }

    // Returns true if a peer is vouched and allowed to broadcast
    fun isVouched(context: Context, peerFingerprint: String): Boolean {
        return HavenPreferences.getVouchedFingerprints(context).contains(peerFingerprint)
    }

    // Returns all vouched fingerprints
    fun getVouchedPeers(context: Context): Set<String> {
        return HavenPreferences.getVouchedFingerprints(context)
    }

    // Remove a vouched peer (ban)
    fun revokeVouch(context: Context, peerFingerprint: String) {
        val current = HavenPreferences.getVouchedFingerprints(context).toMutableSet()
        current.remove(peerFingerprint)
        // Re-save the updated set
        val prefs = context.getSharedPreferences("haven_vouch_temp", Context.MODE_PRIVATE)
        // Delegate back through HavenPreferences for encrypted storage
        current.forEach { HavenPreferences.addVouchedFingerprint(context, it) }
        Log.i(TAG, "Revoked vouch for: ${peerFingerprint.take(8)}...")
    }
}
