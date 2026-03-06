package com.brwnkid.homeinventory.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

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
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val quantity: Int = 1,
    val locationId: String,
    val barcode: String? = null,
    val imageUris: List<String> = emptyList(),
    val sortOrder: Int = 0,
    val lastModified: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)
