package com.waterproofing.inventory.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.waterproofing.inventory.InventoryApplication
import com.waterproofing.inventory.ui.ViewModelFactory
import com.waterproofing.inventory.ui.dashboard.DashboardScreen
import com.waterproofing.inventory.ui.dashboard.DashboardViewModel
import com.waterproofing.inventory.ui.more.CategoryManagementScreen
import com.waterproofing.inventory.ui.more.CategoryViewModel
import com.waterproofing.inventory.ui.more.ExpiryManagementScreen
import com.waterproofing.inventory.ui.more.LowStockScreen
import com.waterproofing.inventory.ui.more.MoreScreen
import com.waterproofing.inventory.ui.products.ProductDetailScreen
import com.waterproofing.inventory.ui.products.ProductDetailViewModel
import com.waterproofing.inventory.ui.products.ProductListScreen
import com.waterproofing.inventory.ui.products.ProductViewModel
import com.waterproofing.inventory.ui.stock.AddStockScreen
import com.waterproofing.inventory.ui.stock.RemoveStockScreen
import com.waterproofing.inventory.ui.stock.StockScreen
import com.waterproofing.inventory.ui.stock.StockViewModel
import com.waterproofing.inventory.ui.stock.TransactionHistoryScreen
import com.waterproofing.inventory.ui.variants.VariantDetailScreen
import com.waterproofing.inventory.ui.variants.VariantViewModel

sealed class NavigationItem(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : NavigationItem(Screen.Dashboard.route, "Dashboard", Icons.Default.Dashboard)
    object Products : NavigationItem(Screen.Products.route, "Products", Icons.Default.Inventory)
    object Stock : NavigationItem(Screen.Stock.route, "Stock", Icons.AutoMirrored.Filled.ListAlt)
    object More : NavigationItem(Screen.More.route, "More", Icons.Default.MoreHoriz)
}

@Composable
fun MainAppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as InventoryApplication

    val factory = remember(app) {
        ViewModelFactory(
            app.categoryRepository,
            app.productRepository,
            app.variantRepository,
            app.batchRepository,
            app.transactionRepository
        )
    }

    val productViewModel: ProductViewModel = viewModel(factory = factory)
    val categoryViewModel: CategoryViewModel = viewModel(factory = factory)
    val productDetailViewModel: ProductDetailViewModel = viewModel(factory = factory)
    val variantViewModel: VariantViewModel = viewModel(factory = factory)
    val stockViewModel: StockViewModel = viewModel(factory = factory)
    val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)

    val items = listOf(
        NavigationItem.Dashboard,
        NavigationItem.Products,
        NavigationItem.Stock,
        NavigationItem.More
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            // Only show bottom navigation on root screens
            val isRootScreen = items.any { it.route == currentRoute }

            if (isRootScreen) {
                NavigationBar {
                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToLowStock = { navController.navigate(Screen.LowStock.route) },
                    onNavigateToExpiry = { navController.navigate(Screen.ExpiryManagement.route) },
                    onNavigateToHistory = { navController.navigate(Screen.TransactionHistory.createRoute()) }
                )
            }
            composable(Screen.Products.route) {
                ProductListScreen(
                    viewModel = productViewModel,
                    onNavigateToDetail = { productId ->
                        navController.navigate(Screen.ProductDetail.createRoute(productId))
                    }
                )
            }
            composable(Screen.Stock.route) {
                StockScreen(
                    onNavigateToAddStock = { navController.navigate(Screen.AddStock.createRoute()) },
                    onNavigateToRemoveStock = { navController.navigate(Screen.RemoveStock.createRoute()) },
                    onNavigateToHistory = { navController.navigate(Screen.TransactionHistory.createRoute()) }
                )
            }
            composable(Screen.More.route) {
                MoreScreen(
                    onNavigateToExpiry = { navController.navigate(Screen.ExpiryManagement.route) },
                    onNavigateToLowStock = { navController.navigate(Screen.LowStock.route) },
                    onNavigateToCategories = { navController.navigate(Screen.CategoryManagement.route) },
                    onNavigateToBackupRestore = { navController.navigate(Screen.BackupRestore.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }

            // Real Sub-screens
            composable(Screen.ProductDetail.route) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId")?.toLongOrNull() ?: 0L
                ProductDetailScreen(
                    productId = productId,
                    viewModel = productDetailViewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToVariant = { variantId ->
                        navController.navigate(Screen.VariantDetail.createRoute(variantId))
                    }
                )
            }

            composable(Screen.VariantDetail.route) { backStackEntry ->
                val variantId = backStackEntry.arguments?.getString("variantId")?.toLongOrNull() ?: 0L
                VariantDetailScreen(
                    variantId = variantId,
                    viewModel = variantViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.CategoryManagement.route) {
                CategoryManagementScreen(
                    viewModel = categoryViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Placeholder Screens for other features (implemented in later phases)
            composable(Screen.BatchDetail.route) { backStackEntry ->
                val batchId = backStackEntry.arguments?.getString("batchId")?.toLongOrNull() ?: 0L
                PlaceholderScreen(title = "Batch Detail ($batchId)", onBack = { navController.popBackStack() })
            }
            composable(Screen.AddStock.route) {
                AddStockScreen(
                    viewModel = stockViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.RemoveStock.route) {
                RemoveStockScreen(
                    viewModel = stockViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.TransactionHistory.route) {
                TransactionHistoryScreen(
                    viewModel = stockViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.ExpiryManagement.route) {
                ExpiryManagementScreen(
                    viewModel = dashboardViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.LowStock.route) {
                LowStockScreen(
                    viewModel = dashboardViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.BackupRestore.route) {
                PlaceholderScreen(title = "Backup & Restore", onBack = { navController.popBackStack() })
            }
            composable(Screen.Settings.route) {
                PlaceholderScreen(title = "Settings", onBack = { navController.popBackStack() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(title: String, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text("$title Screen Skeleton")
        }
    }
}
