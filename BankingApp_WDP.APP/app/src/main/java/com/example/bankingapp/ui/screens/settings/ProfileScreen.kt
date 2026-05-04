package com.example.bankingapp.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bankingapp.ui.components.BaGhostButton
import com.example.bankingapp.ui.components.BaInput
import com.example.bankingapp.ui.components.BaPrimaryButton
import com.example.bankingapp.ui.theme.BaDarkInk3
import com.example.bankingapp.ui.theme.BaGold
import com.example.bankingapp.ui.theme.BaLightInk3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isDark = isSystemInDarkTheme()
    val ink3 = if (isDark) BaDarkInk3 else BaLightInk3

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("Informațiile au fost salvate.")
            viewModel.clearSaveSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = "Profil",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector        = Icons.Default.ArrowBack,
                            contentDescription = "Înapoi"
                        )
                    }
                },
                actions = {
                    if (!uiState.isEditMode) {
                        IconButton(onClick = { viewModel.enterEditMode() }) {
                            Icon(
                                imageVector        = Icons.Default.Edit,
                                contentDescription = "Editează",
                                tint               = BaGold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Column(
                modifier            = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = BaGold)
            }
        } else if (uiState.isEditMode) {
            EditModeContent(
                uiState    = uiState,
                viewModel  = viewModel,
                modifier   = Modifier.padding(innerPadding)
            )
        } else {
            ViewModeContent(
                uiState  = uiState,
                ink3     = ink3,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun ViewModeContent(
    uiState: ProfileUiState,
    ink3: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        val maskedCnp = "—"

        ProfileFieldCard(label = "PRENUME",       value = uiState.firstName.ifEmpty { "—" },    ink3 = ink3)
        Spacer(Modifier.height(8.dp))
        ProfileFieldCard(label = "NUME",          value = uiState.lastName.ifEmpty { "—" },     ink3 = ink3)
        Spacer(Modifier.height(8.dp))
        ProfileFieldCard(label = "EMAIL",         value = uiState.email.ifEmpty { "—" },        ink3 = ink3)
        Spacer(Modifier.height(8.dp))
        ProfileFieldCard(label = "TELEFON",       value = uiState.phone.ifEmpty { "—" },        ink3 = ink3)
        Spacer(Modifier.height(8.dp))
        ProfileFieldCard(label = "ADRESĂ",        value = uiState.address.ifEmpty { "—" },      ink3 = ink3)
        Spacer(Modifier.height(8.dp))
        ProfileFieldCard(label = "DATA NAȘTERII", value = uiState.dateOfBirth.ifEmpty { "—" },  ink3 = ink3)
        Spacer(Modifier.height(8.dp))
        ProfileFieldCard(label = "CNP",           value = maskedCnp,                            ink3 = ink3)
    }
}

@Composable
private fun ProfileFieldCard(
    label: String,
    value: String,
    ink3: androidx.compose.ui.graphics.Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = ink3
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text  = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun EditModeContent(
    uiState: ProfileUiState,
    viewModel: ProfileViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        BaInput(
            value         = uiState.editFirstName,
            onValueChange = { viewModel.onEditFirstName(it) },
            label         = "Prenume"
        )

        Spacer(Modifier.height(12.dp))

        BaInput(
            value         = uiState.editLastName,
            onValueChange = { viewModel.onEditLastName(it) },
            label         = "Nume"
        )

        Spacer(Modifier.height(12.dp))

        // EMAIL — read-only (politică bancară)
        BaInput(
            value         = uiState.email,
            onValueChange = {},
            label         = "Email",
            enabled       = false
        )

        Spacer(Modifier.height(12.dp))

        BaInput(
            value           = uiState.editPhone,
            onValueChange   = { viewModel.onEditPhone(it) },
            label           = "Telefon",
            keyboardType    = KeyboardType.Phone
        )

        Spacer(Modifier.height(12.dp))

        BaInput(
            value         = uiState.editAddress,
            onValueChange = { viewModel.onEditAddress(it) },
            label         = "Adresă"
        )

        Spacer(Modifier.height(12.dp))

        BaInput(
            value         = uiState.editDateOfBirth,
            onValueChange = { viewModel.onEditDateOfBirth(it) },
            label         = "Data nașterii (ex: 1995-03-15)"
        )

        Spacer(Modifier.height(12.dp))

        // CNP — read-only (politică bancară)
        BaInput(
            value         = "—",
            onValueChange = {},
            label         = "CNP",
            enabled       = false
        )

        Spacer(Modifier.height(24.dp))

        BaPrimaryButton(
            text      = "Salvează",
            onClick   = { viewModel.saveProfile() },
            isLoading = uiState.isSaving,
            modifier  = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            BaGhostButton(
                text    = "Anulează",
                onClick = { viewModel.cancelEdit() }
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}
