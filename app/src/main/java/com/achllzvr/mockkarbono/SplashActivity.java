package com.achllzvr.mockkarbono;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import com.achllzvr.mockkarbono.utils.PrefsManager;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // Set the layout

        // Add a small delay (e.g., 1.5 seconds) so the user actually sees the logo
        // before being routed. This feels more polished than an instant flick.
        new Handler(Looper.getMainLooper()).postDelayed(this::routeUser, 1500);
    }

    private void routeUser() {

        PrefsManager prefs = new PrefsManager(this);

        Intent intent;

        // --- TEMPORARY TESTING CODE: START ---
        // Forces the app to think you are a returning user who hasn't finished setup
        prefs.setFirstRun(false);          // Not a fresh install
        prefs.setOnboardingCompleted(false); // But setup isn't done
        // --- TEMPORARY TESTING CODE: END ---

        if (prefs.isFirstRun()) {
            // Case 1: Brand new install
            intent = new Intent(this, OnboardingActivity.class);
        } else {
            // Case 3: Regular User
            intent = new Intent(this, MainActivity.class);
        }

        startActivity(intent);
        finish();
    }
}