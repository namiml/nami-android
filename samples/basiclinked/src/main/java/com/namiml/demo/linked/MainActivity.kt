package com.namiml.demo.linked

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.namiml.billing.NamiPurchase
import com.namiml.billing.NamiPurchaseManager
import com.namiml.billing.NamiPurchaseState
import com.namiml.demo.linked.databinding.ActivityMainBinding
import com.namiml.entitlement.NamiEntitlement
import com.namiml.entitlement.NamiEntitlementManager
import com.namiml.ml.NamiMLManager
import com.namiml.paywall.NamiPaywallManager
import com.namiml.paywall.PreparePaywallResult

private const val THROTTLED_CLICK_DELAY = 500L // in millis

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.aboutButton.onThrottledClick {
            startActivity(AboutActivity.getIntent(this))
        }
        binding.subscriptionButton.onThrottledClick {
            NamiMLManager.coreAction("subscribe")
            NamiPaywallManager.preparePaywallForDisplay { result ->
                when (result) {
                    is PreparePaywallResult.Success -> {
                        NamiPaywallManager.raisePaywall(this)
                    }
                    is PreparePaywallResult.Failure -> {
                        Log.d(LOG_TAG, "preparePaywallForDisplay Error -> ${result.error}")
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // This is to register entitlement change listener during lifecycle of this activity
        NamiEntitlementManager.registerEntitlementChangeListener { activeEntitlements ->
            Log.d(LOG_TAG, "EntitlementChangeListener triggered")
            logActiveEntitlements(activeEntitlements)
            handleActiveEntitlements(activeEntitlements)
        }

        // This is to register purchase change listener during lifecycle of this activity
        NamiPurchaseManager.registerPurchasesChangedListener { purchases, state, error ->
            Log.d(LOG_TAG, "PurchasesChangedHandler triggered")
            evaluateLastPurchaseEvent(purchases, state, error)
        }

        // This is to check for active entitlements on app resume to take any action if you want
        handleActiveEntitlements(NamiEntitlementManager.activeEntitlements())
    }

    private fun logActiveEntitlements(activeEntitlements: List<NamiEntitlement>) {
        if (activeEntitlements.isNotEmpty()) {
            Log.d(LOG_TAG, "Active entitlements")
            for (ent in activeEntitlements) {
                Log.d(LOG_TAG, "\tName: " + ent.name)
                Log.d(LOG_TAG, "\tReferenceId: " + ent.referenceId)
            }
        } else {
            Log.d(LOG_TAG, "No active entitlements")
        }
    }

    override fun onPause() {
        super.onPause()
        NamiEntitlementManager.registerEntitlementChangeListener(null)
        NamiPurchaseManager.registerPurchasesChangedListener(null)
    }

    // If at least one entitlement is active, then show text on UI as active
    private fun handleActiveEntitlements(activeEntitlements: List<NamiEntitlement>) {
        var isActive = false
        var textResId = R.string.entitlement_status_inactivate
        if (activeEntitlements.isNotEmpty()) {
            isActive = true
            textResId = R.string.entitlement_status_active
        }
        binding.subscriptionStatus.apply {
            text = getText(textResId)
            isEnabled = isActive
        }
    }

    private fun evaluateLastPurchaseEvent(
        activePurchases: List<NamiPurchase>,
        namiPurchaseState: NamiPurchaseState,
        errorMsg: String?
    ) {
        Log.d(LOG_TAG, "Purchase State ${namiPurchaseState.name}")
        when (namiPurchaseState) {
            NamiPurchaseState.PURCHASED -> {
                Log.d(LOG_TAG, "\nActive Purchases: ")
                activePurchases.forEachIndexed { index, activePurchase ->
                    Log.d(LOG_TAG, "index $index")
                    Log.d(LOG_TAG, activePurchase.toString())
                }
            }
            NamiPurchaseState.PENDING -> {
                if (activePurchases.any { it.skuId == IAP_SKU }) {
                    Log.d(LOG_TAG, "Found a pending consumable! Consuming now!")
                    NamiPurchaseManager.consumePurchasedSKU(IAP_SKU)
                }

            }
            else -> Log.d(LOG_TAG, "Reason : ${errorMsg ?: "Unknown"}")
        }
    }
}

fun View.onThrottledClick(invokeWhenClicked: () -> Unit) {
    setOnClickListener {
        this.isClickable = false
        this.postDelayed({
            this.isClickable = true
        }, THROTTLED_CLICK_DELAY)
        invokeWhenClicked()
    }
}