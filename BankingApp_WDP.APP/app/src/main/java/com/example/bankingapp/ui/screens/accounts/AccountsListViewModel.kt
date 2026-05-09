package com.example.bankingapp.ui.screens.accounts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bankingapp.data.local.TokenManager
import com.example.bankingapp.data.model.account.AccountResponse
import com.example.bankingapp.data.model.account.CreateAccountRequest
import com.example.bankingapp.data.network.RetrofitClient
import com.example.bankingapp.data.repository.AccountsRepository
import com.example.bankingapp.data.util.DataRefreshBus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AccountsListUiState(
    val isLoading: Boolean = false,
    val accounts: List<AccountResponse> = emptyList(),
    val error: String? = null,
    val showCreateSheet: Boolean = false,
    val selectedType: String = "Current",
    val selectedCurrency: String = "MDL",
    val overdraftLimit: String = "",
    val companyName: String = "",
    val companyNameError: String? = null,
    val isCreating: Boolean = false,
    val createSuccess: String? = null
)

class AccountsListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository   = AccountsRepository()
    private val tokenManager = TokenManager(application)

    private val _uiState = MutableStateFlow(AccountsListUiState())
    val uiState: StateFlow<AccountsListUiState> = _uiState.asStateFlow()

    init {
        loadAccounts()
        viewModelScope.launch {
            DataRefreshBus.events.collect { refresh() }
        }
    }

    // Refresh silent (fără spinner dacă datele există deja)
    fun refresh() {
        val hasData = _uiState.value.accounts.isNotEmpty()
        viewModelScope.launch {
            if (!hasData) _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val token = tokenManager.getAccessToken() ?: ""
                RetrofitClient.setAuthToken(token)
                val accounts = repository.getAccounts()
                _uiState.update { it.copy(isLoading = false, accounts = accounts) }
            } catch (e: retrofit2.HttpException) {
                val msg = when (e.code()) {
                    401  -> "Sesiunea a expirat."
                    else -> "Eroare server (${e.code()})."
                }
                _uiState.update { it.copy(isLoading = false, error = msg) }
            } catch (e: java.io.IOException) {
                _uiState.update { it.copy(isLoading = false, error = "Fără conexiune la internet.") }
            }
        }
    }

    fun loadAccounts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val token = tokenManager.getAccessToken() ?: ""
                RetrofitClient.setAuthToken(token)
                val accounts = repository.getAccounts()
                _uiState.update { it.copy(isLoading = false, accounts = accounts) }
            } catch (e: retrofit2.HttpException) {
                val msg = when (e.code()) {
                    401  -> "Sesiunea a expirat."
                    else -> "Eroare server (${e.code()})."
                }
                _uiState.update { it.copy(isLoading = false, error = msg) }
            } catch (e: java.io.IOException) {
                _uiState.update { it.copy(isLoading = false, error = "Fără conexiune la internet.") }
            }
        }
    }

    fun showCreateSheet() {
        _uiState.update {
            it.copy(
                showCreateSheet = true,
                selectedType     = "Current",
                selectedCurrency = "MDL",
                overdraftLimit   = "",
                companyName      = "",
                companyNameError = null
            )
        }
    }

    fun hideCreateSheet() {
        _uiState.update { it.copy(showCreateSheet = false, companyNameError = null) }
    }

    fun setSelectedType(type: String) {
        _uiState.update { it.copy(selectedType = type, companyNameError = null) }
    }

    fun setSelectedCurrency(currency: String) {
        _uiState.update { it.copy(selectedCurrency = currency) }
    }

    fun setOverdraftLimit(value: String) {
        _uiState.update { it.copy(overdraftLimit = value) }
    }

    fun setCompanyName(name: String) {
        _uiState.update { it.copy(companyName = name, companyNameError = null) }
    }

    fun createAccount() {
        val state = _uiState.value
        if (state.selectedType == "Business" && state.companyName.isBlank()) {
            _uiState.update { it.copy(companyNameError = "Numele companiei este obligatoriu.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true) }
            try {
                val request = CreateAccountRequest(
                    type          = accountTypeToInt(state.selectedType),
                    currency      = state.selectedCurrency,
                    overdraftLimit = if (state.selectedType == "Current" && state.overdraftLimit.isNotBlank())
                        state.overdraftLimit.toDoubleOrNull() else null,
                    companyName   = if (state.selectedType == "Business") state.companyName.trim() else null
                )
                repository.createAccount(request)
                val updated = repository.getAccounts()
                _uiState.update {
                    it.copy(
                        isCreating      = false,
                        showCreateSheet = false,
                        accounts        = updated,
                        createSuccess   = "Cont creat cu succes",
                        selectedType    = "Current",
                        selectedCurrency = "MDL",
                        overdraftLimit  = "",
                        companyName     = ""
                    )
                }
            } catch (e: retrofit2.HttpException) {
                val msg = when (e.code()) {
                    401  -> "Sesiunea a expirat."
                    else -> "Eroare la crearea contului."
                }
                _uiState.update { it.copy(isCreating = false, error = msg) }
            } catch (e: java.io.IOException) {
                _uiState.update { it.copy(isCreating = false, error = "Fără conexiune la internet.") }
            }
        }
    }

    fun clearError() { _uiState.update { it.copy(error = null) } }
    fun clearCreateSuccess() { _uiState.update { it.copy(createSuccess = null) } }
}

private fun accountTypeToInt(type: String): Int = when (type) {
    "Current"  -> 0
    "Savings"  -> 1
    "Business" -> 2
    else       -> 0
}
