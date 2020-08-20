package com.namiml.demo.basic.java;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        setOnThrottledClick(binding.aboutButton, this::onAboutClicked);
        setOnThrottledClick(binding.subscriptionButton, this::onSubscriptionClicked);

        // If at least one entitlement is enabled, make this an active subscription
        NamiEntitlementManager.registerEntitlementChangeListener(namiEntitlements ->
                handleEntitlementChangeListener(namiEntitlements, binding));
    }

    @Override
    protected void onResume() {
        super.onResume();
        CustomerJourneyState customerJourneyState = NamiCustomerManager.currentCustomerJourneyState();
        if (customerJourneyState != null) {
            Log.d(LOG_TAG, "currentCustomerJourneyState");
            Log.d(LOG_TAG, "formerSubscriber ==> " + customerJourneyState.getFormerSubscriber());
            Log.d(LOG_TAG, "inGracePeriod ==> " + customerJourneyState.getInGracePeriod());
            Log.d(LOG_TAG, "inIntroOfferPeriod ==> " + customerJourneyState.getInIntroOfferPeriod());
            Log.d(LOG_TAG, "inTrialPeriod ==> " + customerJourneyState.getInTrialPeriod());
        }
    }

    private Unit handleEntitlementChangeListener(List<NamiEntitlement> namiEntitlements,
                                                 ActivityMainBinding binding) {
        if (!namiEntitlements.isEmpty()) {
            boolean atLeastOneActive = false;
            for (NamiEntitlement namiEntitlement : namiEntitlements) {
                if (namiEntitlement.isActive()) {
                    atLeastOneActive = true;
                    break;
                }
            }
            binding.subscriptionStatus.setEnabled(atLeastOneActive);
            if (atLeastOneActive) {
                binding.subscriptionStatus.setText(R.string.subscription_status_active);
            } else {
                binding.subscriptionStatus.setText(R.string.subscription_status_inactivate);
            }
        }
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
