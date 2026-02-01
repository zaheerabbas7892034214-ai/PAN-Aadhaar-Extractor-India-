package com.panaadhaar.extractor.india.utils

object Constants {
    // Billing
    const val PRODUCT_ID_PRO = "id_pro_unlock"
    const val BILLING_PREFS = "billing_prefs"
    const val PREF_IS_PRO = "is_pro"
    const val PREF_FREE_SCANS_USED = "free_scans_used"
    const val PREF_PURCHASE_TOKEN = "purchase_token"
    
    // Free tier limits
    const val FREE_SCAN_LIMIT = 3
    
    // Regex patterns
    const val PAN_REGEX = "^[A-Z]{5}[0-9]{4}[A-Z]$"
    const val AADHAAR_REGEX = "\\d{4}\\s?\\d{4}\\s?\\d{4}"
    
    // File providers
    const val FILE_PROVIDER_AUTHORITY = "com.panaadhaar.extractor.india.fileprovider"
    
    // Export
    const val EXPORT_DIR = "PAN_Aadhaar_Exports"
}
