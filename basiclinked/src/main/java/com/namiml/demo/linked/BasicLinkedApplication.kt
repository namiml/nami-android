package com.namiml.demo.linked

import android.app.Application
import android.widget.Toast
import com.namiml.*
import com.namiml.entitlement.NamiEntitlement
import com.namiml.entitlement.NamiEntitlementManager
import com.namiml.entitlement.NamiEntitlementPlatform

class BasicLinkedApplication : Application() {

    companion object {
        private const val NAMI_APP_ID = "2dc699a5-43c6-4e3a-9166-957e1640741b"
    }
    override fun onCreate() {
        super.onCreate()
        Nami.configure(NamiConfiguration.build(this, NAMI_APP_ID) {
                namiLogLevel = NamiLogLevel.DEBUG.takeIf { BuildConfig.DEBUG } ?: NamiLogLevel.ERROR
            }
        )

        NamiPaywallManager.registerApplicationSignInProvider { context, paywallData, developerPaywallID ->
            Toast.makeText(context, "Sign in clicked", Toast.LENGTH_SHORT).show()
        }

        NamiPaywallManager.registerApplicationPaywallProvider { _, paywallData, products, developerPaywallId ->
            val productsArray = ArrayList(products?.toMutableList() ?: mutableListOf())
            startActivity(
                PaywallActivity.getIntent(
                    this,
                    paywallData,
                    productsArray,
                    developerPaywallId
                )
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