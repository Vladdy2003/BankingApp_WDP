package com.example.bankingapp.ui.screens.accounts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bankingapp.data.local.TokenManager
import com.example.bankingapp.data.model.account.AccountResponse
import com.example.bankingapp.data.model.account.UpdateAccountRequest
import com.example.bankingapp.data.model.transaction.TransactionResponse
import com.example.bankingapp.data.network.RetrofitClient
import com.example.bankingapp.data.repository.AccountsRepository
import com.example.bankingapp.data.util.DataRefreshBus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AccountDetailUiState(
    val isLoading: Boolean                      = true,
    val account: AccountResponse?               = null,
    val transactions: List<TransactionResponse> = emptyList(),
    val selectedFilter: String                  = "ALL",
    val currentPage: Int                        = 1,
    val isLoadingMore: Boolean                  = false,
    val hasMore: Boolean                        = true,
    val error: String?                          = null,
    // Edit sheet
    val showEditSheet: Boolean                  = false,
    val isUpdating: Boolean                     = false,
    val editCurrency: String                    = "",
    val editOverdraftLimit: String              = "",
    val editInterestRate: String                = "",
    val editCompanyName: String                 = "",
    val editCurrencyError: String?              = null,
    val updateSuccess: String?                  = null,
    // Close dialog
    val showCloseDialog: Boolean                = false,
    val isDeleting: Boolean                     = false,
    val accountClosed: Boolean                  = false
)

class AccountDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository   = AccountsRepository()
    private val tokenManager = TokenManager(application)

    private val _uiState = MutableStateFlow(AccountDetailUiState())
    val uiState: StateFlow<AccountDetailUiState> = _uiState.asStateFlow()

    private var accountId: String = ""

    fun init(id: String) {
        if (accountId == id) return
        accountId = id
        loadAccount()
        loadTransactions(reset = true)
        viewModelScope.launch {
            DataRefreshBus.events.collect {
                if (accountId.isNotEmpty()) {
                    loadAccount()
                    loadTransactions(reset = true)
                }
            }
        }
    }

    // ── Load account ──────────────────────────────────────────────────────────

    fun loadAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val token = tokenManager.getAccessToken() ?: ""
                RetrofitClient.setAuthToken(token)
                val account = repository.getAccount(accountId)
                _uiState.update {
                    it.copy(
                        isLoading          = false,
                        account            = account,
                        editCurrency       = account.currency,
                        editOverdraftLimit = account.overdraftLimit?.toString() ?: "",
                        editInterestRate   = account.interestRate?.toString()   ?: "",
                        editCompanyName    = account.companyName                ?: ""
                    )
                }
            } catch (e: retrofit2.HttpException) {
                val msg = when (e.code()) {
                    401  -> "Sesiunea a expirat."
                    404  -> "Contul nu a fost găsit."
                    else -> "Eroare server (${e.code()})."
                }
                _uiState.update { it.copy(isLoading = false, error = msg) }
            } catch (e: java.io.IOException) {
                _uiState.update { it.copy(isLoading = false, error = "Fără conexiune la internet.") }
            }
        }
    }

    // ── Load transactions (infinite scroll) ──────────────────────────────────

    fun loadTransactions(reset: Boolean = false) {
        val state = _uiState.value
        if (!reset && (state.isLoadingMore || !state.hasMore)) return
        val page = if (reset) 1 else state.currentPage

        viewModelScope.launch {
            if (!reset) _uiState.update { it.copy(isLoadingMore = true) }
            try {
                val result = repository.getAccountTransactions(accountId, page, 20)
                _uiState.update {
                    it.copy(
                        transactions  = if (reset) result else it.transactions + result,
                        currentPage   = page + 1,
                        hasMore       = result.size == 20,
                        isLoadingMore = false
                    )
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    fun setFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    // ── Edit account ──────────────────────────────────────────────────────────

    fun showEditSheet() {
        val account = _uiState.value.account ?: return
        _uiState.update {
            it.copy(
                showEditSheet      = true,
                editCurrency       = account.currency,
                editOverdraftLimit = account.overdraftLimit?.toString() ?: "",
                editInterestRate   = account.interestRate?.toString()   ?: "",
                editCompanyName    = account.companyName                ?: "",
                editCurrencyError  = null
            )
        }
    }

    fun hideEditSheet() {
        _uiState.update { it.copy(showEditSheet = false, editCurrencyError = null) }
    }

    fun setEditCurrency(v: String)      { _uiState.update { it.copy(editCurrency = v, editCurrencyError = null) } }
    fun setEditOverdraftLimit(v: String){ _uiState.update { it.copy(editOverdraftLimit = v) } }
    fun setEditInterestRate(v: String)  { _uiState.update { it.copy(editInterestRate = v) } }
    fun setEditCompanyName(v: String)   { _uiState.update { it.copy(editCompanyName = v) } }

    fun updateAccount() {
        val state = _uiState.value
        if (state.editCurrency.isBlank()) {
            _uiState.update { it.copy(editCurrencyError = "Moneda este obligatorie.") }
            return
        }
        val accountType = state.account?.type ?: ""
        val isCurrent  = accountType == "0" || accountType.equals("Current",  ignoreCase = true)
        val isSavings  = accountType == "1" || accountType.equals("Savings",  ignoreCase = true)
        val isBusiness = accountType == "2" || accountType.equals("Business", ignoreCase = true)

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            try {
                val request = UpdateAccountRequest(
                    currency       = state.editCurrency,
                    overdraftLimit = if (isCurrent)  state.editOverdraftLimit.toDoubleOrNull() else null,
                    interestRate   = if (isSavings)  state.editInterestRate.toDoubleOrNull()   else null,
                    companyName    = if (isBusiness) state.editCompanyName.trim().takeIf { it.isNotBlank() } else null
                )
                val updated = repository.updateAccount(accountId, request)
                _uiState.update {
                    it.copy(
                        isUpdating    = false,
                        showEditSheet = false,
                        account       = updated,
                        updateSuccess = "Cont actualizat cu succes"
                    )
                }
            } catch (e: retrofit2.HttpException) {
                val msg = when (e.code()) {
                    401  -> "Sesiunea a expirat."
                    else -> "Eroare la actualizarea contului."
                }
                _uiState.update { it.copy(isUpdating = false, error = msg) }
            } catch (e: java.io.IOException) {
                _uiState.update { it.copy(isUpdating = false, error = "Fără conexiune la internet.") }
            }
        }
    }

    // ── Close account ─────────────────────────────────────────────────────────

    fun showCloseDialog()  { _uiState.update { it.copy(showCloseDialog = true) } }
    fun hideCloseDialog()  { _uiState.update { it.copy(showCloseDialog = false) } }

    fun closeAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            try {
                repository.deleteAccount(accountId)
                _uiState.update { it.copy(isDeleting = false, showCloseDialog = false, accountClosed = true) }
            } catch (e: retrofit2.HttpException) {
                val msg = when (e.code()) {
                    400  -> "Nu se poate închide contul. Asigurați-vă că soldul este 0."
                    401  -> "Sesiunea a expirat."
                    else -> "Eroare la închiderea contului."
                }
                _uiState.update { it.copy(isDeleting = false, showCloseDialog = false, error = msg) }
            } catch (e: java.io.IOException) {
                _uiState.update { it.copy(isDeleting = false, showCloseDialog = false, error = "Fără conexiune la internet.") }
            }
        }
    }

    // ── Utils ─────────────────────────────────────────────────────────────────

    fun clearError()         { _uiState.update { it.copy(error = null) } }
    fun clearUpdateSuccess() { _uiState.update { it.copy(updateSuccess = null) } }
}
