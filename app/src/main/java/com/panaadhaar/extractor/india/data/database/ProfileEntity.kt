package com.panaadhaar.extractor.india.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "document_type")
    val documentType: String,
    
    @ColumnInfo(name = "pan_number")
    val panNumber: String?,
    
    @ColumnInfo(name = "name")
    val name: String?,
    
    @ColumnInfo(name = "father_name")
    val fatherName: String?,
    
    @ColumnInfo(name = "dob")
    val dob: String?,
    
    @ColumnInfo(name = "aadhaar_number")
    val aadhaarNumber: String?,
    
    @ColumnInfo(name = "gender")
    val gender: String?,
    
    @ColumnInfo(name = "address")
    val address: String?,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)
