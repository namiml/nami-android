package com.namiml.demo.basic

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.namiml.billing.NamiPurchase
import com.namiml.billing.NamiPurchaseManager
import com.namiml.billing.NamiPurchaseState
import com.namiml.customer.NamiCustomerManager
import com.namiml.demo.basic.databinding.ActivityMainBinding
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
            Log.d(LOG_TAG, "Entitlements Change Listener triggered")
            logActiveEntitlements(activeEntitlements)
            handleActiveEntitlements(activeEntitlements)
        }

        // This is to register purchase change listener during lifecycle of this activity
        NamiPurchaseManager.registerPurchasesChangedListener { purchases, state, error ->
            evaluateLastPurchaseEvent(purchases, state, error)
        }

        handleActiveEntitlements(NamiEntitlementManager.activeEntitlements())

        NamiCustomerManager.currentCustomerJourneyState()?.let {
            Log.d(LOG_TAG, "currentCustomerJourneyState")
            Log.d(LOG_TAG, "formerSubscriber ==> ${it.formerSubscriber}")
            Log.d(LOG_TAG, "inGracePeriod ==> ${it.inGracePeriod}")
            Log.d(LOG_TAG, "inIntroOfferPeriod ==> ${it.inIntroOfferPeriod}")
            Log.d(LOG_TAG, "inTrialPeriod ==> ${it.inTrialPeriod}")
        }
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

    // If at least one entitlement is active, then show text on UI as active
    private fun handleActiveEntitlements(activeEntitlements: List<NamiEntitlement>) {
        var isActive = false
        var textResId = R.string.subscription_status_inactivate
        if (activeEntitlements.isNotEmpty()) {
            isActive = true
            textResId = R.string.subscription_status_active
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
        if (namiPurchaseState == NamiPurchaseState.PURCHASED) {
            Log.d(LOG_TAG, "\nActive Purchases: ")
            for (pur in activePurchases) {
                Log.d(LOG_TAG, "\tSkuId: ${pur.skuId}")
            }
        } else {
            Log.d(LOG_TAG, "Reason : ${errorMsg ?: "Unknown"}")
        }
    }

    private fun View.onThrottledClick(invokeWhenClicked: () -> Unit) {
        setOnClickListener {
            this.isClickable = false
            this.postDelayed({
                this.isClickable = true
            }, THROTTLED_CLICK_DELAY)
            invokeWhenClicked()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (NamiPaywallManager.didUserCloseBlockingNamiPaywall(requestCode, resultCode)) {
            val msg = "User closed a blocking paywall"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }
}