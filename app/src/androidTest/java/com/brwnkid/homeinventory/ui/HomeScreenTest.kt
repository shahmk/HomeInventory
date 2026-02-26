package com.brwnkid.homeinventory.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.brwnkid.homeinventory.ui.home.HomeScreen
import com.brwnkid.homeinventory.ui.home.HomeContent
import com.brwnkid.homeinventory.ui.home.HomeUiState
import com.brwnkid.homeinventory.ui.home.HomeViewModel
import com.brwnkid.homeinventory.ui.theme.HomeInventoryTheme
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_emptyList_displaysNoItemsText() {
        // We'll use the screen directly without the ViewModel to test the UI logic
        // Or we can mock the ViewModel if we had it as an interface
        // Since it's a class, we might need Mockito or just use a dummy
        
        composeTestRule.setContent {
            HomeInventoryTheme {
                HomeContent(
                    searchQuery = "",
                    homeUiState = HomeUiState(emptyList()),
                    isCardView = false,
                    onSearchQueryChange = {},
                    onToggleViewMode = {},
                    onIncrement = {},
                    onDecrement = {},
                    onDelete = {},
                    onItemClick = {},
                    onLocationConfirm = {},
                    navigateToItemEntry = {},
                    navigateToSettings = {},
                    onImageClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("No items found. Add some!").assertIsDisplayed()
    }

    @Test
    fun homeScreen_withItems_displaysItemsAndHeaders() {
        val items = listOf(
            com.brwnkid.homeinventory.ui.home.HomeUiItem.Header("Kitchen"),
            com.brwnkid.homeinventory.ui.home.HomeUiItem.ItemEntry(
                com.brwnkid.homeinventory.data.Item(id = 1, name = "Milk", locationId = 1)
            )
        )

        composeTestRule.setContent {
            HomeInventoryTheme {
                HomeContent(
                    searchQuery = "",
                    homeUiState = HomeUiState(items),
                    isCardView = false,
                    onSearchQueryChange = {},
                    onToggleViewMode = {},
                    onIncrement = {},
                    onDecrement = {},
                    onDelete = {},
                    onItemClick = {},
                    onLocationConfirm = {},
                    navigateToItemEntry = {},
                    navigateToSettings = {},
                    onImageClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Kitchen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Milk").assertIsDisplayed()
    }
}
