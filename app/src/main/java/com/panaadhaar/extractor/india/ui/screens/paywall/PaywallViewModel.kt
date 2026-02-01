package com.panaadhaar.extractor.india.ui.screens.paywall

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panaadhaar.extractor.india.data.repository.BillingRepository
import com.panaadhaar.extractor.india.data.repository.EntitlementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PaywallViewModel(
    private val billingRepository: BillingRepository,
    private val entitlementRepository: EntitlementRepository
) : ViewModel() {

    sealed class BillingState {
        object Idle : BillingState()
        object Loading : BillingState()
        object PurchaseInProgress : BillingState()
        object Success : BillingState()
        data class Error(val message: String) : BillingState()
        object Cancelled : BillingState()
    }

    private val _billingState = MutableStateFlow<BillingState>(BillingState.Idle)
    val billingState: StateFlow<BillingState> = _billingState.asStateFlow()

    init {
        setupBilling()
        observePurchaseResults()
    }

    private fun setupBilling() {
        viewModelScope.launch {
            _billingState.value = BillingState.Loading

            try {
                val connected = billingRepository.startConnection()
                if (connected) {
                    val productsLoaded = billingRepository.queryProducts()
                    if (productsLoaded) {
                        _billingState.value = BillingState.Idle
                    } else {
                        _billingState.value = BillingState.Error("Failed to load products")
                    }
                } else {
                    _billingState.value = BillingState.Error("Failed to connect to billing service")
                }
            } catch (e: Exception) {
                _billingState.value = BillingState.Error(
                    e.message ?: "Billing setup failed"
                )
            }
        }
    }

    private fun observePurchaseResults() {
        viewModelScope.launch {
            billingRepository.purchaseResult.collect { result ->
                when (result) {
                    is BillingRepository.PurchaseResult.Success -> {
                        _billingState.value = BillingState.Success
                    }
                    is BillingRepository.PurchaseResult.Error -> {
                        _billingState.value = BillingState.Error(result.message)
                    }
                    is BillingRepository.PurchaseResult.Cancelled -> {
                        _billingState.value = BillingState.Cancelled
                    }
                    is BillingRepository.PurchaseResult.Pending -> {
                        _billingState.value = BillingState.PurchaseInProgress
                    }
                    null -> {}
                }
            }
        }
    }

    fun initiatePurchase(activity: Activity) {
        viewModelScope.launch {
            _billingState.value = BillingState.PurchaseInProgress

            try {
                val launched = billingRepository.launchPurchaseFlow(activity)
                if (!launched) {
                    _billingState.value = BillingState.Error("Failed to launch purchase flow")
                }
            } catch (e: Exception) {
                _billingState.value = BillingState.Error(
                    e.message ?: "Failed to initiate purchase"
                )
            }
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            _billingState.value = BillingState.Loading

            try {
                val restored = billingRepository.restorePurchases()
                if (restored) {
                    _billingState.value = BillingState.Success
                } else {
                    _billingState.value = BillingState.Error("No purchases found to restore")
                }
            } catch (e: Exception) {
                _billingState.value = BillingState.Error(
                    e.message ?: "Failed to restore purchases"
                )
            }
        }
    }

    fun resetState() {
        _billingState.value = BillingState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        billingRepository.disconnect()
    }
}
