package com.example.bankingapp.ui.screens.transactions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bankingapp.data.local.TokenManager
import com.example.bankingapp.data.model.transaction.TransactionResponse
import com.example.bankingapp.data.network.RetrofitClient
import com.example.bankingapp.data.repository.TransactionsRepository
import com.example.bankingapp.data.util.DataRefreshBus
import com.example.bankingapp.data.util.parseApiMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class TransactionGroup(val dateLabel: String, val transactions: List<TransactionResponse>)

data class FilterState(
    val type: String   = "",
    val status: String = "",
    val from: String   = "",
    val to: String     = ""
) {
    val isActive: Boolean get() = type.isNotEmpty() || status.isNotEmpty() || from.isNotEmpty() || to.isNotEmpty()
}

data class TransactionsListUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val groups: List<TransactionGroup> = emptyList(),
    val currentPage: Int = 1,
    val hasMore: Boolean = true,
    val error: String? = null,
    val appliedFilter: FilterState = FilterState(),
    val pendingFilter: FilterState = FilterState(),
    val showFilterSheet: Boolean = false,
    val showDepositSheet: Boolean = false,
    val showWithdrawSheet: Boolean = false,
    val showTransferSheet: Boolean = false
)

class TransactionsListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository   = TransactionsRepository()
    private val tokenManager = TokenManager(application)

    private val _uiState = MutableStateFlow(TransactionsListUiState())
    val uiState: StateFlow<TransactionsListUiState> = _uiState.asStateFlow()

    private val allTransactions = mutableListOf<TransactionResponse>()

    private var initialLoadDone = false

    init {
        loadPage(reset = true)
        viewModelScope.launch {
            DataRefreshBus.events.collect { loadPage(reset = true) }
        }
    }

    /** Apelat la fiecare revenire în tab — declanșează refresh doar după prima încărcare. */
    fun refreshOnResume() {
        if (initialLoadDone) loadPage(reset = true)
    }

    fun loadPage(reset: Boolean = false) {
        val state = _uiState.value
        if (!reset && (state.isLoadingMore || !state.hasMore)) return

        val page = if (reset) 1 else state.currentPage + 1
        viewModelScope.launch {
            if (reset) {
                _uiState.update { it.copy(isLoading = true, error = null) }
                allTransactions.clear()
            } else {
                _uiState.update { it.copy(isLoadingMore = true) }
            }
            try {
                val token = tokenManager.getAccessToken() ?: ""
                RetrofitClient.setAuthToken(token)
                val f    = state.appliedFilter
                val data = repository.getTransactions(
                    type     = f.type.ifBlank { null },
                    status   = f.status.ifBlank { null },
                    from     = f.from.ifBlank { null },
                    to       = f.to.ifBlank { null },
                    page     = page,
                    pageSize = 20
                )
                allTransactions.addAll(data)
                _uiState.update {
                    it.copy(
                        isLoading     = false,
                        isLoadingMore = false,
                        groups        = groupByDate(allTransactions),
                        currentPage   = page,
                        hasMore       = data.size >= 20
                    )
                }
            } catch (e: retrofit2.HttpException) {
                val msg = e.parseApiMessage() ?: "Eroare server (${e.code()})."
                _uiState.update { it.copy(isLoading = false, isLoadingMore = false, error = msg) }
            } catch (e: java.io.IOException) {
                _uiState.update { it.copy(isLoading = false, isLoadingMore = false, error = "Fără conexiune la internet.") }
            } finally {
                if (reset) initialLoadDone = true
            }
        }
    }

    fun loadMore() { loadPage(reset = false) }

    fun refresh() { loadPage(reset = true) }

    // ── Filters ───────────────────────────────────────────────────────────────
    fun showFilterSheet()  { _uiState.update { it.copy(showFilterSheet = true, pendingFilter = it.appliedFilter) } }
    fun hideFilterSheet()  { _uiState.update { it.copy(showFilterSheet = false) } }

    fun setPendingType(v: String)   { _uiState.update { it.copy(pendingFilter = it.pendingFilter.copy(type = v)) } }
    fun setPendingStatus(v: String) { _uiState.update { it.copy(pendingFilter = it.pendingFilter.copy(status = v)) } }
    fun setPendingFrom(v: String)   { _uiState.update { it.copy(pendingFilter = it.pendingFilter.copy(from = v)) } }
    fun setPendingTo(v: String)     { _uiState.update { it.copy(pendingFilter = it.pendingFilter.copy(to = v)) } }

    fun applyFilters() {
        _uiState.update { it.copy(appliedFilter = it.pendingFilter, showFilterSheet = false) }
        loadPage(reset = true)
    }

    fun resetFilters() {
        _uiState.update { it.copy(appliedFilter = FilterState(), pendingFilter = FilterState(), showFilterSheet = false) }
        loadPage(reset = true)
    }

    // ── Sheet visibility ──────────────────────────────────────────────────────
    fun showDeposit()  { _uiState.update { it.copy(showDepositSheet = true) } }
    fun showWithdraw() { _uiState.update { it.copy(showWithdrawSheet = true) } }
    fun showTransfer() { _uiState.update { it.copy(showTransferSheet = true) } }
    fun hideDeposit()  { _uiState.update { it.copy(showDepositSheet = false) } }
    fun hideWithdraw() { _uiState.update { it.copy(showWithdrawSheet = false) } }
    fun hideTransfer() { _uiState.update { it.copy(showTransferSheet = false) } }

    fun clearError() { _uiState.update { it.copy(error = null) } }
}

private fun groupByDate(txList: List<TransactionResponse>): List<TransactionGroup> {
    val today     = Calendar.getInstance()
    val yesterday = Calendar.getInstance().also { it.add(Calendar.DAY_OF_YEAR, -1) }
    val fmt       = SimpleDateFormat("d MMMM yyyy", Locale("ro", "RO"))
    val parsers   = listOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",     Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",        Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd",                   Locale.getDefault())
    )

    return txList
        .groupBy { tx ->
            val date = parsers.firstNotNullOfOrNull { p -> runCatching { p.parse(tx.createdAt) }.getOrNull() }
            if (date == null) return@groupBy tx.createdAt.take(10)
            val cal = Calendar.getInstance().also { it.time = date }
            when {
                isSameDay(cal, today)     -> "Astăzi"
                isSameDay(cal, yesterday) -> "Ieri"
                else                      -> fmt.format(date)
            }
        }
        .map { (label, list) -> TransactionGroup(label, list) }
}

private fun isSameDay(a: Calendar, b: Calendar): Boolean =
    a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
    a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
