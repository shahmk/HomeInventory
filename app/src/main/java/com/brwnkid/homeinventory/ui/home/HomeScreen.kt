package com.brwnkid.homeinventory.ui.home

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.GridView
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
    navigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val homeUiState by viewModel.homeUiState.collectAsState()
    val isCardView by viewModel.isCardView.collectAsState()
    var selectedImageUri by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home Inventory") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = viewModel::toggleViewMode) {
                        Icon(
                            imageVector = if (isCardView) Icons.AutoMirrored.Filled.List else Icons.Filled.GridView,
                            contentDescription = if (isCardView) "List View" else "Card View"
                        )
                    }
                    IconButton(onClick = navigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
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
                    // Sub-FAB: Item — slides up with a slight delay
                    AnimatedVisibility(
                        visible = showFabMenu,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(durationMillis = 200, delayMillis = 60)
                        ) + fadeIn(animationSpec = tween(durationMillis = 200, delayMillis = 60)),
                        exit = slideOutVertically(
                            targetOffsetY = { it },
                            animationSpec = tween(durationMillis = 150)
                        ) + fadeOut(animationSpec = tween(durationMillis = 150))
                    ) {
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
                    }

                    // Sub-FAB: Location — slides up immediately
                    AnimatedVisibility(
                        visible = showFabMenu,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(durationMillis = 200, delayMillis = 0)
                        ) + fadeIn(animationSpec = tween(durationMillis = 200, delayMillis = 0)),
                        exit = slideOutVertically(
                            targetOffsetY = { it },
                            animationSpec = tween(durationMillis = 150, delayMillis = 40)
                        ) + fadeOut(animationSpec = tween(durationMillis = 150, delayMillis = 40))
                    ) {
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

                    // Main FAB with animated icon swap
                    FloatingActionButton(
                        onClick = { showFabMenu = !showFabMenu },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        AnimatedContent(
                            targetState = showFabMenu,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(200)) togetherWith
                                    fadeOut(animationSpec = tween(200))
                            },
                            label = "FAB icon"
                        ) { isOpen ->
                            Icon(
                                imageVector = if (isOpen) Icons.Default.Close else Icons.Default.Add,
                                contentDescription = if (isOpen) "Close" else "Add"
                            )
                        }
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
                isCardView = isCardView,
                onIncrement = viewModel::incrementItemQuantity,
                onDecrement = viewModel::decrementItemQuantity,
                onDelete = viewModel::deleteItem,
                onItemClick = { navigateToItemEdit(it.id) },
                onImageClick = { uri -> selectedImageUri = uri },
                modifier = Modifier.weight(1f)
            )
        }

        // Animated image preview dialog — fades in/out
        AnimatedVisibility(
            visible = selectedImageUri != null,
            enter = fadeIn(animationSpec = tween(250)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            selectedImageUri?.let { uri ->
                ImagePreviewDialog(
                    imageUri = uri,
                    onDismiss = { selectedImageUri = null }
                )
            }
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
    isCardView: Boolean,
    onIncrement: (Item) -> Unit,
    onDecrement: (Item) -> Unit,
    onDelete: (Item) -> Unit,
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
            if (isCardView) {
                InventoryGrid(
                    homeItems = homeItems,
                    onIncrement = onIncrement,
                    onDecrement = onDecrement,
                    onDelete = onDelete,
                    onItemClick = onItemClick,
                    onImageClick = onImageClick,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            } else {
                InventoryList(
                    homeItems = homeItems,
                    onIncrement = onIncrement,
                    onDecrement = onDecrement,
                    onDelete = onDelete,
                    onItemClick = onItemClick,
                    onImageClick = onImageClick,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun InventoryList(
    homeItems: List<HomeUiItem>,
    onIncrement: (Item) -> Unit,
    onDecrement: (Item) -> Unit,
    onDelete: (Item) -> Unit,
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
                       onDelete = onDelete,
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
    onDelete: (Item) -> Unit,
    modifier: Modifier = Modifier,
    elevation: androidx.compose.ui.unit.Dp = 2.dp
) {
    var showDeletePrompt by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .padding(vertical = 4.dp)
            .clickable { onItemClick(item) },
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        AnimatedContent(
            targetState = showDeletePrompt,
            transitionSpec = {
                if (targetState) {
                    // Showing delete prompt: slide in from end, slide out to start
                    (slideInVertically(
                        initialOffsetY = { -it / 3 },
                        animationSpec = tween(250)
                    ) + fadeIn(animationSpec = tween(250))) togetherWith
                        (slideOutVertically(
                            targetOffsetY = { it / 3 },
                            animationSpec = tween(200)
                        ) + fadeOut(animationSpec = tween(200)))
                } else {
                    // Returning to normal: reverse
                    (slideInVertically(
                        initialOffsetY = { it / 3 },
                        animationSpec = tween(250)
                    ) + fadeIn(animationSpec = tween(250))) togetherWith
                        (slideOutVertically(
                            targetOffsetY = { -it / 3 },
                            animationSpec = tween(200)
                        ) + fadeOut(animationSpec = tween(200)))
                }
            },
            label = "delete prompt"
        ) { isShowingDeletePrompt ->
            if (isShowingDeletePrompt) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Remove Item?",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        androidx.compose.material3.TextButton(
                            onClick = { 
                                 showDeletePrompt = false
                                 if (item.quantity == 1) {
                                     onDecrement(item) // Should result in 0
                                 }
                            }
                        ) {
                            Text("No")
                        }
                        androidx.compose.material3.Button(
                            onClick = { 
                                showDeletePrompt = false
                                onDelete(item)
                            }
                        ) {
                            Text("Yes")
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left: Image
                    if (item.imageUris.isNotEmpty()) {
                        AsyncImage(
                            model = File(item.imageUris.first()),
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .clickable { onImageClick(item.imageUris.first()) },
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
                            onClick = { 
                                if (item.quantity <= 1) {
                                    showDeletePrompt = true
                                } else {
                                    onDecrement(item)
                                }
                            },
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
    }
}

@Composable
private fun InventoryGrid(
    homeItems: List<HomeUiItem>,
    onIncrement: (Item) -> Unit,
    onDecrement: (Item) -> Unit,
    onDelete: (Item) -> Unit,
    onItemClick: (Item) -> Unit,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val lazyGridState = rememberLazyGridState()
    var itemToDelete by remember { mutableStateOf<Item?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog && itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false 
                itemToDelete = null
            },
            title = { Text("Remove Item?") },
            text = { Text("Are you sure you want to remove ${itemToDelete?.name}?") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        itemToDelete?.let { onDelete(it) }
                        showDeleteDialog = false
                        itemToDelete = null
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { 
                        showDeleteDialog = false 
                        itemToDelete = null 
                    }
                ) {
                    Text("No")
                }
            }
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        state = lazyGridState,
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = homeItems,
            span = { item ->
                when (item) {
                    is HomeUiItem.Header -> GridItemSpan(2)
                    is HomeUiItem.ItemEntry -> GridItemSpan(1)
                }
            },
            key = {
                when (it) {
                    is HomeUiItem.Header -> "header_${it.name}"
                    is HomeUiItem.ItemEntry -> "item_${it.item.id}"
                }
            }
        ) { uiItem ->
             when (uiItem) {
                is HomeUiItem.Header -> {
                    LocationHeader(name = uiItem.name)
                }
                is HomeUiItem.ItemEntry -> {
                    InventoryCard(
                        item = uiItem.item,
                        onIncrement = onIncrement,
                        onDecrement = { item ->
                             if (item.quantity <= 1) {
                                  itemToDelete = item
                                  showDeleteDialog = true
                             } else {
                                  onDecrement(item)
                             }
                        },
                        onItemClick = onItemClick,
                        onImageClick = onImageClick,
                        modifier = Modifier
                    )
                }
            }
        }
    }
}

@Composable
private fun InventoryCard(
    item: Item,
    onIncrement: (Item) -> Unit,
    onDecrement: (Item) -> Unit,
    onItemClick: (Item) -> Unit,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onItemClick(item) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
                    .clickable { 
                        if (item.imageUris.isNotEmpty()) {
                            onImageClick(item.imageUris.first())
                        }
                    }
            ) {
                if (item.imageUris.isNotEmpty()) {
                    AsyncImage(
                        model = File(item.imageUris.first()),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Inventory,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Qty: ${item.quantity}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                         IconButton(
                             onClick = { onDecrement(item) },
                             modifier = Modifier.size(32.dp)
                         ) {
                             Icon(
                                 Icons.Default.Remove, 
                                 contentDescription = "Decrease",
                                 modifier = Modifier.size(16.dp)
                             )
                         }
                         IconButton(
                             onClick = { onIncrement(item) },
                             modifier = Modifier.size(32.dp)
                         ) {
                             Icon(
                                 Icons.Default.Add, 
                                 contentDescription = "Increase",
                                 modifier = Modifier.size(16.dp)
                             )
                         }
                    }
                }
            }
        }
    }
}
