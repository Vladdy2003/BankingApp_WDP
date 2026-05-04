package com.example.bankingapp.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bankingapp.data.local.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean       = true,
    val firstName: String        = "",
    val lastName: String         = "",
    val email: String            = "",
    val phone: String            = "",
    val address: String          = "",
    val dateOfBirth: String      = "",
    val isEditMode: Boolean      = false,
    val editFirstName: String    = "",
    val editLastName: String     = "",
    val editPhone: String        = "",
    val editAddress: String      = "",
    val editDateOfBirth: String  = "",
    val isSaving: Boolean        = false,
    val saveSuccess: Boolean     = false,
    val error: String?           = null
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init { loadProfile() }

    private fun loadProfile() {
        viewModelScope.launch {
            val fullName    = tokenManager.getFullName() ?: ""
            val nameParts   = fullName.split(" ", limit = 2)
            val firstName   = nameParts.getOrElse(0) { "" }
            val lastName    = nameParts.getOrElse(1) { "" }
            val email       = tokenManager.getEmail()       ?: ""
            val phone       = tokenManager.getPhone()       ?: ""
            val address     = tokenManager.getAddress()     ?: ""
            val dateOfBirth = tokenManager.getDateOfBirth() ?: ""

            _uiState.update {
                it.copy(
                    isLoading   = false,
                    firstName   = firstName,
                    lastName    = lastName,
                    email       = email,
                    phone       = phone,
                    address     = address,
                    dateOfBirth = dateOfBirth
                )
            }
        }
    }

    fun enterEditMode() {
        val s = _uiState.value
        _uiState.update {
            it.copy(
                isEditMode      = true,
                editFirstName   = s.firstName,
                editLastName    = s.lastName,
                editPhone       = s.phone,
                editAddress     = s.address,
                editDateOfBirth = s.dateOfBirth
            )
        }
    }

    fun cancelEdit() {
        _uiState.update { it.copy(isEditMode = false) }
    }

    fun onEditFirstName(v: String)    { _uiState.update { it.copy(editFirstName   = v) } }
    fun onEditLastName(v: String)     { _uiState.update { it.copy(editLastName    = v) } }
    fun onEditPhone(v: String)        { _uiState.update { it.copy(editPhone       = v) } }
    fun onEditAddress(v: String)      { _uiState.update { it.copy(editAddress     = v) } }
    fun onEditDateOfBirth(v: String)  { _uiState.update { it.copy(editDateOfBirth = v) } }

    fun saveProfile() {
        val s = _uiState.value
        if (s.editFirstName.isBlank()) {
            _uiState.update { it.copy(error = "Prenumele nu poate fi gol.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val fullName = "${s.editFirstName.trim()} ${s.editLastName.trim()}".trim()
                tokenManager.saveProfileInfo(
                    fullName    = fullName,
                    phone       = s.editPhone.trim(),
                    address     = s.editAddress.trim(),
                    dateOfBirth = s.editDateOfBirth.trim()
                )
                _uiState.update {
                    it.copy(
                        isSaving    = false,
                        isEditMode  = false,
                        saveSuccess = true,
                        firstName   = s.editFirstName.trim(),
                        lastName    = s.editLastName.trim(),
                        phone       = s.editPhone.trim(),
                        address     = s.editAddress.trim(),
                        dateOfBirth = s.editDateOfBirth.trim()
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = "Eroare la salvare.") }
            }
        }
    }

    fun clearError()       { _uiState.update { it.copy(error = null) } }
    fun clearSaveSuccess() { _uiState.update { it.copy(saveSuccess = false) } }
}
