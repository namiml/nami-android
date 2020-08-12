package com.namiml.demo.linked

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.namiml.Nami
import com.namiml.NamiExternalIdentifierType
import com.namiml.billing.NamiPurchaseCompleteResult
import com.namiml.billing.NamiPurchaseManager
import com.namiml.demo.linked.databinding.ActivityPaywallBinding
import com.namiml.paywall.NamiPaywall
import com.namiml.paywall.NamiSKU
import com.namiml.paywall.SubscriptionPeriod

class PaywallActivity : AppCompatActivity() {

    companion object {

        // Think of this as a data required by this view but outside scope of the view. Just like
        // a ViewModel which acts like a container. This reference would be nullified when
        // activity is destroyed. It's just easy and fastest way of achieving this for demo purposes
        private var namiPaywall: NamiPaywall? = null
        private const val INTENT_EXTRA_KEY_SKUS = "intent_extra_key_skus"
        private const val INTENT_EXTRA_KEY_ID = "intent_extra_key_id"

        fun getIntent(
            context: Context,
            namiPaywall: NamiPaywall,
            skus: ArrayList<NamiSKU>?,
            developerPaywallId: String?
        ): Intent {
            this.namiPaywall = namiPaywall
            return Intent(context, PaywallActivity::class.java).apply {
                putParcelableArrayListExtra(INTENT_EXTRA_KEY_SKUS, skus)
                putExtra(INTENT_EXTRA_KEY_ID, developerPaywallId)
            }
        }
    }

    private lateinit var binding: ActivityPaywallBinding
    private var allowBackPress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaywallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        intent?.let {
            setupUi(namiPaywall, it.getParcelableArrayListExtra(INTENT_EXTRA_KEY_SKUS))
        }
        overridePendingTransition(R.anim.slide_up, R.anim.stay_still)
    }

    override fun onDestroy() {
        super.onDestroy()
        namiPaywall = null
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.stay_still, R.anim.slide_down)
    }

    override fun onBackPressed() {
        if (allowBackPress) {
            super.onBackPressed()
        }
    }

    private fun setupUi(namiPaywall: NamiPaywall?, skus: List<NamiSKU>?) {
        val namiPaywallLocal = namiPaywall ?: return
        setupPaywallBackground(namiPaywallLocal)
        setupCloseButton(namiPaywallLocal)
        setupHeaderBody(namiPaywallLocal)
        buildCallToActionButtons(skus, binding.linkedPaywallProductParent)
        setupSignInAndRestoreButtons(namiPaywallLocal)
        namiPaywallLocal.purchaseTerms?.let {
            binding.linkedPaywallPurchaseTerms.apply {
                visibility = View.VISIBLE
                text = it
            }
        }

        val urlSpanListener: (widget: View, url: String, toolbarTitleResId: Int) -> Unit =
            { _, url, titleResId ->
                startActivity(WebViewActivity.getIntent(this, url, titleResId))
            }
        namiPaywallLocal.getFooterText(this, urlSpanListener, urlSpanListener).let {
            binding.linkedPaywallTosPolicy.apply {
                visibility = View.VISIBLE
                text = it
                movementMethod = LinkMovementMethod.getInstance()
            }
        }
    }

    private fun setupHeaderBody(namiPaywall: NamiPaywall) {
        binding.apply {
            linkedPaywallHeader.text = namiPaywall.title
            linkedPaywallBody.text = namiPaywall.body
        }
    }

    private fun setupSignInAndRestoreButtons(namiPaywall: NamiPaywall) {
        if (namiPaywall.signInControl) {
            binding.linkedPaywallSignInButton.apply {
                setOnClickListener {
                    // Once user signs in, you may provide unique identifier that can be
                    // used to link different devices to the same customer in the Nami platform.
                    // Here at this stage, since we don't have real sign in flow in this demo
                    // app, we're just setting this test identifier when the sign-in button is
                    // pressed on paywall
                    Nami.setExternalIdentifier(
                        TEST_EXTERNAL_IDENTIFIER,
                        NamiExternalIdentifierType.UUID
                    )
                }
                visibility = View.VISIBLE
            }
        }
        if (namiPaywall.restoreControl) {
            binding.linkedPaywallRestoreButton.visibility = View.VISIBLE
        }
    }

    private fun setupCloseButton(namiPaywall: NamiPaywall) {
        if (namiPaywall.allowClosing) {
            allowBackPress = true
            binding.linkedPaywallClose.apply {
                visibility = View.VISIBLE
                setOnClickListener {
                    finish()
                }
            }
        }
    }

    private fun setupPaywallBackground(namiPaywall: NamiPaywall) {
        namiPaywall.backgroundImage?.let { bitmap ->
            binding.linkedPaywallBg.background = BitmapDrawable(resources, bitmap)
        } ?: run {
            with(ContextCompat.getColor(this, R.color.colorAccent)) {
                binding.linkedPaywallBg.background = ColorDrawable(this)
            }
        }
    }

    /**
     * Sort NamiSKUs by subscription duration.
     */
    private fun sortNamiSKUs(skus: List<NamiSKU>?): List<NamiSKU>? {
        return skus?.sortedBy {
            it.subscriptionPeriod?.duration
        }
    }

    /**
     * @return Return formatted button label
     */
    private fun getLabel(namiSKU: NamiSKU): String {
        namiSKU.localizedPrice.also { price ->
            return namiSKU.subscriptionPeriod?.let { subscriptionPeriod ->
                "$price / ${subscriptionPeriod.getDurationLabel()}"
            } ?: run {
                if (IS_DEVELOPMENT_MODE_ON) {
                    getString(R.string.sku_error_title)
                } else {
                    "${namiSKU.skuName} - $price"
                }
            }
        }
    }

    private fun SubscriptionPeriod.getDurationLabel(): String {
        return when (this) {
            SubscriptionPeriod.WEEKLY -> "Weekly"
            SubscriptionPeriod.MONTHLY -> "Monthly"
            SubscriptionPeriod.QUARTERLY -> "Quarterly"
            SubscriptionPeriod.HALF_YEAR -> "Six Months"
            SubscriptionPeriod.ANNUAL -> "Yearly"
            else -> "Error"
        }
    }

    @SuppressLint("InflateParams")
    private fun getSKUButton(
        buttonText: CharSequence,
        subtitle: CharSequence? = null,
        listener: View.OnClickListener? = null
    ): View {
        return layoutInflater.inflate(R.layout.view_sku_button_group, null).also {
            with(it.findViewById<MaterialButton>(R.id.sku_button)) {
                text = buttonText
                listener?.let { buttonListener ->
                    setOnClickListener(buttonListener)
                }
            }
            with(it.findViewById<MaterialTextView>(R.id.sku_button_subtitle)) {
                visibility = View.VISIBLE
                text = subtitle
            }
        }
    }

    /**
     * Return null if namiSKU is well-formed and value. Other return the skuID of the
     * app config entitlement for debugging purposes
     */
    private fun getSubTitle(namiSKU: NamiSKU): CharSequence? {
        val skuDetails = NamiPurchaseManager.getSkuDetails(namiSKU.skuId)
        return if (skuDetails == null && IS_DEVELOPMENT_MODE_ON) {
            getString(R.string.sku_error_sku_ref, namiSKU.skuId)
        } else {
            null
        }
    }

    private fun buildCallToActionButtons(skus: List<NamiSKU>?, container: ViewGroup) {
        sortNamiSKUs(skus)?.let {
            for (namiSKU in it) {
                getLabel(namiSKU).also { label ->
                    val button = getSKUButton(
                        buttonText = label,
                        subtitle = getSubTitle(namiSKU),
                        listener = View.OnClickListener {
                            NamiPurchaseManager.buySKU(this, namiSKU.skuId, onPurchaseComplete)
                        }
                    )
                    container.addView(button)
                }
            }
        }
    }

    private val onPurchaseComplete: ((NamiPurchaseCompleteResult) -> Unit) = {
        Log.d(
            this.javaClass.name,
            "Purchase flow completed, isSuccessful=${it.isSuccessful}, " +
                    "code=${it.billingResponseCode}"
        )
        finish()
    }
}