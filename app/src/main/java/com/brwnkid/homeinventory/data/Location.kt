package com.brwnkid.homeinventory.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "locations")
data class Location(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val lastModified: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)
