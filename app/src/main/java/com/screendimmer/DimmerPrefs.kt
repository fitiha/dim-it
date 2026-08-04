package com.screendimmer

import android.content.Context
import android.content.SharedPreferences

class DimmerPrefs(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var dimLevel: Int
        get() = prefs.getInt(KEY_DIM_LEVEL, DEFAULT_DIM_LEVEL)
        set(value) = prefs.edit().putInt(KEY_DIM_LEVEL, value.coerceIn(0, 100)).apply()

    var isActive: Boolean
        get() = prefs.getBoolean(KEY_IS_ACTIVE, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_ACTIVE, value).apply()

    fun getDimAlpha(): Float {
        return dimLevel / 100f
    }

    companion object {
        private const val PREFS_NAME = "dimmer_prefs"
        private const val KEY_DIM_LEVEL = "dim_level"
        private const val KEY_IS_ACTIVE = "is_active"
        private const val DEFAULT_DIM_LEVEL = 50
    }
}
