package com.waterproofing.inventory.ui.more

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waterproofing.inventory.data.database.AppDatabase
import com.waterproofing.inventory.domain.BackupManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class BackupRestoreState {
    object Idle : BackupRestoreState()
    object Loading : BackupRestoreState()
    data class ExportSuccess(val shareIntent: Intent) : BackupRestoreState()
    data class ImportSuccess(val fileName: String) : BackupRestoreState()
    data class Error(val message: String) : BackupRestoreState()
}

class BackupRestoreViewModel : ViewModel() {

    private val _state = MutableStateFlow<BackupRestoreState>(BackupRestoreState.Idle)
    val state: StateFlow<BackupRestoreState> = _state

    fun exportBackup(context: Context) {
        _state.value = BackupRestoreState.Loading
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // Checkpoint WAL so the .db file is consistent
                try {
                    AppDatabase.getDatabase(context).openHelper.writableDatabase
                        .execSQL("PRAGMA wal_checkpoint(FULL)")
                } catch (_: Exception) { /* ignore checkpoint errors */ }

                val result = BackupManager.export(context)
                result.fold(
                    onSuccess = { uri ->
                        val shareIntent = BackupManager.buildShareIntent(uri)
                        _state.value = BackupRestoreState.ExportSuccess(shareIntent)
                    },
                    onFailure = { e ->
                        _state.value = BackupRestoreState.Error(e.message ?: "Export failed")
                    }
                )
            }
        }
    }

    fun importBackup(context: Context, sourceUri: Uri) {
        _state.value = BackupRestoreState.Loading
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val result = BackupManager.import(context, sourceUri)
                result.fold(
                    onSuccess = { fileName ->
                        _state.value = BackupRestoreState.ImportSuccess(fileName)
                    },
                    onFailure = { e ->
                        _state.value = BackupRestoreState.Error(e.message ?: "Restore failed")
                    }
                )
            }
        }
    }

    fun resetState() {
        _state.value = BackupRestoreState.Idle
    }
}
