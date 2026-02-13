package com.brwnkid.homeinventory.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.brwnkid.homeinventory.ui.home.HomeScreen
import com.brwnkid.homeinventory.ui.item.ItemEntryScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument

enum class InventoryScreen {
    Home,
    ItemEntry,
    ItemEdit
}

@Composable
fun InventoryNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = InventoryScreen.Home.name,
        modifier = modifier
    ) {
        composable(route = InventoryScreen.Home.name) {
            HomeScreen(
                navigateToItemEntry = { navController.navigate(InventoryScreen.ItemEntry.name) },
                navigateToItemEdit = { itemId ->
                    navController.navigate("${InventoryScreen.ItemEdit.name}/$itemId")
                }
            )
        }
        composable(route = InventoryScreen.ItemEntry.name) {
            ItemEntryScreen(
                navigateBack = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() }
            )
        }
        composable(
            route = "${InventoryScreen.ItemEdit.name}/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.IntType })
        ) {
            ItemEntryScreen(
                navigateBack = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() }
            )
        }
    }
}
