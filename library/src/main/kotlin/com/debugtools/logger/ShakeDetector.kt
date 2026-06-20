package com.debugtools.logger

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Shake detector that triggers when device is shaken.
 * Uses accelerometer to detect sudden movement.
 */
class ShakeDetector(
    context: Context,
    private val onShake: () -> Unit
) : SensorEventListener {
    
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    
    private var lastShakeTime = 0L
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    
    companion object {
        private const val SHAKE_THRESHOLD = 10f // Reduced from 12f - easier to trigger
        private const val SHAKE_INTERVAL_MS = 1000L // Min time between shakes
    }
    
    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }
    
    fun stop() {
        sensorManager.unregisterListener(this)
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return
        
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastShakeTime < SHAKE_INTERVAL_MS) return
        
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        
        val deltaX = x - lastX
        val deltaY = y - lastY
        val deltaZ = z - lastZ
        
        val acceleration = sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ)
        
        if (acceleration > SHAKE_THRESHOLD) {
            lastShakeTime = currentTime
            onShake()
        }
        
        lastX = x
        lastY = y
        lastZ = z
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed
    }
}
