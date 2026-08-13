package com.waterproofing.inventory.ui.more

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onNavigateToExpiry: () -> Unit,
    onNavigateToLowStock: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToBackupRestore: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("More") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Alerts & Tools", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            MoreMenuItem(
                icon = Icons.Default.Schedule,
                title = "Expiry Management",
                subtitle = "View expired and expiring-soon batches",
                onClick = onNavigateToExpiry
            )
            MoreMenuItem(
                icon = Icons.Default.Warning,
                title = "Low Stock Alerts",
                subtitle = "Variants below minimum threshold",
                onClick = onNavigateToLowStock
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Text("Settings", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            MoreMenuItem(
                icon = Icons.Default.Category,
                title = "Manage Categories",
                subtitle = "Add, edit, or delete product categories",
                onClick = onNavigateToCategories
            )
            MoreMenuItem(
                icon = Icons.Default.Backup,
                title = "Backup & Restore",
                subtitle = "Export or import database",
                onClick = onNavigateToBackupRestore
            )
            MoreMenuItem(
                icon = Icons.Default.Settings,
                title = "Settings",
                subtitle = "App preferences and defaults",
                onClick = onNavigateToSettings
            )
        }
    }
}

@Composable
private fun MoreMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}
