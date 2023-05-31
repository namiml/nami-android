package com.namiml.demo.basicpaywallmgmt

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState
import com.android.billingclient.api.PurchasesUpdatedListener
import com.namiml.Nami
import com.namiml.app.test.LOG_TAG
import com.namiml.paywall.NamiPaywallManager
import com.namiml.paywall.NamiPurchaseSource
import com.namiml.paywall.NamiSKU
import com.namiml.paywall.model.NamiPurchaseSuccess
import java.util.Date
import kotlin.math.pow

/**
 * Monitors the state of the connecting between the app client and Google Play Billing Services
 */

private const val BASE_DELAY: Double = 2.0

internal object GooglePlayBillingHelper {

    private var isAvailable: Boolean = false

    private lateinit var billingClient: BillingClient
    private val googlePlayBillingClientListener = GooglePlayBillingClientListener {
        this.isAvailable = it
        // Populate purchase history with records from Google Play Billing
    }

    /**
     * A connection to Google Play Billing is required before doing any billing operations
     */
    fun connectToGooglePlay(context: Context, purchaseListener: GooglePlayPurchaseListener) {
        if (Nami.purchaseManagementEnabled() == true) {
//            Log.d(LOG_TAG, "Nami SDK is handling connecting to Google Play.")
            return
        }

        if (this::billingClient.isInitialized) {
            billingClient.endConnection()
        }
        Log.d(LOG_TAG, "Starting Google Play Billing connection process")

        billingClient = BillingClient.newBuilder(context)
            .enablePendingPurchases()
            .setListener(purchaseListener).build().also {
                purchaseListener.billingClient = it
                purchaseListener.context = context
            }
        billingClient.startConnection(googlePlayBillingClientListener)
    }
}
internal class GooglePlayBillingClientListener(
    val callback: (Boolean) -> Unit,
) : BillingClientStateListener {

    private var attemptCount: Double = 1.0
    private val handler = Handler(Looper.getMainLooper())

    override fun onBillingSetupFinished(result: BillingResult) {
        val isConnected = result.responseCode == BillingClient.BillingResponseCode.OK

        if (isConnected) {
            attemptCount = 1.0 // Reset to original value
            Log.d(LOG_TAG, "Connected to Google Play Billing")
        } else {
            Log.d(
                LOG_TAG,
                "Could not connect to Google Play Billing - Error Code: ${result.responseCode}",
            )

            if (attemptCount <= 7.0) {
                Log.d(
                    LOG_TAG,
                    "Making attempt #${attemptCount.toInt()} to connect to Google Play Billing",
                )

                handler.postDelayed(
                    {
                        attemptCount++
                        callback.invoke(false)
                    },
                    ((BASE_DELAY.pow(attemptCount)) * 1000).toLong(),
                )
            }
        }

        callback.invoke(isConnected)
    }

    override fun onBillingServiceDisconnected() {
        Log.d(LOG_TAG, "Nami is disconnected from Google Play Billing. Launching retry...")
        callback.invoke(false)

        // Begin process to re-establish connection to Google Play Billing.
        val serviceDisconnectedResult = BillingResult.newBuilder()
            .setResponseCode(BillingClient.BillingResponseCode.SERVICE_DISCONNECTED).build()
        onBillingSetupFinished(serviceDisconnectedResult)
    }
}

/**
 * Process post purchase events emitted by Google Play Billing Client
 */
internal class GooglePlayPurchaseListener : PurchasesUpdatedListener {
    var billingClient: BillingClient? = null
    var context: Context? = null
    var paywall: Activity? = null
    var sku: NamiSKU? = null

    private fun handlePurchase(purchase: Purchase) {
        if (Nami.purchaseManagementEnabled() == true) {
            Log.d(
                LOG_TAG,
                "Nami SDK has purchase management is enabled, so not handling this purchase here.",
            )
            return
        }

        if (purchase.purchaseState === PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                billingClient?.acknowledgePurchase(acknowledgePurchaseParams.build()) {
                    if (it.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d(LOG_TAG, "Purchase Acknowledged")

                        if (sku != null && paywall != null) {
                            val namiSku = sku
                            val purchaseDate = Date(purchase.purchaseTime)
                            val purchaseSuccess = NamiPurchaseSuccess.GooglePlay(
                                product = namiSku!!,
                                purchaseDate = purchaseDate,
                                expiresDate = null,
                                description = null,
                                orderId = purchase.orderId,
                                purchaseSource = NamiPurchaseSource.CAMPAIGN,
                                purchaseToken = purchase.purchaseToken,
                            )
                            NamiPaywallManager.buySkuComplete(paywall!!, purchaseSuccess)
                        }
                    }
                }
            }
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (Nami.purchaseManagementEnabled() == true) {
            Log.d(
                LOG_TAG,
                "Nami SDK has purchase management is enabled, so ignoring onPurchasesUpdated.",
            )
            return
        }

        Log.d(LOG_TAG, "onPurchasesUpdated's responseCode = ${result.responseCode}")

        if (result.debugMessage.isNotEmpty()) {
            Log.d(
                LOG_TAG,
                "PostPurchaseListener - onPurchasesUpdated's " +
                    "debugMessage = ${result.debugMessage}",
            )
        }

        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (result.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            // Handle any other error codes.
            Log.d(LOG_TAG, "GooglePlayPurchaseListener - ${result.responseCode}")
        }
    }
}