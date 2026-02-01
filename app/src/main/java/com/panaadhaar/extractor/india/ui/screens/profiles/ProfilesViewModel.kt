package com.panaadhaar.extractor.india.ui.screens.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panaadhaar.extractor.india.data.model.ExtractedData
import com.panaadhaar.extractor.india.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfilesViewModel(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    sealed class ProfilesState {
        object Loading : ProfilesState()
        data class Success(val profiles: List<ExtractedData>) : ProfilesState()
        data class Error(val message: String) : ProfilesState()
    }

    private val _profilesState = MutableStateFlow<ProfilesState>(ProfilesState.Loading)
    val profilesState: StateFlow<ProfilesState> = _profilesState.asStateFlow()

    init {
        loadProfiles()
    }

    fun loadProfiles() {
        viewModelScope.launch {
            _profilesState.value = ProfilesState.Loading

            try {
                profileRepository.getAllProfiles().collect { profiles ->
                    _profilesState.value = ProfilesState.Success(profiles)
                }
            } catch (e: Exception) {
                _profilesState.value = ProfilesState.Error(
                    e.message ?: "Failed to load profiles"
                )
            }
        }
    }

    suspend fun getProfileById(profileId: Long): ExtractedData? {
        return try {
            profileRepository.getProfileById(profileId)
        } catch (e: Exception) {
            null
        }
    }

    fun deleteProfile(profileId: Long) {
        viewModelScope.launch {
            try {
                profileRepository.deleteProfile(profileId)
            } catch (e: Exception) {
                _profilesState.value = ProfilesState.Error(
                    e.message ?: "Failed to delete profile"
                )
            }
        }
    }
}
