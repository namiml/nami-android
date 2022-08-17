package com.namiml.demo.basic

import android.app.Application
import android.util.Log
import android.widget.Toast
import com.namiml.Nami
import com.namiml.NamiConfiguration

import com.namiml.NamiLogLevel
import com.namiml.customer.NamiCustomerManager
import com.namiml.paywall.NamiPaywallManager

const val TEST_EXTERNAL_IDENTIFIER = "9a9999a9-99aa-99a9-aa99-999a999999a9"
const val LOG_TAG = "DemoBasic"
const val IAP_SKU = "com.namiml.basic.demo.inapp.donate"

class BasicApplication : Application() {

    companion object {
        private const val NAMI_APP_PLATFORM_ID = "3d062066-9d3c-430e-935d-855e2c56dd8e"
    }

    override fun onCreate() {
        super.onCreate()

        Nami.configure(
            NamiConfiguration.build(this, "3d062066-9d3c-430e-935d-855e2c56dd8e") {
                logLevel = NamiLogLevel.DEBUG.takeIf { BuildConfig.DEBUG } ?: NamiLogLevel.ERROR
                // developmentMode = true
                // bypassStore = true
                // namiLanguageCode = NamiLanguageCode.EN
            }
        )

        NamiPaywallManager.registerSignInHandler { context ->
            Toast.makeText(context, "Sign in clicked", Toast.LENGTH_SHORT).show()
            // Once user signs in, you may provide unique identifier that can be used to link
            // different devices to the same customer in the Nami platform.
            // Here at this stage, since we don't have real sign in flow in this demo app, we're
            // just setting this test identifier when the sign-in button is pressed on paywall
            NamiCustomerManager.login(TEST_EXTERNAL_IDENTIFIER)
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

        NamiPaywallManager.renderCustomUiHandler { _, namiPaywall, skus ->
            Log.d(LOG_TAG, "calling linked paywall")
        }
    }
}
