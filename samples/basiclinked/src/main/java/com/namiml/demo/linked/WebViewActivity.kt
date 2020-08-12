package com.namiml.demo.linked

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.namiml.demo.linked.databinding.ActivityWebViewBinding

class WebViewActivity : AppCompatActivity() {

    companion object {
        private const val INTENT_EXTRA_KEY_URL = "intent_extra_key_url"
        private const val INTENT_EXTRA_KEY_TOOLBAR_TITLE = "intent_extra_key_toolbar_title"
        internal fun getIntent(
            context: Context,
            url: String,
            @StringRes toolbarTitleResId: Int
        ): Intent {
            return Intent(context, WebViewActivity::class.java).apply {
                putExtra(INTENT_EXTRA_KEY_URL, url)
                putExtra(INTENT_EXTRA_KEY_TOOLBAR_TITLE, toolbarTitleResId)
            }
        }
    }

    private lateinit var binding: ActivityWebViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.webViewToolbar)
        binding.webViewToolbar.apply {
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationOnClickListener {
                onBackPressed()
            }
        }
        intent?.let {
            it.getStringExtra(INTENT_EXTRA_KEY_URL)?.let { url ->
                setupWebView(url)
            } ?: finish()
            with(it.getIntExtra(INTENT_EXTRA_KEY_TOOLBAR_TITLE, R.string.terms_of_use)) {
                setTitle(this)
            }
        } ?: finish()
    }

    private fun setupWebView(url: String) {
        binding.webView.apply {
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.webViewProgress.visibility = View.GONE
                }
            }
            binding.webViewProgress.visibility = View.VISIBLE
            loadUrl(url)
        }
    }
}
