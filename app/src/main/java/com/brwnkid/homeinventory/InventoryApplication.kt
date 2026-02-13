package com.brwnkid.homeinventory

import android.app.Application
import android.content.Context
import com.brwnkid.homeinventory.data.InventoryDatabase
import com.brwnkid.homeinventory.data.InventoryRepository
import com.brwnkid.homeinventory.data.OfflineInventoryRepository

class InventoryApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}

interface AppContainer {
    val itemsRepository: InventoryRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val itemsRepository: InventoryRepository by lazy {
        OfflineInventoryRepository(InventoryDatabase.getDatabase(context).inventoryDao())
    }
}
