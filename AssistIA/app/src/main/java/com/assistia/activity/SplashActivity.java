package com.assistia.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.assistia.BuildConfig;
import com.assistia.MainActivity;
import com.assistia.R;

public class SplashActivity extends AppCompatActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_splash);

    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    EdgeToEdge.enable(this);

    new Handler(Looper.getMainLooper()).postDelayed(() -> {
      startActivity(new Intent(SplashActivity.this, MainActivity.class));
      finish();
    }, Long.parseLong(BuildConfig.ASSISTANT_SPLASH_DURATION));

  }
}
