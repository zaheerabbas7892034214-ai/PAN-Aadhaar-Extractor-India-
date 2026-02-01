package com.panaadhaar.extractor.india.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EntitlementDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entitlement: EntitlementEntity)
    
    @Query("SELECT * FROM entitlements WHERE id = 1")
    fun getEntitlement(): Flow<EntitlementEntity?>
    
    @Query("SELECT * FROM entitlements WHERE id = 1")
    suspend fun getEntitlementOnce(): EntitlementEntity?
    
    @Query("UPDATE entitlements SET is_pro = :isPro WHERE id = 1")
    suspend fun updateProStatus(isPro: Boolean): Int
    
    @Query("UPDATE entitlements SET free_scans_used = free_scans_used + 1 WHERE id = 1")
    suspend fun incrementFreeScansUsed(): Int
    
    @Query("DELETE FROM entitlements")
    suspend fun resetAll()
}
