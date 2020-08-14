package com.namiml.demo.basic.java;

import android.app.Application;
import android.widget.Toast;

import com.namiml.Nami;
import com.namiml.NamiConfiguration;
import com.namiml.NamiExternalIdentifierType;
import com.namiml.NamiLogLevel;
import com.namiml.paywall.NamiPaywallManager;

import kotlin.Unit;

public class BasicApplication extends Application {

    public boolean allowAutoRaisingPaywall = true;
    private static final String TEST_EXTERNAL_IDENTIFIER = "9a9999a9-99aa-99a9-aa99-999a999999a9";
    private static final String NAMI_APP_PLATFORM_ID = "3d062066-9d3c-430e-935d-855e2c56dd8e";

    @Override
    public void onCreate() {
        super.onCreate();
        NamiConfiguration.Builder builder = new NamiConfiguration
                .Builder(this, NAMI_APP_PLATFORM_ID);
        // builder.bypassStore(true);
        // builder.developmentMode(true);

        if (BuildConfig.DEBUG) {
            builder.logLevel(NamiLogLevel.DEBUG);
        }

        Nami.configure(builder.build());

        NamiPaywallManager.registerApplicationSignInProvider((context, namiPaywall, uuid) -> {
            Toast.makeText(context, "Sign in clicked!", Toast.LENGTH_SHORT).show();
            // Once user signs in, you may provide unique identifier that can be used to link
            // different devices to the same customer in the Nami platform.
            // Here at this stage, since we don't have real sign in flow in this demo app, we're
            // just setting this test identifier when the sign-in button is pressed on paywall
            Nami.setExternalIdentifier(TEST_EXTERNAL_IDENTIFIER, NamiExternalIdentifierType.UUID);
            return Unit.INSTANCE;
        });

        NamiPaywallManager.registerApplicationAutoRaisePaywallBlocker(
                () -> allowAutoRaisingPaywall);
    }
}