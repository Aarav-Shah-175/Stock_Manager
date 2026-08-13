package com.waterproofing.inventory.ui.more

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val vm: BackupRestoreViewModel = viewModel()
    val state by vm.state.collectAsState()

    // SAF launcher for picking a .db file to restore
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            // Persist read permission across reboots
            context.contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            vm.importBackup(context, uri)
        }
    }

    // Confirmation dialog before restore (destructive action)
    var showRestoreConfirm by remember { mutableStateOf(false) }

    if (showRestoreConfirm) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirm = false },
            title = { Text("Restore Database?") },
            text = {
                Text(
                    "This will REPLACE all current inventory data with the selected backup file. " +
                    "The app will need to restart for changes to take effect.\n\n" +
                    "This action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestoreConfirm = false
                        importLauncher.launch(arrayOf("application/octet-stream", "*/*"))
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Choose File & Restore") }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirm = false }) { Text("Cancel") }
            }
        )
    }

    // Share intent side-effect
    LaunchedEffect(state) {
        if (state is BackupRestoreState.ExportSuccess) {
            val shareIntent = (state as BackupRestoreState.ExportSuccess).shareIntent
            context.startActivity(Intent.createChooser(shareIntent, "Save Backup To…"))
        }
    }

    // Post-import restart dialog
    var showRestartDialog by remember { mutableStateOf(false) }
    LaunchedEffect(state) {
        if (state is BackupRestoreState.ImportSuccess) showRestartDialog = true
    }
    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Restore Complete") },
            text = {
                val name = (state as? BackupRestoreState.ImportSuccess)?.fileName ?: ""
                Text("Database restored from:\n$name\n\nPlease restart the app now for changes to take effect.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showRestartDialog = false
                    vm.resetState()
                    // Restart the app
                    val pm = context.packageManager
                    val intent = pm.getLaunchIntentForPackage(context.packageName)
                    intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    (context as? Activity)?.finishAffinity()
                }) { Text("Restart Now") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore") },
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Info card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Database Backup",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Backup creates a copy of the full inventory database. " +
                        "Restore replaces all data with a previously saved backup file. " +
                        "Store backups in a safe location (cloud drive, email, etc.).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            HorizontalDivider()

            // Export button
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { vm.exportBackup(context) }
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text("Export Backup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "Save a timestamped .db backup file",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Import button
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showRestoreConfirm = true }
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Upload,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Column {
                        Text("Restore Backup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "Replace current data from a .db backup file",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Status feedback
            when (val s = state) {
                is BackupRestoreState.Loading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Working…")
                    }
                }
                is BackupRestoreState.Error -> {
                    Text(
                        "⚠ ${s.message}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(onClick = vm::resetState, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("Dismiss")
                    }
                }
                is BackupRestoreState.ExportSuccess -> {
                    Text(
                        "✓ Backup created. Choose where to save it in the share sheet.",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else -> {}
            }
        }
    }
}
