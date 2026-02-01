package com.panaadhaar.extractor.india.data.model

data class ExtractedData(
    val documentType: DocumentType,
    val panData: PANData? = null,
    val aadhaarData: AadhaarData? = null,
    val rawText: String = ""
)
