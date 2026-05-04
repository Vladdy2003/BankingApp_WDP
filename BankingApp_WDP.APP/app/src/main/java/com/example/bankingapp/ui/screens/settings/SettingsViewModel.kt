package com.example.bankingapp.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bankingapp.data.local.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val fullName: String        = "",
    val email: String           = "",
    val showLogoutDialog: Boolean = false,
    val isLoggedOut: Boolean    = false
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init { loadUserInfo() }

    private fun loadUserInfo() {
        viewModelScope.launch {
            val fullName = tokenManager.getFullName() ?: ""
            val email    = tokenManager.getEmail()    ?: ""
            _uiState.update { it.copy(fullName = fullName, email = email) }
        }
    }

    fun showLogoutDialog() { _uiState.update { it.copy(showLogoutDialog = true) } }
    fun hideLogoutDialog() { _uiState.update { it.copy(showLogoutDialog = false) } }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clear()
            _uiState.update { it.copy(isLoggedOut = true) }
        }
    }
}
