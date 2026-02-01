package com.panaadhaar.extractor.india.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    
    @Insert
    suspend fun insert(profile: ProfileEntity): Long
    
    @Query("SELECT * FROM profiles ORDER BY timestamp DESC")
    fun getAllProfiles(): Flow<List<ProfileEntity>>
    
    @Query("SELECT * FROM profiles WHERE id = :profileId")
    suspend fun getProfileById(profileId: Long): ProfileEntity?
    
    @Delete
    suspend fun delete(profile: ProfileEntity)
    
    @Query("DELETE FROM profiles")
    suspend fun deleteAll()
}
