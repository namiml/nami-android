package com.namiml.demo.linked

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.namiml.Nami
import com.namiml.NamiExternalIdentifierType
import com.namiml.billing.NamiPurchaseCompleteResult
import com.namiml.billing.NamiPurchaseManager
import com.namiml.demo.linked.databinding.ActivityPaywallBinding
import com.namiml.demo.linked.databinding.ViewSkuButtonGroupBinding
import com.namiml.paywall.NamiPaywall
import com.namiml.paywall.NamiSKU

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
        setupCloseButton(namiPaywallLocal)
        setupHeaderBody(namiPaywallLocal)
        skus?.let {
            buildCallToActionButtons(skus, binding.linkedPaywallProductParent)
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
     * @return Return formatted button label
     */
    private fun getLabel(namiSKU: NamiSKU): String {
        return namiSKU.generateButtonLabelForPaywall(this) ?: run {
            if (IS_DEVELOPMENT_MODE_ON) {
                getString(R.string.sku_error_title)
            } else {
                "${namiSKU.skuDetails.title}} - ${namiSKU.skuDetails.price}"
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun getSKUButton(
        buttonText: CharSequence,
        subtitle: CharSequence? = null,
        listener: View.OnClickListener? = null
    ): View {
        ViewSkuButtonGroupBinding.inflate(layoutInflater, null, false).also { binding ->
            binding.skuButton.apply {
                text = buttonText
                listener?.let { buttonListener ->
                    setOnClickListener(buttonListener)
                }
            }
            binding.skuButtonSubtitle.apply {
                visibility = View.VISIBLE
                text = subtitle
            }
            return binding.root
        }
    }

    /**
     * Return null if namiSKU is well-formed value. Other return the skuID for debugging purposes
     */
    private fun getSubTitle(namiSKU: NamiSKU): CharSequence? {
        return if (IS_DEVELOPMENT_MODE_ON) {
            getString(R.string.sku_error_sku_ref, namiSKU.skuId)
        } else {
            null
        }
    }

    private fun buildCallToActionButtons(skus: List<NamiSKU>, container: ViewGroup) {
        for (namiSKU in skus) {
            getSKUButton(
                buttonText = getLabel(namiSKU),
                subtitle = getSubTitle(namiSKU),
                listener = {
                    NamiPurchaseManager.buySKU(this, namiSKU.skuId, onPurchaseComplete)
                }
            ).also { button ->
                container.addView(button)
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