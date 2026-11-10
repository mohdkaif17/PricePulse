package com.example.pricepulse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pricepulse.MainActivity;
import com.example.pricepulse.R;
import com.example.pricepulse.data.remote.SessionManager;

/**
 * SplashActivity - PricePulse App Launcher Screen
 * Displays brand identity for ~1.5s, checks for an active authenticated session,
 * and seamlessly restores session into MainActivity or directs to LoginActivity.
 */
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY_MS = 1500;
    private final Handler splashHandler = new Handler(Looper.getMainLooper());
    private Runnable navigationRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        navigationRunnable = () -> {
            SessionManager sessionManager = new SessionManager(SplashActivity.this);
            Intent intent;
            if (sessionManager.isLoggedIn()) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        };

        splashHandler.postDelayed(navigationRunnable, SPLASH_DELAY_MS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (navigationRunnable != null) {
            splashHandler.removeCallbacks(navigationRunnable);
        }
    }
}
