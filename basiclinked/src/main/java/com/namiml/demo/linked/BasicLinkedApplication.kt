package com.namiml.demo.linked

import android.app.Application
import android.content.Intent
import android.widget.Toast
import com.namiml.Nami
import com.namiml.NamiConfiguration
import com.namiml.NamiLogLevel
import com.namiml.NamiPaywallManager
import com.namiml.entitlement.NamiEntitlement
import com.namiml.entitlement.NamiEntitlementManager
import com.namiml.entitlement.NamiEntitlementPlatform

class BasicLinkedApplication : Application() {

    companion object {
        private const val NAMI_APP_ID = "eea3721d-a13f-4a94-872a-3c2b795953da"
    }

    override fun onCreate() {
        super.onCreate()
        Nami.configure(NamiConfiguration.build(this, NAMI_APP_ID) {
            namiLogLevel = NamiLogLevel.DEBUG.takeIf { BuildConfig.DEBUG } ?: NamiLogLevel.ERROR
        })

        NamiPaywallManager.registerApplicationSignInProvider { context, paywallData, developerPaywallID ->
            Toast.makeText(context, "Sign in clicked", Toast.LENGTH_SHORT).show()
        }

        NamiPaywallManager.registerApplicationPaywallProvider { context, paywallData, products, developerPaywallId ->
            val productsArray = ArrayList(products?.toMutableList() ?: mutableListOf())
            startActivity(
                PaywallActivity.getIntent(
                    context,
                    paywallData,
                    productsArray,
                    developerPaywallId
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            )
        }

        // Entitlements
        NamiEntitlementManager.setEntitlements(
            listOf(
                NamiEntitlement("premium_content", NamiEntitlementPlatform.WEB),
                NamiEntitlement("bonus_pack", NamiEntitlementPlatform.ROKU)
            )
        )
    }
}