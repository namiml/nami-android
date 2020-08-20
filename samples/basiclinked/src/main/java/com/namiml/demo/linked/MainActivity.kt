package com.namiml.demo.linked

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.namiml.billing.NamiPurchase
import com.namiml.billing.NamiPurchaseManager
import com.namiml.billing.NamiPurchaseState
import com.namiml.customer.NamiCustomerManager
import com.namiml.demo.linked.databinding.ActivityMainBinding
import com.namiml.entitlement.NamiEntitlement
import com.namiml.entitlement.NamiEntitlementManager
import com.namiml.ml.NamiMLManager
import com.namiml.paywall.NamiPaywallManager

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
            NamiMLManager.coreAction(listOf("subscribe"))
            if (NamiPaywallManager.canRaisePaywall()) {
                NamiPaywallManager.raisePaywall(this)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        NamiEntitlementManager.registerEntitlementChangeListener { entitlements ->
            evaluateEntitlements(entitlements)
        }

        NamiPurchaseManager.registerPurchasesChangedHandler { purchases, state, error ->
            evaluateLastPurchaseEvent(purchases, state, error)
        }

        // If at least one entitlement is enabled, make this an active subscription
        evaluateEntitlements(NamiEntitlementManager.getEntitlements())

        NamiCustomerManager.currentCustomerJourneyState()?.let {
            Log.d(LOG_TAG, "currentCustomerJourneyState")
            Log.d(LOG_TAG, "formerSubscriber ==> ${it.formerSubscriber}")
            Log.d(LOG_TAG, "inGracePeriod ==> ${it.inGracePeriod}")
            Log.d(LOG_TAG, "inIntroOfferPeriod ==> ${it.inIntroOfferPeriod}")
            Log.d(LOG_TAG, "inTrialPeriod ==> ${it.inTrialPeriod}")
        }
    }

    override fun onPause() {
        super.onPause()
        NamiEntitlementManager.registerEntitlementChangeListener(null)
        NamiPurchaseManager.registerPurchasesChangedHandler(null)
    }

    private fun evaluateEntitlements(entitlements: List<NamiEntitlement>) {
        entitlements.any { it.isActive() }.let { isActive ->
            binding.subscriptionStatus.apply {
                text = getText(
                    R.string.subscription_status_active.takeIf { isActive }
                        ?: R.string.subscription_status_inactivate
                )
                isEnabled = isActive
            }
        }
    }

    private fun evaluateLastPurchaseEvent(
        activePurchases: List<NamiPurchase>,
        namiPurchaseState: NamiPurchaseState,
        errorMsg: String?
    ) {
        when (namiPurchaseState) {
            NamiPurchaseState.PURCHASED -> {
                Log.d(LOG_TAG, "Enable access to entitlement now")

                val activeEntitlements = NamiEntitlementManager.activeEntitlements()
                Log.d(LOG_TAG, "Active Entitlements: ")
                for (ent in activeEntitlements) {
                    Log.d(LOG_TAG, "\tName: " + ent.name)
                    Log.d(LOG_TAG, "\tReferenceId: " + ent.referenceId)
                }

                Log.d(LOG_TAG, "\nActive Purchases: ")
                for (pur in activePurchases) {
                    Log.d(LOG_TAG, "\tSkuId: ${pur.skuId}")
                }
            }
            NamiPurchaseState.CANCELLED -> {
                Log.d(
                    LOG_TAG,
                    "User decided to not proceed with purchase. " +
                            "Do not enable access to entitlement"
                )
                Log.d(LOG_TAG, "Reason : ${errorMsg ?: "Unknown"}")
            }
            NamiPurchaseState.UNKNOWN -> {
                printNoAction()
            }
            else -> {
                printNoAction()
            }
        }
    }

    private fun printNoAction() {
        val msg = "Purchase Event was dispatched but there is no corresponding action " +
                "to take at this time"
        Log.d(LOG_TAG, msg)
    }

    private fun View.onThrottledClick(invokeWhenClicked: () -> Unit) {
        setOnClickListener {
            this.isClickable = false
            this.postDelayed({
                this.isClickable = true
            }, 500)
            invokeWhenClicked()
        }
    }
}