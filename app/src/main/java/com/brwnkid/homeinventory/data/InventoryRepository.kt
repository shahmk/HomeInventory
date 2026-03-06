package com.brwnkid.homeinventory.data

import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    fun getAllItemsStream(): Flow<List<Item>>
    fun getItemStream(id: String): Flow<Item?>
    fun getItemsByLocationStream(locationId: String): Flow<List<Item>>
    suspend fun insertItem(item: Item)
    suspend fun deleteItem(item: Item)
    suspend fun updateItem(item: Item)
    suspend fun updateItems(items: List<Item>)

    fun getAllLocationsStream(): Flow<List<Location>>
    fun getLocationStream(id: String): Flow<Location?>
    suspend fun insertLocation(location: Location)
    suspend fun deleteLocation(location: Location)
    suspend fun updateLocation(location: Location)

    // Sync methods
    suspend fun getAllItemsSync(): List<Item>
    suspend fun getAllLocationsSync(): List<Location>
    suspend fun upsertItem(item: Item)
    suspend fun upsertLocation(location: Location)
}

class OfflineInventoryRepository(private val inventoryDao: InventoryDao) : InventoryRepository {
    override fun getAllItemsStream(): Flow<List<Item>> = inventoryDao.getAllItems()
    override fun getItemStream(id: String): Flow<Item?> = inventoryDao.getItem(id)
    override fun getItemsByLocationStream(locationId: String): Flow<List<Item>> = inventoryDao.getItemsByLocation(locationId)

    override fun getAllLocationsStream(): Flow<List<Location>> = inventoryDao.getAllLocations()
    override fun getLocationStream(id: String): Flow<Location?> = inventoryDao.getLocation(id)
    
    override suspend fun insertItem(item: Item) {
        inventoryDao.insertItem(item.copy(lastModified = System.currentTimeMillis()))
    }

    override suspend fun updateItem(item: Item) {
        inventoryDao.updateItem(item.copy(lastModified = System.currentTimeMillis()))
    }

    override suspend fun deleteItem(item: Item) {
        inventoryDao.updateItem(item.copy(isDeleted = true, lastModified = System.currentTimeMillis()))
    }

    override suspend fun updateItems(items: List<Item>) {
        inventoryDao.updateItems(items.map { it.copy(lastModified = System.currentTimeMillis()) })
    }

    override suspend fun insertLocation(location: Location) {
        inventoryDao.insertLocation(location.copy(lastModified = System.currentTimeMillis()))
    }

    override suspend fun updateLocation(location: Location) {
        inventoryDao.updateLocation(location.copy(lastModified = System.currentTimeMillis()))
    }

    override suspend fun deleteLocation(location: Location) {
        inventoryDao.updateLocation(location.copy(isDeleted = true, lastModified = System.currentTimeMillis()))
    }

    override suspend fun getAllItemsSync(): List<Item> = inventoryDao.getAllItemsSync()
    override suspend fun getAllLocationsSync(): List<Location> = inventoryDao.getAllLocationsSync()
    
    override suspend fun upsertItem(item: Item) = inventoryDao.upsertItem(item)
    override suspend fun upsertLocation(location: Location) = inventoryDao.upsertLocation(location)
}
