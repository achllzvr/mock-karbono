package com.achllzvr.mockkarbono.ui.onboarding;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.achllzvr.mockkarbono.OnboardingActivity;
import com.achllzvr.mockkarbono.R;

import java.util.Locale;

public class AILimitAnalysisFragment extends Fragment {

    private LinearLayout llLoading, llResultContainer;
    private TextView tvStatus, tvUserEstimate, tvSafeLimit, tvTipContent;
    private Button btnContinue;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai_analysis, container, false);

        llLoading = view.findViewById(R.id.llLoading);
        llResultContainer = view.findViewById(R.id.llResultContainer);
        tvStatus = view.findViewById(R.id.tvStatus);
        tvUserEstimate = view.findViewById(R.id.tvUserEstimate);
        tvSafeLimit = view.findViewById(R.id.tvSafeLimit);
        tvTipContent = view.findViewById(R.id.tvTipContent);
        btnContinue = view.findViewById(R.id.btnContinue);

        btnContinue.setOnClickListener(v -> {
            if (getActivity() instanceof OnboardingActivity) {
                ((OnboardingActivity) getActivity()).goToNextStep();
            }
        });

        startSimulation();

        return view;
    }

    private void startSimulation() {
        SharedPreferences prefs = requireContext().getSharedPreferences("karbono_prefs", Context.MODE_PRIVATE);

        // Retrieve calculated data from the questionnaire
        float userEstimate = prefs.getFloat("calc_user_estimate", 5.0f);
        boolean isHeavyGamer = prefs.getBoolean("calc_heavy_gamer", false);
        boolean hasHeavyAppliances = prefs.getBoolean("calc_heavy_appliances", false);
        boolean isHeavySocial = prefs.getBoolean("calc_heavy_social", false);

        Handler handler = new Handler(Looper.getMainLooper());

        // Simulation Step 1: Connecting
        handler.postDelayed(() -> tvStatus.setText("Calculating Appliance Load..."), 1000);

        // Simulation Step 2: Analyzing
        handler.postDelayed(() -> tvStatus.setText("Analyzing Digital Footprint..."), 2500);

        // Simulation Step 3: Show Results
        handler.postDelayed(() -> {
            llLoading.setVisibility(View.GONE);
            llResultContainer.setVisibility(View.VISIBLE);

            // Set Values
            tvUserEstimate.setText(String.format(Locale.US, "%.1f", userEstimate));
            tvSafeLimit.setText("6.8"); // Based on PH safe average logic

            // Generate "AI" Tip based on highest impact area
            // Logic derived from research documents:
            if (hasHeavyAppliances) {
                // Insight: AC units are major contributors
                tvTipContent.setText("Your appliances are energy hungry! An AC running for 8 hours can emit ~9.6kg of CO2 daily—more than the safe limit alone.");
            } else if (isHeavyGamer) {
                // Insight: Gaming emissions
                tvTipContent.setText("Gaming has a hidden cost. Did you know that reducing gaming by just 1 hour can save up to 0.86kg of CO2?");
            } else if (isHeavySocial) {
                // Insight: Social Media scrolling
                tvTipContent.setText("Scrolling adds up! 5 hours of social media creates ~60g of CO2 daily. It's invisible, but it's there.");
            } else {
                // General Tip: Email impact
                tvTipContent.setText("Even small digital habits count. A standard email generates 4g of CO2, and spam emails add up even faster!");
            }

        }, 4000);
    }
}