package com.namiml.demo.linked

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.namiml.NamiPaywallManager
import com.namiml.demo.linked.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.subscriptionStatus.apply {
            text = getText(R.string.subscription_status_inactivate)
            isEnabled = false
        }
        binding.aboutButton.setOnClickListener {
            startActivity(AboutActivity.getIntent(this))
        }
        binding.subscriptionButton.setOnClickListener {
            if (NamiPaywallManager.canRaisePaywall()) {
                NamiPaywallManager.raisePaywall(this)
            }
        }
    }
}
