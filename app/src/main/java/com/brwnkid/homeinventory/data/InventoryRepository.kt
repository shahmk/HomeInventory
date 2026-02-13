package com.brwnkid.homeinventory.data

import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    fun getAllItemsStream(): Flow<List<Item>>
    fun getItemStream(id: Int): Flow<Item?>
    fun getItemsByLocationStream(locationId: Int): Flow<List<Item>>
    suspend fun insertItem(item: Item)
    suspend fun deleteItem(item: Item)
    suspend fun updateItem(item: Item)
    suspend fun updateItems(items: List<Item>)

    fun getAllLocationsStream(): Flow<List<Location>>
    fun getLocationStream(id: Int): Flow<Location?>
    suspend fun insertLocation(location: Location)
    suspend fun deleteLocation(location: Location)
    suspend fun updateLocation(location: Location)
}

class OfflineInventoryRepository(private val inventoryDao: InventoryDao) : InventoryRepository {
    override fun getAllItemsStream(): Flow<List<Item>> = inventoryDao.getAllItems()
    override fun getItemStream(id: Int): Flow<Item?> = inventoryDao.getItem(id)
    override fun getItemsByLocationStream(locationId: Int): Flow<List<Item>> = inventoryDao.getItemsByLocation(locationId)
    override suspend fun insertItem(item: Item) = inventoryDao.insertItem(item)
    override suspend fun deleteItem(item: Item) = inventoryDao.deleteItem(item)
    override suspend fun updateItem(item: Item) = inventoryDao.updateItem(item)
    override suspend fun updateItems(items: List<Item>) = inventoryDao.updateItems(items)

    override fun getAllLocationsStream(): Flow<List<Location>> = inventoryDao.getAllLocations()
    override fun getLocationStream(id: Int): Flow<Location?> = inventoryDao.getLocation(id)
    override suspend fun insertLocation(location: Location) = inventoryDao.insertLocation(location)
    override suspend fun deleteLocation(location: Location) = inventoryDao.deleteLocation(location)
    override suspend fun updateLocation(location: Location) = inventoryDao.updateLocation(location)
}
