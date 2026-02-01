package com.panaadhaar.extractor.india

import android.app.Application
import com.panaadhaar.extractor.india.data.database.AppDatabase

class PanAadhaarApplication : Application() {
    
    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }
    
    override fun onCreate() {
        super.onCreate()
    }
}
