package com.panaadhaar.extractor.india.ui.screens.export

import android.app.Application
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.panaadhaar.extractor.india.data.model.DocumentType
import com.panaadhaar.extractor.india.data.model.ExtractedData
import com.panaadhaar.extractor.india.data.repository.EntitlementRepository
import com.panaadhaar.extractor.india.data.repository.ProfileRepository
import com.panaadhaar.extractor.india.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.File
import java.io.FileWriter

class ExportViewModel(
    application: Application,
    private val profileRepository: ProfileRepository,
    private val entitlementRepository: EntitlementRepository
) : AndroidViewModel(application) {

    sealed class ExportState {
        object Idle : ExportState()
        object Exporting : ExportState()
        data class Success(val fileUri: Uri) : ExportState()
        data class Error(val message: String) : ExportState()
        object ProRequired : ExportState()
    }

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    fun exportToPdf(profileId: Long) {
        viewModelScope.launch {
            _exportState.value = ExportState.Exporting

            try {
                val isPro = entitlementRepository.isPro()
                if (!isPro) {
                    _exportState.value = ExportState.ProRequired
                    return@launch
                }

                val profile = profileRepository.getProfileById(profileId)
                if (profile == null) {
                    _exportState.value = ExportState.Error("Profile not found")
                    return@launch
                }

                val cacheDir = getApplication<Application>().cacheDir
                val exportDir = File(cacheDir, Constants.EXPORT_DIR)
                if (!exportDir.exists()) {
                    exportDir.mkdirs()
                }

                val pdfFile = File(exportDir, "profile_${profileId}_${System.currentTimeMillis()}.pdf")
                val pdfWriter = PdfWriter(pdfFile)
                val pdfDocument = PdfDocument(pdfWriter)
                val document = Document(pdfDocument)

                document.add(Paragraph("Document Type: ${profile.documentType.name}"))
                document.add(Paragraph(" "))

                when (profile.documentType) {
                    DocumentType.PAN -> {
                        profile.panData?.let { panData ->
                            document.add(Paragraph("PAN Number: ${panData.panNumber}"))
                            document.add(Paragraph("Name: ${panData.name}"))
                            document.add(Paragraph("Father's Name: ${panData.fatherName}"))
                            document.add(Paragraph("Date of Birth: ${panData.dob}"))
                        }
                    }
                    DocumentType.AADHAAR -> {
                        profile.aadhaarData?.let { aadhaarData ->
                            document.add(Paragraph("Aadhaar Number: ${aadhaarData.aadhaarNumber}"))
                            document.add(Paragraph("Name: ${aadhaarData.name}"))
                            document.add(Paragraph("Date of Birth: ${aadhaarData.dob}"))
                            document.add(Paragraph("Gender: ${aadhaarData.gender}"))
                            document.add(Paragraph("Address: ${aadhaarData.address}"))
                        }
                    }
                    DocumentType.UNKNOWN -> {
                        document.add(Paragraph("Unknown document type"))
                    }
                }

                document.close()

                val fileUri = FileProvider.getUriForFile(
                    getApplication(),
                    Constants.FILE_PROVIDER_AUTHORITY,
                    pdfFile
                )

                _exportState.value = ExportState.Success(fileUri)
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(
                    e.message ?: "Failed to export PDF"
                )
            }
        }
    }

    fun exportAllToCsv() {
        viewModelScope.launch {
            _exportState.value = ExportState.Exporting

            try {
                val isPro = entitlementRepository.isPro()
                if (!isPro) {
                    _exportState.value = ExportState.ProRequired
                    return@launch
                }

                val profiles = profileRepository.getAllProfiles().first()
                if (profiles.isEmpty()) {
                    _exportState.value = ExportState.Error("No profiles to export")
                    return@launch
                }

                val cacheDir = getApplication<Application>().cacheDir
                val exportDir = File(cacheDir, Constants.EXPORT_DIR)
                if (!exportDir.exists()) {
                    exportDir.mkdirs()
                }

                val csvFile = File(exportDir, "all_profiles_${System.currentTimeMillis()}.csv")
                val fileWriter = FileWriter(csvFile)
                val csvPrinter = CSVPrinter(
                    fileWriter,
                    CSVFormat.DEFAULT.withHeader(
                        "Document Type", "PAN Number", "Aadhaar Number",
                        "Name", "Father's Name", "DOB", "Gender", "Address"
                    )
                )

                profiles.forEach { profile ->
                    when (profile.documentType) {
                        DocumentType.PAN -> {
                            profile.panData?.let { panData ->
                                csvPrinter.printRecord(
                                    "PAN",
                                    panData.panNumber,
                                    "",
                                    panData.name,
                                    panData.fatherName,
                                    panData.dob,
                                    "",
                                    ""
                                )
                            }
                        }
                        DocumentType.AADHAAR -> {
                            profile.aadhaarData?.let { aadhaarData ->
                                csvPrinter.printRecord(
                                    "AADHAAR",
                                    "",
                                    aadhaarData.aadhaarNumber,
                                    aadhaarData.name,
                                    "",
                                    aadhaarData.dob,
                                    aadhaarData.gender,
                                    aadhaarData.address
                                )
                            }
                        }
                        DocumentType.UNKNOWN -> {
                            csvPrinter.printRecord("UNKNOWN", "", "", "", "", "", "", "")
                        }
                    }
                }

                csvPrinter.flush()
                csvPrinter.close()

                val fileUri = FileProvider.getUriForFile(
                    getApplication(),
                    Constants.FILE_PROVIDER_AUTHORITY,
                    csvFile
                )

                _exportState.value = ExportState.Success(fileUri)
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(
                    e.message ?: "Failed to export CSV"
                )
            }
        }
    }

    fun resetState() {
        _exportState.value = ExportState.Idle
    }
}
