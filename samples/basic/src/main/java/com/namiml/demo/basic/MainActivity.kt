package com.namiml.demo.basic

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.namiml.demo.basic.databinding.ActivityMainBinding
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