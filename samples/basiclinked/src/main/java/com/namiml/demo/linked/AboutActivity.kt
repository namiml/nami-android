package com.namiml.demo.linked

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.namiml.demo.linked.databinding.ActivityAboutBinding
import com.namiml.ml.NamiMLManager

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    companion object {
        private const val CORE_CONTENT_LABEL = "About"
        fun getIntent(context: Context): Intent {
            return Intent(context, AboutActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.apply {
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationOnClickListener {
                onBackPressed()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (application as BasicLinkedApplication).allowAutoRaisingPaywall = false
        NamiMLManager.enterCoreContent(CORE_CONTENT_LABEL)
    }

    override fun onPause() {
        super.onPause()
        (application as BasicLinkedApplication).allowAutoRaisingPaywall = true
        NamiMLManager.exitCoreContent(CORE_CONTENT_LABEL)
    }
}