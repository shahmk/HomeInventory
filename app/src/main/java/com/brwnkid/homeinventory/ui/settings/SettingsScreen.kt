package com.brwnkid.homeinventory.ui.settings

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brwnkid.homeinventory.ui.AppViewModelProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val backupState = viewModel.backupUiState
    val syncState = viewModel.syncUiState

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        uri?.let { viewModel.performBackup(it) }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.performRestore(it) }
    }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            viewModel.handleSignInResult(result.data)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Backup & Restore",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            )

            Button(
                onClick = {
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    backupLauncher.launch("inventory_backup_$timeStamp.zip")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Backup Data")
            }

            OutlinedButton(
                onClick = {
                    restoreLauncher.launch(arrayOf("application/zip", "application/octet-stream"))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Restore Data")
            }

            when (backupState) {
                is BackupUiState.Loading -> {
                    CircularProgressIndicator()
                    Text("Processing...")
                }
                is BackupUiState.Success -> {
                    Text(
                        text = backupState.message,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is BackupUiState.Error -> {
                    Text(
                        text = backupState.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> {}
            }

            // Cloud Sync Section
            Text(
                text = "Cloud Sync & Sharing",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
            )
            
            Text(
                text = "The app will automatically create a 'Home Inventory App' folder in your Google Drive. To share with family, simply open your Drive and share that folder with them.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val account = viewModel.signedInAccount
                    if (account != null) {
                        Text(
                            text = "Signed in as: ${account.email}",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.signOut() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Sign Out")
                            }
                            Button(
                                onClick = { viewModel.performSync() },
                                modifier = Modifier.weight(1f),
                                enabled = syncState !is SyncUiState.Loading
                            ) {
                                Icon(Icons.Default.CloudSync, contentDescription = null)
                                Text(" Sync Now", modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    } else {
                         Button(
                            onClick = { signInLauncher.launch(viewModel.authManager.getSignInIntent()) },
                            modifier = Modifier.fillMaxWidth()
                         ) {
                            Icon(Icons.Default.FolderShared, contentDescription = null)
                            Text(" Sign in with Google", modifier = Modifier.padding(start = 8.dp))
                         }
                    }
                }
            }
            
            when (syncState) {
                is SyncUiState.Loading -> {
                    CircularProgressIndicator()
                    Text("Syncing with cloud...")
                }
                is SyncUiState.Success -> {
                    Text(
                        text = "${syncState.message} (Last sync: ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(syncState.lastSync))})",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is SyncUiState.Error -> {
                    Text(
                        text = syncState.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> {}
            }
        }
    }
}
