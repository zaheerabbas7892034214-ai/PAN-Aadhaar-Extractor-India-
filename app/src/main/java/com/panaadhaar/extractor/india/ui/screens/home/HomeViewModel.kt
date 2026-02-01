package com.panaadhaar.extractor.india.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panaadhaar.extractor.india.data.model.ExtractedData
import com.panaadhaar.extractor.india.data.repository.EntitlementRepository
import com.panaadhaar.extractor.india.data.repository.ProfileRepository
import com.panaadhaar.extractor.india.utils.Constants
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val profileRepository: ProfileRepository,
    private val entitlementRepository: EntitlementRepository
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        data class Success(
            val isPro: Boolean,
            val scansRemaining: Int,
            val recentScans: List<ExtractedData>
        ) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            try {
                val isPro = entitlementRepository.isPro()
                val freeScansUsed = entitlementRepository.getFreeScansUsed()
                val scansRemaining = if (isPro) {
                    Int.MAX_VALUE
                } else {
                    (Constants.FREE_SCAN_LIMIT - freeScansUsed).coerceAtLeast(0)
                }
                
                profileRepository.getAllProfiles()
                    .map { profiles -> profiles.take(5) }
                    .collect { recentScans ->
                        _uiState.value = UiState.Success(
                            isPro = isPro,
                            scansRemaining = scansRemaining,
                            recentScans = recentScans
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load home data")
            }
        }
    }

    fun canScan(): Boolean {
        val currentState = _uiState.value
        return if (currentState is UiState.Success) {
            currentState.isPro || currentState.scansRemaining > 0
        } else {
            false
        }
    }
}
