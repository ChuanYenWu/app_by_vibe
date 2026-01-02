package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.BookApplication
import com.example.myapplication.data.repository.BackupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class BackupUiState {
    object Idle : BackupUiState()
    object Loading : BackupUiState()
    data class Success(val message: String) : BackupUiState()
    data class Error(val message: String) : BackupUiState()
}

class SettingsViewModel(private val repository: BackupRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
    val uiState: StateFlow<BackupUiState> = _uiState

    fun exportData(onDataReady: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.Loading
            try {
                val json = repository.exportToJson()
                onDataReady(json)
                _uiState.value = BackupUiState.Success("Data exported successfully")
            } catch (e: Exception) {
                _uiState.value = BackupUiState.Error(e.message ?: "Export failed")
            }
        }
    }

    fun importData(json: String) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.Loading
            val result = repository.importFromJson(json)
            if (result.isSuccess) {
                _uiState.value = BackupUiState.Success("Data restored successfully")
            } else {
                _uiState.value = BackupUiState.Error(result.exceptionOrNull()?.message ?: "Restore failed")
            }
        }
    }

    fun resetState() {
        _uiState.value = BackupUiState.Idle
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BookApplication)
                val repository = application.container.backupRepository
                SettingsViewModel(repository)
            }
        }
    }
}
