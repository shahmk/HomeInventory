package com.brwnkid.homeinventory.ui.item

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brwnkid.homeinventory.data.InventoryRepository
import com.brwnkid.homeinventory.data.Item
import com.brwnkid.homeinventory.data.Location
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import androidx.lifecycle.SavedStateHandle

class ItemEntryViewModel(
    savedStateHandle: SavedStateHandle,
    private val itemsRepository: InventoryRepository
) : ViewModel() {
    var itemUiState by mutableStateOf(ItemUiState())
        private set

    private val itemId: Int = savedStateHandle["itemId"] ?: 0

    val locationList: StateFlow<List<Location>> =
        itemsRepository.getAllLocationsStream()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    init {
        if (itemId > 0) {
            viewModelScope.launch {
                itemsRepository.getItemStream(itemId)
                    .filterNotNull()
                    .first()
                    .let { item ->
                        itemUiState = ItemUiState(itemDetails = item.toItemDetails(), isEntryValid = true)
                    }
            }
        }
    }

    fun updateUiState(itemDetails: ItemDetails) {
        itemUiState =
            ItemUiState(itemDetails = itemDetails, isEntryValid = validateInput(itemDetails))
    }

    suspend fun saveItem() {
        if (validateInput()) {
            if (itemId > 0) {
                itemsRepository.updateItem(itemUiState.itemDetails.toItem().copy(id = itemId))
            } else {
                itemsRepository.insertItem(itemUiState.itemDetails.toItem())
            }
        }
    }

    suspend fun deleteItem() {
        if (itemId > 0) {
            itemsRepository.deleteItem(itemUiState.itemDetails.toItem().copy(id = itemId))
        }
    }

    suspend fun saveLocation(name: String) {
        if (name.isNotBlank()) {
            itemsRepository.insertLocation(Location(name = name))
        }
    }

    private fun validateInput(uiState: ItemDetails = itemUiState.itemDetails): Boolean {
        return uiState.name.isNotBlank() && uiState.locationId.isNotBlank()
    }
}

data class ItemUiState(
    val itemDetails: ItemDetails = ItemDetails(),
    val isEntryValid: Boolean = false
)

data class ItemDetails(
    val id: Int = 0,
    val name: String = "",
    val quantity: String = "1",
    val locationId: String = "",
    val description: String = "",
    val imageUri: String? = null
)

fun ItemDetails.toItem(): Item = Item(
    id = id,
    name = name,
    quantity = quantity.toIntOrNull() ?: 0,
    locationId = locationId.toIntOrNull() ?: 0,
    description = description,
    imageUri = imageUri
)

fun Item.toItemDetails(): ItemDetails = ItemDetails(
    id = id,
    name = name,
    quantity = quantity.toString(),
    locationId = locationId.toString(),
    description = description ?: "",
    imageUri = imageUri
)
