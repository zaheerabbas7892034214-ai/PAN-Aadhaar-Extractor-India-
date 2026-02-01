package com.panaadhaar.extractor.india.utils

import com.panaadhaar.extractor.india.data.model.AadhaarData
import com.panaadhaar.extractor.india.data.model.DocumentType
import com.panaadhaar.extractor.india.data.model.ExtractedData
import com.panaadhaar.extractor.india.data.model.PANData
import java.util.regex.Pattern

object DocumentParser {
    
    fun parseDocument(text: String): ExtractedData {
        val documentType = detectDocumentType(text)
        
        return when (documentType) {
            DocumentType.PAN -> {
                val panData = extractPANData(text)
                ExtractedData(
                    documentType = DocumentType.PAN,
                    panData = panData,
                    rawText = text
                )
            }
            DocumentType.AADHAAR -> {
                val aadhaarData = extractAadhaarData(text)
                ExtractedData(
                    documentType = DocumentType.AADHAAR,
                    aadhaarData = aadhaarData,
                    rawText = text
                )
            }
            DocumentType.UNKNOWN -> {
                ExtractedData(
                    documentType = DocumentType.UNKNOWN,
                    rawText = text
                )
            }
        }
    }
    
    private fun detectDocumentType(text: String): DocumentType {
        val upperText = text.uppercase()
        
        val panKeywords = listOf("INCOME TAX", "PERMANENT ACCOUNT NUMBER", "INCOME TAX DEPARTMENT")
        val aadhaarKeywords = listOf("GOVERNMENT OF INDIA", "AADHAAR", "UNIQUE IDENTIFICATION")
        
        val hasPanKeywords = panKeywords.any { upperText.contains(it) }
        val hasAadhaarKeywords = aadhaarKeywords.any { upperText.contains(it) }
        
        val panPattern = Pattern.compile(Constants.PAN_REGEX.removePrefix("^").removeSuffix("$"))
        val hasPanPattern = panPattern.matcher(text).find()
        
        val aadhaarPattern = Pattern.compile(Constants.AADHAAR_REGEX)
        val hasAadhaarPattern = aadhaarPattern.matcher(text).find()
        
        return when {
            hasPanKeywords || hasPanPattern -> DocumentType.PAN
            hasAadhaarKeywords || hasAadhaarPattern -> DocumentType.AADHAAR
            else -> DocumentType.UNKNOWN
        }
    }
    
    private fun extractPANData(text: String): PANData {
        val lines = text.lines().map { it.trim() }
        
        var panNumber = ""
        var name = ""
        var fatherName = ""
        var dob = ""
        
        val panPattern = Pattern.compile(Constants.PAN_REGEX.removePrefix("^").removeSuffix("$"))
        for (line in lines) {
            val matcher = panPattern.matcher(line)
            if (matcher.find()) {
                val candidate = matcher.group()
                if (ValidationUtils.validatePAN(candidate)) {
                    panNumber = candidate
                    break
                }
            }
        }
        
        for (i in lines.indices) {
            val line = lines[i]
            val upperLine = line.uppercase()
            
            if (name.isEmpty() && (upperLine.contains("NAME") || i > 0 && lines[i - 1].uppercase().contains("NAME"))) {
                val nameCandidate = line.replace(Regex("NAME\\s*:?\\s*", RegexOption.IGNORE_CASE), "").trim()
                if (nameCandidate.isNotEmpty() && nameCandidate.length > 2 && nameCandidate.matches(Regex("[A-Za-z\\s]+"))) {
                    name = nameCandidate
                }
            }
            
            if (fatherName.isEmpty() && (upperLine.contains("FATHER") || i > 0 && lines[i - 1].uppercase().contains("FATHER"))) {
                val fatherCandidate = line.replace(Regex("FATHER.*?NAME\\s*:?\\s*", RegexOption.IGNORE_CASE), "").trim()
                if (fatherCandidate.isNotEmpty() && fatherCandidate.length > 2 && fatherCandidate.matches(Regex("[A-Za-z\\s]+"))) {
                    fatherName = fatherCandidate
                }
            }
            
            if (dob.isEmpty()) {
                val dobPattern = Pattern.compile("\\d{2}[/-]\\d{2}[/-]\\d{4}")
                val dobMatcher = dobPattern.matcher(line)
                if (dobMatcher.find()) {
                    dob = dobMatcher.group()
                }
            }
        }
        
        return PANData(
            panNumber = panNumber,
            name = name,
            fatherName = fatherName,
            dob = dob
        )
    }
    
    private fun extractAadhaarData(text: String): AadhaarData {
        val lines = text.lines().map { it.trim() }
        
        var aadhaarNumber = ""
        var name = ""
        var dob = ""
        var gender = ""
        var address = ""
        
        val aadhaarRaw = ValidationUtils.extractAadhaarNumber(text)
        if (aadhaarRaw != null) {
            aadhaarNumber = ValidationUtils.maskAadhaarNumber(aadhaarRaw)
        }
        
        for (i in lines.indices) {
            val line = lines[i]
            val upperLine = line.uppercase()
            
            if (gender.isEmpty()) {
                when {
                    upperLine.contains("MALE") && !upperLine.contains("FEMALE") -> gender = "Male"
                    upperLine.contains("FEMALE") -> gender = "Female"
                }
            }
            
            if (dob.isEmpty()) {
                val dobPatterns = listOf(
                    Pattern.compile("\\d{2}[/-]\\d{2}[/-]\\d{4}"),
                    Pattern.compile("DOB\\s*:?\\s*(\\d{2}[/-]\\d{2}[/-]\\d{4})", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("BIRTH\\s*:?\\s*(\\d{2}[/-]\\d{2}[/-]\\d{4})", Pattern.CASE_INSENSITIVE)
                )
                
                for (pattern in dobPatterns) {
                    val matcher = pattern.matcher(line)
                    if (matcher.find()) {
                        dob = if (matcher.groupCount() > 0) matcher.group(1) else matcher.group()
                        break
                    }
                }
            }
        }
        
        val nameIndex = lines.indexOfFirst { it.matches(Regex("[A-Z][a-z]+(?:\\s+[A-Z][a-z]+)+")) }
        if (nameIndex != -1) {
            name = lines[nameIndex]
        }
        
        val addressLines = mutableListOf<String>()
        var startCollecting = false
        for (line in lines) {
            val upperLine = line.uppercase()
            if (upperLine.contains("ADDRESS") || upperLine.contains("S/O") || upperLine.contains("D/O") || upperLine.contains("C/O")) {
                startCollecting = true
                continue
            }
            if (startCollecting && line.length > 10 && !line.contains(Regex("\\d{4}\\s?\\d{4}\\s?\\d{4}"))) {
                addressLines.add(line)
                if (addressLines.size >= 3) break
            }
        }
        address = addressLines.joinToString(", ")
        
        return AadhaarData(
            aadhaarNumber = aadhaarNumber,
            name = name,
            dob = dob,
            gender = gender,
            address = address
        )
    }
}
