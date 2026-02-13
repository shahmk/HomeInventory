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
    @Query("SELECT * FROM items ORDER BY sortOrder ASC, name ASC")
    fun getAllItems(): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE id = :id")
    fun getItem(id: Int): Flow<Item?>

    @Query("SELECT * FROM items WHERE locationId = :locationId ORDER BY sortOrder ASC, name ASC")
    fun getItemsByLocation(locationId: Int): Flow<List<Item>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertItem(item: Item)

    @Update
    suspend fun updateItem(item: Item)

    @Update
    suspend fun updateItems(items: List<Item>)

    @Delete
    suspend fun deleteItem(item: Item)

    // Locations
    @Query("SELECT * FROM locations ORDER BY name ASC")
    fun getAllLocations(): Flow<List<Location>>
    
    @Query("SELECT * FROM locations WHERE id = :id")
    fun getLocation(id: Int): Flow<Location?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLocation(location: Location)

    @Update
    suspend fun updateLocation(location: Location)

    @Delete
    suspend fun deleteLocation(location: Location)
}
