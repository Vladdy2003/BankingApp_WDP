package com.example.bankingapp.ui.screens.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bankingapp.data.local.TokenManager
import com.example.bankingapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String         = "",
    val password: String      = "",
    val emailError: String?   = null,
    val passwordError: String? = null,
    val isLoading: Boolean    = false,
    val error: String?        = null,
    val isSuccess: Boolean    = false
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository   = AuthRepository()
    private val tokenManager = TokenManager(application)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun login() {
        val state = _uiState.value
        if (!validate(state)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = repository.login(state.email.trim(), state.password)
                tokenManager.saveTokens(response.accessToken, response.refreshToken)
                tokenManager.saveUserInfo(response.fullName, response.email, response.userId)
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: retrofit2.HttpException) {
                val message = when (e.code()) {
                    401  -> "Email sau parolă incorectă."
                    400  -> "Date de autentificare invalide."
                    else -> "Eroare server (${e.code()}). Încercați din nou."
                }
                _uiState.update { it.copy(isLoading = false, error = message) }
            } catch (e: java.io.IOException) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Fără conexiune la internet.")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "A apărut o eroare neașteptată.")
                }
            }
        }
    }

    private fun validate(state: LoginUiState): Boolean {
        var valid = true

        val emailError = when {
            state.email.isBlank()             -> "Emailul este obligatoriu."
            !state.email.contains("@")        -> "Introduceți un email valid."
            else                              -> null
        }
        val passwordError = when {
            state.password.isEmpty()          -> "Parola este obligatorie."
            else                              -> null
        }

        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            valid = false
        }
        return valid
    }
}
