package com.example.bankingapp.ui.screens.cards

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bankingapp.data.model.account.AccountResponse
import com.example.bankingapp.data.model.card.CardResponse
import com.example.bankingapp.data.model.card.UpdateCardLimitsRequest
import com.example.bankingapp.data.network.RetrofitClient
import com.example.bankingapp.data.repository.AccountsRepository
import com.example.bankingapp.data.repository.CardsRepository
import com.example.bankingapp.data.local.TokenManager
import com.example.bankingapp.data.util.parseApiMessage
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CardDetailUiState(
    val isLoading: Boolean = false,
    val card: CardResponse? = null,
    val associatedAccount: AccountResponse? = null,
    val error: String? = null,
    val actionError: String? = null,
    val showLimitsSheet: Boolean = false,
    val editDailyLimit: String = "",
    val editMonthlyLimit: String = "",
    val isUpdatingLimits: Boolean = false,
    val showBlockDialog: Boolean = false,
    val isBlocking: Boolean = false,
    val showCloseDialog: Boolean = false,
    val isDeleting: Boolean = false,
    val cardDeleted: Boolean = false
)

class CardDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val cardsRepository   = CardsRepository()
    private val accountsRepository = AccountsRepository()
    private val tokenManager      = TokenManager(application)

    private val _uiState = MutableStateFlow(CardDetailUiState())
    val uiState: StateFlow<CardDetailUiState> = _uiState.asStateFlow()

    private var cardId: String = ""
    private var initialized = false

    fun init(id: String) {
        if (initialized) return
        initialized = true
        cardId = id
        loadCard()
    }

    private fun loadCard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val token = tokenManager.getAccessToken() ?: ""
                RetrofitClient.setAuthToken(token)
                val card    = cardsRepository.getCard(cardId)
                val account = runCatching { accountsRepository.getAccount(card.accountId) }.getOrNull()
                _uiState.update {
                    it.copy(
                        isLoading          = false,
                        card               = card,
                        associatedAccount  = account,
                        editDailyLimit     = card.dailyLimit.toFormattedString(),
                        editMonthlyLimit   = card.monthlyLimit.toFormattedString()
                    )
                }
            } catch (e: retrofit2.HttpException) {
                val msg = e.parseApiMessage() ?: when (e.code()) {
                    401  -> "Sesiunea a expirat."
                    404  -> "Cardul nu a fost găsit."
                    else -> "Eroare server (${e.code()})."
                }
                _uiState.update { it.copy(isLoading = false, error = msg) }
            } catch (e: java.io.IOException) {
                _uiState.update { it.copy(isLoading = false, error = "Fără conexiune la internet.") }
            }
        }
    }

    // ── Block ──────────────────────────────────────────────────────────────────

    fun showBlockDialog()  { _uiState.update { it.copy(showBlockDialog = true) } }
    fun hideBlockDialog()  { _uiState.update { it.copy(showBlockDialog = false) } }

    fun blockCard() {
        viewModelScope.launch {
            _uiState.update { it.copy(showBlockDialog = false, isBlocking = true, actionError = null) }
            try {
                cardsRepository.blockCard(cardId)
                _uiState.update { state ->
                    state.copy(
                        isBlocking = false,
                        card       = state.card?.copy(status = "Blocked")
                    )
                }
            } catch (e: retrofit2.HttpException) {
                val msg = e.parseApiMessage() ?: "Nu s-a putut bloca cardul."
                _uiState.update { it.copy(isBlocking = false, actionError = msg) }
            } catch (e: java.io.IOException) {
                _uiState.update { it.copy(isBlocking = false, actionError = "Fără conexiune la internet.") }
            }
        }
    }

    fun unblockCard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBlocking = true, actionError = null) }
            try {
                cardsRepository.unblockCard(cardId)
                _uiState.update { state ->
                    state.copy(
                        isBlocking = false,
                        card       = state.card?.copy(status = "Active")
                    )
                }
            } catch (e: retrofit2.HttpException) {
                val msg = e.parseApiMessage() ?: when (e.code()) {
                    403  -> "Cardul a fost blocat de administrator și nu poate fi deblocat."
                    else -> "Nu s-a putut debloca cardul."
                }
                _uiState.update { it.copy(isBlocking = false, actionError = msg) }
            } catch (e: java.io.IOException) {
                _uiState.update { it.copy(isBlocking = false, actionError = "Fără conexiune la internet.") }
            }
        }
    }

    // ── Limits ─────────────────────────────────────────────────────────────────

    fun showLimitsSheet() {
        val card = _uiState.value.card ?: return
        _uiState.update {
            it.copy(
                showLimitsSheet  = true,
                editDailyLimit   = card.dailyLimit.toFormattedString(),
                editMonthlyLimit = card.monthlyLimit.toFormattedString()
            )
        }
    }
    fun hideLimitsSheet() { _uiState.update { it.copy(showLimitsSheet = false) } }

    fun setEditDailyLimit(value: String)   { _uiState.update { it.copy(editDailyLimit = value) } }
    fun setEditMonthlyLimit(value: String) { _uiState.update { it.copy(editMonthlyLimit = value) } }

    fun updateLimits() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingLimits = true, actionError = null) }
            try {
                val request = UpdateCardLimitsRequest(
                    dailyLimit   = state.editDailyLimit.toDoubleOrNull(),
                    monthlyLimit = state.editMonthlyLimit.toDoubleOrNull()
                )
                cardsRepository.updateLimits(cardId, request)
                val updatedCard = state.card?.copy(
                    dailyLimit   = state.editDailyLimit.toDoubleOrNull() ?: state.card.dailyLimit,
                    monthlyLimit = state.editMonthlyLimit.toDoubleOrNull() ?: state.card.monthlyLimit
                )
                _uiState.update {
                    it.copy(
                        isUpdatingLimits = false,
                        showLimitsSheet  = false,
                        card             = updatedCard
                    )
                }
            } catch (e: retrofit2.HttpException) {
                val msg = e.parseApiMessage() ?: "Nu s-au putut actualiza limitele."
                _uiState.update { it.copy(isUpdatingLimits = false, actionError = msg) }
            } catch (e: java.io.IOException) {
                _uiState.update { it.copy(isUpdatingLimits = false, actionError = "Fără conexiune la internet.") }
            }
        }
    }

    // ── Close / Delete ─────────────────────────────────────────────────────────

    fun showCloseDialog() { _uiState.update { it.copy(showCloseDialog = true) } }
    fun hideCloseDialog() { _uiState.update { it.copy(showCloseDialog = false) } }

    fun closeCard() {
        viewModelScope.launch {
            _uiState.update { it.copy(showCloseDialog = false, isDeleting = true, actionError = null) }
            try {
                cardsRepository.deleteCard(cardId)
                _uiState.update { it.copy(isDeleting = false, cardDeleted = true) }
            } catch (e: retrofit2.HttpException) {
                val msg = e.parseApiMessage() ?: "Nu s-a putut închide cardul."
                _uiState.update { it.copy(isDeleting = false, actionError = msg) }
            } catch (e: java.io.IOException) {
                _uiState.update { it.copy(isDeleting = false, actionError = "Fără conexiune la internet.") }
            }
        }
    }

    fun clearActionError() { _uiState.update { it.copy(actionError = null) } }
}

private fun Double.toFormattedString(): String =
    if (this % 1.0 == 0.0) this.toLong().toString() else "%.2f".format(this)
