package com.example.bankingapp.ui.screens.notifications

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bankingapp.data.local.TokenManager
import com.example.bankingapp.data.model.notification.NotificationResponse
import com.example.bankingapp.data.network.RetrofitClient
import com.example.bankingapp.data.repository.NotificationsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 20

data class NotificationsUiState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val notifications: List<NotificationResponse> = emptyList(),
    val filteredNotifications: List<NotificationResponse> = emptyList(),
    val activeFilter: String = "TOATE",
    val currentPage: Int = 1,
    val hasMore: Boolean = true,
    val error: String? = null
)

class NotificationsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository   = NotificationsRepository()
    private val tokenManager = TokenManager(application)

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init { loadPage(1, reset = true) }

    private fun loadPage(page: Int, reset: Boolean = false) {
        if (!reset && (_uiState.value.isLoadingMore || !_uiState.value.hasMore)) return
        viewModelScope.launch {
            _uiState.update {
                if (reset) it.copy(isLoading = true, error = null)
                else it.copy(isLoadingMore = true)
            }
            try {
                val token = tokenManager.getAccessToken() ?: ""
                RetrofitClient.setAuthToken(token)
                val page_data = repository.getNotifications(page, PAGE_SIZE)
                _uiState.update { state ->
                    val merged = if (reset) page_data else state.notifications + page_data
                    val hasMore = page_data.size >= PAGE_SIZE
                    state.copy(
                        isLoading     = false,
                        isLoadingMore = false,
                        notifications = merged,
                        filteredNotifications = applyFilter(merged, state.activeFilter),
                        currentPage   = page,
                        hasMore       = hasMore
                    )
                }
            } catch (e: retrofit2.HttpException) {
                val msg = when (e.code()) {
                    401  -> "Sesiunea a expirat."
                    else -> "Eroare server (${e.code()})."
                }
                _uiState.update { it.copy(isLoading = false, isLoadingMore = false, error = msg) }
            } catch (_: java.io.IOException) {
                _uiState.update { it.copy(isLoading = false, isLoadingMore = false, error = "Fără conexiune la internet.") }
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMore || state.isLoading) return
        loadPage(state.currentPage + 1)
    }

    fun refresh() = loadPage(1, reset = true)

    fun setFilter(filter: String) {
        _uiState.update { state ->
            state.copy(
                activeFilter          = filter,
                filteredNotifications = applyFilter(state.notifications, filter)
            )
        }
    }

    fun markAsRead(id: Int) {
        val notification = _uiState.value.notifications.find { it.id == id }
        if (notification == null || notification.isRead) return

        _uiState.update { state ->
            val updated = state.notifications.map { if (it.id == id) it.copy(isRead = true) else it }
            state.copy(
                notifications          = updated,
                filteredNotifications  = applyFilter(updated, state.activeFilter)
            )
        }
        viewModelScope.launch {
            try {
                val token = tokenManager.getAccessToken() ?: ""
                RetrofitClient.setAuthToken(token)
                repository.markAsRead(id)
            } catch (_: Exception) { }
        }
    }

    fun markAllAsRead() {
        _uiState.update { state ->
            val updated = state.notifications.map { it.copy(isRead = true) }
            state.copy(
                notifications          = updated,
                filteredNotifications  = applyFilter(updated, state.activeFilter)
            )
        }
        viewModelScope.launch {
            try {
                val token = tokenManager.getAccessToken() ?: ""
                RetrofitClient.setAuthToken(token)
                repository.markAllAsRead()
            } catch (_: Exception) { }
        }
    }

    fun deleteNotification(id: Int) {
        _uiState.update { state ->
            val updated = state.notifications.filter { it.id != id }
            state.copy(
                notifications          = updated,
                filteredNotifications  = applyFilter(updated, state.activeFilter)
            )
        }
        viewModelScope.launch {
            try {
                val token = tokenManager.getAccessToken() ?: ""
                RetrofitClient.setAuthToken(token)
                repository.deleteNotification(id)
            } catch (_: Exception) { }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    private fun applyFilter(
        notifications: List<NotificationResponse>,
        filter: String
    ): List<NotificationResponse> {
        if (filter == "TOATE") return notifications
        val backendType = when (filter) {
            "TRANZACȚIE" -> "transaction"
            "SECURITATE" -> "security"
            "SISTEM"     -> "system"
            "MARKETING"  -> "marketing"
            else         -> return notifications
        }
        return notifications.filter { it.type.lowercase() == backendType }
    }
}
