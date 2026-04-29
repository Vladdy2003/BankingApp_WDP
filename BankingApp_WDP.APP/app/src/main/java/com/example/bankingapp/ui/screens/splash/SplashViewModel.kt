package com.example.bankingapp.ui.screens.splash

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bankingapp.data.local.TokenManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel(application: Application) : AndroidViewModel(application) {

    sealed class Destination {
        object Loading : Destination()
        object Login   : Destination()
        object Main    : Destination()
    }

    private val _destination = MutableStateFlow<Destination>(Destination.Loading)
    val destination: StateFlow<Destination> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            delay(1200L)
            val token = TokenManager(getApplication()).getAccessToken()
            _destination.value = if (!token.isNullOrBlank()) Destination.Main else Destination.Login
        }
    }
}
