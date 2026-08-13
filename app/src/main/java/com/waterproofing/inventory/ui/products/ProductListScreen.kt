package com.waterproofing.inventory.ui.products

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.waterproofing.inventory.data.entity.CategoryEntity
import com.waterproofing.inventory.data.entity.ProductEntity
import com.waterproofing.inventory.data.model.ProductWithCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    viewModel: ProductViewModel,
    onNavigateToDetail: (Long) -> Unit
) {
    val products by viewModel.products.collectAsState()
    val archivedProducts by viewModel.archivedProducts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0 = Active, 1 = Archived

    var showAddDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<ProductWithCategory?>(null) }
    var productToArchive by remember { mutableStateOf<ProductWithCategory?>(null) }
    var productToRestore by remember { mutableStateOf<ProductWithCategory?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Inventory Products") })
        },
        floatingActionButton = {
            if (activeTab == 0) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Product")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search by name, brand, category, SKU, batch...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Tab Row
            TabRow(selectedTabIndex = activeTab) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Active") }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Archived") }
                )
            }

            // Products List
            val displayProducts = if (activeTab == 0) products else archivedProducts

            if (displayProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No search results." else "No products found.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayProducts, key = { it.id }) { product ->
                        ProductCardItem(
                            product = product,
                            onCardClick = { onNavigateToDetail(product.id) },
                            onEdit = { productToEdit = product },
                            onArchive = { productToArchive = product },
                            onRestore = { productToRestore = product }
                        )
                    }
                }
            }
        }

        // Add Product Dialog
        if (showAddDialog) {
            ProductAddEditDialog(
                categories = categories,
                onDismiss = { showAddDialog = false },
                onConfirm = { name, brand, categoryId, desc ->
                    viewModel.addProduct(name, brand, categoryId, desc)
                    showAddDialog = false
                }
            )
        }

        // Edit Product Dialog
        productToEdit?.let { product ->
            ProductAddEditDialog(
                product = product,
                categories = categories,
                onDismiss = { productToEdit = null },
                onConfirm = { name, brand, categoryId, desc ->
                    viewModel.updateProduct(
                        ProductEntity(
                            id = product.id,
                            name = name,
                            brand = brand,
                            categoryId = categoryId,
                            description = desc,
                            isArchived = product.isArchived,
                            createdAt = product.createdAt
                        )
                    )
                    productToEdit = null
                }
            )
        }

        // Archive Dialog
        productToArchive?.let { product ->
            AlertDialog(
                onDismissRequest = { productToArchive = null },
                title = { Text("Archive Product") },
                text = { Text("Archive \"${product.name}\"? It will be hidden from normal active list but its transaction history is preserved.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.archiveProduct(product.id)
                            productToArchive = null
                        }
                    ) {
                        Text("Archive")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { productToArchive = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Restore Dialog
        productToRestore?.let { product ->
            AlertDialog(
                onDismissRequest = { productToRestore = null },
                title = { Text("Restore Product") },
                text = { Text("Restore \"${product.name}\" to Active inventory?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.restoreProduct(product.id)
                            productToRestore = null
                        }
                    ) {
                        Text("Restore")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { productToRestore = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProductCardItem(
    product: ProductWithCategory,
    onCardClick: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onRestore: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Brand: ${product.brand}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Product",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (!product.isArchived) {
                        IconButton(onClick = onArchive) {
                            Icon(
                                imageVector = Icons.Default.Archive,
                                contentDescription = "Archive Product",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        IconButton(onClick = onRestore) {
                            Icon(
                                imageVector = Icons.Default.Unarchive,
                                contentDescription = "Restore Product",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (product.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            product.categoryName?.let { category ->
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow {
                    AssistChip(
                        onClick = {},
                        label = { Text(category) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductAddEditDialog(
    product: ProductWithCategory? = null,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long?, String) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var brand by remember { mutableStateOf(product?.brand ?: "") }
    var desc by remember { mutableStateOf(product?.description ?: "") }

    var expanded by remember { mutableStateOf(false) }
    var selectedCategory: CategoryEntity? by remember {
        mutableStateOf(categories.find { it.id == product?.categoryId })
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Add Product" else "Edit Product") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category dropdown selector
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedCategory?.name ?: "No Category Selected",
                        onValueChange = {},
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None (Clear Category)") },
                            onClick = {
                                selectedCategory = null
                                expanded = false
                            }
                        )
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description / Notes") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && brand.isNotBlank()) {
                        onConfirm(name, brand, selectedCategory?.id, desc)
                    }
                }
            ) {
                Text(if (product == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
