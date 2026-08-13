package com.example.utils

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CrosshairConfig(
    val preset: Int = 1,
    val imageUri: String = "",
    val scale: Float = 1.0f,
    val alpha: Float = 1.0f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f
)

class CrosshairPrefs private constructor(context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: CrosshairPrefs? = null

        fun getInstance(context: Context): CrosshairPrefs {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CrosshairPrefs(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    private val prefs: SharedPreferences = context.getSharedPreferences("crosshair_prefs", Context.MODE_PRIVATE)

    private val _configFlow = MutableStateFlow(loadConfig())
    val configFlow: StateFlow<CrosshairConfig> = _configFlow.asStateFlow()

    private fun loadConfig(): CrosshairConfig {
        return CrosshairConfig(
            preset = prefs.getInt("preset", 1),
            imageUri = prefs.getString("imageUri", "") ?: "",
            scale = prefs.getFloat("scale", 1.0f),
            alpha = prefs.getFloat("alpha", 1.0f),
            offsetX = prefs.getFloat("offsetX", 0f),
            offsetY = prefs.getFloat("offsetY", 0f)
        )
    }

    fun updateConfig(config: CrosshairConfig) {
        prefs.edit().apply {
            putInt("preset", config.preset)
            putString("imageUri", config.imageUri)
            putFloat("scale", config.scale)
            putFloat("alpha", config.alpha)
            putFloat("offsetX", config.offsetX)
            putFloat("offsetY", config.offsetY)
            apply()
        }
        _configFlow.value = config
    }
}
