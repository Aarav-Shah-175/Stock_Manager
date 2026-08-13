package com.waterproofing.inventory.ui.variants

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.waterproofing.inventory.data.model.BatchWithProductInfo
import com.waterproofing.inventory.domain.ExpiryCalculator
import com.waterproofing.inventory.domain.ExpiryStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VariantDetailScreen(
    variantId: Long,
    viewModel: VariantViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(variantId) {
        viewModel.setVariantId(variantId)
    }

    val variant by viewModel.variant.collectAsState()
    val batches by viewModel.batches.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var batchToEdit by remember { mutableStateOf<BatchWithProductInfo?>(null) }

    var showDeleteVariantDialog by remember { mutableStateOf(false) }
    var batchToDelete by remember { mutableStateOf<BatchWithProductInfo?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(variant?.name ?: "Variant Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteVariantDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Variant",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Batch")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Variant info summary banner
            variant?.let { v ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = v.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total Stock: ${v.totalStock} ${v.unit}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Min Alert: ${v.minStockThreshold} ${v.unit}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Inventory Batches",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (batches.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No batches currently in stock.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(batches, key = { it.id }) { batch ->
                        BatchCardItem(
                            batch = batch,
                            onEdit = { batchToEdit = batch },
                            onDelete = { batchToDelete = batch }
                        )
                    }
                }
            }
        }

        // Add Batch Dialog
        if (showAddDialog) {
            BatchAddEditDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { bNo, qty, mfg, slVal, slUnit, exp, price, supplier, inv, notes ->
                    viewModel.addBatch(bNo, qty, mfg, slVal, slUnit, exp, price, supplier, inv, notes)
                    showAddDialog = false
                }
            )
        }

        // Edit Batch Dialog
        batchToEdit?.let { batch ->
            BatchAddEditDialog(
                batch = batch,
                onDismiss = { batchToEdit = null },
                onConfirm = { bNo, qty, mfg, slVal, slUnit, exp, price, supplier, inv, notes ->
                    viewModel.updateBatch(
                        batchId = batch.id,
                        batchNumber = bNo,
                        currentQuantity = qty,
                        mfgDate = mfg,
                        shelfLifeValue = slVal,
                        shelfLifeUnit = slUnit,
                        expiryDate = exp,
                        purchasePrice = price,
                        supplier = supplier,
                        invoiceNumber = inv,
                        notes = notes
                    )
                    batchToEdit = null
                }
            )
        }

        // Delete Variant Dialog
        if (showDeleteVariantDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteVariantDialog = false },
                title = { Text("Delete Variant") },
                text = { Text("Are you sure you want to delete this variant? All associated batches and transaction history for this variant will be permanently deleted.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteVariantDialog = false
                            viewModel.deleteVariant {
                                onBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteVariantDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Delete Batch Dialog
        batchToDelete?.let { batch ->
            AlertDialog(
                onDismissRequest = { batchToDelete = null },
                title = { Text("Delete Batch") },
                text = { Text("Are you sure you want to delete batch \"${batch.batchNumber}\"? All associated transactions for this batch will be permanently deleted.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteBatch(batch.id)
                            batchToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { batchToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun BatchCardItem(
    batch: BatchWithProductInfo,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val expiryStatus = ExpiryCalculator.getStatus(batch.expiryDate)
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val statusColor = when (expiryStatus) {
        ExpiryStatus.EXPIRED -> MaterialTheme.colorScheme.error
        ExpiryStatus.EXPIRING_SOON -> MaterialTheme.colorScheme.tertiary
        ExpiryStatus.NORMAL -> MaterialTheme.colorScheme.primary
    }

    val statusText = when (expiryStatus) {
        ExpiryStatus.EXPIRED -> "EXPIRED"
        ExpiryStatus.EXPIRING_SOON -> "EXPIRING SOON"
        ExpiryStatus.NORMAL -> "NORMAL"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Batch: ${batch.batchNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Quantity: ${batch.currentQuantity} ${batch.unit}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Batch")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Batch",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Expiry: ${sdf.format(Date(batch.expiryDate))}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    batch.mfgDate?.let { mfg ->
                        Text(
                            text = "MFG: ${sdf.format(Date(mfg))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    contentColor = statusColor,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.padding(2.dp)
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Advanced business details
            if (!batch.supplier.isNullOrEmpty() || batch.purchasePrice != null || !batch.invoiceNumber.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    batch.supplier?.let {
                        Text(
                            text = "Supplier: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    batch.invoiceNumber?.let {
                        Text(
                            text = "Invoice #: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    batch.purchasePrice?.let {
                        Text(
                            text = "Price: ₹$it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (!batch.notes.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Notes: ${batch.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchAddEditDialog(
    batch: BatchWithProductInfo? = null,
    onDismiss: () -> Unit,
    onConfirm: (
        batchNumber: String,
        currentQuantity: Double,
        mfgDate: Long?,
        shelfLifeValue: Int?,
        shelfLifeUnit: String?,
        expiryDate: Long,
        purchasePrice: Double?,
        supplier: String?,
        invoiceNumber: String?,
        notes: String?
    ) -> Unit
) {
    val batchNumber = remember { batch?.batchNumber ?: "B-${SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())}" }
    var quantityStr by remember { mutableStateOf(batch?.currentQuantity?.toString() ?: "") }

    var mfgDate by remember { mutableStateOf(batch?.mfgDate) }
    var shelfLifeValueStr by remember { mutableStateOf(batch?.shelfLifeValue?.toString() ?: "") }
    var shelfLifeUnit by remember { mutableStateOf(batch?.shelfLifeUnit ?: "Months") }

    var expiryDate by remember { mutableStateOf(batch?.expiryDate ?: System.currentTimeMillis()) }
    var isManualExpiry by remember { mutableStateOf(batch != null && batch.shelfLifeValue == null) }

    var notes by remember { mutableStateOf(batch?.notes ?: "") }

    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    var showMfgPicker by remember { mutableStateOf(false) }
    var showExpPicker by remember { mutableStateOf(false) }

    // Automatic calculation when MFG date or shelf life changes
    LaunchedEffect(mfgDate, shelfLifeValueStr, shelfLifeUnit, isManualExpiry) {
        if (!isManualExpiry) {
            val mfg = mfgDate
            val value = shelfLifeValueStr.toIntOrNull()
            if (mfg != null && value != null && value > 0) {
                expiryDate = ExpiryCalculator.calculate(mfg, value, shelfLifeUnit)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (batch == null) "Add Stock Batch" else "Edit Stock Batch") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = quantityStr,
                    onValueChange = { quantityStr = it },
                    label = { Text("Current Quantity*") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider()

                Text(
                    text = "Shelf Life & Expiry Options",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isManualExpiry,
                        onCheckedChange = { isManualExpiry = it }
                    )
                    Text(text = "Specify Expiry Date manually")
                }

                // MFG Date picker
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = mfgDate?.let { sdf.format(Date(it)) } ?: "Not Set",
                        onValueChange = {},
                        label = { Text("Manufacturing Date") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showMfgPicker = true }
                    )
                }

                if (!isManualExpiry) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = shelfLifeValueStr,
                            onValueChange = { shelfLifeValueStr = it },
                            label = { Text("Shelf Life Value") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        // Exposed dropdown for unit
                        var dropdownExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = shelfLifeUnit,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Unit") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { dropdownExpanded = true }
                            )
                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                listOf("Days", "Months", "Years").forEach { unit ->
                                    DropdownMenuItem(
                                        text = { Text(unit) },
                                        onClick = {
                                            shelfLifeUnit = unit
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Expiry Date picker
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = sdf.format(Date(expiryDate)),
                        onValueChange = {},
                        label = { Text("Expiry Date*") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        enabled = isManualExpiry
                    )
                    if (isManualExpiry) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showExpPicker = true }
                        )
                    }
                }

                if (!isManualExpiry && mfgDate != null && shelfLifeValueStr.toIntOrNull() != null) {
                    Text(
                        text = "Calculated automatically from Manufacturing Date & Shelf Life.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                HorizontalDivider()

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Remarks (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityStr.toDoubleOrNull() ?: 0.0
                    val slVal = shelfLifeValueStr.toIntOrNull()

                    if (batchNumber.isNotBlank() && qty > 0.0) {
                        onConfirm(
                            batchNumber,
                            qty,
                            mfgDate,
                            if (isManualExpiry) null else slVal,
                            if (isManualExpiry) null else shelfLifeUnit,
                            expiryDate,
                            batch?.purchasePrice,
                            batch?.supplier,
                            batch?.invoiceNumber,
                            notes
                        )
                    }
                }
            ) {
                Text(if (batch == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    // Manufacturing Date Picker Dialog
    if (showMfgPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = mfgDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showMfgPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    mfgDate = datePickerState.selectedDateMillis
                    showMfgPicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMfgPicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Expiry Date Picker Dialog
    if (showExpPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = expiryDate
        )
        DatePickerDialog(
            onDismissRequest = { showExpPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    expiryDate = datePickerState.selectedDateMillis ?: expiryDate
                    showExpPicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExpPicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
