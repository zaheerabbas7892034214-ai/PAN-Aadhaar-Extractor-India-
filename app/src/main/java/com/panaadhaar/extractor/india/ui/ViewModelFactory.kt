package com.panaadhaar.extractor.india.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.panaadhaar.extractor.india.data.repository.BillingRepository
import com.panaadhaar.extractor.india.data.repository.EntitlementRepository
import com.panaadhaar.extractor.india.data.repository.ProfileRepository
import com.panaadhaar.extractor.india.ui.screens.camera.CameraViewModel
import com.panaadhaar.extractor.india.ui.screens.export.ExportViewModel
import com.panaadhaar.extractor.india.ui.screens.home.HomeViewModel
import com.panaadhaar.extractor.india.ui.screens.paywall.PaywallViewModel
import com.panaadhaar.extractor.india.ui.screens.profiles.ProfilesViewModel
import com.panaadhaar.extractor.india.ui.screens.results.ResultsViewModel
import com.panaadhaar.extractor.india.ui.screens.settings.SettingsViewModel

class ViewModelFactory(
    private val application: Application,
    private val profileRepository: ProfileRepository,
    private val entitlementRepository: EntitlementRepository,
    private val billingRepository: BillingRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(profileRepository, entitlementRepository) as T
            }
            modelClass.isAssignableFrom(CameraViewModel::class.java) -> {
                CameraViewModel(application, entitlementRepository) as T
            }
            modelClass.isAssignableFrom(ResultsViewModel::class.java) -> {
                ResultsViewModel(profileRepository, entitlementRepository) as T
            }
            modelClass.isAssignableFrom(ProfilesViewModel::class.java) -> {
                ProfilesViewModel(profileRepository) as T
            }
            modelClass.isAssignableFrom(ExportViewModel::class.java) -> {
                ExportViewModel(application, profileRepository, entitlementRepository) as T
            }
            modelClass.isAssignableFrom(PaywallViewModel::class.java) -> {
                PaywallViewModel(billingRepository, entitlementRepository) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(profileRepository, entitlementRepository, billingRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
