package com.waterproofing.inventory.ui.more

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.waterproofing.inventory.data.model.BatchWithProductInfo
import com.waterproofing.inventory.domain.ExpiryCalculator
import com.waterproofing.inventory.domain.ExpiryStatus
import com.waterproofing.inventory.ui.dashboard.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpiryManagementScreen(
    viewModel: DashboardViewModel,
    onBack: () -> Unit
) {
    val expired by viewModel.expiredBatches.collectAsState()
    val expiringSoon by viewModel.expiringSoonBatches.collectAsState()
    val warningDays by viewModel.expiryWarningDays.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Expiring Soon (${expiringSoon.size})", "Expired (${expired.size})")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expiry Management") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            val currentList = if (selectedTab == 0) expiringSoon else expired

            if (currentList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (selectedTab == 0) "No batches expiring within $warningDays days." else "No expired batches with remaining stock.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(currentList, key = { it.id }) { batch ->
                        ExpiryBatchCard(batch)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpiryBatchCard(batch: BatchWithProductInfo) {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val status = ExpiryCalculator.getStatus(batch.expiryDate)
    val isExpired = status == ExpiryStatus.EXPIRED

    val containerColor = if (isExpired) MaterialTheme.colorScheme.errorContainer
    else MaterialTheme.colorScheme.tertiaryContainer
    val contentColor = if (isExpired) MaterialTheme.colorScheme.onErrorContainer
    else MaterialTheme.colorScheme.onTertiaryContainer
    val accentColor = if (isExpired) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.tertiary

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(batch.productName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = contentColor)
                    Text(batch.variantName, style = MaterialTheme.typography.bodySmall, color = contentColor)
                }
                Surface(
                    color = accentColor.copy(alpha = 0.2f),
                    contentColor = accentColor,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        if (isExpired) "EXPIRED" else "EXPIRING SOON",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = accentColor.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Batch: ${batch.batchNumber}", style = MaterialTheme.typography.bodySmall, color = contentColor)
                Text("Expiry: ${sdf.format(Date(batch.expiryDate))}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = accentColor)
            }
            Text("Remaining Qty: ${batch.currentQuantity} ${batch.unit}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = contentColor)
            batch.supplier?.let {
                Text("Supplier: $it", style = MaterialTheme.typography.bodySmall, color = contentColor)
            }
        }
    }
}
