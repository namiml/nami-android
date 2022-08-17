package com.namiml.demo.linked

import android.app.Application
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.namiml.Nami
import com.namiml.NamiConfiguration
import com.namiml.NamiLogLevel
import com.namiml.customer.NamiCustomerManager
import com.namiml.paywall.NamiPaywallManager

const val TEST_EXTERNAL_IDENTIFIER = "9a9999a9-99aa-99a9-aa99-999a999999a9"
const val LOG_TAG = "DemoLinked"
const val IS_DEVELOPMENT_MODE_ON = true
const val IAP_SKU = "com.namiml.linked.demo.inapp.donate"

class BasicLinkedApplication : Application() {

    companion object {
        private const val NAMI_APP_PLATFORM_ID = "a95cef52-35e0-4794-8755-577492c2d5d1"
    }

    override fun onCreate() {
        super.onCreate()
        Nami.configure(
            NamiConfiguration.build(this, NAMI_APP_PLATFORM_ID) {
                logLevel = NamiLogLevel.DEBUG.takeIf { BuildConfig.DEBUG } ?: NamiLogLevel.ERROR
                // developmentMode = IS_DEVELOPMENT_MODE_ON
                // bypassStore = true
            }
        )

        NamiPaywallManager.registerSignInHandler { cxt ->
            Toast.makeText(cxt, "Sign in clicked", Toast.LENGTH_SHORT).show()
            // Once user signs in, you may provide unique identifier that can be used to link
            // different devices to the same customer in the Nami platform.
            // Here at this stage, since we don't have real sign in flow in this demo app, we're
            // just setting this test identifier when the sign-in button is pressed on paywall
            NamiCustomerManager.login(TEST_EXTERNAL_IDENTIFIER)
        }

        NamiPaywallManager.renderCustomUiHandler { _, namiPaywall, skus ->
            namiPaywall.extraData?.let { extraDataFromPaywall ->
                for (entry in extraDataFromPaywall.entries) {
                    Log.d(LOG_TAG, "${entry.key} --> ${entry.value}")
                }
            }
            Log.d(LOG_TAG, "styleData ==> ${namiPaywall.styleData}")
            PaywallActivity.getIntent(this, namiPaywall, skus).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }.also { intent ->
                startActivity(intent)
            }
        }

        NamiCustomerManager.registerJourneyStateHandler { journeyState ->
            Log.d(LOG_TAG, "Customer journey state:")
            Log.d(LOG_TAG, "formerSubscriber ==> ${journeyState.formerSubscriber}")
            Log.d(LOG_TAG, "inGracePeriod ==> ${journeyState.inGracePeriod}")
            Log.d(LOG_TAG, "inIntroOfferPeriod ==> ${journeyState.inIntroOfferPeriod}")
            Log.d(LOG_TAG, "inTrialPeriod ==> ${journeyState.inTrialPeriod}")
            Log.d(LOG_TAG, "isCancelled ==> ${journeyState.isCancelled}")
            Log.d(LOG_TAG, "inPause ==> ${journeyState.inPause}")
            Log.d(LOG_TAG, "inAccountHold ==> ${journeyState.inAccountHold}")
        }
    }
}
