package com.namiml.demo.linked

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.namiml.Nami
import com.namiml.NamiExternalIdentifierType
import com.namiml.billing.NamiPurchase
import com.namiml.billing.NamiPurchaseCompleteResult
import com.namiml.billing.NamiPurchaseManager
import com.namiml.demo.linked.databinding.ActivityPaywallBinding
import com.namiml.demo.linked.databinding.ViewSkuButtonGroupBinding
import com.namiml.paywall.NamiPaywall
import com.namiml.paywall.NamiSKU
import com.namiml.paywall.PaywallStyleData

class PaywallActivity : AppCompatActivity() {

    companion object {

        // Think of this as a data required by this view but outside scope of the view. Just like
        // a ViewModel which acts like a container. This reference would be nullified when
        // activity is destroyed. It's just easy and fastest way of achieving this for demo purposes
        private var namiPaywall: NamiPaywall? = null
        private var skus: List<NamiSKU>? = null
        private const val PLAY_STORE_SUBSCRIPTION_URL =
            "https://play.google.com/store/account/subscriptions"

        fun getIntent(
            context: Context,
            namiPaywall: NamiPaywall,
            skus: List<NamiSKU>?
        ): Intent {
            this.namiPaywall = namiPaywall
            this.skus = skus
            return Intent(context, PaywallActivity::class.java)
        }
    }

    private lateinit var binding: ActivityPaywallBinding
    private var allowBackPress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaywallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUi(namiPaywall, skus)
        overridePendingTransition(R.anim.slide_up, R.anim.stay_still)
    }

    override fun onDestroy() {
        super.onDestroy()
        namiPaywall = null
        skus = null
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
        setupCloseButton(namiPaywallLocal, namiPaywallLocal.styleData)
        setupHeaderBody(namiPaywallLocal)
        skus?.let {
            val featuredSkuIds = namiPaywall.formattedSkus.filter { it.featured }.map { it.skuId }
            buildCallToActionButtons(skus, binding, namiPaywallLocal.styleData, featuredSkuIds)
        }
        setupSignInButton(namiPaywallLocal)
        setupRestoreButton(namiPaywallLocal)
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

    private fun setupSignInButton(namiPaywall: NamiPaywall) {
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
    }

    private fun setupRestoreButton(namiPaywall: NamiPaywall) {
        if (namiPaywall.restoreControl) {
            binding.linkedPaywallRestoreButton.apply {
                visibility = View.VISIBLE
                setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(PLAY_STORE_SUBSCRIPTION_URL)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    private fun setupCloseButton(namiPaywall: NamiPaywall, styleData: PaywallStyleData?) {
        if (namiPaywall.allowClosing) {
            allowBackPress = true
            binding.linkedPaywallClose.apply {
                visibility = View.VISIBLE
                styleData?.closeButtonTextColor?.let {
                    setColorFilter(Color.parseColor(it))
                }
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

    private fun buildCallToActionButtons(
        skus: List<NamiSKU>,
        binding: ActivityPaywallBinding,
        styleData: PaywallStyleData?,
        featuredSkuIds: List<String>
    ) {
        val purchases = NamiPurchaseManager.allPurchases()
        skus.forEachIndexed { index, namiSKU ->
            val buttonGroupBinding = when (index) {
                0 -> {
                    binding.linkedPaywallProductOne
                }
                1 -> {
                    binding.linkedPaywallProductTwo
                }
                else -> {
                    binding.linkedPaywallProductThree
                }
            }
            setButtonData(buttonGroupBinding, namiSKU, purchases, styleData, featuredSkuIds)
        }
    }

    private fun setButtonData(
        binding: ViewSkuButtonGroupBinding,
        namiSKU: NamiSKU,
        allPurchases: List<NamiPurchase>,
        styleData: PaywallStyleData?,
        featuredSkuIds: List<String>
    ) {
        val skuId = namiSKU.skuId
        binding.apply {
            with(skuDescription) {
                text = namiSKU.skuDetails.description
                styleData?.skuButtonTextColor?.let { textColor ->
                    setTextColor(Color.parseColor(textColor))
                }
            }
            skuPrice.text = namiSKU.skuDetails.price
            if (allPurchases.any { it.skuId == skuId }) {
                skuActiveIndicator.visibility = View.VISIBLE
            }
            if (featuredSkuIds.contains(namiSKU.skuId)) {
                skuFeaturedIndicator.visibility = View.VISIBLE
            }
            styleData?.skuButtonColor?.let { bgColor ->
                skuParent.setBackgroundColor(Color.parseColor(bgColor))
            }
            root.visibility = View.VISIBLE
            root.setOnClickListener {
                NamiPurchaseManager.buySKU(this@PaywallActivity, skuId, onPurchaseComplete)
            }
        }
    }

    private val onPurchaseComplete: ((NamiPurchaseCompleteResult) -> Unit) = {
        Log.d(
            this.javaClass.name,
            "Purchase flow completed, isSuccessful=${it.isSuccessful}, " +
                    "code=${it.billingResponseCode}"
        )
        if (it.isSuccessful) {
            finish()
        }
    }
}