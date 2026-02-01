package com.panaadhaar.extractor.india.ui.screens.camera

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.panaadhaar.extractor.india.data.model.ExtractedData
import com.panaadhaar.extractor.india.data.repository.EntitlementRepository
import com.panaadhaar.extractor.india.utils.DocumentParser
import com.panaadhaar.extractor.india.utils.TextExtractionUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CameraViewModel(
    application: Application,
    private val entitlementRepository: EntitlementRepository
) : AndroidViewModel(application) {

    sealed class ExtractionState {
        object Idle : ExtractionState()
        object Processing : ExtractionState()
        data class Success(val extractedData: ExtractedData) : ExtractionState()
        data class Error(val message: String) : ExtractionState()
    }

    private val _extractionState = MutableStateFlow<ExtractionState>(ExtractionState.Idle)
    val extractionState: StateFlow<ExtractionState> = _extractionState.asStateFlow()

    fun processCapturedImage(imageUri: Uri) {
        viewModelScope.launch {
            _extractionState.value = ExtractionState.Processing

            try {
                val rawText = TextExtractionUtils.extractTextFromImage(imageUri, getApplication())
                
                if (rawText.isBlank()) {
                    _extractionState.value = ExtractionState.Error("No text found in image")
                    return@launch
                }

                val extractedData = DocumentParser.parseDocument(rawText)
                
                entitlementRepository.incrementFreeScan()
                
                _extractionState.value = ExtractionState.Success(extractedData)
            } catch (e: Exception) {
                _extractionState.value = ExtractionState.Error(
                    e.message ?: "Failed to extract text from image"
                )
            }
        }
    }

    fun resetState() {
        _extractionState.value = ExtractionState.Idle
    }
}
