package com.waterproofing.inventory.ui.stock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.waterproofing.inventory.data.model.BatchWithProductInfo
import com.waterproofing.inventory.data.model.ProductWithCategory
import com.waterproofing.inventory.data.model.VariantWithStock
import com.waterproofing.inventory.domain.ExpiryCalculator
import com.waterproofing.inventory.domain.ExpiryStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoveStockScreen(
    viewModel: StockViewModel,
    onBack: () -> Unit
) {
    val products by viewModel.allProducts.collectAsState()
    val variants by viewModel.variantsForProduct.collectAsState()
    val batches by viewModel.batchesForVariant.collectAsState()

    var selectedProduct by remember { mutableStateOf<ProductWithCategory?>(null) }
    var selectedVariant by remember { mutableStateOf<VariantWithStock?>(null) }
    // FEFO: auto-suggest first non-depleted batch
    var selectedBatch by remember { mutableStateOf<BatchWithProductInfo?>(null) }

    var productSearchQuery by remember { mutableStateOf("") }
    var quantityStr by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("Issued") }
    var customerProject by remember { mutableStateOf("") }
    var invoiceNumber by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var productExpanded by remember { mutableStateOf(false) }
    var variantExpanded by remember { mutableStateOf(false) }
    var batchExpanded by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    // Sync search query when selectedProduct changes
    LaunchedEffect(selectedProduct) {
        productSearchQuery = selectedProduct?.let { "${it.name} — ${it.brand}" } ?: ""
    }

    val filteredProducts = remember(products, productSearchQuery, selectedProduct) {
        val selectedName = selectedProduct?.let { "${it.name} — ${it.brand}" } ?: ""
        if (productSearchQuery == selectedName) products
        else products.filter {
            it.name.contains(productSearchQuery, ignoreCase = true) ||
            it.brand.contains(productSearchQuery, ignoreCase = true)
        }
    }

    // Auto-select FEFO batch when batch list changes
    LaunchedEffect(batches) {
        if (selectedBatch == null) {
            selectedBatch = batches.firstOrNull { it.currentQuantity > 0 }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stock OUT") },
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
            Text("Select Product & Variant", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            // Product searchable autocomplete dropdown
            ExposedDropdownMenuBox(
                expanded = productExpanded,
                onExpandedChange = { productExpanded = it }
            ) {
                OutlinedTextField(
                    value = productSearchQuery,
                    onValueChange = {
                        productSearchQuery = it
                        productExpanded = true
                        if (it.isBlank()) {
                            selectedProduct = null
                            selectedVariant = null
                            selectedBatch = null
                            viewModel.selectProduct(null)
                        }
                    },
                    label = { Text("Product* (Searchable)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                if (filteredProducts.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = productExpanded,
                        onDismissRequest = { productExpanded = false }
                    ) {
                        filteredProducts.forEach { product ->
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
            }

            // Variant dropdown
            ExposedDropdownMenuBox(expanded = variantExpanded, onExpandedChange = { if (selectedProduct != null) variantExpanded = !variantExpanded }) {
                OutlinedTextField(
                    readOnly = true,
                    value = selectedVariant?.let { "${it.name} — Stock: ${it.totalStock} ${it.unit}" } ?: "Select Variant",
                    onValueChange = {},
                    label = { Text("Variant*") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = variantExpanded) },
                    enabled = selectedProduct != null,
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = variantExpanded, onDismissRequest = { variantExpanded = false }) {
                    variants.forEach { variant ->
                        DropdownMenuItem(
                            text = { Text("${variant.name} — ${variant.totalStock} ${variant.unit}") },
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

            // FEFO Batch selector
            if (batches.isNotEmpty()) {
                HorizontalDivider()
                Text(
                    "Batch Selection",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Oldest expiry batch is auto-selected.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                ExposedDropdownMenuBox(expanded = batchExpanded, onExpandedChange = { batchExpanded = !batchExpanded }) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedBatch?.let { "${it.batchNumber} | Qty: ${it.currentQuantity} | Exp: ${sdf.format(Date(it.expiryDate))}" } ?: "No batch selected",
                        onValueChange = {},
                        label = { Text("Selected Batch") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = batchExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = batchExpanded, onDismissRequest = { batchExpanded = false }) {
                        batches.filter { it.currentQuantity > 0 }.forEach { batch ->
                            val status = ExpiryCalculator.getStatus(batch.expiryDate)
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("${batch.batchNumber} | Qty: ${batch.currentQuantity}")
                                        Text(
                                            sdf.format(Date(batch.expiryDate)),
                                            color = when (status) {
                                                ExpiryStatus.EXPIRED -> MaterialTheme.colorScheme.error
                                                ExpiryStatus.EXPIRING_SOON -> MaterialTheme.colorScheme.tertiary
                                                ExpiryStatus.NORMAL -> MaterialTheme.colorScheme.primary
                                            }
                                        )
                                    }
                                },
                                onClick = {
                                    selectedBatch = batch
                                    batchExpanded = false
                                }
                            )
                        }
                    }
                }

                // Expiry warning
                selectedBatch?.let { batch ->
                    val status = ExpiryCalculator.getStatus(batch.expiryDate)
                    if (status != ExpiryStatus.NORMAL) {
                        Surface(
                            color = if (status == ExpiryStatus.EXPIRED)
                                MaterialTheme.colorScheme.errorContainer
                            else
                                MaterialTheme.colorScheme.tertiaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null)
                                Text(
                                    if (status == ExpiryStatus.EXPIRED)
                                        "Warning: This batch is EXPIRED (${sdf.format(Date(batch.expiryDate))})"
                                    else
                                        "This batch is expiring soon (${sdf.format(Date(batch.expiryDate))})",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider()

            Text("Quantity & Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = quantityStr,
                onValueChange = { quantityStr = it },
                label = { Text("Quantity to Remove*") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { selectedVariant?.let { Text(it.unit) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                supportingText = selectedBatch?.let { { Text("Available: ${it.currentQuantity}") } }
            )

            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Reason") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

        /*    OutlinedTextField(
                value = customerProject,
                onValueChange = { customerProject = it },
                label = { Text("Customer / Project (Optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = invoiceNumber,
                onValueChange = { invoiceNumber = it },
                label = { Text("Invoice Number (Optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            ) */

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
                            viewModel.stockOut(
                                batchId = batch.id,
                                variantId = batch.variantId,
                                productId = batch.productId,
                                quantity = qty,
                                unit = variant?.unit ?: batch.unit,
                                reason = reason,
                                customerProject = customerProject,
                                invoiceNumber = invoiceNumber,
                                notes = notes
                            ) { result ->
                                when (result) {
                                    is StockOperationResult.Success -> {
                                        successMessage = "✓ Stock issued successfully."
                                        quantityStr = ""
                                        customerProject = ""
                                        invoiceNumber = ""
                                        notes = ""
                                        // Reset FEFO suggestion
                                        selectedBatch = null
                                    }
                                    is StockOperationResult.Error -> errorMessage = result.message
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedBatch != null && selectedBatch!!.currentQuantity > 0,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Confirm Stock OUT")
            }
        }
    }
}
