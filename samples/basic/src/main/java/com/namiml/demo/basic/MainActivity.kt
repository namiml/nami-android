package com.namiml.demo.basic

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.namiml.customer.NamiCustomerManager
import com.namiml.demo.basic.databinding.ActivityMainBinding
import com.namiml.entitlement.NamiEntitlementManager
import com.namiml.ml.NamiMLManager
import com.namiml.paywall.NamiPaywallManager

const val LOG_TAG = "DemoBasic"

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

        // If at least one entitlement is enabled, make this an active subscription
        NamiEntitlementManager.registerEntitlementChangeListener { entitlements ->
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
    }

    override fun onResume() {
        super.onResume()
        NamiCustomerManager.currentCustomerJourneyState()?.let {
            Log.d(LOG_TAG, "currentCustomerJourneyState")
            Log.d(LOG_TAG, "formerSubscriber ==> ${it.formerSubscriber}")
            Log.d(LOG_TAG, "inGracePeriod ==> ${it.inGracePeriod}")
            Log.d(LOG_TAG, "inIntroOfferPeriod ==> ${it.inIntroOfferPeriod}")
            Log.d(LOG_TAG, "inTrialPeriod ==> ${it.inTrialPeriod}")
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (NamiPaywallManager.didUserCloseBlockingNamiPaywall(requestCode, resultCode)) {
            Toast.makeText(this, "User closed the paywall", Toast.LENGTH_SHORT).show();
        }
    }
}