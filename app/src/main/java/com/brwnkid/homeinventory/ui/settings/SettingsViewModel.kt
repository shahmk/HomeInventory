package com.brwnkid.homeinventory.ui.settings

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.brwnkid.homeinventory.data.BackupRepository
import kotlinx.coroutines.launch

sealed interface BackupUiState {
    object Idle : BackupUiState
    object Loading : BackupUiState
    data class Success(val message: String) : BackupUiState
    data class Error(val message: String) : BackupUiState
}

sealed interface SyncUiState {
    object Idle : SyncUiState
    object Loading : SyncUiState
    data class Success(val message: String, val lastSync: Long = 0) : SyncUiState
    data class Error(val message: String) : SyncUiState
}

class SettingsViewModel(
    private val backupRepository: com.brwnkid.homeinventory.data.BackupRepository,
    private val syncManager: com.brwnkid.homeinventory.data.sync.SyncManager,
    val authManager: com.brwnkid.homeinventory.data.sync.GoogleDriveAuthManager
) : ViewModel() {
    var backupUiState: BackupUiState by mutableStateOf(BackupUiState.Idle)
        private set

    var syncUiState: SyncUiState by mutableStateOf(SyncUiState.Idle)
        private set

    var signedInAccount: GoogleSignInAccount? by mutableStateOf(authManager.getSignedInAccount())
        private set

    var shareEmail: String by mutableStateOf("")
        private set

    fun updateShareEmail(email: String) {
        shareEmail = email
    }


    fun performBackup(uri: Uri) {
        viewModelScope.launch {
            backupUiState = BackupUiState.Loading
            val result = backupRepository.performBackup(uri)
            backupUiState = if (result.isSuccess) {
                BackupUiState.Success("Backup completed successfully")
            } else {
                BackupUiState.Error("Backup failed: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun performRestore(uri: Uri) {
        viewModelScope.launch {
            backupUiState = BackupUiState.Loading
            val result = backupRepository.performRestore(uri)
            backupUiState = if (result.isSuccess) {
                BackupUiState.Success("Restore completed successfully")
            } else {
                BackupUiState.Error("Restore failed: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun resetState() {
        backupUiState = BackupUiState.Idle
        syncUiState = SyncUiState.Idle
    }

    fun handleSignInResult(intent: Intent?) {
        try {
            signedInAccount = authManager.getSignedInAccountFromIntent(intent)
            syncUiState = SyncUiState.Idle
        } catch (e: ApiException) {
            e.printStackTrace()
            val statusCode = e.statusCode
            var errorMsg = "Sign in failed (Code: $statusCode)."
            if (statusCode == 10) {
                errorMsg += " Make sure your SHA-1 fingerprint is added to the Google Cloud Console OAuth credentials."
            }
            syncUiState = SyncUiState.Error(errorMsg)
            signedInAccount = null
        } catch (e: Exception) {
            e.printStackTrace()
            syncUiState = SyncUiState.Error("Sign in failed: ${e.message}")
            signedInAccount = null
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authManager.signOut()
            signedInAccount = null
        }
    }

    fun performSync() {
        viewModelScope.launch {
            syncUiState = SyncUiState.Loading
            val result = syncManager.sync()
            syncUiState = if (result.isSuccess) {
                SyncUiState.Success("Sync completed", System.currentTimeMillis())
            } else {
                SyncUiState.Error("Sync failed: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun shareWithEmail() {
        if (shareEmail.isBlank()) return
        viewModelScope.launch {
            syncUiState = SyncUiState.Loading
            val result = syncManager.shareFolderWithEmail(shareEmail)
            syncUiState = if (result.isSuccess) {
                shareEmail = ""
                SyncUiState.Success("Folder shared successfully", System.currentTimeMillis())
            } else {
                SyncUiState.Error("Failed to share folder: ${result.exceptionOrNull()?.message}")
            }
        }
    }
}
