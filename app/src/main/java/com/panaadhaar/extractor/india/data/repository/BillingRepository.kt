package com.panaadhaar.extractor.india.data.repository

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.panaadhaar.extractor.india.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class BillingRepository(
    private val context: Context,
    private val entitlementRepository: EntitlementRepository,
    private val coroutineScope: CoroutineScope
) {
    
    private var billingClient: BillingClient? = null
    private var productDetails: ProductDetails? = null
    
    private val _purchaseResult = MutableStateFlow<PurchaseResult?>(null)
    val purchaseResult: StateFlow<PurchaseResult?> = _purchaseResult.asStateFlow()
    
    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            _purchaseResult.value = PurchaseResult.Cancelled
        } else {
            _purchaseResult.value = PurchaseResult.Error("Purchase failed: ${billingResult.debugMessage}")
        }
    }
    
    suspend fun startConnection(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            billingClient = BillingClient.newBuilder(context)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build()
            
            billingClient?.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        continuation.resume(true)
                    } else {
                        continuation.resume(false)
                    }
                }
                
                override fun onBillingServiceDisconnected() {
                    continuation.resume(false)
                }
            })
        }
    }
    
    suspend fun queryProducts(): Boolean {
        val client = billingClient ?: return false
        
        return suspendCancellableCoroutine { continuation ->
            val productList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(Constants.PRODUCT_ID_PRO)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )
            
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()
            
            client.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                    productDetails = productDetailsList[0]
                    continuation.resume(true)
                } else {
                    continuation.resume(false)
                }
            }
        }
    }
    
    suspend fun launchPurchaseFlow(activity: Activity): Boolean {
        val client = billingClient ?: return false
        val details = productDetails ?: return false
        
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .build()
        )
        
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        
        val billingResult = client.launchBillingFlow(activity, billingFlowParams)
        return billingResult.responseCode == BillingClient.BillingResponseCode.OK
    }
    
    private fun handlePurchase(purchase: Purchase) {
        when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                if (!purchase.isAcknowledged) {
                    acknowledgePurchase(purchase)
                } else {
                    unlockProFeatures(purchase.purchaseToken, coroutineScope)
                }
            }
            Purchase.PurchaseState.PENDING -> {
                _purchaseResult.value = PurchaseResult.Pending
            }
            Purchase.PurchaseState.UNSPECIFIED_STATE -> {
                _purchaseResult.value = PurchaseResult.Error("Purchase state unspecified")
            }
        }
    }
    
    private fun acknowledgePurchase(purchase: Purchase) {
        val client = billingClient ?: return
        
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        
        client.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                unlockProFeatures(purchase.purchaseToken, coroutineScope)
            } else {
                _purchaseResult.value = PurchaseResult.Error("Failed to acknowledge purchase")
            }
        }
    }
    
    private fun unlockProFeatures(token: String, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                entitlementRepository.unlockPro(token)
                _purchaseResult.value = PurchaseResult.Success
            } catch (e: Exception) {
                _purchaseResult.value = PurchaseResult.Error("Failed to unlock pro: ${e.message}")
            }
        }
    }
    
    suspend fun restorePurchases(): Boolean {
        val client = billingClient ?: return false
        
        return suspendCancellableCoroutine { continuation ->
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
            
            client.queryPurchasesAsync(params) { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    var foundPurchase = false
                    for (purchase in purchases) {
                        if (purchase.products.contains(Constants.PRODUCT_ID_PRO) && 
                            purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                            unlockProFeatures(purchase.purchaseToken, coroutineScope)
                            foundPurchase = true
                            break
                        }
                    }
                    continuation.resume(foundPurchase)
                } else {
                    continuation.resume(false)
                }
            }
        }
    }
    
    fun disconnect() {
        billingClient?.endConnection()
        billingClient = null
    }
    
    sealed class PurchaseResult {
        object Success : PurchaseResult()
        object Pending : PurchaseResult()
        object Cancelled : PurchaseResult()
        data class Error(val message: String) : PurchaseResult()
    }
}
