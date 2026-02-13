package com.brwnkid.homeinventory.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brwnkid.homeinventory.data.InventoryRepository
import com.brwnkid.homeinventory.data.Item
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class HomeUiItem {
    data class Header(val name: String) : HomeUiItem()
    data class ItemEntry(val item: Item) : HomeUiItem()
}

data class HomeUiState(val homeItems: List<HomeUiItem> = listOf())

class HomeViewModel(private val itemsRepository: InventoryRepository) : ViewModel() {
    private val _searchQuery = kotlinx.coroutines.flow.MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val homeUiState: StateFlow<HomeUiState> =
        kotlinx.coroutines.flow.combine(
            itemsRepository.getAllItemsStream(),
            itemsRepository.getAllLocationsStream(),
            _searchQuery
        ) { items, locations, query ->
            val locationMap = locations.associateBy { it.id }
            
            val filteredItems = if (query.isBlank()) {
                items
            } else {
                items.filter {
                    it.name.contains(query, ignoreCase = true) ||
                    (it.description?.contains(query, ignoreCase = true) == true)
                }
            }

            val grouped = filteredItems.groupBy { item ->
                locationMap[item.locationId]?.name ?: "Unknown Location"
            }
            
            // Sort locations by name to ensure consistent order
            val sortedLocations = locations.sortedBy { it.name }
            val flattenedList = mutableListOf<HomeUiItem>()
            
            // Add known locations
            for (location in sortedLocations) {
                val itemsInLocation = grouped[location.name] ?: emptyList()
                if (itemsInLocation.isNotEmpty()) {
                    flattenedList.add(HomeUiItem.Header(location.name))
                    flattenedList.addAll(itemsInLocation.map { HomeUiItem.ItemEntry(it) })
                }
            }
            
            // Handle items with unknown locations (if any)
            val unknownLocationItems = grouped["Unknown Location"]
            if (!unknownLocationItems.isNullOrEmpty()) {
                 flattenedList.add(HomeUiItem.Header("Unknown Location"))
                 flattenedList.addAll(unknownLocationItems.map { HomeUiItem.ItemEntry(it) })
            }
            
            HomeUiState(flattenedList)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun incrementItemQuantity(item: Item) {
        viewModelScope.launch {
            itemsRepository.updateItem(item.copy(quantity = item.quantity + 1))
        }
    }

    fun decrementItemQuantity(item: Item) {
        viewModelScope.launch {
            if (item.quantity > 0) {
                itemsRepository.updateItem(item.copy(quantity = item.quantity - 1))
            }
        }
    }



    fun saveLocation(name: String) {
        viewModelScope.launch {
            itemsRepository.insertLocation(com.brwnkid.homeinventory.data.Location(name = name))
        }
    }
}
