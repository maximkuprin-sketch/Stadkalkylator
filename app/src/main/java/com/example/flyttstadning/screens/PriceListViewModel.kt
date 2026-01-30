package com.example.flyttstadning.screens

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flyttstadning.FlyttApplication
import com.example.flyttstadning.data.database.PriceBand
import com.example.flyttstadning.data.repository.PreferencesRepository
import com.example.flyttstadning.data.repository.PriceRepository
import com.example.flyttstadning.utils.FileParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PriceListViewModel(
    application: Application,
    private val priceRepository: PriceRepository,
    private val preferencesRepository: PreferencesRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PriceListUiState())
    val uiState: StateFlow<PriceListUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            priceRepository.allPrices.collect { prices ->
                val lastTime = preferencesRepository.getLastImportTime()
                val dateStr = if (lastTime > 0) {
                    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(lastTime))
                } else {
                    "Aldrig"
                }
                _uiState.value = _uiState.value.copy(
                    priceList = prices,
                    lastImportDate = dateStr
                )
            }
        }
    }

    fun importFile(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Read file
                val resolver = getApplication<Application>().contentResolver
                // Get filename
                var fileName = "unknown"
                resolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex("_display_name") // OpenableColumns.DISPLAY_NAME check?
                    if (cursor.moveToFirst() && nameIndex >= 0) {
                        fileName = cursor.getString(nameIndex)
                    }
                }
                
                // Hack: if filename unknown, guess from uri or try both? 
                // Let's assume user picks file with extensions.
                // If URI doesn't give a name easily, we might need a workaround.
                // But normally ACTION_OPEN_DOCUMENT gives a name.
                
                // NOTE: We should copy the stream to a temp file or read directly.
                // POI needs InputStream (mostly).
                
                withContext(Dispatchers.IO) {
                    resolver.openInputStream(uri)?.use { inputStream ->
                        val bands = FileParser.parseInputStream(inputStream, fileName)
                        priceRepository.replacePriceList(bands)
                        preferencesRepository.saveLastImportTime(System.currentTimeMillis())
                    }
                }
                // Refresh is automatic via Flow
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Unknown error during import")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun exportCsv(onContentReady: (String) -> Unit) {
        viewModelScope.launch {
            val prices = priceRepository.exportPrices()
            val sb = StringBuilder()
            prices.forEach { 
                sb.append("${it.rangeStart}-${it.rangeEnd},${it.price}\n")
            }
            onContentReady(sb.toString())
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: androidx.lifecycle.viewmodel.CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as FlyttApplication
                return PriceListViewModel(
                    application,
                    application.container.priceRepository,
                    application.container.preferencesRepository
                ) as T
            }
        }
    }
}

data class PriceListUiState(
    val priceList: List<PriceBand> = emptyList(),
    val lastImportDate: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
