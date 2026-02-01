package com.panaadhaar.extractor.india.ui.screens.results

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panaadhaar.extractor.india.data.model.ExtractedData
import com.panaadhaar.extractor.india.data.repository.EntitlementRepository
import com.panaadhaar.extractor.india.data.repository.ProfileRepository
import com.panaadhaar.extractor.india.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ResultsViewModel(
    private val profileRepository: ProfileRepository,
    private val entitlementRepository: EntitlementRepository
) : ViewModel() {

    sealed class SaveState {
        object Idle : SaveState()
        object Saving : SaveState()
        object Success : SaveState()
        data class Error(val message: String) : SaveState()
        object LimitReached : SaveState()
    }

    private val _extractedData = MutableStateFlow<ExtractedData?>(null)
    val extractedData: StateFlow<ExtractedData?> = _extractedData.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    private val _canPerformAction = MutableStateFlow(false)
    val canPerformAction: StateFlow<Boolean> = _canPerformAction.asStateFlow()

    init {
        checkEntitlementStatus()
    }

    private fun checkEntitlementStatus() {
        viewModelScope.launch {
            try {
                val isPro = entitlementRepository.isPro()
                val freeScansUsed = entitlementRepository.getFreeScansUsed()
                _canPerformAction.value = isPro || freeScansUsed < Constants.FREE_SCAN_LIMIT
            } catch (e: Exception) {
                _canPerformAction.value = false
            }
        }
    }

    fun setExtractedData(data: ExtractedData) {
        _extractedData.value = data
        checkEntitlementStatus()
    }

    fun saveProfile() {
        viewModelScope.launch {
            val data = _extractedData.value
            if (data == null) {
                _saveState.value = SaveState.Error("No data to save")
                return@launch
            }

            _saveState.value = SaveState.Saving

            try {
                val isPro = entitlementRepository.isPro()
                val freeScansUsed = entitlementRepository.getFreeScansUsed()

                if (!isPro && freeScansUsed >= Constants.FREE_SCAN_LIMIT) {
                    _saveState.value = SaveState.LimitReached
                    return@launch
                }

                profileRepository.saveProfile(data)
                _saveState.value = SaveState.Success
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e.message ?: "Failed to save profile")
            }
        }
    }

    fun copyToClipboard(context: Context, label: String, text: String) {
        viewModelScope.launch {
            try {
                val canPerform = _canPerformAction.value
                if (!canPerform) {
                    return@launch
                }

                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(label, text)
                clipboard.setPrimaryClip(clip)
            } catch (e: Exception) {
                // Handle silently or show error
            }
        }
    }

    fun canSaveOrCopy(): Boolean {
        return _canPerformAction.value
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }
}
