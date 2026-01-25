package com.achllzvr.mockkarbono.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.achllzvr.mockkarbono.OnboardingActivity;
import com.achllzvr.mockkarbono.R;
import com.achllzvr.mockkarbono.ui.adapters.AchievementAdapter;
import com.achllzvr.mockkarbono.utils.AnimationHelper;
import com.achllzvr.mockkarbono.utils.PrefsManager;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private PrefsManager prefs;
    private LinearLayout layoutGuest, layoutUser;
    private TextView tvName, tvEmail;
    private TextView tvStreakCount, tvTotalSaved, tvGlobalRank;
    private RecyclerView recyclerBadges;
    private FrameLayout btnSettings, btnEditAvatar;
    private LinearLayout btnEditProfile, btnPrivacy, btnHelp;
    private Button btnLogin, btnLogout;
    private TextView btnViewAllBadges;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        prefs = new PrefsManager(requireContext());

        // Find Views
        layoutGuest = view.findViewById(R.id.layoutGuestState);
        layoutUser = view.findViewById(R.id.layoutUserState);
        tvName = view.findViewById(R.id.tvDisplayName);
        tvEmail = view.findViewById(R.id.tvEmail);

        // Stats
        tvStreakCount = view.findViewById(R.id.tvStreakCount);
        tvTotalSaved = view.findViewById(R.id.tvTotalSaved);
        tvGlobalRank = view.findViewById(R.id.tvGlobalRank);

        // Buttons & Actions
        btnSettings = view.findViewById(R.id.btnSettings);
        btnEditAvatar = view.findViewById(R.id.btnEditAvatar);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnViewAllBadges = view.findViewById(R.id.btnViewAllBadges);

        // Settings shortcuts
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnPrivacy = view.findViewById(R.id.btnPrivacy);
        btnHelp = view.findViewById(R.id.btnHelp);

        // Badges
        recyclerBadges = view.findViewById(R.id.recyclerBadges);
        setupBadges();

        // Check Auth State and update UI
        updateUI();

        // Setup Click Listeners
        setupListeners();

        // Add button press animations
        AnimationHelper.addButtonPressAnimation(btnLogin);
        if (btnLogout != null) {
            AnimationHelper.addButtonPressAnimation(btnLogout);
        }

        return view;
    }

    private void setupBadges() {
        // Mock achievement data
        List<AchievementAdapter.Achievement> achievements = new ArrayList<>();
        achievements.add(new AchievementAdapter.Achievement(
                "First Steps", "Complete your first day", R.drawable.ic_leaf, true));
        achievements.add(new AchievementAdapter.Achievement(
                "Week Warrior", "7 day streak", R.drawable.ic_streak, true));
        achievements.add(new AchievementAdapter.Achievement(
                "Eco Champion", "Save 50kg CO₂", R.drawable.ic_tree, false));
        achievements.add(new AchievementAdapter.Achievement(
                "Community Leader", "Join a group", R.drawable.ic_community, false));

        AchievementAdapter adapter = new AchievementAdapter(achievements, false);
        recyclerBadges.setLayoutManager(new LinearLayoutManager(requireContext(),
                LinearLayoutManager.HORIZONTAL, false));
        recyclerBadges.setAdapter(adapter);
    }

    private void setupListeners() {
        // Settings Button
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                AnimationHelper.bounce(v);
                // Navigate to settings
                Toast.makeText(requireContext(), "Settings coming soon!", Toast.LENGTH_SHORT).show();
            });
        }

        // Edit Avatar
        if (btnEditAvatar != null) {
            btnEditAvatar.setOnClickListener(v -> {
                AnimationHelper.bounce(v);
                Toast.makeText(requireContext(), "Edit avatar coming soon!", Toast.LENGTH_SHORT).show();
            });
        }

        // Login Button (Guest State)
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), OnboardingActivity.class);
            intent.putExtra("force_login", true);
            startActivity(intent);
        });

        // Logout Button (Logged In State)
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                // Animate logout
                AnimationHelper.fadeOut(layoutUser, 300);

                // Clear token after animation
                v.postDelayed(() -> {
                    prefs.saveToken(null);
                    updateUI();
                    AnimationHelper.fadeIn(layoutGuest, 300);
                }, 300);

                Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show();
            });
        }

        // View All Badges
        if (btnViewAllBadges != null) {
            btnViewAllBadges.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "All achievements coming soon!", Toast.LENGTH_SHORT).show();
            });
        }

        // Edit Profile
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Edit profile coming soon!", Toast.LENGTH_SHORT).show();
            });
        }

        // Privacy
        if (btnPrivacy != null) {
            btnPrivacy.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Privacy settings coming soon!", Toast.LENGTH_SHORT).show();
            });
        }

        // Help
        if (btnHelp != null) {
            btnHelp.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Help & Support coming soon!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void updateUI() {
        String token = prefs.getToken();

        if (token != null && !token.isEmpty()) {
            // ===== LOGGED IN STATE =====
            layoutGuest.setVisibility(View.GONE);
            layoutUser.setVisibility(View.VISIBLE);

            // Set user data
            tvName.setText(prefs.getUsername());
            tvEmail.setText(prefs.getEmail());

            // Load and display stats
            loadUserStats();

            // Animate entrance
            AnimationHelper.animateCardEntrance(layoutUser, 0);

        } else {
            // ===== GUEST STATE =====
            layoutGuest.setVisibility(View.VISIBLE);
            layoutUser.setVisibility(View.GONE);

            tvName.setText("Guest Explorer");
            tvEmail.setText("guest@karbono.app");

            // Animate entrance
            AnimationHelper.animateCardEntrance(layoutGuest, 0);
        }
    }

    private void loadUserStats() {
        // TODO: Load real stats from database/API
        // For now, use mock data

        if (tvStreakCount != null) {
            tvStreakCount.setText("12");
        }

        if (tvTotalSaved != null) {
            tvTotalSaved.setText("24.5");
        }

        if (tvGlobalRank != null) {
            tvGlobalRank.setText("#127");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh UI when fragment becomes visible
        updateUI();
    }
}