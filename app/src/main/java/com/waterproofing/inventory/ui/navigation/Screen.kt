package com.waterproofing.inventory.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Products : Screen("products")
    object Stock : Screen("stock")
    object More : Screen("more")

    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: Long) = "product_detail/$productId"
    }
    object VariantDetail : Screen("variant_detail/{variantId}") {
        fun createRoute(variantId: Long) = "variant_detail/$variantId"
    }
    object BatchDetail : Screen("batch_detail/{batchId}") {
        fun createRoute(batchId: Long) = "batch_detail/$batchId"
    }
    object AddStock : Screen("add_stock?productId={productId}&variantId={variantId}") {
        fun createRoute(productId: Long? = null, variantId: Long? = null): String {
            return when {
                productId != null && variantId != null -> "add_stock?productId=$productId&variantId=$variantId"
                productId != null -> "add_stock?productId=$productId"
                variantId != null -> "add_stock?variantId=$variantId"
                else -> "add_stock"
            }
        }
    }
    object RemoveStock : Screen("remove_stock?productId={productId}&variantId={variantId}") {
        fun createRoute(productId: Long? = null, variantId: Long? = null): String {
            return when {
                productId != null && variantId != null -> "remove_stock?productId=$productId&variantId=$variantId"
                productId != null -> "remove_stock?productId=$productId"
                variantId != null -> "remove_stock?variantId=$variantId"
                else -> "remove_stock"
            }
        }
    }
    object TransactionHistory : Screen("transaction_history?productId={productId}&variantId={variantId}&batchId={batchId}") {
        fun createRoute(productId: Long? = null, variantId: Long? = null, batchId: Long? = null): String {
            val builder = StringBuilder("transaction_history")
            val params = mutableListOf<String>()
            if (productId != null) params.add("productId=$productId")
            if (variantId != null) params.add("variantId=$variantId")
            if (batchId != null) params.add("batchId=$batchId")
            if (params.isNotEmpty()) {
                builder.append("?").append(params.joinToString("&"))
            }
            return builder.toString()
        }
    }
    object ExpiryManagement : Screen("expiry_management")
    object LowStock : Screen("low_stock")
    object CategoryManagement : Screen("category_management")
    object BackupRestore : Screen("backup_restore")
    object Settings : Screen("settings")
}
