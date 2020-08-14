package com.namiml.demo.basic.java;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.namiml.demo.basic.java.databinding.ActivityAboutBinding;
import com.namiml.ml.NamiMLManager;

import java.util.Collections;
import java.util.List;

public class AboutActivity extends AppCompatActivity {

    private static final List<String> CORE_CONTENT_LABELS = Collections.singletonList("About");

    static Intent getIntent(Context context) {
        return new Intent(context, AboutActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAboutBinding binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        binding.toolbar.setNavigationOnClickListener(view -> onBackPressed());
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((BasicApplication) getApplication()).allowAutoRaisingPaywall = false;
        NamiMLManager.enterCoreContent(CORE_CONTENT_LABELS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((BasicApplication) getApplication()).allowAutoRaisingPaywall = true;
        NamiMLManager.exitCoreContent(CORE_CONTENT_LABELS);
    }
}