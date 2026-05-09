package com.example.bankingapp.ui.screens.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bankingapp.data.local.TokenManager
import com.example.bankingapp.data.model.account.AccountResponse
import com.example.bankingapp.data.model.card.CardResponse
import com.example.bankingapp.data.model.report.MonthlyBreakdown
import com.example.bankingapp.data.model.transaction.TransactionResponse
import com.example.bankingapp.data.network.RetrofitClient
import com.example.bankingapp.data.repository.DashboardRepository
import com.example.bankingapp.data.util.DataRefreshBus
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val firstName: String = "",
    val accounts: List<AccountResponse> = emptyList(),
    val cards: List<CardResponse> = emptyList(),
    val recentTransactions: List<TransactionResponse> = emptyList(),
    val monthlyBreakdowns: List<MonthlyBreakdown> = emptyList(),
    val unreadNotificationCount: Int = 0,
    val error: String? = null
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository    = DashboardRepository()
    private val tokenManager  = TokenManager(application)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // Flag pentru a distinge prima încărcare (init) de re-intrarea în tab
    private var initialLoadDone = false

    init {
        loadData()
        viewModelScope.launch {
            DataRefreshBus.events.collect { refresh() }
        }
    }

    /** Apelat de DashboardScreen la fiecare intrare în compoziție.
     *  Declanșează refresh NUMAI dacă datele au fost deja încărcate cel puțin odată,
     *  pentru a evita dubla-cerere la pornire. */
    fun refreshOnResume() {
        if (initialLoadDone) refresh()
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true, error = null) }
        loadData(isRefresh = true)
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun loadData(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!isRefresh) _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val token = tokenManager.getAccessToken() ?: ""
                RetrofitClient.setAuthToken(token)

                val fullName   = tokenManager.getFullName() ?: ""
                val firstName  = fullName.split(" ").firstOrNull() ?: fullName

                val accountsDeferred      = async { runCatching { repository.getAccounts() }.getOrElse { emptyList() } }
                val cardsDeferred         = async { runCatching { repository.getCards() }.getOrElse { emptyList() } }
                val transactionsDeferred  = async { runCatching { repository.getRecentTransactions() }.getOrElse { emptyList() } }
                val unreadCountDeferred   = async { runCatching { repository.getUnreadNotificationCount() }.getOrElse { 0 } }

                val accounts     = accountsDeferred.await()
                val cards        = cardsDeferred.await()
                val transactions = transactionsDeferred.await()
                val unreadCount  = unreadCountDeferred.await()

                val monthlyBreakdowns = accounts.firstOrNull()?.let { firstAccount ->
                    val year = Calendar.getInstance().get(Calendar.YEAR)
                    runCatching {
                        repository.getAnnualSummary(firstAccount.id, year).monthlyBreakdowns
                    }.getOrElse { emptyList() }
                } ?: emptyList()

                _uiState.update {
                    it.copy(
                        isLoading            = false,
                        isRefreshing         = false,
                        firstName            = firstName,
                        accounts             = accounts,
                        cards                = cards,
                        recentTransactions   = transactions,
                        monthlyBreakdowns    = monthlyBreakdowns,
                        unreadNotificationCount = unreadCount
                    )
                }
                initialLoadDone = true
            } catch (e: Exception) {
                val message = when (e) {
                    is java.io.IOException -> "Fără conexiune la internet."
                    is retrofit2.HttpException -> when (e.code()) {
                        401  -> "Sesiunea a expirat. Reconectați-vă."
                        403  -> "Nu aveți permisiunea pentru această acțiune."
                        else -> "Eroare server (${e.code()}). Încercați din nou."
                    }
                    else -> "A apărut o eroare neașteptată."
                }
                _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = message) }
                initialLoadDone = true
            }
        }
    }
}
