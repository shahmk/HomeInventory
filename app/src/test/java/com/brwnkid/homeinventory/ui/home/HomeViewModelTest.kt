package com.brwnkid.homeinventory.ui.home

import com.brwnkid.homeinventory.data.InventoryRepository
import com.brwnkid.homeinventory.data.Item
import com.brwnkid.homeinventory.data.Location
import com.brwnkid.homeinventory.rules.MainDispatcherRule
import app.cash.turbine.test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.brwnkid.homeinventory.test.FakeInventoryRepository

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: HomeViewModel
    private lateinit var repository: FakeInventoryRepository
    private val syncManager = org.mockito.Mockito.mock(com.brwnkid.homeinventory.data.sync.SyncManager::class.java)

    @Before
    fun setup() {
        repository = FakeInventoryRepository()
        viewModel = HomeViewModel(repository, syncManager)
    }

    @Test
    fun homeViewModel_initialState_isCorrect() = runTest {
        viewModel.homeUiState.test {
            val initialState = awaitItem()
            assertTrue(initialState.homeItems.isEmpty())
        }
    }

    @Test
    fun homeViewModel_loadItems_groupsByLocation() = runTest {
        val location = Location(id = "1", name = "Kitchen")
        val item1 = Item(id = "1", name = "Milk", locationId = "1")
        val item2 = Item(id = "2", name = "Eggs", locationId = "1")
        
        repository.insertLocation(location)
        repository.insertItem(item1)
        repository.insertItem(item2)

        viewModel.homeUiState.test {
            // Wait for initial empty state or skip it
            var state = awaitItem()
            if (state.homeItems.isEmpty()) {
                state = awaitItem()
            }
            
            assertEquals(1, state.homeItems.filterIsInstance<HomeUiItem.Header>().size)
            assertEquals("Kitchen", (state.homeItems[0] as HomeUiItem.Header).name)
            assertEquals(2, state.homeItems.filterIsInstance<HomeUiItem.ItemEntry>().size)
        }
    }

    @Test
    fun homeViewModel_onSearchQueryChange_filtersItems() = runTest {
        val location = Location(id = "1", name = "Kitchen")
        val item1 = Item(id = "1", name = "Milk", locationId = "1")
        val item2 = Item(id = "2", name = "Eggs", locationId = "1")
        
        repository.insertLocation(location)
        repository.insertItem(item1)
        repository.insertItem(item2)

        viewModel.onSearchQueryChange("Milk")

        viewModel.homeUiState.test {
             var state = awaitItem()
            // Skip potential intermediate states if they don't match our filter yet
            while (state.homeItems.isNotEmpty() && state.homeItems.filterIsInstance<HomeUiItem.ItemEntry>().size != 1) {
                state = awaitItem()
            }
            
            val items = state.homeItems.filterIsInstance<HomeUiItem.ItemEntry>()
            assertEquals(1, items.size)
            assertEquals("Milk", items[0].item.name)
        }
    }

    @Test
    fun homeViewModel_toggleViewMode_changesState() = runTest {
        assertEquals(false, viewModel.isCardView.value)
        viewModel.toggleViewMode()
        assertEquals(true, viewModel.isCardView.value)
    }
}
