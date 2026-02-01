package com.panaadhaar.extractor.india.utils

import java.util.regex.Pattern

object ValidationUtils {
    
    fun validatePAN(pan: String): Boolean {
        if (pan.isBlank()) return false
        val pattern = Pattern.compile(Constants.PAN_REGEX)
        return pattern.matcher(pan.trim()).matches()
    }
    
    fun extractAadhaarNumber(text: String): String? {
        val pattern = Pattern.compile(Constants.AADHAAR_REGEX)
        val matcher = pattern.matcher(text)
        return if (matcher.find()) {
            matcher.group()
        } else null
    }
    
    fun maskAadhaarNumber(aadhaar: String): String {
        // Mask first 8 digits: XXXX XXXX 1234
        val cleaned = aadhaar.replace("\\s".toRegex(), "")
        return if (cleaned.length == 12) {
            "XXXX XXXX ${cleaned.substring(8)}"
        } else {
            aadhaar
        }
    }
}
