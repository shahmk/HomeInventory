package com.brwnkid.homeinventory.ui.item

import androidx.lifecycle.SavedStateHandle
import com.brwnkid.homeinventory.data.Item
import com.brwnkid.homeinventory.rules.MainDispatcherRule
import com.brwnkid.homeinventory.test.FakeInventoryRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlinx.coroutines.flow.first

class ItemEntryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ItemEntryViewModel
    private lateinit var repository: FakeInventoryRepository
    private val syncManager = org.mockito.Mockito.mock(com.brwnkid.homeinventory.data.sync.SyncManager::class.java)

    @Before
    fun setup() {
        repository = FakeInventoryRepository()
    }

    @Test
    fun itemEntryViewModel_initialState_isCorrect() = runTest {
        viewModel = ItemEntryViewModel(SavedStateHandle(), repository, syncManager)
        val state = viewModel.itemUiState
        assertEquals("", state.itemDetails.name)
        assertFalse(state.isEntryValid)
    }

    @Test
    fun itemEntryViewModel_updateUiState_validInput_setsIsEntryValid() = runTest {
        viewModel = ItemEntryViewModel(SavedStateHandle(), repository, syncManager)
        viewModel.updateUiState(ItemDetails(name = "Milk", locationId = "1"))
        assertTrue(viewModel.itemUiState.isEntryValid)
    }

    @Test
    fun itemEntryViewModel_updateUiState_invalidInput_setsIsEntryValidFalse() = runTest {
        viewModel = ItemEntryViewModel(SavedStateHandle(), repository, syncManager)
        viewModel.updateUiState(ItemDetails(name = "", locationId = "1"))
        assertFalse(viewModel.itemUiState.isEntryValid)
    }

    @Test
    fun itemEntryViewModel_saveItem_insertsNewItem() = runTest {
        viewModel = ItemEntryViewModel(SavedStateHandle(), repository, syncManager)
        viewModel.updateUiState(ItemDetails(name = "Milk", locationId = "1", quantity = "2"))
        viewModel.saveItem()
        
        val items = repository.getAllItemsStream().first()
        assertEquals(1, items.size)
        assertEquals("Milk", items[0].name)
    }

    @Test
    fun itemEntryViewModel_loadItem_forEditing() = runTest {
        val item = Item(id = "1", name = "Milk", locationId = "1", quantity = 5)
        repository.insertItem(item)
        
        viewModel = ItemEntryViewModel(SavedStateHandle(mapOf("itemId" to "1")), repository, syncManager)
        
        // Wait for coroutine in init to complete
        // Since we use UnconfinedTestDispatcher in MainDispatcherRule, it should be immediate
        
        val state = viewModel.itemUiState
        assertEquals("Milk", state.itemDetails.name)
        assertEquals("5", state.itemDetails.quantity)
        assertTrue(state.isEntryValid)
    }

    @Test
    fun itemEntryViewModel_onNameSelected_updatesNameAndDescription() = runTest {
        viewModel = ItemEntryViewModel(SavedStateHandle(), repository, syncManager)
        viewModel.updateUiState(
            ItemDetails(description = "Existing"),
            candidateNames = listOf("Milk", "Fresh")
        )
        
        viewModel.onNameSelected("Milk")
        
        val state = viewModel.itemUiState
        assertEquals("Milk", state.itemDetails.name)
        // Description should contain "Existing" and "Fresh" (the other candidate)
        assertTrue(state.itemDetails.description.contains("Existing"))
        assertTrue(state.itemDetails.description.contains("Fresh"))
        assertFalse(state.itemDetails.description.contains("Milk"))
    }
}
