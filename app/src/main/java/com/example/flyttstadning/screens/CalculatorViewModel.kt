package com.example.flyttstadning.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flyttstadning.FlyttApplication
import com.example.flyttstadning.data.database.PriceBand
import com.example.flyttstadning.data.repository.PriceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CalculatorViewModel(
    application: Application,
    private val priceRepository: PriceRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    private var prices: List<PriceBand> = emptyList()

    init {
        viewModelScope.launch {
            priceRepository.ensureDefaults() // Ensure we have something
            priceRepository.allPrices.collect {
                prices = it
                recalculate()
            }
        }
    }

    fun onSqmChange(input: String) {
        _uiState.value = _uiState.value.copy(sqmInput = input)
        recalculate()
    }

    private fun recalculate() {
        val input = _uiState.value.sqmInput
        val sqm = input.toIntOrNull()

        if (sqm == null) {
            _uiState.value = _uiState.value.copy(calculatedPrice = null, error = null)
            return
        }

        // Find price band
        // "30-35" means (30, 35]. i.e. 30 < sqm <= 35.
        // "0-30" means <= 30.
        // Logic: find band where rangeStart < sqm <= rangeEnd.
        // Wait, what if exactly rangeStart?
        // If 0-30 and 30-35.
        // 30 falls in 0-30?
        // Requirement: "30-35" meaning (30,35].
        // If 0-30, does that mean (0,30]? 
        // Requirement: 'If range is "0-30" interpret as <=30.' (So 0 inclusive to 30 inclusive).
        // Let's assume standard intervals: (Start, End].
        // Except for the first one starting at 0, which is [0, End]?
        // Or if start is 0, it includes 0?
        
        // Let's iterate.
        val match = prices.firstOrNull { band ->
            if (band.rangeStart == 0) {
                 sqm >= 0 && sqm <= band.rangeEnd
            } else {
                 sqm > band.rangeStart && sqm <= band.rangeEnd
            }
        }

        if (match != null) {
            _uiState.value = _uiState.value.copy(calculatedPrice = match.price, error = null)
        } else {
            // Check if user is outside all ranges?
            _uiState.value = _uiState.value.copy(calculatedPrice = null, error = "Ingen prisinformation för detta intervall")
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: androidx.lifecycle.viewmodel.CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as FlyttApplication
                return CalculatorViewModel(
                    application,
                    application.container.priceRepository
                ) as T
            }
        }
    }
}

data class CalculatorUiState(
    val sqmInput: String = "",
    val calculatedPrice: Int? = null,
    val error: String? = null
)
