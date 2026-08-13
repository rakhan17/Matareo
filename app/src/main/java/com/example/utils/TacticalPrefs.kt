package com.example.utils

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TacticalConfig(
    val macroEnabled: Boolean = false,
    val shadowPiercerEnabled: Boolean = false,
    val sensitivityOverdriveEnabled: Boolean = false,
    val sniperScopeEnabled: Boolean = false
)

class TacticalPrefs private constructor(context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: TacticalPrefs? = null

        fun getInstance(context: Context): TacticalPrefs {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TacticalPrefs(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    private val prefs: SharedPreferences = context.getSharedPreferences("tactical_prefs", Context.MODE_PRIVATE)

    private val _configFlow = MutableStateFlow(loadConfig())
    val configFlow: StateFlow<TacticalConfig> = _configFlow.asStateFlow()

    private fun loadConfig(): TacticalConfig {
        return TacticalConfig(
            macroEnabled = prefs.getBoolean("macroEnabled", false),
            shadowPiercerEnabled = prefs.getBoolean("shadowPiercerEnabled", false),
            sensitivityOverdriveEnabled = prefs.getBoolean("sensitivityOverdriveEnabled", false),
            sniperScopeEnabled = prefs.getBoolean("sniperScopeEnabled", false)
        )
    }

    fun updateConfig(config: TacticalConfig) {
        prefs.edit().apply {
            putBoolean("macroEnabled", config.macroEnabled)
            putBoolean("shadowPiercerEnabled", config.shadowPiercerEnabled)
            putBoolean("sensitivityOverdriveEnabled", config.sensitivityOverdriveEnabled)
            putBoolean("sniperScopeEnabled", config.sniperScopeEnabled)
            apply()
        }
        _configFlow.value = config
    }
}
