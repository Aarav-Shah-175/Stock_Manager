package com.waterproofing.inventory.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ListAlt
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.waterproofing.inventory.ui.dashboard.DashboardScreen
import com.waterproofing.inventory.ui.more.MoreScreen
import com.waterproofing.inventory.ui.products.ProductListScreen
import com.waterproofing.inventory.ui.stock.StockScreen

sealed class NavigationItem(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : NavigationItem(Screen.Dashboard.route, "Dashboard", Icons.Default.Dashboard)
    object Products : NavigationItem(Screen.Products.route, "Products", Icons.Default.Inventory)
    object Stock : NavigationItem(Screen.Stock.route, "Stock", Icons.Default.ListAlt)
    object More : NavigationItem(Screen.More.route, "More", Icons.Default.MoreHoriz)
}

@Composable
fun MainAppNavigation() {
    val navController = rememberNavController()
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
                DashboardScreen()
            }
            composable(Screen.Products.route) {
                ProductListScreen(
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

            // Skeletons for sub-routes
            composable(Screen.ProductDetail.route) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId")?.toLongOrNull() ?: 0L
                PlaceholderScreen(title = "Product Detail ($productId)", onBack = { navController.popBackStack() })
            }
            composable(Screen.VariantDetail.route) { backStackEntry ->
                val variantId = backStackEntry.arguments?.getString("variantId")?.toLongOrNull() ?: 0L
                PlaceholderScreen(title = "Variant Detail ($variantId)", onBack = { navController.popBackStack() })
            }
            composable(Screen.BatchDetail.route) { backStackEntry ->
                val batchId = backStackEntry.arguments?.getString("batchId")?.toLongOrNull() ?: 0L
                PlaceholderScreen(title = "Batch Detail ($batchId)", onBack = { navController.popBackStack() })
            }
            composable(Screen.AddStock.route) {
                PlaceholderScreen(title = "Add Stock", onBack = { navController.popBackStack() })
            }
            composable(Screen.RemoveStock.route) {
                PlaceholderScreen(title = "Remove Stock", onBack = { navController.popBackStack() })
            }
            composable(Screen.TransactionHistory.route) {
                PlaceholderScreen(title = "Transaction History", onBack = { navController.popBackStack() })
            }
            composable(Screen.ExpiryManagement.route) {
                PlaceholderScreen(title = "Expiry Management", onBack = { navController.popBackStack() })
            }
            composable(Screen.LowStock.route) {
                PlaceholderScreen(title = "Low Stock Alerts", onBack = { navController.popBackStack() })
            }
            composable(Screen.CategoryManagement.route) {
                PlaceholderScreen(title = "Category Management", onBack = { navController.popBackStack() })
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
