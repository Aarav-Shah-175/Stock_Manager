package com.waterproofing.inventory.ui.stock

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
import com.waterproofing.inventory.data.model.BatchWithProductInfo
import com.waterproofing.inventory.data.model.ProductWithCategory
import com.waterproofing.inventory.data.model.VariantWithStock
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStockScreen(
    viewModel: StockViewModel,
    onBack: () -> Unit
) {
    val products by viewModel.allProducts.collectAsState()
    val variants by viewModel.variantsForProduct.collectAsState()
    val batches by viewModel.batchesForVariant.collectAsState()

    var selectedProduct by remember { mutableStateOf<ProductWithCategory?>(null) }
    var selectedVariant by remember { mutableStateOf<VariantWithStock?>(null) }
    var selectedBatch by remember { mutableStateOf<BatchWithProductInfo?>(null) }

    var quantityStr by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("Received") }
    var invoiceNumber by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var productExpanded by remember { mutableStateOf(false) }
    var variantExpanded by remember { mutableStateOf(false) }
    var batchExpanded by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stock IN") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Select Product, Variant & Batch", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            // Product dropdown
            ExposedDropdownMenuBox(expanded = productExpanded, onExpandedChange = { productExpanded = !productExpanded }) {
                OutlinedTextField(
                    readOnly = true,
                    value = selectedProduct?.name ?: "Select Product",
                    onValueChange = {},
                    label = { Text("Product*") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = productExpanded, onDismissRequest = { productExpanded = false }) {
                    products.forEach { product ->
                        DropdownMenuItem(
                            text = { Text("${product.name} — ${product.brand}") },
                            onClick = {
                                selectedProduct = product
                                selectedVariant = null
                                selectedBatch = null
                                viewModel.selectProduct(product.id)
                                productExpanded = false
                            }
                        )
                    }
                }
            }

            // Variant dropdown
            ExposedDropdownMenuBox(expanded = variantExpanded, onExpandedChange = { if (selectedProduct != null) variantExpanded = !variantExpanded }) {
                OutlinedTextField(
                    readOnly = true,
                    value = selectedVariant?.let { "${it.name} (${it.unit})" } ?: "Select Variant",
                    onValueChange = {},
                    label = { Text("Variant*") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = variantExpanded) },
                    enabled = selectedProduct != null,
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = variantExpanded, onDismissRequest = { variantExpanded = false }) {
                    variants.forEach { variant ->
                        DropdownMenuItem(
                            text = { Text("${variant.name} — Stock: ${variant.totalStock} ${variant.unit}") },
                            onClick = {
                                selectedVariant = variant
                                selectedBatch = null
                                viewModel.selectVariant(variant.id)
                                variantExpanded = false
                            }
                        )
                    }
                }
            }

            // Batch dropdown
            ExposedDropdownMenuBox(expanded = batchExpanded, onExpandedChange = { if (selectedVariant != null) batchExpanded = !batchExpanded }) {
                OutlinedTextField(
                    readOnly = true,
                    value = selectedBatch?.let { "${it.batchNumber} — ${sdf.format(Date(it.expiryDate))}" } ?: "Select Batch",
                    onValueChange = {},
                    label = { Text("Batch*") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = batchExpanded) },
                    enabled = selectedVariant != null,
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = batchExpanded, onDismissRequest = { batchExpanded = false }) {
                    batches.forEach { batch ->
                        DropdownMenuItem(
                            text = { Text("${batch.batchNumber} | Qty: ${batch.currentQuantity} | Exp: ${sdf.format(Date(batch.expiryDate))}") },
                            onClick = {
                                selectedBatch = batch
                                batchExpanded = false
                            }
                        )
                    }
                }
            }

            HorizontalDivider()

            Text("Quantity & Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = quantityStr,
                onValueChange = { quantityStr = it },
                label = { Text("Quantity to Add*") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { selectedVariant?.let { Text(it.unit) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Reason") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = invoiceNumber,
                onValueChange = { invoiceNumber = it },
                label = { Text("Invoice Number (Optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            successMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = {
                    val batch = selectedBatch
                    val variant = selectedVariant
                    val qty = quantityStr.toDoubleOrNull()

                    when {
                        batch == null -> errorMessage = "Please select a batch."
                        qty == null || qty <= 0 -> errorMessage = "Enter a valid quantity."
                        else -> {
                            errorMessage = null
                            viewModel.stockIn(
                                batchId = batch.id,
                                variantId = batch.variantId,
                                productId = batch.productId,
                                quantity = qty,
                                unit = variant?.unit ?: batch.unit,
                                reason = reason,
                                invoiceNumber = invoiceNumber,
                                notes = notes
                            ) { result ->
                                when (result) {
                                    is StockOperationResult.Success -> {
                                        successMessage = "✓ Stock added successfully."
                                        quantityStr = ""
                                        invoiceNumber = ""
                                        notes = ""
                                    }
                                    is StockOperationResult.Error -> errorMessage = result.message
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedBatch != null
            ) {
                Text("Confirm Stock IN")
            }
        }
    }
}
