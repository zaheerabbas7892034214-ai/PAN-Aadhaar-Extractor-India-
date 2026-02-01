package com.panaadhaar.extractor.india.data.repository

import android.content.SharedPreferences
import com.panaadhaar.extractor.india.data.database.EntitlementDao
import com.panaadhaar.extractor.india.data.database.EntitlementEntity
import com.panaadhaar.extractor.india.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EntitlementRepository(
    private val entitlementDao: EntitlementDao,
    private val sharedPreferences: SharedPreferences
) {
    
    suspend fun initializeEntitlement() {
        val existing = entitlementDao.getEntitlementOnce()
        if (existing == null) {
            val isPro = sharedPreferences.getBoolean(Constants.PREF_IS_PRO, false)
            val freeScansUsed = sharedPreferences.getInt(Constants.PREF_FREE_SCANS_USED, 0)
            val purchaseToken = sharedPreferences.getString(Constants.PREF_PURCHASE_TOKEN, null)
            
            entitlementDao.upsert(
                EntitlementEntity(
                    id = 1,
                    isPro = isPro,
                    freeScansUsed = freeScansUsed,
                    purchaseToken = purchaseToken
                )
            )
        }
    }
    
    fun getEntitlementFlow(): Flow<EntitlementEntity> {
        return entitlementDao.getEntitlement().map { entity ->
            entity ?: EntitlementEntity(id = 1, isPro = false, freeScansUsed = 0)
        }
    }
    
    suspend fun isPro(): Boolean {
        val entitlement = entitlementDao.getEntitlementOnce()
        val isPro = entitlement?.isPro ?: false
        
        sharedPreferences.edit()
            .putBoolean(Constants.PREF_IS_PRO, isPro)
            .apply()
        
        return isPro
    }
    
    suspend fun getFreeScansUsed(): Int {
        val entitlement = entitlementDao.getEntitlementOnce()
        val freeScansUsed = entitlement?.freeScansUsed ?: 0
        
        sharedPreferences.edit()
            .putInt(Constants.PREF_FREE_SCANS_USED, freeScansUsed)
            .apply()
        
        return freeScansUsed
    }
    
    suspend fun incrementFreeScan() {
        val updated = entitlementDao.incrementFreeScansUsed()
        if (updated > 0) {
            val newCount = getFreeScansUsed()
            sharedPreferences.edit()
                .putInt(Constants.PREF_FREE_SCANS_USED, newCount)
                .apply()
        }
    }
    
    suspend fun unlockPro(token: String) {
        entitlementDao.upsert(
            EntitlementEntity(
                id = 1,
                isPro = true,
                freeScansUsed = getFreeScansUsed(),
                purchaseToken = token
            )
        )
        
        sharedPreferences.edit()
            .putBoolean(Constants.PREF_IS_PRO, true)
            .putString(Constants.PREF_PURCHASE_TOKEN, token)
            .apply()
    }
    
    suspend fun clearAllData() {
        entitlementDao.resetAll()
        
        sharedPreferences.edit()
            .putBoolean(Constants.PREF_IS_PRO, false)
            .putInt(Constants.PREF_FREE_SCANS_USED, 0)
            .remove(Constants.PREF_PURCHASE_TOKEN)
            .apply()
        
        initializeEntitlement()
    }
}
