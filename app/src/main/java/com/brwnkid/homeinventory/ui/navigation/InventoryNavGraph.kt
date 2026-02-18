package com.brwnkid.homeinventory.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.brwnkid.homeinventory.ui.home.HomeScreen
import com.brwnkid.homeinventory.ui.item.ItemEntryScreen
import com.brwnkid.homeinventory.ui.settings.SettingsScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument

enum class InventoryScreen {
    Home,
    ItemEntry,
    ItemEdit,
    Settings
}

private const val TRANSITION_DURATION = 350

@Composable
fun InventoryNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = InventoryScreen.Home.name,
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(TRANSITION_DURATION)
            ) + fadeIn(animationSpec = tween(TRANSITION_DURATION))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(TRANSITION_DURATION)
            ) + fadeOut(animationSpec = tween(TRANSITION_DURATION))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(TRANSITION_DURATION)
            ) + fadeIn(animationSpec = tween(TRANSITION_DURATION))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(TRANSITION_DURATION)
            ) + fadeOut(animationSpec = tween(TRANSITION_DURATION))
        }
    ) {
        composable(route = InventoryScreen.Home.name) {
            HomeScreen(
                navigateToItemEntry = { navController.navigate(InventoryScreen.ItemEntry.name) },
                navigateToItemEdit = { itemId ->
                    navController.navigate("${InventoryScreen.ItemEdit.name}/$itemId")
                },
                navigateToSettings = { navController.navigate(InventoryScreen.Settings.name) }
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
        composable(route = InventoryScreen.Settings.name) {
            SettingsScreen(
                navigateBack = { navController.popBackStack() }
            )
        }
    }
}
