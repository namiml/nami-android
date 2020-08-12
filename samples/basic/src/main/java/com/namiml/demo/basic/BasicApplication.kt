package com.namiml.demo.basic

import android.app.Application
import android.widget.Toast
import com.namiml.Nami
import com.namiml.NamiConfiguration
import com.namiml.NamiExternalIdentifierType
import com.namiml.NamiLogLevel
import com.namiml.paywall.NamiPaywallManager

private const val TEST_EXTERNAL_IDENTIFIER = "9a9999a9-99aa-99a9-aa99-999a999999a9"

class BasicApplication : Application() {

    internal var allowAutoRaisingPaywall = true

    companion object {
        private const val NAMI_APP_PLATFORM_ID = "3d062066-9d3c-430e-935d-855e2c56dd8e"
    }

    override fun onCreate() {
        super.onCreate()
        Nami.configure(
            NamiConfiguration.build(this, NAMI_APP_PLATFORM_ID) {
                logLevel = NamiLogLevel.DEBUG.takeIf { BuildConfig.DEBUG } ?: NamiLogLevel.ERROR
                // developmentMode = true
                // bypassStore = true
            }
        )

        NamiPaywallManager.registerApplicationSignInProvider { context, paywallData, paywallId ->
            Toast.makeText(context, "Sign in clicked", Toast.LENGTH_SHORT).show()
            // Once user signs in, you may provide unique identifier that can be used to link
            // different devices to the same customer in the Nami platform.
            // Here at this stage, since we don't have real sign in flow in this demo app, we're
            // just setting this test identifier when the sign-in button is pressed on paywall
            Nami.setExternalIdentifier(TEST_EXTERNAL_IDENTIFIER, NamiExternalIdentifierType.UUID)
        }

        NamiPaywallManager.registerApplicationAutoRaisePaywallBlocker {
            return@registerApplicationAutoRaisePaywallBlocker allowAutoRaisingPaywall
        }
    }
}