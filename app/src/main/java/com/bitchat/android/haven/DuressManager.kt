package com.bitchat.android.haven

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorManager
import android.util.Log

object DuressManager {

    private const val TAG = "DuressManager"
    private const val SHAKE_THRESHOLD = 2.5f
    private const val SHAKE_COUNT_REQUIRED = 3
    private const val SHAKE_RESET_MS = 3000L

    private var shakeCount = 0
    private var lastShakeTime = 0L

    // Deletes all Haven message/chat data, leaves decoy notes intact
    fun wipeRealData(context: Context) {
        try {
            // Clear the BitChat identity and message stores
            val prefsToWipe = listOf(
                "bitchat_identity",
                "bitchat_messages",
                "bitchat_channels",
                "bitchat_favorites",
                "bitchat_peers"
            )
            prefsToWipe.forEach { name ->
                context.getSharedPreferences(name, Context.MODE_PRIVATE)
                    .edit().clear().apply()
            }
            Log.i(TAG, "Real data wiped")
        } catch (e: Exception) {
            Log.e(TAG, "Wipe failed", e)
        }
    }

    fun onShakeDetected(context: Context, onWipe: () -> Unit) {
        val now = System.currentTimeMillis()
        if (now - lastShakeTime > SHAKE_RESET_MS) {
            shakeCount = 0
        }
        shakeCount++
        lastShakeTime = now
        if (shakeCount >= SHAKE_COUNT_REQUIRED) {
            shakeCount = 0
            wipeRealData(context)
            onWipe()
        }
    }

    fun registerShakeListener(
        context: Context,
        onWipe: () -> Unit
    ): Pair<SensorManager, android.hardware.SensorEventListener>? {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            ?: return null
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            ?: return null

        val listener = object : android.hardware.SensorEventListener {
            override fun onSensorChanged(event: android.hardware.SensorEvent) {
                val x = event.values[0] / SensorManager.GRAVITY_EARTH
                val y = event.values[1] / SensorManager.GRAVITY_EARTH
                val z = event.values[2] / SensorManager.GRAVITY_EARTH
                val gForce = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                if (gForce > SHAKE_THRESHOLD) {
                    onShakeDetected(context, onWipe)
                }
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        return Pair(sensorManager, listener)
    }
}
