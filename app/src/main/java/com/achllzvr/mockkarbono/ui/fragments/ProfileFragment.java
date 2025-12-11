package com.achllzvr.mockkarbono.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.achllzvr.mockkarbono.R;

/**
 * Profile Fragment - Soft Pop / Duolingo Style
 * Displays user profile, badges, and settings
 */
public class ProfileFragment extends Fragment {

    // Profile views
    private ImageView imgProfileAvatar;
    private TextView tvUsername;
    private TextView tvJoinDate;

    // Stats views
    private TextView tvStreakStat;
    private TextView tvTreesPlanted;
    private TextView tvCO2Saved;

    // Settings buttons
    private LinearLayout btnAppPreferences;
    private LinearLayout btnNotificationSettings;
    private LinearLayout btnAbout;
    private TextView btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Bind profile views
        imgProfileAvatar = view.findViewById(R.id.imgProfileAvatar);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvJoinDate = view.findViewById(R.id.tvJoinDate);

        // Bind stats views
        tvStreakStat = view.findViewById(R.id.tvStreakStat);
        tvTreesPlanted = view.findViewById(R.id.tvTreesPlanted);
        tvCO2Saved = view.findViewById(R.id.tvCO2Saved);

        // Bind settings buttons
        btnAppPreferences = view.findViewById(R.id.btnAppPreferences);
        btnNotificationSettings = view.findViewById(R.id.btnNotificationSettings);
        btnAbout = view.findViewById(R.id.btnAbout);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Load mock profile data
        loadMockProfileData();

        // Setup click listeners
        setupClickListeners();

        return view;
    }

    private void loadMockProfileData() {
        // Mock data - in real app, this would come from Firebase Auth and Firestore
        tvUsername.setText("EcoWarrior_PH");
        tvJoinDate.setText("Member since Dec 2025");

        // Stats
        tvStreakStat.setText("12");
        tvTreesPlanted.setText("5");
        tvCO2Saved.setText("24kg");
    }

    private void setupClickListeners() {
        btnAppPreferences.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Opening App Preferences...", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to preferences screen
        });

        btnNotificationSettings.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Opening Notification Settings...", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to notification settings
        });

        btnAbout.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Karbono v1.0 - Track your carbon footprint", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Signing out...", Toast.LENGTH_SHORT).show();
            // TODO: Implement sign out logic
        });
    }
}

