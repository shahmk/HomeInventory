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

    override fun getAllItemsStream(): Flow<List<Item>> = itemsFlow.map { it.filter { item -> !item.isDeleted } }
    override fun getItemStream(id: String): Flow<Item?> = itemsFlow.map { it.find { item -> item.id == id && !item.isDeleted } }
    override fun getItemsByLocationStream(locationId: String): Flow<List<Item>> = itemsFlow.map { it.filter { item -> item.locationId == locationId && !item.isDeleted } }
    
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

    override fun getAllLocationsStream(): Flow<List<Location>> = locationsFlow.map { it.filter { loc -> !loc.isDeleted } }
    override fun getLocationStream(id: String): Flow<Location?> = locationsFlow.map { it.find { loc -> loc.id == id && !loc.isDeleted } }
    
    override suspend fun insertLocation(location: Location) {
        locationsFlow.value = locationsFlow.value + location
    }

    override suspend fun deleteLocation(location: Location) {
        locationsFlow.value = locationsFlow.value.filter { it.id != location.id }
    }

    override suspend fun updateLocation(location: Location) {
        locationsFlow.value = locationsFlow.value.map { if (it.id == location.id) location else it }
    }

    override suspend fun getAllItemsSync(): List<Item> = itemsFlow.value
    override suspend fun getAllLocationsSync(): List<Location> = locationsFlow.value

    override suspend fun upsertItem(item: Item) {
        if (itemsFlow.value.any { it.id == item.id }) {
            updateItem(item)
        } else {
            insertItem(item)
        }
    }

    override suspend fun upsertLocation(location: Location) {
        if (locationsFlow.value.any { it.id == location.id }) {
            updateLocation(location)
        } else {
            insertLocation(location)
        }
    }
}
