package com.example.marketplacesiswa.ui.viewmodels

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "marketplace_prefs")

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")

    private val _isOnboardingCompleted = MutableStateFlow<Boolean?>(null)
    val isOnboardingCompleted: StateFlow<Boolean?> = _isOnboardingCompleted.asStateFlow()

    init {
        viewModelScope.launch {
            application.dataStore.data
                .map { prefs -> prefs[ONBOARDING_COMPLETED] ?: false }
                .collect { completed ->
                    _isOnboardingCompleted.value = completed
                }
        }
    }

    fun setOnboardingCompleted() {
        viewModelScope.launch {
            getApplication<Application>().dataStore.edit { prefs ->
                prefs[ONBOARDING_COMPLETED] = true
            }
        }
    }
}
