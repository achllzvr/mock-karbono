package com.achllzvr.mockkarbono;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.achllzvr.mockkarbono.ui.adapters.OnboardingPagerAdapter;
import com.achllzvr.mockkarbono.utils.PrefsManager;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Button btnSkip;
    private boolean isReinstaller;
    private PrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding); // Ensure you have this layout

        prefsManager = new PrefsManager(this);

        // Check if user is a "Reinstaller" (Has logged in before but local data was cleared)
        isReinstaller = getIntent().getBooleanExtra("IS_REINSTALLER", false);

        viewPager = findViewById(R.id.viewPager);
        btnSkip = findViewById(R.id.btnSkip); // Ensure this ID exists in your XML

        setupViewPager();
        setupSkipButton();
    }

    private void setupViewPager() {
        // Initialize the adapter with the activity context
        OnboardingPagerAdapter adapter = new OnboardingPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Disable user swiping to enforce the flow (Must complete steps to proceed)
        viewPager.setUserInputEnabled(false);
    }

    private void setupSkipButton() {
        // Only reinstallers can skip the setup
        if (isReinstaller) {
            btnSkip.setVisibility(View.VISIBLE);
            btnSkip.setOnClickListener(v -> completeOnboarding());
        } else {
            btnSkip.setVisibility(View.GONE);
        }
    }

    /**
     * Called by Fragments to advance the flow
     */
    public void goToNextStep() {
        int currentItem = viewPager.getCurrentItem();
        int totalItems = viewPager.getAdapter().getItemCount();

        if (currentItem < totalItems - 1) {
            viewPager.setCurrentItem(currentItem + 1);
        } else {
            completeOnboarding();
        }
    }

    private void completeOnboarding() {
        // Mark onboarding as done
        prefsManager.setFirstRun(false);
        prefsManager.setOnboardingCompleted(true);

        // Navigate to Main Dashboard
        Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}