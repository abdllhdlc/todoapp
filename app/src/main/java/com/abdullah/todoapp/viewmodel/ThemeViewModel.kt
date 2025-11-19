package com.abdullah.todoapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.abdullah.todoapp.ui.theme.ThemeMode
import com.abdullah.todoapp.ui.theme.ThemePreferences
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val initialTheme: ThemeMode = runBlocking(Dispatchers.IO) {
        ThemePreferences.themeModeFlow(getApplication()).first()
    }

    val themeMode: StateFlow<ThemeMode> = ThemePreferences
        .themeModeFlow(getApplication())
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = initialTheme
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            ThemePreferences.setThemeMode(getApplication(), mode)
        }
    }

    companion object {
        fun provideFactory(
            application: Application
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return ThemeViewModel(application) as T
            }
        }
    }
} 