package com.example.bankingapp.ui.screens.transactions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bankingapp.data.local.TokenManager
import com.example.bankingapp.data.model.account.AccountResponse
import com.example.bankingapp.data.model.transaction.DepositRequest
import com.example.bankingapp.data.model.transaction.TransferRequest
import com.example.bankingapp.data.model.transaction.WithdrawRequest
import com.example.bankingapp.data.network.RetrofitClient
import com.example.bankingapp.data.repository.AccountsRepository
import com.example.bankingapp.data.repository.TransactionsRepository
import com.example.bankingapp.data.util.DataRefreshBus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TransactionSheetUiState(
    val accounts: List<AccountResponse> = emptyList(),
    val isLoadingAccounts: Boolean = false,
    // shared inputs
    val selectedAccountId: String = "",
    val amount: String = "",
    val description: String = "",
    val amountError: String? = null,
    // transfer-specific
    val transferMode: String = "OWN",   // "OWN" | "IBAN"
    val toOwnAccountId: String = "",
    val toIban: String = "",
    val ibanError: String? = null,
    // submission
    val isSubmitting: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null
)

class TransactionSheetViewModel(application: Application) : AndroidViewModel(application) {

    private val accountsRepository     = AccountsRepository()
    private val transactionsRepository = TransactionsRepository()
    private val tokenManager           = TokenManager(application)

    private val _uiState = MutableStateFlow(TransactionSheetUiState())
    val uiState: StateFlow<TransactionSheetUiState> = _uiState.asStateFlow()

    init { loadAccounts() }

    fun loadAccounts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAccounts = true) }
            try {
                val token = tokenManager.getAccessToken() ?: ""
                RetrofitClient.setAuthToken(token)
                val all    = accountsRepository.getAccounts()
                val active = all.filter { it.status == "Active" || it.status == "0" }
                val list   = active.ifEmpty { all }
                _uiState.update {
                    it.copy(
                        isLoadingAccounts = false,
                        accounts          = list,
                        selectedAccountId = if (it.selectedAccountId.isEmpty()) list.firstOrNull()?.id ?: "" else it.selectedAccountId,
                        toOwnAccountId    = if (it.toOwnAccountId.isEmpty())    list.drop(1).firstOrNull()?.id ?: "" else it.toOwnAccountId
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingAccounts = false) }
            }
        }
    }

    fun preselect(accountId: String) {
        if (accountId.isNotEmpty()) {
            _uiState.update { it.copy(selectedAccountId = accountId) }
        }
    }

    // ── inputs ────────────────────────────────────────────────────────────────
    fun setSelectedAccount(id: String)  { _uiState.update { it.copy(selectedAccountId = id, amountError = null) } }
    fun setToOwnAccount(id: String)     { _uiState.update { it.copy(toOwnAccountId = id, ibanError = null) } }
    fun setAmount(v: String)            { _uiState.update { it.copy(amount = v, amountError = null) } }
    fun setDescription(v: String)       { _uiState.update { it.copy(description = v) } }
    fun setTransferMode(m: String)      { _uiState.update { it.copy(transferMode = m, ibanError = null) } }
    fun setToIban(v: String)            { _uiState.update { it.copy(toIban = v.filter { c -> c.isLetterOrDigit() }.uppercase().take(24), ibanError = null) } }

    // ── deposit ───────────────────────────────────────────────────────────────
    fun deposit() {
        val s = _uiState.value
        val amt = s.amount.toDoubleOrNull()
        if (amt == null || amt <= 0) { _uiState.update { it.copy(amountError = "Suma trebuie să fie mai mare decât 0.") }; return }
        if (s.selectedAccountId.isEmpty()) { _uiState.update { it.copy(amountError = "Selectați un cont.") }; return }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            try {
                val req = DepositRequest(
                    toAccountId = s.selectedAccountId,
                    amount      = amt,
                    currency    = accountCurrency(s),
                    description = s.description.ifBlank { null }
                )
                transactionsRepository.deposit(req)
                _uiState.update { it.copy(isSubmitting = false, successMessage = "Depozit efectuat: +${formatAmt(amt)} ${accountCurrency(s)}") }
                DataRefreshBus.notifyRefresh()
            } catch (e: retrofit2.HttpException) {
                _uiState.update { it.copy(isSubmitting = false, error = httpError(e)) }
            } catch (e: java.io.IOException) {
                _uiState.update { it.copy(isSubmitting = false, error = "Fără conexiune la internet.") }
            }
        }
    }

    // ── withdraw ──────────────────────────────────────────────────────────────
    fun withdraw() {
        val s = _uiState.value
        val amt = s.amount.toDoubleOrNull()
        if (amt == null || amt <= 0) { _uiState.update { it.copy(amountError = "Suma trebuie să fie mai mare decât 0.") }; return }
        val account = s.accounts.firstOrNull { it.id == s.selectedAccountId }
        if (account != null && amt > account.balance) {
            _uiState.update { it.copy(amountError = "Sold insuficient (disponibil: ${formatAmt(account.balance)} ${account.currency}).") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            try {
                val req = WithdrawRequest(
                    fromAccountId = s.selectedAccountId,
                    amount        = amt,
                    currency      = accountCurrency(s),
                    description   = s.description.ifBlank { null }
                )
                transactionsRepository.withdraw(req)
                _uiState.update { it.copy(isSubmitting = false, successMessage = "Retragere efectuată: -${formatAmt(amt)} ${accountCurrency(s)}") }
                DataRefreshBus.notifyRefresh()
            } catch (e: retrofit2.HttpException) {
                _uiState.update { it.copy(isSubmitting = false, error = httpError(e)) }
            } catch (e: java.io.IOException) {
                _uiState.update { it.copy(isSubmitting = false, error = "Fără conexiune la internet.") }
            }
        }
    }

    // ── transfer ──────────────────────────────────────────────────────────────
    fun transfer() {
        val s   = _uiState.value
        val amt = s.amount.toDoubleOrNull()
        if (amt == null || amt <= 0) { _uiState.update { it.copy(amountError = "Suma trebuie să fie mai mare decât 0.") }; return }

        val toId: String = if (s.transferMode == "OWN") {
            s.toOwnAccountId
        } else {
            // resolve IBAN → id
            val raw = s.toIban.filter { it.isLetterOrDigit() }
            if (raw.length != 24) { _uiState.update { it.copy(ibanError = "IBAN-ul trebuie să aibă 24 de caractere.") }; return }
            val found = s.accounts.firstOrNull { it.iban.filter { c -> c.isLetterOrDigit() } == raw }
            found?.id ?: run {
                _uiState.update { it.copy(ibanError = "Contul destinatar nu a fost găsit.") }
                return
            }
        }

        if (toId == s.selectedAccountId) { _uiState.update { it.copy(ibanError = "Contul sursă și destinație nu pot fi identice.") }; return }

        val account = s.accounts.firstOrNull { it.id == s.selectedAccountId }
        if (account != null && amt > account.balance) {
            _uiState.update { it.copy(amountError = "Sold insuficient (disponibil: ${formatAmt(account.balance)} ${account.currency}).") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            try {
                val req = TransferRequest(
                    fromAccountId = s.selectedAccountId,
                    toAccountId   = toId,
                    amount        = amt,
                    currency      = accountCurrency(s),
                    description   = s.description.ifBlank { null }
                )
                transactionsRepository.transfer(req)
                _uiState.update { it.copy(isSubmitting = false, successMessage = "Transfer efectuat: -${formatAmt(amt)} ${accountCurrency(s)}") }
                DataRefreshBus.notifyRefresh()
            } catch (e: retrofit2.HttpException) {
                _uiState.update { it.copy(isSubmitting = false, error = httpError(e)) }
            } catch (e: java.io.IOException) {
                _uiState.update { it.copy(isSubmitting = false, error = "Fără conexiune la internet.") }
            }
        }
    }

    fun resetSheet() {
        _uiState.update {
            it.copy(
                amount       = "",
                description  = "",
                amountError  = null,
                ibanError    = null,
                toIban       = "",
                transferMode = "OWN",
                error        = null
            )
        }
    }

    fun clearSuccess() { _uiState.update { it.copy(successMessage = null) } }
    fun clearError()   { _uiState.update { it.copy(error = null) } }

    private fun accountCurrency(s: TransactionSheetUiState): String =
        s.accounts.firstOrNull { it.id == s.selectedAccountId }?.currency ?: "MDL"

    private fun httpError(e: retrofit2.HttpException) = when (e.code()) {
        400  -> "Date invalide."
        402  -> "Sold insuficient."
        404  -> "Cont negăsit."
        else -> "Eroare server (${e.code()})."
    }
}

internal fun formatAmt(v: Double): String =
    if (v % 1.0 == 0.0) "%,.0f".format(v) else "%,.2f".format(v)
