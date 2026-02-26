package com.brwnkid.homeinventory.test

import com.brwnkid.homeinventory.data.InventoryRepository
import com.brwnkid.homeinventory.data.Item
import com.brwnkid.homeinventory.data.Location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeInventoryRepository : InventoryRepository {
    private val itemsFlow = MutableStateFlow<List<Item>>(emptyList())
    private val locationsFlow = MutableStateFlow<List<Location>>(emptyList())

    override fun getAllItemsStream(): Flow<List<Item>> = itemsFlow
    override fun getItemStream(id: Int): Flow<Item?> = itemsFlow.map { it.find { item -> item.id == id } }
    override fun getItemsByLocationStream(locationId: Int): Flow<List<Item>> = itemsFlow.map { it.filter { item -> item.locationId == locationId } }
    
    override suspend fun insertItem(item: Item) {
        itemsFlow.value = itemsFlow.value + item
    }

    override suspend fun deleteItem(item: Item) {
        itemsFlow.value = itemsFlow.value.filter { it.id != item.id }
    }

    override suspend fun updateItem(item: Item) {
        itemsFlow.value = itemsFlow.value.map { if (it.id == item.id) item else it }
    }

    override suspend fun updateItems(items: List<Item>) {
        val updates = items.associateBy { it.id }
        itemsFlow.value = itemsFlow.value.map { updates[it.id] ?: it }
    }

    override fun getAllLocationsStream(): Flow<List<Location>> = locationsFlow
    override fun getLocationStream(id: Int): Flow<Location?> = locationsFlow.map { it.find { loc -> loc.id == id } }
    
    override suspend fun insertLocation(location: Location) {
        locationsFlow.value = locationsFlow.value + location
    }

    override suspend fun deleteLocation(location: Location) {
        locationsFlow.value = locationsFlow.value.filter { it.id != location.id }
    }

    override suspend fun updateLocation(location: Location) {
        locationsFlow.value = locationsFlow.value.map { if (it.id == location.id) location else it }
    }
}
