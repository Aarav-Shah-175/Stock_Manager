package com.waterproofing.inventory.ui.products

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.waterproofing.inventory.data.entity.VariantEntity
import com.waterproofing.inventory.data.model.VariantWithStock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: Long,
    viewModel: ProductDetailViewModel,
    onBack: () -> Unit,
    onNavigateToVariant: (Long) -> Unit
) {
    LaunchedEffect(productId) {
        viewModel.setProductId(productId)
    }

    val product by viewModel.product.collectAsState()
    val variants by viewModel.variants.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var variantToEdit by remember { mutableStateOf<VariantWithStock?>(null) }
    var variantToArchive by remember { mutableStateOf<VariantWithStock?>(null) }

    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product?.name ?: "Product Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Product",
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
                Icon(Icons.Default.Add, contentDescription = "Add Variant")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Product Info Banner
            product?.let { prod ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = prod.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        prod.categoryName?.let { cat ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Category: $cat",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (prod.description.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = prod.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Variants Header
            Text(
                text = "Product Variants",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Variants List
            if (variants.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No variants added to this product.",
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(variants, key = { it.id }) { variant ->
                        VariantCardItem(
                            variant = variant,
                            onClick = { onNavigateToVariant(variant.id) },
                            onEdit = { variantToEdit = variant },
                            onArchive = { variantToArchive = variant }
                        )
                    }
                }
            }
        }

        // Add Variant Dialog
        if (showAddDialog) {
            VariantAddEditDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { name, qValue, unit, threshold ->
                    viewModel.addVariant(name, qValue, unit, threshold)
                    showAddDialog = false
                }
            )
        }

        // Edit Variant Dialog
        variantToEdit?.let { variant ->
            VariantAddEditDialog(
                variant = variant,
                onDismiss = { variantToEdit = null },
                onConfirm = { name, qValue, unit, threshold ->
                    viewModel.updateVariant(
                        VariantEntity(
                            id = variant.id,
                            productId = variant.productId,
                            name = name,
                            quantityValue = qValue,
                            unit = unit,
                            minStockThreshold = threshold,
                            isArchived = variant.isArchived,
                            createdAt = variant.createdAt
                        )
                    )
                    variantToEdit = null
                }
            )
        }

        // Archive Variant Dialog
        variantToArchive?.let { variant ->
            AlertDialog(
                onDismissRequest = { variantToArchive = null },
                title = { Text("Archive Variant") },
                text = { Text("Archive variant \"${variant.name}\"? It will be hidden from normal active views but history remains.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.archiveVariant(variant.id)
                            variantToArchive = null
                        }
                    ) {
                        Text("Archive")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { variantToArchive = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Delete Product Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Product") },
                text = { Text("Are you sure you want to delete this product? All variants, batches, and transactions for this product will be permanently deleted.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteDialog = false
                            viewModel.deleteProduct {
                                onBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun VariantCardItem(
    variant: VariantWithStock,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit
) {
    val isLowStock = variant.totalStock < variant.minStockThreshold

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isLowStock) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = variant.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isLowStock) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Variant",
                            tint = if (isLowStock) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onArchive) {
                        Icon(
                            imageVector = Icons.Default.Archive,
                            contentDescription = "Archive Variant",
                            tint = if (isLowStock) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Total Stock: ${variant.totalStock} ${variant.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Minimum Stock: ${variant.minStockThreshold} ${variant.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isLowStock) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline
                    )
                }

                if (isLowStock) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Low Stock Warning",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.width(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "LOW STOCK",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VariantAddEditDialog(
    variant: VariantWithStock? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, quantityValue: Double, unit: String, minStockThreshold: Double) -> Unit
) {
    var name by remember { mutableStateOf(variant?.name ?: "") }
    var unit by remember { mutableStateOf(variant?.unit ?: "") }
    var thresholdStr by remember { mutableStateOf(variant?.minStockThreshold?.let { if (it > 0.0) it.toString() else "" } ?: "") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (variant == null) "Add Variant" else "Edit Variant") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { 
                        name = it
                        if (it.isNotBlank()) showError = false
                    },
                    label = { Text("Variant Name* (e.g. 5 kg bucket, 20L drum)") },
                    isError = showError && name.isBlank(),
                    supportingText = if (showError && name.isBlank()) {
                        { Text("Variant name is required", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = { 
                        unit = it
                        if (it.isNotBlank()) showError = false
                    },
                    label = { Text("Unit* (e.g. Bucket, Can, kg, L)") },
                    isError = showError && unit.isBlank(),
                    supportingText = if (showError && unit.isBlank()) {
                        { Text("Unit is required", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = thresholdStr,
                    onValueChange = { thresholdStr = it },
                    label = { Text("Low Stock Alert At (units)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val threshold = thresholdStr.toDoubleOrNull() ?: 0.0
                    val qValue = variant?.quantityValue ?: 1.0
                    if (name.isNotBlank() && unit.isNotBlank()) {
                        onConfirm(name.trim(), qValue, unit.trim(), threshold)
                    } else {
                        showError = true
                    }
                }
            ) {
                Text(if (variant == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
