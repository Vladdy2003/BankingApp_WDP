package com.example.bankingapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_prefs")

class ThemePreferences(private val context: Context) {

    companion object {
        private val KEY_DARK = booleanPreferencesKey("dark_theme")
    }

    val isDarkTheme: Flow<Boolean> = context.themeDataStore.data.map { it[KEY_DARK] ?: false }

    suspend fun setDarkTheme(dark: Boolean) {
        context.themeDataStore.edit { it[KEY_DARK] = dark }
    }
}
