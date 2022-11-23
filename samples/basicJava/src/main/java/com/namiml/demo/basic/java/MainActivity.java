package com.namiml.demo.basic.java;

import static com.namiml.demo.basic.java.BasicApplication.LOG_TAG;

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

import com.namiml.billing.NamiPurchase;
import com.namiml.billing.NamiPurchaseManager;
import com.namiml.billing.NamiPurchaseState;
import com.namiml.campaign.NamiCampaignManager;
import com.namiml.demo.basic.java.databinding.ActivityMainBinding;
import com.namiml.entitlement.NamiEntitlement;
import com.namiml.entitlement.NamiEntitlementManager;
import com.namiml.ml.NamiMLManager;
import com.namiml.paywall.NamiPaywallManager;

import java.util.List;

import kotlin.Unit;

public class MainActivity extends AppCompatActivity {

    private static final long THROTTLED_CLICK_DELAY = 500L; // in millis
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

        // This is to register entitlement change listener during lifecycle of this activity
        NamiEntitlementManager.registerActiveEntitlementsHandler(activeEntitlements -> {
            Log.d(LOG_TAG, "Active Entitlements Listener triggered");
            logActiveEntitlements(activeEntitlements);
            handleActiveEntitlements(activeEntitlements);
            return Unit.INSTANCE;
        });

        NamiPurchaseManager.registerPurchasesChangedHandler((namiPurchases, namiPurchaseState, error) -> {
            evaluateLastPurchaseEvent(namiPurchases, namiPurchaseState, error);
            return Unit.INSTANCE;
        });

        // This is to check for active entitlements on app resume to take any action if you want
        handleActiveEntitlements(NamiEntitlementManager.active());
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

    // If at least one entitlement is active, then show text on UI as active
    private void handleActiveEntitlements(List<NamiEntitlement> activeEntitlements) {

        this.runOnUiThread(()-> {
            int textResId = R.string.entitlement_status_inactivate;
            boolean showAsActive = false;
            if (!activeEntitlements.isEmpty()) {
                showAsActive = true;
                textResId = R.string.entitlement_status_active;
            }
            binding.subscriptionStatus.setEnabled(showAsActive);
            binding.subscriptionStatus.setText(textResId);
        });
    }

    private void onSubscriptionClicked(Activity activity) {
        NamiMLManager.coreAction("subscribe");

        NamiCampaignManager.launch(this,"", ((action, skuId )  -> {
            Log.d(LOG_TAG, "New Paywall Action " + action +" with sku :" + skuId);
            return Unit.INSTANCE;
        }), (launchCampaignResult -> {
            launchCampaignResult.onSuccessOrElse(
                    () -> Log.d(LOG_TAG, "Launch Campaign Success"),
                    error -> Log.d(LOG_TAG, "Launch Campaign Error -> " + error),
                    (activePurchases, purchaseState, error) -> {
                        Log.d(LOG_TAG, "Purchase changed -> " + purchaseState);

                        switch (purchaseState){
                            case PURCHASED:
                                Log.d(LOG_TAG, "Purchased! -> " + activePurchases);
                                break;
                            case CANCELLED:
                                Log.d(LOG_TAG, "Cancelled Purchase Flow! -> " + activePurchases);
                                break;
                            default:
                                break;
                        }
                    }

            );
            return Unit.INSTANCE;
        }));
    }

    private void evaluateLastPurchaseEvent(
            @NonNull List<NamiPurchase> activePurchases,
            @NonNull NamiPurchaseState namiPurchaseState,
            @Nullable String errorMsg
    ) {
        Log.d(LOG_TAG, "Purchase State " + namiPurchaseState.name());
        if (namiPurchaseState == NamiPurchaseState.PURCHASED) {
            Log.d(LOG_TAG, "\nActive Purchases: ");
            for (NamiPurchase purchase : activePurchases) {
                Log.d(LOG_TAG, "\tSkuId: " + purchase.getSkuId());
            }
        } else {
            Log.d(LOG_TAG, "Reason : " + errorMsg);
        }
    }

    private void setOnThrottledClick(View view, Consumer<Activity> consumer) {
        view.setOnClickListener(viewLocal -> {
            viewLocal.setClickable(false);
            viewLocal.postDelayed(() -> viewLocal.setClickable(true), THROTTLED_CLICK_DELAY);
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
