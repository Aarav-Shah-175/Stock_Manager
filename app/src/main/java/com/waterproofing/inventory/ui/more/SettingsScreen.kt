package com.waterproofing.inventory.ui.more

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val companyName by viewModel.companyName.collectAsState()
    val expiryWarningDays by viewModel.expiryWarningDays.collectAsState()
    val defaultCurrency by viewModel.defaultCurrency.collectAsState()

    val autoBackupEnabled by viewModel.autoBackupEnabled.collectAsState()
    val backupTime by viewModel.backupTime.collectAsState()
    val keepBackups by viewModel.keepBackups.collectAsState()
    val lastSuccessfulBackup by viewModel.lastSuccessfulBackup.collectAsState()
    val lastBackupStatus by viewModel.lastBackupStatus.collectAsState()
    val autoBackupFolderUri by viewModel.autoBackupFolderUri.collectAsState()

    var companyNameInput by remember { mutableStateOf("") }
    var expiryWarningDaysInput by remember { mutableStateOf("") }
    var defaultCurrencyInput by remember { mutableStateOf("") }
    var backupTimeInput by remember { mutableStateOf("") }
    var keepBackupsInput by remember { mutableStateOf("") }

    var userHasEdited by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val sdfDisplay = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    // Folder Picker Launcher using SAF OpenDocumentTree
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            try {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
                viewModel.setAutoBackupFolderUri(uri.toString())
                successMessage = "✓ Custom backup folder set successfully."
            } catch (e: Exception) {
                errorMessage = "Failed to obtain permission for selected folder: ${e.message}"
            }
        }
    }

    // Synchronize UI inputs with DB state whenever DB state changes, unless user is currently editing
    LaunchedEffect(companyName, expiryWarningDays, defaultCurrency, backupTime, keepBackups) {
        if (!userHasEdited) {
            companyNameInput = companyName
            expiryWarningDaysInput = expiryWarningDays.toString()
            defaultCurrencyInput = defaultCurrency
            backupTimeInput = backupTime
            keepBackupsInput = keepBackups.toString()
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
                    userHasEdited = true
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
                    userHasEdited = true
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
                    userHasEdited = true
                    successMessage = null
                },
                label = { Text("Default Currency Symbol / Code") },
                placeholder = { Text("INR") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            // Automatic Backup Section
            Text(
                "Automatic Offline Backup Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Automatic Daily Backup",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Schedule daily local database backup (100% offline)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Switch(
                    checked = autoBackupEnabled,
                    onCheckedChange = { viewModel.setAutoBackupEnabled(it) }
                )
            }

            if (autoBackupEnabled) {
                // Interactive Time Picker Selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = formatDisplayTime(backupTimeInput),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Backup Scheduled Time") },
                        trailingIcon = {
                            IconButton(onClick = { showTimePicker = true }) {
                                Icon(Icons.Default.Schedule, contentDescription = "Select Backup Time")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showTimePicker = true }
                    )
                }

                OutlinedTextField(
                    value = keepBackupsInput,
                    onValueChange = {
                        keepBackupsInput = it
                        userHasEdited = true
                        successMessage = null
                    },
                    label = { Text("Keep Backups (Retention Limit)") },
                    placeholder = { Text("7") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Backup Status & Storage Location Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Backup Details & Storage Location",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Automatic Backup: ${if (autoBackupEnabled) "ON (${formatDisplayTime(backupTimeInput)})" else "OFF"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Keep Backups: $keepBackups",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Last Successful Backup: " + (lastSuccessfulBackup?.let { sdfDisplay.format(Date(it)) } ?: "Never"),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    lastBackupStatus?.let { status ->
                        if (status.startsWith("FAILED")) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Latest Status: $status",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Backup Save Folder:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (!autoBackupFolderUri.isNullOrBlank()) {
                            AssistChip(
                                onClick = {},
                                label = { Text("Custom Folder") },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        } else {
                            AssistChip(
                                onClick = {},
                                label = { Text("App Internal Storage") },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                    }

                    val folderPathText = if (!autoBackupFolderUri.isNullOrBlank()) {
                        formatFolderUri(autoBackupFolderUri!!)
                    } else {
                        File(context.filesDir, "backups").absolutePath
                    }

                    Text(
                        text = folderPathText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { folderPickerLauncher.launch(null) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Select Backup Folder")
                        }

                        if (!autoBackupFolderUri.isNullOrBlank()) {
                            IconButton(
                                onClick = {
                                    viewModel.setAutoBackupFolderUri(null)
                                    successMessage = "✓ Reset to default internal storage."
                                }
                            ) {
                                Icon(
                                    Icons.Default.RestartAlt,
                                    contentDescription = "Reset to default storage",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Automatic backups are saved safely in local app storage or your selected custom folder. To export a backup file to custom locations (Downloads, Google Drive, SD card), use the 'Backup & Restore' option in the More tab.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

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

                    val keep = keepBackupsInput.toIntOrNull()
                    if (keep == null || keep <= 0) {
                        errorMessage = "Please enter a valid positive integer for backup retention limit."
                        return@Button
                    }

                    errorMessage = null

                    viewModel.setCompanyName(companyNameInput)
                    viewModel.setExpiryWarningDays(days)
                    viewModel.setDefaultCurrency(defaultCurrencyInput)
                    viewModel.setBackupTime(backupTimeInput.ifBlank { "23:30" })
                    viewModel.setKeepBackups(keep)

                    userHasEdited = false
                    successMessage = "✓ Settings saved successfully."
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Settings")
            }
        }

        // Time Picker Dialog
        if (showTimePicker) {
            val parts = backupTimeInput.split(":")
            val initialH = parts.getOrNull(0)?.toIntOrNull() ?: 23
            val initialM = parts.getOrNull(1)?.toIntOrNull() ?: 30
            val timePickerState = rememberTimePickerState(
                initialHour = initialH,
                initialMinute = initialM,
                is24Hour = false
            )

            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val h = String.format("%02d", timePickerState.hour)
                        val m = String.format("%02d", timePickerState.minute)
                        backupTimeInput = "$h:$m"
                        userHasEdited = true
                        showTimePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text("Cancel")
                    }
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Select Daily Backup Time",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        TimePicker(state = timePickerState)
                    }
                }
            )
        }
    }
}

/** Formats HH:mm string (e.g., "23:30") to friendly 12-hour format "11:30 PM" */
private fun formatDisplayTime(timeStr: String): String {
    if (timeStr.isBlank()) return "11:30 PM"
    return try {
        val parts = timeStr.split(":")
        val h = parts[0].toInt()
        val m = parts[1].toInt()
        val ampm = if (h >= 12) "PM" else "AM"
        val h12 = if (h % 12 == 0) 12 else h % 12
        String.format("%d:%02d %s", h12, m, ampm)
    } catch (_: Exception) {
        timeStr
    }
}

/** Formats document tree URI to a friendly display string */
private fun formatFolderUri(uriStr: String): String {
    return try {
        val decoded = Uri.decode(uriStr)
        if (decoded.contains(":")) {
            val folderPath = decoded.substringAfterLast(":")
            "Folder: $folderPath"
        } else {
            decoded
        }
    } catch (_: Exception) {
        uriStr
    }
}
