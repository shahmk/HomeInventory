package com.brwnkid.homeinventory.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    // Items
    @Query("SELECT * FROM items WHERE isDeleted = 0 ORDER BY sortOrder ASC, name ASC")
    fun getAllItems(): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE id = :id AND isDeleted = 0")
    fun getItem(id: String): Flow<Item?>

    @Query("SELECT * FROM items WHERE locationId = :locationId AND isDeleted = 0 ORDER BY sortOrder ASC, name ASC")
    fun getItemsByLocation(locationId: String): Flow<List<Item>>

    @Query("SELECT * FROM items")
    suspend fun getAllItemsSync(): List<Item>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: Item)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItem(item: Item)

    @Update
    suspend fun updateItem(item: Item)

    @Update
    suspend fun updateItems(items: List<Item>)

    @Delete
    suspend fun deleteItem(item: Item)

    // Locations
    @Query("SELECT * FROM locations WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllLocations(): Flow<List<Location>>
    
    @Query("SELECT * FROM locations WHERE id = :id AND isDeleted = 0")
    fun getLocation(id: String): Flow<Location?>

    @Query("SELECT * FROM locations")
    suspend fun getAllLocationsSync(): List<Location>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: Location)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLocation(location: Location)

    @Update
    suspend fun updateLocation(location: Location)

    @Delete
    suspend fun deleteLocation(location: Location)
}
