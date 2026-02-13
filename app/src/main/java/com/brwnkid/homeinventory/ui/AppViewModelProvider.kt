package com.brwnkid.homeinventory.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.brwnkid.homeinventory.InventoryApplication
import com.brwnkid.homeinventory.ui.home.HomeViewModel
import com.brwnkid.homeinventory.ui.item.ItemEntryViewModel

import androidx.lifecycle.createSavedStateHandle

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            HomeViewModel(inventoryApplication().container.itemsRepository)
        }
        initializer {
            ItemEntryViewModel(
                this.createSavedStateHandle(),
                inventoryApplication().container.itemsRepository
            )
        }
    }
}

fun CreationExtras.inventoryApplication(): InventoryApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as InventoryApplication)
