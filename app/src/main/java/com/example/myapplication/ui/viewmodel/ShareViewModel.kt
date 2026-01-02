package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.util.ParsedBookInfo
import com.example.myapplication.util.WebPageParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ShareUiState {
    object Idle : ShareUiState()
    object Loading : ShareUiState()
    data class Success(val bookInfo: ParsedBookInfo) : ShareUiState()
    data class Error(val message: String) : ShareUiState()
}

class ShareViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<ShareUiState>(ShareUiState.Idle)
    val uiState: StateFlow<ShareUiState> = _uiState

    fun parseUrl(url: String) {
        viewModelScope.launch {
            _uiState.value = ShareUiState.Loading
            try {
                // Extract URL from text if it's a mixed string
                val extractedUrl = extractUrl(url)
                if (extractedUrl == null) {
                    _uiState.value = ShareUiState.Error("Invalid URL")
                    return@launch
                }
                
                val result = WebPageParser.parseUrl(extractedUrl)
                _uiState.value = ShareUiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = ShareUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun extractUrl(input: String): String? {
        val urlRegex = """(https?://[^\s,]+)""".toRegex()
        return urlRegex.find(input)?.value
    }
}
