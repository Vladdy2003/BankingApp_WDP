package com.example.bankingapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "banking_prefs")

class TokenManager(private val context: Context) {

    companion object {
        val KEY_ACCESS_TOKEN  = stringPreferencesKey("access_token")
        val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val KEY_FULL_NAME     = stringPreferencesKey("full_name")
        val KEY_EMAIL         = stringPreferencesKey("email")
        val KEY_USER_ID       = stringPreferencesKey("user_id")
        val KEY_PHONE         = stringPreferencesKey("phone")
        val KEY_ADDRESS       = stringPreferencesKey("address")
        val KEY_DATE_OF_BIRTH = stringPreferencesKey("date_of_birth")
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN]  = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun saveUserInfo(fullName: String, email: String, userId: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_FULL_NAME] = fullName
            prefs[KEY_EMAIL]     = email
            prefs[KEY_USER_ID]   = userId
        }
    }

    suspend fun getAccessToken(): String? =
        context.dataStore.data.map { it[KEY_ACCESS_TOKEN] }.first()

    suspend fun getRefreshToken(): String? =
        context.dataStore.data.map { it[KEY_REFRESH_TOKEN] }.first()

    suspend fun getFullName(): String? =
        context.dataStore.data.map { it[KEY_FULL_NAME] }.first()

    suspend fun getEmail(): String? =
        context.dataStore.data.map { it[KEY_EMAIL] }.first()

    suspend fun getUserId(): String? =
        context.dataStore.data.map { it[KEY_USER_ID] }.first()

    suspend fun getPhone(): String? =
        context.dataStore.data.map { it[KEY_PHONE] }.first()

    suspend fun getAddress(): String? =
        context.dataStore.data.map { it[KEY_ADDRESS] }.first()

    suspend fun getDateOfBirth(): String? =
        context.dataStore.data.map { it[KEY_DATE_OF_BIRTH] }.first()

    suspend fun saveProfileInfo(fullName: String, phone: String, address: String, dateOfBirth: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_FULL_NAME]     = fullName
            prefs[KEY_PHONE]         = phone
            prefs[KEY_ADDRESS]       = address
            prefs[KEY_DATE_OF_BIRTH] = dateOfBirth
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
