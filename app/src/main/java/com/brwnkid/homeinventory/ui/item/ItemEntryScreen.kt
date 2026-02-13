package com.brwnkid.homeinventory.ui.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brwnkid.homeinventory.data.Location
import com.brwnkid.homeinventory.ui.AppViewModelProvider
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemEntryScreen(
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    canNavigateBack: Boolean = true,
    viewModel: ItemEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()
    val locations by viewModel.locationList.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Item") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        ItemEntryBody(
            itemUiState = viewModel.itemUiState,
            locations = locations,
            onItemValueChange = viewModel::updateUiState,
            onSaveClick = {
                coroutineScope.launch {
                    viewModel.saveItem()
                    navigateBack()
                }
            },
            onAddLocation = { name ->
                coroutineScope.launch {
                    viewModel.saveLocation(name)
                }
            },
            onDelete = {
                coroutineScope.launch {
                    viewModel.deleteItem()
                    navigateBack()
                }
            },
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
        )
    }
}

private fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val fileName = "img_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)
        val outputStream = FileOutputStream(file)
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun createImageFile(context: Context): File {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir: File = context.cacheDir
    return File.createTempFile(
        "JPEG_${timeStamp}_",
        ".jpg",
        storageDir
    )
}

@Composable
fun ItemEntryBody(
    itemUiState: ItemUiState,
    locations: List<Location>,
    onItemValueChange: (ItemDetails) -> Unit,
    onSaveClick: () -> Unit,
    onAddLocation: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showQuantityZeroConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ItemInputForm(
            itemDetails = itemUiState.itemDetails,
            locations = locations,
            onValueChange = onItemValueChange,
            onAddLocation = onAddLocation,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                if (itemUiState.itemDetails.quantity == "0" && itemUiState.itemDetails.id != 0) {
                    showQuantityZeroConfirmation = true
                } else {
                    onSaveClick()
                }
            },
            enabled = itemUiState.isEntryValid,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Save")
        }
        if (itemUiState.itemDetails.id != 0) {
            OutlinedButton(
                onClick = { showDeleteConfirmation = true },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Delete")
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Item") },
            text = { Text("Are you sure you want to delete this item?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmation = false
                    onDelete()
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showQuantityZeroConfirmation) {
        AlertDialog(
            onDismissRequest = { showQuantityZeroConfirmation = false },
            title = { Text("Quantity is 0") },
            text = { Text("Quantity is set to 0. Do you want to delete this item or save it as-is?") },
            confirmButton = {
                TextButton(onClick = {
                    showQuantityZeroConfirmation = false
                    onDelete()
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showQuantityZeroConfirmation = false
                    onSaveClick()
                }) {
                    Text("Save As-Is")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemInputForm(
    itemDetails: ItemDetails,
    locations: List<Location>,
    onValueChange: (ItemDetails) -> Unit,
    onAddLocation: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var newLocationName by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("") } // For display
    
    // Update display name if locationId matches a location
    val selectedLocation = locations.find { it.id.toString() == itemDetails.locationId }
    if (selectedLocation != null) {
        locationName = selectedLocation.name
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val context = LocalContext.current
        var showImageSourceDialog by remember { mutableStateOf(false) }
        var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

        val photoPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { uri ->
                if (uri != null) {
                    val savedPath = saveImageToInternalStorage(context, uri)
                    if (savedPath != null) {
                        onValueChange(itemDetails.copy(imageUri = savedPath))
                    }
                }
            }
        )

        val cameraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture(),
            onResult = { success ->
                if (success && tempPhotoUri != null) {
                   val savedPath = saveImageToInternalStorage(context, tempPhotoUri!!)
                   if (savedPath != null) {
                       onValueChange(itemDetails.copy(imageUri = savedPath))
                   }
                }
            }
        )
        
        if (showImageSourceDialog) {
            AlertDialog(
                onDismissRequest = { showImageSourceDialog = false },
                title = { Text("Choose Image Source") },
                text = {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showImageSourceDialog = false
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .padding(16.dp),
                             verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                            Text("  Gallery", modifier = Modifier.padding(start = 16.dp))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showImageSourceDialog = false
                                    val photoFile = createImageFile(context)
                                    tempPhotoUri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        photoFile
                                    )
                                    cameraLauncher.launch(tempPhotoUri!!)
                                }
                                .padding(16.dp),
                             verticalAlignment = Alignment.CenterVertically
                        ) {
                             Icon(Icons.Default.CameraAlt, contentDescription = null)
                             Text("  Camera", modifier = Modifier.padding(start = 16.dp))
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showImageSourceDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        Box(
            modifier = Modifier
                .size(150.dp)
                .clickable {
                    showImageSourceDialog = true
                },
            contentAlignment = Alignment.Center
        ) {
            if (itemDetails.imageUri != null) {
                AsyncImage(
                    model = File(itemDetails.imageUri),
                    contentDescription = "Item Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Add Photo",
                        modifier = Modifier.size(48.dp)
                    )
                    Text(text = "Add Photo")
                }
            }
        }

        OutlinedTextField(
            value = itemDetails.name,
            onValueChange = { onValueChange(itemDetails.copy(name = it)) },
            label = { Text("Item Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = itemDetails.quantity,
            onValueChange = { onValueChange(itemDetails.copy(quantity = it)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Text("Quantity") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        // Location Dropdown
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                readOnly = true,
                value = locationName,
                onValueChange = {},
                label = { Text("Location") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                locations.forEach { location ->
                    DropdownMenuItem(
                        text = { Text(location.name) },
                        onClick = {
                            locationName = location.name
                            onValueChange(itemDetails.copy(locationId = location.id.toString()))
                            expanded = false
                        }
                    )
                }
                if (locations.isEmpty()) {
                     DropdownMenuItem(
                        text = { Text("No locations found") },
                        onClick = { expanded = false }
                    )
                }
            }
        }

        // Add New Location
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newLocationName,
                onValueChange = { newLocationName = it },
                label = { Text("New Location Name") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedButton(
                onClick = { 
                    onAddLocation(newLocationName)
                    newLocationName = ""
                },
                enabled = newLocationName.isNotBlank()
            ) {
                Text("Add Loc")
            }
        }

        OutlinedTextField(
            value = itemDetails.description,
            onValueChange = { onValueChange(itemDetails.copy(description = it)) },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            minLines = 3
        )
    }
}
