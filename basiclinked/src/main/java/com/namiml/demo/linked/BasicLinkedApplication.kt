package com.namiml.demo.linked

import android.app.Application
import android.content.Intent
import android.widget.Toast
import com.namiml.Nami
import com.namiml.NamiConfiguration
import com.namiml.NamiLogLevel
import com.namiml.NamiPaywallManager

class BasicLinkedApplication : Application() {

    companion object {
        private const val NAMI_APP_ID = "54635e21-87ed-4ed6-9119-9abb493bc9b0"
    }

    override fun onCreate() {
        super.onCreate()
        Nami.configure(NamiConfiguration.build(this, NAMI_APP_ID) {
            developmentMode = true
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
    }
}