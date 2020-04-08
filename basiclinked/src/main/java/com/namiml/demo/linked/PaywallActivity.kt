package com.namiml.demo.linked

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.namiml.UrlSpanListener
import com.namiml.api.model.NamiPaywall
import com.namiml.api.model.SKU
import com.namiml.demo.linked.databinding.ActivityPaywallBinding
import com.namiml.paywall.NamiSKU
import com.namiml.store.NamiCache

class PaywallActivity : AppCompatActivity() {

    companion object {

        private const val INTENT_EXTRA_KEY_PAYWALL_DATA = "intent_extra_key_paywall_data"
        private const val INTENT_EXTRA_KEY_PRODUCTS = "intent_extra_key_products"
        private const val INTENT_EXTRA_KEY_ID = "intent_extra_key_id"

        fun getIntent(
            context: Context,
            paywallData: NamiPaywall,
            products: ArrayList<NamiSKU>?,
            developerPaywallId: String?
        ): Intent {
            return Intent(context, PaywallActivity::class.java).apply {
                putExtra(INTENT_EXTRA_KEY_PAYWALL_DATA, paywallData)
                putParcelableArrayListExtra(INTENT_EXTRA_KEY_PRODUCTS, products)
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
            setupUi(
                it.getParcelableExtra(INTENT_EXTRA_KEY_PAYWALL_DATA),
                it.getParcelableArrayListExtra(INTENT_EXTRA_KEY_PRODUCTS),
                it.getStringExtra(INTENT_EXTRA_KEY_ID)
            )
        }
        overridePendingTransition(R.anim.slide_up, R.anim.stay_still)
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

    private fun setupUi(
        namiPaywall: NamiPaywall?,
        products: List<NamiSKU>?,
        paywallId: String?
    ) {
        namiPaywall?.let {
            NamiCache.getBackgroundImageForPaywall(it.id)?.let { bitmap ->
                binding.linkedPaywallBg.background = BitmapDrawable(resources, bitmap)
            } ?: run {
                with(ContextCompat.getColor(this, R.color.colorAccent)) {
                    binding.linkedPaywallBg.background = ColorDrawable(this)
                }
            }

            if (it.allowClosing) {
                allowBackPress = true
                binding.linkedPaywallClose.apply {
                    visibility = View.VISIBLE
                    setOnClickListener {
                        finish()
                    }
                }
            }
            binding.apply {
                linkedPaywallHeader.text = it.title
                linkedPaywallBody.text = it.body
            }

            products?.forEach {
                binding.linkedPaywallProductParent.addView(
                    MaterialButton(
                        ContextThemeWrapper(
                            this,
                            R.style.Paywall_Button
                        )
                    ).apply { text = it.skuName }
                )
            }
            if (it.signInControl) {
                binding.linkedPaywallSignInButton.visibility = View.VISIBLE
            }
            if (it.restoreControl) {
                binding.linkedPaywallRestoreButton.visibility = View.VISIBLE
            }
            it.purchaseTerms?.let {
                binding.linkedPaywallPurchaseTerms.apply {
                    visibility = View.VISIBLE
                    text = it
                }
            }

            val urlSpanListener: UrlSpanListener = { view, url, titleResId ->
                startActivity(WebViewActivity.getIntent(this, url, titleResId))
            }

            namiPaywall.getFooterText(this, urlSpanListener, urlSpanListener).let {
                binding.linkedPaywallTosPolicy.apply {
                    visibility = View.VISIBLE
                    text = it
                    movementMethod = LinkMovementMethod.getInstance()
                }
            }
        }
    }
}
