package com.panaadhaar.extractor.india.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entitlements")
data class EntitlementEntity(
    @PrimaryKey
    val id: Int = 1,
    
    @ColumnInfo(name = "is_pro")
    val isPro: Boolean = false,
    
    @ColumnInfo(name = "free_scans_used")
    val freeScansUsed: Int = 0,
    
    @ColumnInfo(name = "purchase_token")
    val purchaseToken: String? = null
)
