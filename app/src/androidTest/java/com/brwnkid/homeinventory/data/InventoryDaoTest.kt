package com.brwnkid.homeinventory.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class InventoryDaoTest {
    private lateinit var inventoryDao: InventoryDao
    private lateinit var db: InventoryDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, InventoryDatabase::class.java
        ).build()
        inventoryDao = db.inventoryDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun daoInsertAndGetItem() = runTest {
        val location = Location(id = 1, name = "Kitchen")
        inventoryDao.insertLocation(location)
        
        val item = Item(id = 1, name = "Milk", quantity = 2, locationId = 1)
        inventoryDao.insertItem(item)
        
        inventoryDao.getItem(1).test {
            val result = awaitItem()
            assertEquals(item, result)
        }
    }

    @Test
    @Throws(Exception::class)
    fun daoGetAllItems() = runTest {
        val location = Location(id = 1, name = "Kitchen")
        inventoryDao.insertLocation(location)
        
        val item1 = Item(id = 1, name = "Milk", quantity = 2, locationId = 1, sortOrder = 1)
        val item2 = Item(id = 2, name = "Bread", quantity = 1, locationId = 1, sortOrder = 0)
        
        inventoryDao.insertItem(item1)
        inventoryDao.insertItem(item2)
        
        inventoryDao.getAllItems().test {
            val list = awaitItem()
            assertEquals(2, list.size)
            assertEquals("Bread", list[0].name) // Bread has lower sortOrder
            assertEquals("Milk", list[1].name)
        }
    }

    @Test
    @Throws(Exception::class)
    fun daoUpdateItem() = runTest {
        val location = Location(id = 1, name = "Kitchen")
        inventoryDao.insertLocation(location)
        
        val item = Item(id = 1, name = "Milk", quantity = 2, locationId = 1)
        inventoryDao.insertItem(item)
        
        val updatedItem = item.copy(quantity = 5)
        inventoryDao.updateItem(updatedItem)
        
        inventoryDao.getItem(1).test {
            val result = awaitItem()
            assertEquals(5, result?.quantity)
        }
    }

    @Test
    @Throws(Exception::class)
    fun daoDeleteItem() = runTest {
        val location = Location(id = 1, name = "Kitchen")
        inventoryDao.insertLocation(location)
        
        val item = Item(id = 1, name = "Milk", quantity = 2, locationId = 1)
        inventoryDao.insertItem(item)
        inventoryDao.deleteItem(item)
        
        inventoryDao.getAllItems().test {
            val list = awaitItem()
            assertTrue(list.isEmpty())
        }
    }

    @Test
    @Throws(Exception::class)
    fun daoGetItemsByLocation() = runTest {
        val loc1 = Location(id = 1, name = "Kitchen")
        val loc2 = Location(id = 2, name = "Garage")
        inventoryDao.insertLocation(loc1)
        inventoryDao.insertLocation(loc2)
        
        val item1 = Item(id = 1, name = "Milk", locationId = 1)
        val item2 = Item(id = 2, name = "Tools", locationId = 2)
        
        inventoryDao.insertItem(item1)
        inventoryDao.insertItem(item2)
        
        inventoryDao.getItemsByLocation(1).test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("Milk", list[0].name)
        }
    }
}
