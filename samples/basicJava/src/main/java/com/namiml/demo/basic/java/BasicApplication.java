package com.namiml.demo.basic;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.namiml.Nami;
import com.namiml.NamiConfiguration;
import com.namiml.NamiExternalIdentifierType;
import com.namiml.NamiLanguageCode;
import com.namiml.NamiLogLevel;
import com.namiml.customer.NamiCustomerManager;
import com.namiml.paywall.NamiPaywallManager;

import kotlin.Unit;

public class BasicApplication extends Application {

    private static final String TEST_EXTERNAL_IDENTIFIER = "9a9999a9-99aa-99a9-aa99-999a999999a9";
    private static final String NAMI_APP_PLATFORM_ID = "3d062066-9d3c-430e-935d-855e2c56dd8e";

    @Override
    public void onCreate() {
        super.onCreate();
        NamiConfiguration.Builder builder = new NamiConfiguration
                .Builder(this, BuildConfig.NAMI_APP_ID)
                .bypassStore(true)
                .namiLanguageCode(NamiLanguageCode.EN)

        if (BuildConfig.DEBUG) {
            builder.logLevel(NamiLogLevel.DEBUG);
        }

        Nami.configure(builder.build());

        NamiPaywallManager.registerSignInListener((context, namiPaywall, uuid) -> {
            Toast.makeText(context, "Sign in clicked!", Toast.LENGTH_SHORT).show();
            // Once user signs in, you may provide unique identifier that can be used to link
            // different devices to the same customer in the Nami platform.
            // Here at this stage, since we don't have real sign in flow in this demo app, we're
            // just setting this test identifier when the sign-in button is pressed on paywall
            Nami.setExternalIdentifier(TEST_EXTERNAL_IDENTIFIER, NamiExternalIdentifierType.UUID);
            return Unit.INSTANCE;
        });

        NamiCustomerManager.registerCustomerJourneyChangedListener((journeyState) -> {
            Log.d(LOG_TAG, "Customer journey state changed:");
            Log.d(LOG_TAG, "formerSubscriber ==>" + journeyState.formerSubscriber);
            Log.d(LOG_TAG, "inGracePeriod ==> " + journeyState.inGracePeriod);
            Log.d(LOG_TAG, "inIntroOfferPeriod ==> " + journeyState.inIntroOfferPeriod);
            Log.d(LOG_TAG, "inTrialPeriod ==> " + journeyState.inTrialPeriod);
            Log.d(LOG_TAG, "isCancelled ==> " + journeyState.isCancelled);
            Log.d(LOG_TAG, "inPause ==> " + journeyState.inPause);
            Log.d(LOG_TAG, "inAccountHold ==> " + journeyState.inAccountHold);
            return Unit.INSTANCE;
        });

        NamiPaywallManager.registerApplicationAutoRaisePaywallBlocker(() -> true);
    }
}