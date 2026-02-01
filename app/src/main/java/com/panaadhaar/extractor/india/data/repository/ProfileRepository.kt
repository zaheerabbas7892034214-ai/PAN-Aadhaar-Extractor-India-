package com.panaadhaar.extractor.india.data.repository

import com.panaadhaar.extractor.india.data.database.ProfileDao
import com.panaadhaar.extractor.india.data.database.ProfileEntity
import com.panaadhaar.extractor.india.data.model.DocumentType
import com.panaadhaar.extractor.india.data.model.ExtractedData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProfileRepository(private val profileDao: ProfileDao) {
    
    suspend fun saveProfile(extractedData: ExtractedData): Long {
        val profileEntity = when (extractedData.documentType) {
            DocumentType.PAN -> {
                ProfileEntity(
                    documentType = DocumentType.PAN.name,
                    panNumber = extractedData.panData?.panNumber,
                    name = extractedData.panData?.name,
                    fatherName = extractedData.panData?.fatherName,
                    dob = extractedData.panData?.dob,
                    aadhaarNumber = null,
                    gender = null,
                    address = null
                )
            }
            DocumentType.AADHAAR -> {
                ProfileEntity(
                    documentType = DocumentType.AADHAAR.name,
                    panNumber = null,
                    name = extractedData.aadhaarData?.name,
                    fatherName = null,
                    dob = extractedData.aadhaarData?.dob,
                    aadhaarNumber = extractedData.aadhaarData?.aadhaarNumber,
                    gender = extractedData.aadhaarData?.gender,
                    address = extractedData.aadhaarData?.address
                )
            }
            DocumentType.UNKNOWN -> {
                ProfileEntity(
                    documentType = DocumentType.UNKNOWN.name,
                    panNumber = null,
                    name = null,
                    fatherName = null,
                    dob = null,
                    aadhaarNumber = null,
                    gender = null,
                    address = null
                )
            }
        }
        
        return profileDao.insert(profileEntity)
    }
    
    fun getAllProfiles(): Flow<List<ExtractedData>> {
        return profileDao.getAllProfiles().map { entities ->
            entities.map { entity -> entityToExtractedData(entity) }
        }
    }
    
    suspend fun getProfileById(profileId: Long): ExtractedData? {
        val entity = profileDao.getProfileById(profileId)
        return entity?.let { entityToExtractedData(it) }
    }
    
    suspend fun deleteProfile(profileId: Long) {
        val entity = profileDao.getProfileById(profileId)
        entity?.let { profileDao.delete(it) }
    }
    
    suspend fun deleteAllProfiles() {
        profileDao.deleteAll()
    }
    
    private fun entityToExtractedData(entity: ProfileEntity): ExtractedData {
        val documentType = DocumentType.valueOf(entity.documentType)
        
        return when (documentType) {
            DocumentType.PAN -> {
                ExtractedData(
                    documentType = DocumentType.PAN,
                    panData = com.panaadhaar.extractor.india.data.model.PANData(
                        panNumber = entity.panNumber ?: "",
                        name = entity.name ?: "",
                        fatherName = entity.fatherName ?: "",
                        dob = entity.dob ?: ""
                    )
                )
            }
            DocumentType.AADHAAR -> {
                ExtractedData(
                    documentType = DocumentType.AADHAAR,
                    aadhaarData = com.panaadhaar.extractor.india.data.model.AadhaarData(
                        aadhaarNumber = entity.aadhaarNumber ?: "",
                        name = entity.name ?: "",
                        dob = entity.dob ?: "",
                        gender = entity.gender ?: "",
                        address = entity.address ?: ""
                    )
                )
            }
            DocumentType.UNKNOWN -> {
                ExtractedData(
                    documentType = DocumentType.UNKNOWN
                )
            }
        }
    }
}
