package com.waterproofing.inventory.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.waterproofing.inventory.data.model.BatchWithProductInfo
import com.waterproofing.inventory.data.model.LowStockVariant
import com.waterproofing.inventory.data.model.StockTransactionWithDetails
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToLowStock: () -> Unit,
    onNavigateToExpiry: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val productCount by viewModel.activeProductCount.collectAsState()
    val variantCount by viewModel.activeVariantCount.collectAsState()
    val lowStockVariants by viewModel.lowStockVariants.collectAsState()
    val expiredBatches by viewModel.expiredBatches.collectAsState()
    val expiringSoon by viewModel.expiringSoonBatches.collectAsState()
    val recentTx by viewModel.recentTransactions.collectAsState()
    val warningDays by viewModel.expiryWarningDays.collectAsState()
    val companyName by viewModel.companyName.collectAsState()

    val dashboardTitle = if (companyName.isNotBlank()) "Dashboard - $companyName" else "Dashboard"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(dashboardTitle) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // ── Summary stat cards ──────────────────────────────────
            item {
                Text(
                    "Inventory Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        label = "Products",
                        value = productCount.toString(),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Variants",
                        value = variantCount.toString(),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        label = "Low Stock",
                        value = lowStockVariants.size.toString(),
                        containerColor = if (lowStockVariants.isEmpty())
                            MaterialTheme.colorScheme.surfaceVariant
                        else
                            MaterialTheme.colorScheme.errorContainer,
                        contentColor = if (lowStockVariants.isEmpty())
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { if (lowStockVariants.isNotEmpty()) onNavigateToLowStock() }
                    )
                    StatCard(
                        label = "Expired",
                        value = expiredBatches.size.toString(),
                        containerColor = if (expiredBatches.isEmpty())
                            MaterialTheme.colorScheme.surfaceVariant
                        else
                            MaterialTheme.colorScheme.errorContainer,
                        contentColor = if (expiredBatches.isEmpty())
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { if (expiredBatches.isNotEmpty()) onNavigateToExpiry() }
                    )
                }
            }

            // ── Expiring soon alert ─────────────────────────────────
            if (expiringSoon.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "⚠ Expiring in $warningDays Days (${expiringSoon.size})",
                        actionLabel = "View All",
                        onAction = onNavigateToExpiry
                    )
                }
                items(expiringSoon.take(3), key = { "exp-${it.id}" }) { batch ->
                    ExpiryAlertRow(batch)
                }
                if (expiringSoon.size > 3) {
                    item {
                        TextButton(onClick = onNavigateToExpiry, modifier = Modifier.fillMaxWidth()) {
                            Text("+ ${expiringSoon.size - 3} more batches expiring soon")
                        }
                    }
                }
            }

            // ── Low stock alert ─────────────────────────────────────
            if (lowStockVariants.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "🔴 Low Stock Alerts (${lowStockVariants.size})",
                        actionLabel = "View All",
                        onAction = onNavigateToLowStock
                    )
                }
                items(lowStockVariants.take(3), key = { "ls-${it.variantId}" }) { variant ->
                    LowStockAlertRow(variant)
                }
                if (lowStockVariants.size > 3) {
                    item {
                        TextButton(onClick = onNavigateToLowStock, modifier = Modifier.fillMaxWidth()) {
                            Text("+ ${lowStockVariants.size - 3} more below minimum")
                        }
                    }
                }
            }

            // ── Recent transactions ─────────────────────────────────
            if (recentTx.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Recent Transactions",
                        actionLabel = "History",
                        onAction = onNavigateToHistory
                    )
                }
                items(recentTx, key = { "tx-${it.id}" }) { tx ->
                    RecentTransactionRow(tx)
                }
            }

            // ── Empty state ─────────────────────────────────────────
            if (productCount == 0) {
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Inventory,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No inventory yet.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            "Add products via the Products tab.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun SectionHeader(title: String, actionLabel: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        TextButton(onClick = onAction) { Text(actionLabel) }
    }
}

@Composable
private fun LowStockAlertRow(variant: LowStockVariant) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${variant.productName} — ${variant.variantName}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    "Stock: ${variant.currentStock} / Min: ${variant.minStockThreshold} ${variant.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun ExpiryAlertRow(batch: BatchWithProductInfo) {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${batch.productName} — ${batch.variantName}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    "Batch ${batch.batchNumber} · Exp: ${sdf.format(Date(batch.expiryDate))} · Qty: ${batch.currentQuantity} ${batch.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

@Composable
private fun RecentTransactionRow(tx: StockTransactionWithDetails) {
    val sdf = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val typeColor = when (tx.transactionType) {
        "IN" -> MaterialTheme.colorScheme.primary
        "OUT" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.tertiary
    }
    val typeLabel = when (tx.transactionType) {
        "IN" -> "+ IN"; "OUT" -> "− OUT"; else -> "~ ADJ"
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${tx.productName} — ${tx.variantName}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    tx.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(typeLabel, style = MaterialTheme.typography.labelMedium, color = typeColor, fontWeight = FontWeight.Bold)
                Text("${tx.quantity} ${tx.unit}", style = MaterialTheme.typography.bodySmall, color = typeColor)
                Text(sdf.format(Date(tx.timestamp)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}
