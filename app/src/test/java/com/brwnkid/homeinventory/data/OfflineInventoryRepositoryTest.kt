package com.brwnkid.homeinventory.data

import app.cash.turbine.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FakeInventoryDao : InventoryDao {
    private val itemsFlow = MutableStateFlow<List<Item>>(emptyList())
    private val locationsFlow = MutableStateFlow<List<Location>>(emptyList())

    override fun getAllItems(): Flow<List<Item>> = itemsFlow
    override fun getItem(id: Int): Flow<Item?> = itemsFlow.map { it.find { item -> item.id == id } }
    override fun getItemsByLocation(locationId: Int): Flow<List<Item>> = itemsFlow.map { it.filter { item -> item.locationId == locationId } }
    override suspend fun getAllItemsSync(): List<Item> = itemsFlow.value

    override suspend fun insertItem(item: Item) {
        itemsFlow.value = itemsFlow.value + item
    }

    override suspend fun updateItem(item: Item) {
        itemsFlow.value = itemsFlow.value.map { if (it.id == item.id) item else it }
    }

    override suspend fun updateItems(items: List<Item>) {
        val updates = items.associateBy { it.id }
        itemsFlow.value = itemsFlow.value.map { updates[it.id] ?: it }
    }

    override suspend fun deleteItem(item: Item) {
        itemsFlow.value = itemsFlow.value.filter { it.id != item.id }
    }

    override fun getAllLocations(): Flow<List<Location>> = locationsFlow
    override fun getLocation(id: Int): Flow<Location?> = locationsFlow.map { it.find { loc -> loc.id == id } }
    override suspend fun getAllLocationsSync(): List<Location> = locationsFlow.value

    override suspend fun insertLocation(location: Location) {
        locationsFlow.value = locationsFlow.value + location
    }

    override suspend fun updateLocation(location: Location) {
        locationsFlow.value = locationsFlow.value.map { if (it.id == location.id) location else it }
    }

    override suspend fun deleteLocation(location: Location) {
        locationsFlow.value = locationsFlow.value.filter { it.id != location.id }
    }
}

class OfflineInventoryRepositoryTest {
    private lateinit var repository: OfflineInventoryRepository
    private lateinit var dao: FakeInventoryDao

    @Before
    fun setup() {
        dao = FakeInventoryDao()
        repository = OfflineInventoryRepository(dao)
    }

    @Test
    fun repositoryInsertAndGetItem() = runTest {
        val item = Item(id = 1, name = "Milk", quantity = 2, locationId = 1)
        repository.insertItem(item)
        
        repository.getItemStream(1).test {
            assertEquals(item, awaitItem())
        }
    }

    @Test
    fun repositoryDeleteAndGetItem() = runTest {
        val item = Item(id = 1, name = "Milk", quantity = 2, locationId = 1)
        repository.insertItem(item)
        repository.deleteItem(item)
        
        repository.getItemStream(1).test {
            assertEquals(null, awaitItem())
        }
    }
}
