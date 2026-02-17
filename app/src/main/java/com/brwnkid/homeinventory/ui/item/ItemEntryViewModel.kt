package com.brwnkid.homeinventory.ui.item

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brwnkid.homeinventory.data.InventoryRepository
import com.brwnkid.homeinventory.data.Item
import com.brwnkid.homeinventory.data.Location
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import androidx.lifecycle.SavedStateHandle

class ItemEntryViewModel(
    savedStateHandle: SavedStateHandle,
    private val itemsRepository: InventoryRepository
) : ViewModel() {
    var itemUiState by mutableStateOf(ItemUiState())
        private set

    private val itemId: Int = savedStateHandle["itemId"] ?: 0

    val locationList: StateFlow<List<Location>> =
        itemsRepository.getAllLocationsStream()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    init {
        if (itemId > 0) {
            viewModelScope.launch {
                itemsRepository.getItemStream(itemId)
                    .filterNotNull()
                    .first()
                    .let { item ->
                        itemUiState = ItemUiState(itemDetails = item.toItemDetails(), isEntryValid = true)
                    }
            }
        }
    }

    fun updateUiState(
        itemDetails: ItemDetails, 
        isScanning: Boolean = false, 
        isNameSelectionOpen: Boolean = false,
        candidateNames: List<String> = emptyList()
    ) {
        itemUiState =
            ItemUiState(
                itemDetails = itemDetails, 
                isEntryValid = validateInput(itemDetails), 
                isScanning = isScanning,
                isNameSelectionOpen = isNameSelectionOpen,
                candidateNames = candidateNames
            )
    }

    suspend fun saveItem() {
        if (validateInput()) {
            if (itemId > 0) {
                itemsRepository.updateItem(itemUiState.itemDetails.toItem().copy(id = itemId))
            } else {
                itemsRepository.insertItem(itemUiState.itemDetails.toItem())
            }
        }
    }

    suspend fun deleteItem() {
        if (itemId > 0) {
            itemsRepository.deleteItem(itemUiState.itemDetails.toItem().copy(id = itemId))
        }
    }

    suspend fun saveLocation(name: String) {
        if (name.isNotBlank()) {
            itemsRepository.insertLocation(Location(name = name))
        }
    }

    private fun validateInput(uiState: ItemDetails = itemUiState.itemDetails): Boolean {
        return uiState.name.isNotBlank() && uiState.locationId.isNotBlank()
    }

    fun scanImage(context: android.content.Context, imageUri: String) {
        if (itemUiState.isScanning) return
        updateUiState(itemUiState.itemDetails, isScanning = true)
        android.util.Log.d("ItemEntryViewModel", "Starting scan for URI: $imageUri")

        try {
            val uri = android.net.Uri.fromFile(java.io.File(imageUri))
            val image = com.google.mlkit.vision.common.InputImage.fromFilePath(context, uri)
            val recognizer = com.google.mlkit.vision.text.TextRecognition.getClient(com.google.mlkit.vision.text.latin.TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    android.util.Log.d("ItemEntryViewModel", "Scan success. Text blocks: ${visionText.textBlocks.size}")
                    processRecognizedText(visionText)
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("ItemEntryViewModel", "Scan failed", e)
                    updateUiState(itemUiState.itemDetails, isScanning = false)
                }
        } catch (e: Exception) {
            android.util.Log.e("ItemEntryViewModel", "Error preparing image", e)
            updateUiState(itemUiState.itemDetails, isScanning = false)
        }
    }

    fun onNameSelected(name: String) {
        val currentDescription = itemUiState.itemDetails.description
        
        // Filter out the selected name from the description text blocks
        // We need to reconstruct the logic a bit or store the raw text blocks?
        // Simpler approach: The 'name' passed here is one of the candidates. 
        // We can just append all *other* candidates to the description?
        // Or better: In processRecognizedText, we just stored strings. 
        // If we want to move the *rest* to description, we should probably 
        // just append all candidates *except* the selected one to the existing description.
        
        val newDescriptionBuilder = StringBuilder()
        if (currentDescription.isNotBlank()) {
            newDescriptionBuilder.append(currentDescription).append("\n")
        }
        
        itemUiState.candidateNames.forEach { candidate ->
            if (candidate != name) {
                newDescriptionBuilder.append(candidate).append("\n")
            }
        }
        
        val finalDescription = newDescriptionBuilder.toString().trim()
        
        updateUiState(
            itemUiState.itemDetails.copy(name = name, description = finalDescription),
            isNameSelectionOpen = false
        )
    }

    fun onNameSelectionDismissed() {
        updateUiState(itemUiState.itemDetails, isNameSelectionOpen = false)
    }

    private fun processRecognizedText(visionText: com.google.mlkit.vision.text.Text) {
        if (visionText.textBlocks.isEmpty()) {
            android.util.Log.d("ItemEntryViewModel", "No text found in image")
            updateUiState(itemUiState.itemDetails, isScanning = false)
            return
        }

        // Collect all text blocks as candidates
        val candidates = visionText.textBlocks.map { it.text }
        android.util.Log.d("ItemEntryViewModel", "Candidates found: ${candidates.size}")

        // Open selection dialog with candidates
        updateUiState(
            itemUiState.itemDetails, 
            isScanning = false, 
            candidateNames = candidates, 
            isNameSelectionOpen = true
        )
    }
}

data class ItemUiState(
    val itemDetails: ItemDetails = ItemDetails(),
    val isEntryValid: Boolean = false,
    val isScanning: Boolean = false,
    val isNameSelectionOpen: Boolean = false,
    val candidateNames: List<String> = emptyList()
)

data class ItemDetails(
    val id: Int = 0,
    val name: String = "",
    val quantity: String = "1",
    val locationId: String = "",
    val description: String = "",
    val imageUris: List<String> = emptyList()
)

fun ItemDetails.toItem(): Item = Item(
    id = id,
    name = name,
    quantity = quantity.toIntOrNull() ?: 0,
    locationId = locationId.toIntOrNull() ?: 0,
    description = description,
    imageUris = imageUris
)

fun Item.toItemDetails(): ItemDetails = ItemDetails(
    id = id,
    name = name,
    quantity = quantity.toString(),
    locationId = locationId.toString(),
    description = description ?: "",
    imageUris = imageUris
)
