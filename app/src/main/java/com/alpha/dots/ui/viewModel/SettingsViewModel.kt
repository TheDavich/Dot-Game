package com.alpha.dots.ui.viewModel

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private object PreferencesKeys {
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback") // Correct key type
    }

    // Flow to collect haptic feedback setting
    val hapticFeedbackEnabled: StateFlow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.HAPTIC_FEEDBACK] ?: true // Default to true (enabled)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // Set haptic feedback
    fun setHapticFeedbackEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.HAPTIC_FEEDBACK] = enabled
            }
        }
    }
}