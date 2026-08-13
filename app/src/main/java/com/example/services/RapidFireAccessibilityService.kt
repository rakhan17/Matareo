package com.example.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.*

class RapidFireAccessibilityService : AccessibilityService() {

    private var isFiring = false
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var fireJob: Job? = null

    // For coordinates, we can fetch from SharedPreferences later, 
    // but default to center screen if not set.
    private var targetX = 500f
    private var targetY = 1000f

    override fun onServiceConnected() {
        super.onServiceConnected()
        updateTargetCoordinates()
    }

    private fun updateTargetCoordinates() {
        val prefs = getSharedPreferences("crosshair_prefs", MODE_PRIVATE)
        val offsetX = prefs.getFloat("offsetX", 0f)
        val offsetY = prefs.getFloat("offsetY", 0f)
        
        val displayMetrics = resources.displayMetrics
        targetX = (displayMetrics.widthPixels / 2f) + offsetX
        targetY = (displayMetrics.heightPixels / 2f) + offsetY
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        // Only intercept if Tactical Macro is enabled in settings
        val tacticalPrefs = getSharedPreferences("tactical_prefs", MODE_PRIVATE)
        if (!tacticalPrefs.getBoolean("macroEnabled", false)) {
            return super.onKeyEvent(event)
        }

        if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            when (event.action) {
                KeyEvent.ACTION_DOWN -> {
                    if (!isFiring) {
                        isFiring = true
                        updateTargetCoordinates()
                        startRapidFire()
                    }
                    return true // Consume the event so volume doesn't change
                }
                KeyEvent.ACTION_UP -> {
                    isFiring = false
                    stopRapidFire()
                    return true
                }
            }
        }
        return super.onKeyEvent(event)
    }

    private fun startRapidFire() {
        fireJob = scope.launch {
            while (isFiring) {
                dispatchTap()
                delay(30) // ~33 taps per second if system allows
            }
        }
    }

    private fun stopRapidFire() {
        fireJob?.cancel()
    }

    private fun dispatchTap() {
        val path = Path().apply {
            moveTo(targetX, targetY)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, 10)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gesture, null, null)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not used
    }

    override fun onInterrupt() {
        isFiring = false
        stopRapidFire()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
