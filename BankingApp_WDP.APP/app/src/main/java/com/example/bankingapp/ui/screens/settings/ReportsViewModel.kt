package com.example.bankingapp.ui.screens.settings

import android.app.Application
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bankingapp.data.local.TokenManager
import com.example.bankingapp.data.model.account.AccountResponse
import com.example.bankingapp.data.model.report.AnnualSummaryResponse
import com.example.bankingapp.data.model.report.IncomeVsExpensesResponse
import com.example.bankingapp.data.model.report.MonthlyReportResponse
import com.example.bankingapp.data.model.report.SpendingByCategoryResponse
import com.example.bankingapp.data.network.RetrofitClient
import com.example.bankingapp.data.repository.AccountsRepository
import com.example.bankingapp.data.repository.ReportsRepository
import com.example.bankingapp.data.util.parseApiMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

data class ReportsUiState(
    // Account selector
    val accounts: List<AccountResponse>  = emptyList(),
    val isLoadingAccounts: Boolean        = true,
    val selectedAccountId: String         = "",

    // Section 1 — Extras lunar
    val monthlyYear: Int                  = Calendar.getInstance().get(Calendar.YEAR),
    val monthlyMonth: Int                 = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val monthlyReport: MonthlyReportResponse? = null,
    val isLoadingMonthly: Boolean         = false,
    val isExporting: Boolean              = false,
    val exportMessage: String?            = null,

    // Section 2 — Sumar anual
    val annualYear: Int                   = Calendar.getInstance().get(Calendar.YEAR),
    val annualSummary: AnnualSummaryResponse? = null,
    val isLoadingAnnual: Boolean          = false,

    // Section 3 — Cheltuieli pe categorii
    val spendingFrom: String              = "",
    val spendingTo: String                = "",
    val spendingData: SpendingByCategoryResponse? = null,
    val isLoadingSpending: Boolean        = false,

    // Section 4 — Venituri vs cheltuieli
    val incomeFrom: String                = "",
    val incomeTo: String                  = "",
    val incomeData: IncomeVsExpensesResponse? = null,
    val isLoadingIncome: Boolean          = false,

    val error: String?                    = null
)

class ReportsViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager       = TokenManager(application)
    private val accountsRepository = AccountsRepository()
    private val reportsRepository  = ReportsRepository()

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init { loadAccounts() }

    private fun loadAccounts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAccounts = true) }
            try {
                val token = tokenManager.getAccessToken() ?: ""
                RetrofitClient.setAuthToken(token)
                val accounts = accountsRepository.getAccounts()
                    .filter { it.status != "Closed" && it.status != "4" }
                val selectedId = accounts.firstOrNull()?.id ?: ""
                _uiState.update {
                    it.copy(
                        isLoadingAccounts = false,
                        accounts          = accounts,
                        selectedAccountId = selectedId
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingAccounts = false, error = "Nu s-au putut încărca conturile.") }
            }
        }
    }

    fun selectAccount(accountId: String) {
        _uiState.update {
            it.copy(
                selectedAccountId = accountId,
                monthlyReport     = null,
                annualSummary     = null,
                spendingData      = null,
                incomeData        = null
            )
        }
    }

    // ── Section 1 ────────────────────────────────────────────────────────────

    fun setMonthlyYear(year: Int)    { _uiState.update { it.copy(monthlyYear  = year,  monthlyReport = null) } }
    fun setMonthlyMonth(month: Int)  { _uiState.update { it.copy(monthlyMonth = month, monthlyReport = null) } }

    fun loadMonthlyStatement() {
        val s = _uiState.value
        if (s.selectedAccountId.isEmpty()) {
            _uiState.update { it.copy(error = "Selectați un cont.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMonthly = true) }
            try {
                val report = reportsRepository.getMonthlyStatement(
                    accountId = s.selectedAccountId,
                    year      = s.monthlyYear,
                    month     = s.monthlyMonth
                )
                _uiState.update { it.copy(isLoadingMonthly = false, monthlyReport = report) }
            } catch (e: retrofit2.HttpException) {
                val msg = e.parseApiMessage() ?: if (e.code() == 404) "Nu există date pentru perioada selectată."
                          else "Eroare server (${e.code()})."
                _uiState.update { it.copy(isLoadingMonthly = false, error = msg) }
            } catch (e: java.io.IOException) {
                _uiState.update { it.copy(isLoadingMonthly = false, error = "Fără conexiune la internet.") }
            }
        }
    }

    fun exportReport(format: String) {
        val s = _uiState.value
        if (s.selectedAccountId.isEmpty()) {
            _uiState.update { it.copy(error = "Selectați un cont.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            try {
                val body = reportsRepository.exportReport(
                    accountId = s.selectedAccountId,
                    format    = format,
                    year      = s.monthlyYear,
                    month     = s.monthlyMonth
                )
                val ext      = if (format == "pdf") "pdf" else "csv"
                val filename = "extras_${s.monthlyYear}_${s.monthlyMonth.toString().padStart(2, '0')}.$ext"
                val dir      = getApplication<Application>()
                    .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    ?: getApplication<Application>().filesDir
                val file = File(dir, filename)
                file.outputStream().use { it.write(body.bytes()) }
                _uiState.update {
                    it.copy(
                        isExporting   = false,
                        exportMessage = "Salvat: ${file.absolutePath}"
                    )
                }
            } catch (e: retrofit2.HttpException) {
                val msg = e.parseApiMessage() ?: "Export eșuat (${e.code()})."
                _uiState.update { it.copy(isExporting = false, error = msg) }
            } catch (e: java.io.IOException) {
                _uiState.update { it.copy(isExporting = false, error = "Eroare la salvarea fișierului.") }
            }
        }
    }

    // ── Section 2 ────────────────────────────────────────────────────────────

    fun setAnnualYear(year: Int) { _uiState.update { it.copy(annualYear = year, annualSummary = null) } }

    fun loadAnnualSummary() {
        val s = _uiState.value
        if (s.selectedAccountId.isEmpty()) {
            _uiState.update { it.copy(error = "Selectați un cont.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAnnual = true) }
            try {
                val summary = reportsRepository.getAnnualSummary(s.selectedAccountId, s.annualYear)
                _uiState.update { it.copy(isLoadingAnnual = false, annualSummary = summary) }
            } catch (e: retrofit2.HttpException) {
                val msg = e.parseApiMessage() ?: if (e.code() == 404) "Nu există date pentru anul selectat."
                          else "Eroare server (${e.code()})."
                _uiState.update { it.copy(isLoadingAnnual = false, error = msg) }
            } catch (e: java.io.IOException) {
                _uiState.update { it.copy(isLoadingAnnual = false, error = "Fără conexiune la internet.") }
            }
        }
    }

    // ── Section 3 ────────────────────────────────────────────────────────────

    fun setSpendingFrom(v: String) { _uiState.update { it.copy(spendingFrom = v, spendingData = null) } }
    fun setSpendingTo(v: String)   { _uiState.update { it.copy(spendingTo   = v, spendingData = null) } }

    fun loadSpending() {
        val s = _uiState.value
        if (s.selectedAccountId.isEmpty()) { _uiState.update { it.copy(error = "Selectați un cont.") }; return }
        if (s.spendingFrom.isBlank() || s.spendingTo.isBlank()) {
            _uiState.update { it.copy(error = "Completați intervalul de date.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSpending = true) }
            try {
                val data = reportsRepository.getSpendingByCategory(s.selectedAccountId, s.spendingFrom, s.spendingTo)
                _uiState.update { it.copy(isLoadingSpending = false, spendingData = data) }
            } catch (e: retrofit2.HttpException) {
                val msg = e.parseApiMessage() ?: if (e.code() == 404) "Nu există date pentru perioada selectată."
                          else "Eroare server (${e.code()})."
                _uiState.update { it.copy(isLoadingSpending = false, error = msg) }
            } catch (e: java.io.IOException) {
                _uiState.update { it.copy(isLoadingSpending = false, error = "Fără conexiune la internet.") }
            }
        }
    }

    // ── Section 4 ────────────────────────────────────────────────────────────

    fun setIncomeFrom(v: String) { _uiState.update { it.copy(incomeFrom = v, incomeData = null) } }
    fun setIncomeTo(v: String)   { _uiState.update { it.copy(incomeTo   = v, incomeData = null) } }

    fun loadIncomeVsExpenses() {
        val s = _uiState.value
        if (s.selectedAccountId.isEmpty()) { _uiState.update { it.copy(error = "Selectați un cont.") }; return }
        if (s.incomeFrom.isBlank() || s.incomeTo.isBlank()) {
            _uiState.update { it.copy(error = "Completați intervalul de date.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingIncome = true) }
            try {
                val data = reportsRepository.getIncomeVsExpenses(s.selectedAccountId, s.incomeFrom, s.incomeTo)
                _uiState.update { it.copy(isLoadingIncome = false, incomeData = data) }
            } catch (e: retrofit2.HttpException) {
                val msg = e.parseApiMessage() ?: when (e.code()) {
                    404  -> "Nu există date pentru perioada selectată."
                    501  -> "Funcționalitate indisponibilă în versiunea curentă."
                    else -> "Eroare server (${e.code()})."
                }
                _uiState.update { it.copy(isLoadingIncome = false, error = msg) }
            } catch (e: java.io.IOException) {
                _uiState.update { it.copy(isLoadingIncome = false, error = "Fără conexiune la internet.") }
            }
        }
    }

    fun clearError()         { _uiState.update { it.copy(error = null) } }
    fun clearExportMessage() { _uiState.update { it.copy(exportMessage = null) } }
}
