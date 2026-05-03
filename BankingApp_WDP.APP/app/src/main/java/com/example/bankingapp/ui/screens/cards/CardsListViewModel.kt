package com.example.bankingapp.ui.screens.cards

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bankingapp.data.model.account.AccountResponse
import com.example.bankingapp.data.model.card.CardResponse
import com.example.bankingapp.data.model.card.CreateCardRequest
import com.example.bankingapp.data.network.RetrofitClient
import com.example.bankingapp.data.repository.AccountsRepository
import com.example.bankingapp.data.repository.CardsRepository
import com.example.bankingapp.data.local.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CardsListUiState(
    val isLoading: Boolean = false,
    val cards: List<CardResponse> = emptyList(),
    val accounts: List<AccountResponse> = emptyList(),
    val error: String? = null,
    val actionError: String? = null,
    val showCreateSheet: Boolean = false,
    val selectedAccountId: String = "",
    val selectedType: Int = 0,
    val dailyLimit: String = "1000",
    val monthlyLimit: String = "30000",
    val isCreating: Boolean = false,
    val createSuccess: String? = null,
    val blockingCardId: String? = null
)

class CardsListViewModel(application: Application) : AndroidViewModel(application) {

    private val cardsRepository   = CardsRepository()
    private val accountsRepository = AccountsRepository()
    private val tokenManager      = TokenManager(application)

    private val _uiState = MutableStateFlow(CardsListUiState())
    val uiState: StateFlow<CardsListUiState> = _uiState.asStateFlow()

    init { loadCards() }

    fun loadCards() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val token = tokenManager.getAccessToken() ?: ""
                RetrofitClient.setAuthToken(token)
                val cards    = cardsRepository.getCards()
                val accounts = accountsRepository.getAccounts()
                val activeAccounts = accounts.filter { it.status == "Active" || it.status == "0" }
                val defaultAccountId = activeAccounts.firstOrNull()?.id ?: accounts.firstOrNull()?.id ?: ""
                _uiState.update {
                    it.copy(
                        isLoading         = false,
                        cards             = cards,
                        accounts          = activeAccounts.ifEmpty { accounts },
                        selectedAccountId = if (it.selectedAccountId.isEmpty()) defaultAccountId else it.selectedAccountId
                    )
                }
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

    fun blockCard(cardId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(blockingCardId = cardId, actionError = null) }
            try {
                cardsRepository.blockCard(cardId)
                _uiState.update { state ->
                    state.copy(
                        blockingCardId = null,
                        cards = state.cards.map { if (it.id == cardId) it.copy(status = "Blocked") else it }
                    )
                }
            } catch (e: retrofit2.HttpException) {
                _uiState.update { it.copy(blockingCardId = null, actionError = "Nu s-a putut bloca cardul.") }
            } catch (e: java.io.IOException) {
                _uiState.update { it.copy(blockingCardId = null, actionError = "Fără conexiune la internet.") }
            }
        }
    }

    fun unblockCard(cardId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(blockingCardId = cardId, actionError = null) }
            try {
                cardsRepository.unblockCard(cardId)
                _uiState.update { state ->
                    state.copy(
                        blockingCardId = null,
                        cards = state.cards.map { if (it.id == cardId) it.copy(status = "Active") else it }
                    )
                }
            } catch (e: retrofit2.HttpException) {
                _uiState.update { it.copy(blockingCardId = null, actionError = "Nu s-a putut debloca cardul.") }
            } catch (e: java.io.IOException) {
                _uiState.update { it.copy(blockingCardId = null, actionError = "Fără conexiune la internet.") }
            }
        }
    }

    fun showCreateSheet() { _uiState.update { it.copy(showCreateSheet = true) } }
    fun hideCreateSheet() { _uiState.update { it.copy(showCreateSheet = false) } }

    fun setSelectedAccount(accountId: String) {
        _uiState.update { it.copy(selectedAccountId = accountId) }
    }

    fun setSelectedType(type: Int) {
        val (daily, monthly) = when (type) {
            0    -> "1000"  to "30000"
            1    -> "5000"  to "150000"
            2    -> "500"   to "15000"
            else -> "1000"  to "30000"
        }
        _uiState.update { it.copy(selectedType = type, dailyLimit = daily, monthlyLimit = monthly) }
    }

    fun setDailyLimit(value: String)   { _uiState.update { it.copy(dailyLimit = value) } }
    fun setMonthlyLimit(value: String) { _uiState.update { it.copy(monthlyLimit = value) } }

    fun createCard() {
        val state = _uiState.value
        if (state.selectedAccountId.isEmpty()) {
            _uiState.update { it.copy(actionError = "Selectați un cont.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, actionError = null) }
            try {
                val request = CreateCardRequest(
                    accountId   = state.selectedAccountId,
                    type        = state.selectedType,
                    dailyLimit  = state.dailyLimit.toDoubleOrNull(),
                    monthlyLimit = state.monthlyLimit.toDoubleOrNull()
                )
                val newCard = cardsRepository.createCard(request)
                _uiState.update {
                    it.copy(
                        isCreating    = false,
                        showCreateSheet = false,
                        cards         = it.cards + newCard,
                        createSuccess = "Card emis cu succes."
                    )
                }
            } catch (e: retrofit2.HttpException) {
                val msg = when (e.code()) {
                    400  -> "Date invalide."
                    409  -> "Card existent pentru acest cont."
                    else -> "Eroare server (${e.code()})."
                }
                _uiState.update { it.copy(isCreating = false, actionError = msg) }
            } catch (e: java.io.IOException) {
                _uiState.update { it.copy(isCreating = false, actionError = "Fără conexiune la internet.") }
            }
        }
    }

    fun clearError()         { _uiState.update { it.copy(error = null) } }
    fun clearActionError()   { _uiState.update { it.copy(actionError = null) } }
    fun clearCreateSuccess() { _uiState.update { it.copy(createSuccess = null) } }
}
