package com.waterproofing.inventory.ui.stock

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.waterproofing.inventory.data.model.StockTransactionWithDetails
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    viewModel: StockViewModel,
    onBack: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val fromDateMillis by viewModel.fromDateMillis.collectAsState()
    val toDateMillis by viewModel.toDateMillis.collectAsState()
    val transactions by viewModel.searchedTransactions.collectAsState()

    var showFromDatePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }

    val sdfDate = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    // Filter state: "ALL", "IN", "OUT"
    var filterType by remember { mutableStateOf("ALL") }
    val tabs = listOf("ALL", "IN", "OUT")

    val filtered = if (filterType == "ALL") transactions.filter { it.transactionType != "ADJUSTMENT" }
    else transactions.filter { it.transactionType == filterType }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction History") },
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
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search product or variant name...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Date Range Selection Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // From Date Field
                OutlinedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showFromDatePicker = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = fromDateMillis?.let { "From: ${sdfDate.format(Date(it))}" } ?: "From: Any",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // To Date Field
                OutlinedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showToDatePicker = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = toDateMillis?.let { "To: ${sdfDate.format(Date(it))}" } ?: "To: Any",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                if (fromDateMillis != null || toDateMillis != null || searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearSearchFilters() }) {
                        Icon(Icons.Default.Clear, contentDescription = "Reset filters", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // Type filter tabs
            ScrollableTabRow(
                selectedTabIndex = tabs.indexOf(filterType),
                edgePadding = 0.dp
            ) {
                tabs.forEachIndexed { _, tab ->
                    Tab(
                        selected = filterType == tab,
                        onClick = { filterType = tab },
                        text = { Text(tab) }
                    )
                }
            }

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No matching transactions found.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(filtered, key = { it.id }) { tx ->
                        TransactionCard(tx)
                    }
                }
            }
        }

        // From Date Picker Dialog
        if (showFromDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = fromDateMillis ?: System.currentTimeMillis()
            )
            DatePickerDialog(
                onDismissRequest = { showFromDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val selected = datePickerState.selectedDateMillis
                        viewModel.setDateRange(selected, toDateMillis)
                        showFromDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        viewModel.setDateRange(null, toDateMillis)
                        showFromDatePicker = false
                    }) {
                        Text("Clear")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // To Date Picker Dialog
        if (showToDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = toDateMillis ?: System.currentTimeMillis()
            )
            DatePickerDialog(
                onDismissRequest = { showToDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val selected = datePickerState.selectedDateMillis
                        viewModel.setDateRange(fromDateMillis, selected)
                        showToDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        viewModel.setDateRange(fromDateMillis, null)
                        showToDatePicker = false
                    }) {
                        Text("Clear")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
fun TransactionCard(tx: StockTransactionWithDetails) {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    val typeColor = when (tx.transactionType) {
        "IN" -> MaterialTheme.colorScheme.primary
        "OUT" -> MaterialTheme.colorScheme.secondary
        "ADJUSTMENT" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outline
    }

    val typeLabel = when (tx.transactionType) {
        "IN" -> "+ IN"
        "OUT" -> "− OUT"
        else -> "~ ADJ"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tx.productName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${tx.variantName} — Batch: ${tx.batchNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = typeColor
                    )
                    Text(
                        text = "${tx.quantity} ${tx.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = typeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = tx.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = sdf.format(Date(tx.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            if (!tx.customerProject.isNullOrEmpty()) {
                Text(
                    text = "Project: ${tx.customerProject}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            if (!tx.invoiceNumber.isNullOrEmpty()) {
                Text(
                    text = "Invoice: ${tx.invoiceNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
