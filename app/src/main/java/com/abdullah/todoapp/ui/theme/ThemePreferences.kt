package com.abdullah.todoapp.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATA_STORE_NAME = "user_prefs"

private val Context.dataStore by preferencesDataStore(name = DATA_STORE_NAME)

object ThemePreferences {
    private val KEY_THEME_MODE = intPreferencesKey("theme_mode")

    fun themeModeFlow(context: Context): Flow<ThemeMode> =
        context.dataStore.data.map { prefs ->
            when (prefs[KEY_THEME_MODE] ?: ThemeMode.SYSTEM_DEFAULT.ordinal) {
                ThemeMode.LIGHT.ordinal -> ThemeMode.LIGHT
                ThemeMode.DARK.ordinal -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM_DEFAULT
            }
        }

    suspend fun setThemeMode(context: Context, mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.ordinal
        }
    }
}


