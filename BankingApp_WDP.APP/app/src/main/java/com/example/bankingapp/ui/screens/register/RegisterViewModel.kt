package com.example.bankingapp.ui.screens.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bankingapp.data.model.auth.RegisterRequest
import com.example.bankingapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class PasswordStrength { NONE, WEAK, MEDIUM, STRONG }

data class RegisterUiState(
    val currentStep: Int = 1,
    // Step 1
    val firstName: String = "",
    val lastName: String = "",
    val dateOfBirthMillis: Long? = null,
    val dateOfBirthDisplay: String = "",
    val dateOfBirthIso: String = "",
    val cnp: String = "",
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val dateOfBirthError: String? = null,
    val cnpError: String? = null,
    // Step 2
    val email: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val emailError: String? = null,
    val phoneNumberError: String? = null,
    val addressError: String? = null,
    // Step 3
    val password: String = "",
    val confirmPassword: String = "",
    val termsAccepted: Boolean = false,
    val passwordStrength: PasswordStrength = PasswordStrength.NONE,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val termsError: String? = null,
    // Global
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val registeredEmail: String = ""
)

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // ── Step 1 ────────────────────────────────────────────────────────────────

    fun onFirstNameChange(v: String) =
        _uiState.update { it.copy(firstName = v, firstNameError = null) }

    fun onLastNameChange(v: String) =
        _uiState.update { it.copy(lastName = v, lastNameError = null) }

    fun onDateOfBirthSelected(millis: Long) {
        _uiState.update {
            it.copy(
                dateOfBirthMillis  = millis,
                dateOfBirthDisplay = formatDateDisplay(millis),
                dateOfBirthIso     = formatDateIso(millis),
                dateOfBirthError   = null
            )
        }
    }

    fun onCnpChange(v: String) {
        if (v.length <= 13 && v.all { it.isDigit() })
            _uiState.update { it.copy(cnp = v, cnpError = null) }
    }

    fun proceedFromStep1() {
        val s = _uiState.value
        val firstNameError = when {
            s.firstName.isBlank()        -> "Prenumele este obligatoriu."
            s.firstName.trim().length < 2 -> "Minim 2 caractere."
            else                         -> null
        }
        val lastNameError = when {
            s.lastName.isBlank()        -> "Numele este obligatoriu."
            s.lastName.trim().length < 2 -> "Minim 2 caractere."
            else                        -> null
        }
        val dateOfBirthError = when {
            s.dateOfBirthMillis == null    -> "Data nașterii este obligatorie."
            !isAdult(s.dateOfBirthMillis)  -> "Trebuie să aveți minim 18 ani."
            else                           -> null
        }
        val cnpError = when {
            s.cnp.isBlank()    -> "CNP-ul este obligatoriu."
            s.cnp.length != 13 -> "CNP-ul trebuie să aibă exact 13 cifre."
            else               -> null
        }
        if (listOf(firstNameError, lastNameError, dateOfBirthError, cnpError).any { it != null }) {
            _uiState.update {
                it.copy(
                    firstNameError   = firstNameError,
                    lastNameError    = lastNameError,
                    dateOfBirthError = dateOfBirthError,
                    cnpError         = cnpError
                )
            }
            return
        }
        _uiState.update { it.copy(currentStep = 2) }
    }

    // ── Step 2 ────────────────────────────────────────────────────────────────

    fun onEmailChange(v: String) =
        _uiState.update { it.copy(email = v, emailError = null) }

    fun onPhoneNumberChange(v: String) {
        if (v.all { it.isDigit() })
            _uiState.update { it.copy(phoneNumber = v, phoneNumberError = null) }
    }

    fun onAddressChange(v: String) =
        _uiState.update { it.copy(address = v, addressError = null) }

    fun proceedFromStep2() {
        val s = _uiState.value
        val emailError = when {
            s.email.isBlank()                                    -> "Emailul este obligatoriu."
            !s.email.contains("@") || !s.email.contains(".")   -> "Introduceți un email valid."
            else                                                 -> null
        }
        val phoneNumberError = when {
            s.phoneNumber.isBlank() -> "Numărul de telefon este obligatoriu."
            else                    -> null
        }
        val addressError = when {
            s.address.isBlank()        -> "Adresa este obligatorie."
            s.address.trim().length < 5 -> "Minim 5 caractere."
            else                       -> null
        }
        if (listOf(emailError, phoneNumberError, addressError).any { it != null }) {
            _uiState.update {
                it.copy(
                    emailError       = emailError,
                    phoneNumberError = phoneNumberError,
                    addressError     = addressError
                )
            }
            return
        }
        _uiState.update { it.copy(currentStep = 3) }
    }

    fun goBack() {
        val current = _uiState.value.currentStep
        if (current > 1) _uiState.update { it.copy(currentStep = current - 1) }
    }

    // ── Step 3 ────────────────────────────────────────────────────────────────

    fun onPasswordChange(v: String) {
        _uiState.update {
            it.copy(password = v, passwordError = null, passwordStrength = computePasswordStrength(v))
        }
    }

    fun onConfirmPasswordChange(v: String) =
        _uiState.update { it.copy(confirmPassword = v, confirmPasswordError = null) }

    fun onTermsAcceptedChange(v: Boolean) =
        _uiState.update { it.copy(termsAccepted = v, termsError = null) }

    fun register() {
        val s = _uiState.value
        val passwordError = when {
            s.password.isBlank()    -> "Parola este obligatorie."
            s.password.length < 8  -> "Minim 8 caractere."
            else                   -> null
        }
        val confirmPasswordError = when {
            s.confirmPassword.isBlank()        -> "Confirmați parola."
            s.confirmPassword != s.password    -> "Parolele nu coincid."
            else                               -> null
        }
        val termsError = if (!s.termsAccepted) "Acceptați termenii pentru a continua." else null

        if (listOf(passwordError, confirmPasswordError, termsError).any { it != null }) {
            _uiState.update {
                it.copy(
                    passwordError        = passwordError,
                    confirmPasswordError = confirmPasswordError,
                    termsError           = termsError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                repository.register(
                    RegisterRequest(
                        email       = s.email.trim(),
                        password    = s.password,
                        firstName   = s.firstName.trim(),
                        lastName    = s.lastName.trim(),
                        phoneNumber = "+373${s.phoneNumber}",
                        address     = s.address.trim(),
                        dateOfBirth = s.dateOfBirthIso,
                        cnp         = s.cnp
                    )
                )
                _uiState.update {
                    it.copy(isLoading = false, isSuccess = true, registeredEmail = s.email.trim())
                }
            } catch (e: retrofit2.HttpException) {
                val message = when (e.code()) {
                    409  -> "Există deja un cont cu acest email."
                    400  -> "Date invalide. Verificați câmpurile."
                    else -> "Eroare server (${e.code()}). Încercați din nou."
                }
                _uiState.update { it.copy(isLoading = false, error = message) }
            } catch (e: java.io.IOException) {
                _uiState.update { it.copy(isLoading = false, error = "Fără conexiune la internet.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "A apărut o eroare neașteptată.") }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun formatDateDisplay(millis: Long): String {
        val months = listOf(
            "ianuarie", "februarie", "martie", "aprilie", "mai", "iunie",
            "iulie", "august", "septembrie", "octombrie", "noiembrie", "decembrie"
        )
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        return "${cal.get(Calendar.DAY_OF_MONTH)} ${months[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.YEAR)}"
    }

    private fun formatDateIso(millis: Long): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))

    private fun isAdult(millis: Long): Boolean {
        val dob   = Calendar.getInstance().apply { timeInMillis = millis }
        val today = Calendar.getInstance()
        var age   = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) age--
        return age >= 18
    }
}

fun computePasswordStrength(password: String): PasswordStrength {
    if (password.isEmpty()) return PasswordStrength.NONE
    val hasUpper   = password.any { it.isUpperCase() }
    val hasLower   = password.any { it.isLowerCase() }
    val hasDigit   = password.any { it.isDigit() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }
    val variety    = listOf(hasUpper, hasLower, hasDigit, hasSpecial).count { it }
    return when {
        password.length < 8 -> PasswordStrength.WEAK
        variety >= 3        -> PasswordStrength.STRONG
        variety >= 2        -> PasswordStrength.MEDIUM
        else                -> PasswordStrength.WEAK
    }
}
