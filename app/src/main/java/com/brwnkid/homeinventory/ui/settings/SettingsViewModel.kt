package com.brwnkid.homeinventory.ui.settings

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brwnkid.homeinventory.data.BackupRepository
import kotlinx.coroutines.launch

sealed interface BackupUiState {
    object Idle : BackupUiState
    object Loading : BackupUiState
    data class Success(val message: String) : BackupUiState
    data class Error(val message: String) : BackupUiState
}

class SettingsViewModel(
    private val backupRepository: BackupRepository
) : ViewModel() {
    var backupUiState: BackupUiState by mutableStateOf(BackupUiState.Idle)
        private set

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
    }
}
