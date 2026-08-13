package com.waterproofing.inventory.ui.more

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val companyName by viewModel.companyName.collectAsState()
    val expiryWarningDays by viewModel.expiryWarningDays.collectAsState()
    val defaultCurrency by viewModel.defaultCurrency.collectAsState()

    var companyNameInput by remember { mutableStateOf("") }
    var expiryWarningDaysInput by remember { mutableStateOf("") }
    var defaultCurrencyInput by remember { mutableStateOf("") }

    var isInitialized by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Initialize inputs when data loads
    if (!isInitialized && companyName != "" || expiryWarningDays != 90 || defaultCurrency != "INR") {
        companyNameInput = companyName
        expiryWarningDaysInput = expiryWarningDays.toString()
        defaultCurrencyInput = defaultCurrency
        isInitialized = true
    }

    // fallback check in case they are default but loaded
    LaunchedEffect(companyName, expiryWarningDays, defaultCurrency) {
        if (!isInitialized) {
            companyNameInput = companyName
            expiryWarningDaysInput = expiryWarningDays.toString()
            defaultCurrencyInput = defaultCurrency
            isInitialized = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "App Preferences & Defaults",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = companyNameInput,
                onValueChange = {
                    companyNameInput = it
                    successMessage = null
                },
                label = { Text("Company Name") },
                placeholder = { Text("e.g. Acme Waterproofing Solutions") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = expiryWarningDaysInput,
                onValueChange = {
                    expiryWarningDaysInput = it
                    successMessage = null
                },
                label = { Text("Expiry Warning Days Threshold") },
                placeholder = { Text("90") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = defaultCurrencyInput,
                onValueChange = {
                    defaultCurrencyInput = it
                    successMessage = null
                },
                label = { Text("Default Currency Symbol / Code") },
                placeholder = { Text("INR") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            successMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val days = expiryWarningDaysInput.toIntOrNull()
                    if (days == null || days <= 0) {
                        errorMessage = "Please enter a valid positive integer for warning days."
                        return@Button
                    }
                    errorMessage = null

                    viewModel.setCompanyName(companyNameInput)
                    viewModel.setExpiryWarningDays(days)
                    viewModel.setDefaultCurrency(defaultCurrencyInput)

                    successMessage = "✓ Settings saved successfully."
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Settings")
            }
        }
    }
}
