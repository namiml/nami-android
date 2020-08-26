package com.namiml.demo.basic.java;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Consumer;

import com.namiml.customer.CustomerJourneyState;
import com.namiml.customer.NamiCustomerManager;
import com.namiml.demo.basic.java.databinding.ActivityMainBinding;
import com.namiml.entitlement.NamiEntitlement;
import com.namiml.entitlement.NamiEntitlementManager;
import com.namiml.ml.NamiMLManager;
import com.namiml.paywall.NamiPaywallManager;

import java.util.Collections;
import java.util.List;

import kotlin.Unit;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "DemoBasicJava";
    ActivityMainBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        setOnThrottledClick(binding.aboutButton, this::onAboutClicked);
        setOnThrottledClick(binding.subscriptionButton, this::onSubscriptionClicked);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // This is to check for active entitlements on app resume to take any action if you want
        handleActiveEntitlements(NamiEntitlementManager.activeEntitlements());

        // This is to register entitlement change listener during lifecycle of this activity
        NamiEntitlementManager.registerEntitlementChangeListener(activeEntitlements -> {
            Log.d(LOG_TAG, "Entitlements Change Listener triggered");
            logActiveEntitlements(activeEntitlements);
            return handleActiveEntitlements(activeEntitlements);
        });

        logCustomerJourneyState();
    }

    private void logActiveEntitlements(@NonNull List<NamiEntitlement> activeEntitlements) {
        if (activeEntitlements.isEmpty()) {
            Log.d(LOG_TAG, "No active entitlements");
        } else {
            Log.d(LOG_TAG, "Active entitlements");
            for (NamiEntitlement activeEntitlement : activeEntitlements) {
                Log.d(LOG_TAG, "\tName: " + activeEntitlement.getName());
                Log.d(LOG_TAG, "\tReferenceId: " + activeEntitlement.getReferenceId());
            }
        }
    }

    private void logCustomerJourneyState() {
        CustomerJourneyState customerJourneyState = NamiCustomerManager.currentCustomerJourneyState();
        if (customerJourneyState != null) {
            Log.d(LOG_TAG, "currentCustomerJourneyState");
            Log.d(LOG_TAG, "formerSubscriber ==> " + customerJourneyState.getFormerSubscriber());
            Log.d(LOG_TAG, "inGracePeriod ==> " + customerJourneyState.getInGracePeriod());
            Log.d(LOG_TAG, "inIntroOfferPeriod ==> " + customerJourneyState.getInIntroOfferPeriod());
            Log.d(LOG_TAG, "inTrialPeriod ==> " + customerJourneyState.getInTrialPeriod());
        }
    }

    // If at least one entitlement is active, then show text on UI as active
    private Unit handleActiveEntitlements(List<NamiEntitlement> activeEntitlements) {
        int textResId = R.string.subscription_status_inactivate;
        boolean showAsActive = false;
        if (!activeEntitlements.isEmpty()) {
            showAsActive = true;
            textResId = R.string.subscription_status_active;
        }
        binding.subscriptionStatus.setEnabled(showAsActive);
        binding.subscriptionStatus.setText(textResId);
        return Unit.INSTANCE;
    }

    private void onSubscriptionClicked(Activity activity) {
        NamiMLManager.coreAction(Collections.singletonList("subscribe"));
        if (NamiPaywallManager.canRaisePaywall()) {
            NamiPaywallManager.raisePaywall(activity);
        }
    }

    private void setOnThrottledClick(View view, Consumer<Activity> consumer) {
        view.setOnClickListener(viewLocal -> {
            viewLocal.setClickable(false);
            viewLocal.postDelayed(() -> viewLocal.setClickable(true), 500);
            consumer.accept(MainActivity.this);
        });
    }

    private void onAboutClicked(Activity activity) {
        startActivity(AboutActivity.getIntent(activity));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (NamiPaywallManager.didUserCloseBlockingNamiPaywall(requestCode, resultCode)) {
            Toast.makeText(this, "User closed the paywall", Toast.LENGTH_SHORT).show();
        }
    }
}
