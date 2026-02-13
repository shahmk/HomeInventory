package com.brwnkid.homeinventory.ui.home

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brwnkid.homeinventory.R
import com.brwnkid.homeinventory.data.Item
import com.brwnkid.homeinventory.ui.AppViewModelProvider

import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import java.io.File
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateToItemEntry: () -> Unit,
    navigateToItemEdit: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val homeUiState by viewModel.homeUiState.collectAsState()
    var selectedImageUri by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home Inventory") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            Box {
                var showFabMenu by remember { mutableStateOf(false) }
                var showAddLocationDialog by remember { mutableStateOf(false) }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (showFabMenu) {
                        ExtendedFloatingActionButton(
                            text = { Text("Item") },
                            icon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                            onClick = {
                                showFabMenu = false
                                navigateToItemEntry()
                            },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        )

                        ExtendedFloatingActionButton(
                            text = { Text("Location") },
                            icon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                            onClick = {
                                showFabMenu = false
                                showAddLocationDialog = true
                            },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        )
                    }

                    FloatingActionButton(
                        onClick = { showFabMenu = !showFabMenu },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(
                            imageVector = if (showFabMenu) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = if (showFabMenu) "Close" else "Add"
                        )
                    }
                }

                if (showAddLocationDialog) {
                    AddLocationDialog(
                        onDismiss = { showAddLocationDialog = false },
                        onConfirm = { name ->
                            viewModel.saveLocation(name)
                            showAddLocationDialog = false
                        }
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            InventorySearchBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            HomeBody(
                homeItems = homeUiState.homeItems,
                onIncrement = viewModel::incrementItemQuantity,
                onDecrement = viewModel::decrementItemQuantity,
                onItemClick = { navigateToItemEdit(it.id) },
                onImageClick = { uri -> selectedImageUri = uri },
                modifier = Modifier.weight(1f)
            )
        }
        
        if (selectedImageUri != null) {
            ImagePreviewDialog(
                imageUri = selectedImageUri!!,
                onDismiss = { selectedImageUri = null }
            )
        }
    }
}

@Composable
fun ImagePreviewDialog(
    imageUri: String,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
             AsyncImage(
                model = File(imageUri),
                contentDescription = "Full size image",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun InventorySearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Search items...") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                }
            }
        },
        singleLine = true,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    )
}

@Composable
private fun HomeBody(
    homeItems: List<HomeUiItem>,
    onIncrement: (Item) -> Unit,
    onDecrement: (Item) -> Unit,
    onItemClick: (Item) -> Unit,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        if (homeItems.isEmpty()) {
            Text(
                text = "No items found. Add some!",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            InventoryList(
                homeItems = homeItems,
                onIncrement = onIncrement,
                onDecrement = onDecrement,
                onItemClick = onItemClick,
                onImageClick = onImageClick,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
private fun InventoryList(
    homeItems: List<HomeUiItem>,
    onIncrement: (Item) -> Unit,
    onDecrement: (Item) -> Unit,
    onItemClick: (Item) -> Unit,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()

    LazyColumn(
        modifier = modifier,
        state = lazyListState,
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(items = homeItems, key = { 
            when (it) {
                is HomeUiItem.Header -> "header_${it.name}"
                is HomeUiItem.ItemEntry -> "item_${it.item.id}"
            }
        }) { uiItem ->
            when (uiItem) {
                is HomeUiItem.Header -> {
                    LocationHeader(name = uiItem.name)
                }
                is HomeUiItem.ItemEntry -> {
                   InventoryItem(
                       item = uiItem.item,
                       onIncrement = onIncrement,
                       onDecrement = onDecrement,
                       onItemClick = onItemClick,
                       onImageClick = onImageClick,
                       modifier = Modifier.fillMaxWidth()
                   )
                }
            }
        }
    }
}

@Composable
private fun LocationHeader(name: String, modifier: Modifier = Modifier) {
    Text(
        text = name,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
        .fillMaxWidth()
        .padding(top = 16.dp, bottom = 8.dp, start = 8.dp)
    )
}

@Composable
fun AddLocationDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var locationName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Location") },
        text = {
            OutlinedTextField(
                value = locationName,
                onValueChange = { locationName = it },
                label = { Text("Location Name") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(locationName) },
                enabled = locationName.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun InventoryItem(
    item: Item,
    onIncrement: (Item) -> Unit,
    onDecrement: (Item) -> Unit,
    onItemClick: (Item) -> Unit,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    elevation: androidx.compose.ui.unit.Dp = 2.dp
) {
    Card(
        modifier = modifier
            .padding(vertical = 4.dp)
            .clickable { onItemClick(item) },
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left: Image
            if (item.imageUri != null) {
                AsyncImage(
                    model = File(item.imageUri),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clickable { onImageClick(item.imageUri) },
                    contentScale = ContentScale.Crop
                )
            } else {
                 // Placeholder if no image, to keep layout consistent or could be hidden
                 Box(
                     modifier = Modifier
                         .size(100.dp)
                         .clickable { /* No-op or open filler */ },
                     contentAlignment = Alignment.Center
                 ) {
                     Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(48.dp))
                 }
            }

            // Middle: Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.headlineSmall,
                     modifier = Modifier.fillMaxWidth()
                )
                
                if (item.description?.isNotBlank() == true) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Text(
                    text = "Quantity: ${item.quantity}",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Right: Buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                androidx.compose.material3.OutlinedButton(
                    onClick = { onIncrement(item) },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase quantity"
                    )
                }
                
                androidx.compose.material3.OutlinedButton(
                    onClick = { onDecrement(item) },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease quantity"
                    )
                }
            }
        }
    }
}
