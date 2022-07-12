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
import com.namiml.campaign.LaunchCampaignResult
import com.namiml.campaign.NamiCampaignManager
import com.namiml.customer.NamiCustomerManager
import com.namiml.demo.basic.databinding.ActivityMainBinding
import com.namiml.entitlement.NamiEntitlement
import com.namiml.entitlement.NamiEntitlementManager
import com.namiml.ml.NamiMLManager
import com.namiml.paywall.NamiPaywallManager

private const val THROTTLED_CLICK_DELAY = 500L // in millis

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(LOG_TAG, "MainActivity.kt - onCreate")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.aboutButton.onThrottledClick {
            startActivity(AboutActivity.getIntent(this))
        }

        binding.refreshButton.onThrottledClick {
            refresh()
        }

        binding.loginLogoutButton.onThrottledClick {
            loginLogout()
        }

        binding.loginLogoutButton.text = if (NamiCustomerManager.isLoggedIn()) {
            "Logout"
        } else {
            "Login"
        }

        binding.subscriptionButton.onThrottledClick {
            NamiMLManager.coreAction("subscribe")

            NamiCampaignManager.launch(this) { result ->
                when (result) {
                    is LaunchCampaignResult.Success -> {
                        Log.d(LOG_TAG, "Launch Campaign Success")
                    }
                    is LaunchCampaignResult.Failure -> {
                        Log.d(LOG_TAG, "Launch Campaign Error -> ${result.error}")
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        Log.d(LOG_TAG, "MainActivity.kt - onResume")

        // This is to register entitlement change listener during lifecycle of this activity
        NamiEntitlementManager.registerChangeListener { activeEntitlements ->
            Log.d(LOG_TAG, "Entitlements Change Listener triggered")
            logActiveEntitlements(activeEntitlements)
            handleActiveEntitlements(activeEntitlements)
        }

        // This is to register purchase change listener during lifecycle of this activity
        NamiPurchaseManager.registerPurchasesChangedListener { purchases, state, error ->
            Log.d(LOG_TAG, "Purchases Change Listener triggered")
            evaluateLastPurchaseEvent(purchases, state, error)
        }
        handleActiveEntitlements(NamiEntitlementManager.active())
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
        var textResId = R.string.entitlement_status_inactivate
        if (activeEntitlements.isNotEmpty()) {
            isActive = true
            textResId = R.string.entitlement_status_active
        }
        binding.subscriptionStatus.apply {
            this@MainActivity.runOnUiThread(
                java.lang.Runnable {
                    text = getText(textResId)
                    isEnabled = isActive
                }
            )
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

    private fun View.onThrottledClick(invokeWhenClicked: () -> Unit) {
        setOnClickListener {
            this.isClickable = false
            this.postDelayed({
                this.isClickable = true
            }, THROTTLED_CLICK_DELAY)
            invokeWhenClicked()
        }
    }

    private fun refresh() {
        Log.d(LOG_TAG, "Calling NamiEntitlementManager.refresh()")
        NamiEntitlementManager.refresh() { activeEntitlements ->
            if (!activeEntitlements.isNullOrEmpty()) {
                logActiveEntitlements(activeEntitlements)
                handleActiveEntitlements(activeEntitlements)
            }
        }
    }

    private fun loginLogout() {
        Log.d(LOG_TAG, "Is logged in: ${NamiCustomerManager.loggedInId()}")

        if (NamiCustomerManager.isLoggedIn()) {
            val msg = "Logging out device from external id"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            NamiCustomerManager.logout()
            binding.loginLogoutButton.text = "Login"
        } else {
//            TODO: use the AppSet ID instead of a hard coded one for the test app
//            val client = AppSet.getClient(applicationContext) as AppSetIdClient
//            val task: Task<AppSetIdInfo> = client.appSetIdInfo as Task<AppSetIdInfo>
//
//            task.addOnSuccessListener({
//                // Determine current scope of app set ID.
//                val scope: Int = it.scope
//
//                // Read app set ID value, which uses version 4 of the
//                // universally unique identifier (UUID) format.
//                val id: String = it.id
//
//                val msg = "Logging in device to ${id}"
//                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
//
//                NamiCustomerManager.login(id)
//                binding.loginLogoutButton.text = "Logout"
//
//            })

            val msg = "Logging in device to $TEST_EXTERNAL_IDENTIFIER"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            NamiCustomerManager.login(TEST_EXTERNAL_IDENTIFIER)
            binding.loginLogoutButton.text = "Logout"
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
