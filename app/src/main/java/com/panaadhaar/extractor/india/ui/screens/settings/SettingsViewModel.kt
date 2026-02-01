package com.panaadhaar.extractor.india.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panaadhaar.extractor.india.data.repository.BillingRepository
import com.panaadhaar.extractor.india.data.repository.EntitlementRepository
import com.panaadhaar.extractor.india.data.repository.ProfileRepository
import com.panaadhaar.extractor.india.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val profileRepository: ProfileRepository,
    private val entitlementRepository: EntitlementRepository,
    private val billingRepository: BillingRepository
) : ViewModel() {

    sealed class SettingsState {
        object Idle : SettingsState()
        object Loading : SettingsState()
        data class EntitlementInfo(
            val isPro: Boolean,
            val scansUsed: Int,
            val scansRemaining: Int
        ) : SettingsState()
        data class Success(val message: String) : SettingsState()
        data class Error(val message: String) : SettingsState()
    }

    private val _settingsState = MutableStateFlow<SettingsState>(SettingsState.Idle)
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()

    init {
        loadEntitlementInfo()
    }

    fun loadEntitlementInfo() {
        viewModelScope.launch {
            _settingsState.value = SettingsState.Loading

            try {
                val isPro = entitlementRepository.isPro()
                val scansUsed = entitlementRepository.getFreeScansUsed()
                val scansRemaining = if (isPro) {
                    Int.MAX_VALUE
                } else {
                    (Constants.FREE_SCAN_LIMIT - scansUsed).coerceAtLeast(0)
                }

                _settingsState.value = SettingsState.EntitlementInfo(
                    isPro = isPro,
                    scansUsed = scansUsed,
                    scansRemaining = scansRemaining
                )
            } catch (e: Exception) {
                _settingsState.value = SettingsState.Error(
                    e.message ?: "Failed to load entitlement info"
                )
            }
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            _settingsState.value = SettingsState.Loading

            try {
                val restored = billingRepository.restorePurchases()
                if (restored) {
                    _settingsState.value = SettingsState.Success("Purchases restored successfully")
                    loadEntitlementInfo()
                } else {
                    _settingsState.value = SettingsState.Error("No purchases found to restore")
                }
            } catch (e: Exception) {
                _settingsState.value = SettingsState.Error(
                    e.message ?: "Failed to restore purchases"
                )
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            _settingsState.value = SettingsState.Loading

            try {
                profileRepository.deleteAllProfiles()
                entitlementRepository.clearAllData()
                
                _settingsState.value = SettingsState.Success("All data cleared successfully")
                loadEntitlementInfo()
            } catch (e: Exception) {
                _settingsState.value = SettingsState.Error(
                    e.message ?: "Failed to clear data"
                )
            }
        }
    }

    fun resetState() {
        _settingsState.value = SettingsState.Idle
    }
}
