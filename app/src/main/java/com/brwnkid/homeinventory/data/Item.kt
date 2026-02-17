package com.brwnkid.homeinventory.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "items",
    foreignKeys = [
        ForeignKey(
            entity = Location::class,
            parentColumns = ["id"],
            childColumns = ["locationId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String? = null,
    val quantity: Int = 1,
    val locationId: Int,
    val imageUris: List<String> = emptyList(),
    val sortOrder: Int = 0
)
