package com.achllzvr.mockkarbono.ui.fragments;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.achllzvr.mockkarbono.MainActivity;
import com.achllzvr.mockkarbono.R;
import com.achllzvr.mockkarbono.db.AppDatabase;
import com.achllzvr.mockkarbono.db.dao.DailySummaryDao;
import com.achllzvr.mockkarbono.db.entities.DailySummary;
import com.achllzvr.mockkarbono.ui.adapters.AchievementAdapter;
import com.achllzvr.mockkarbono.ui.adapters.TransactionAdapter;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ProfileFragment extends Fragment {

    private RecyclerView rvAchievements;
    private RecyclerView rvTransactions;
    private AchievementAdapter achievementAdapter;
    private TransactionAdapter transactionAdapter;
    private TextView tvViewAllTransactions;
    private TextView tvViewAllAchievements;
    private TextView tvCo2Saved, tvCurrentStreak, tvTreesPlanted;
    private ImageView imgCo2Saved, imgCurrentStreak, imgTreesPlanted;
    private TextView lblCo2Saved, lblCurrentStreak, lblTreesPlanted;

    private List<AchievementAdapter.Achievement> allAchievements;
    private List<TransactionAdapter.Transaction> allTransactions;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        db = AppDatabase.getInstance(requireContext());

        // Bind views
        rvAchievements = view.findViewById(R.id.rvAchievements);
        rvTransactions = view.findViewById(R.id.rvTransactions);
        tvViewAllTransactions = view.findViewById(R.id.tvViewAllTransactions);
        tvViewAllAchievements = view.findViewById(R.id.tvViewAllAchievements);

        // Bind stats card views
        View statCardCo2 = view.findViewById(R.id.statCardCo2);
        tvCo2Saved = statCardCo2.findViewById(R.id.tvStatValue);
        lblCo2Saved = statCardCo2.findViewById(R.id.tvStatLabel);
        imgCo2Saved = statCardCo2.findViewById(R.id.imgStatIcon);

        View statCardStreak = view.findViewById(R.id.statCardStreak);
        tvCurrentStreak = statCardStreak.findViewById(R.id.tvStatValue);
        lblCurrentStreak = statCardStreak.findViewById(R.id.tvStatLabel);
        imgCurrentStreak = statCardStreak.findViewById(R.id.imgStatIcon);

        View statCardTrees = view.findViewById(R.id.statCardTrees);
        tvTreesPlanted = statCardTrees.findViewById(R.id.tvStatValue);
        lblTreesPlanted = statCardTrees.findViewById(R.id.tvStatLabel);
        imgTreesPlanted = statCardTrees.findViewById(R.id.imgStatIcon);

        setupAchievements(view);
        setupTransactions();

        // Settings Buttons
        view.findViewById(R.id.btnAccountSettings).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Feature will be implemented soon!", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.btnAppPreferences).setOnClickListener(v -> {
            showAppPreferencesDialog();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStats(); // Recalculate stats every time the fragment is resumed
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setTopBarAvatarVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setTopBarAvatarVisibility(View.VISIBLE);
        }
    }

    private void showAppPreferencesDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_app_preferences);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        SwitchMaterial switchAppUsage = dialog.findViewById(R.id.switchAppUsageTracking);
        SwitchMaterial switchNotifications = dialog.findViewById(R.id.switchNotificationTracking);
        Button btnClose = dialog.findViewById(R.id.btnClose);

        switchAppUsage.setChecked(true);
        switchNotifications.setChecked(true);

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showAllAchievementsDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_all_achievements);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        RecyclerView rvAllAchievements = dialog.findViewById(R.id.rvAllAchievements);
        rvAllAchievements.setLayoutManager(new LinearLayoutManager(requireContext())); 
        AchievementAdapter adapter = new AchievementAdapter(allAchievements, true);
        rvAllAchievements.setAdapter(adapter);

        ImageButton btnBack = dialog.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> dialog.dismiss());
        
        Button btnClose = dialog.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void setupAchievements(View view) {
        rvAchievements.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        allAchievements = getAchievements();
        achievementAdapter = new AchievementAdapter(allAchievements.subList(0, 4), false);
        rvAchievements.setAdapter(achievementAdapter);

        tvViewAllAchievements.setOnClickListener(v -> showAllAchievementsDialog());
    }

    private void setupTransactions() {
        rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        allTransactions = getTransactions();
        transactionAdapter = new TransactionAdapter(allTransactions.subList(0, 4));
        rvTransactions.setAdapter(transactionAdapter);

        tvViewAllTransactions.setOnClickListener(v -> {
            transactionAdapter = new TransactionAdapter(allTransactions);
            rvTransactions.setAdapter(transactionAdapter);
            tvViewAllTransactions.setVisibility(View.GONE);
        });
    }

    private void loadStats() {
        Executors.newSingleThreadExecutor().execute(() -> {
            DailySummaryDao summaryDao = db.dailySummaryDao();
            List<DailySummary> summaries = summaryDao.getAll();
            
            double totalCo2Saved = 0;
            double weeklySaved = 0;
            int streak = 0;
            double safeRange = 6.8;

            // Calculate Total and Weekly Saved
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long startOfWeekMs = cal.getTimeInMillis();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

            for (DailySummary summary : summaries) {
                totalCo2Saved += (safeRange - summary.totalKgCO2);
                try {
                    Date date = sdf.parse(summary.date);
                    if (date != null && date.getTime() >= startOfWeekMs) {
                        weeklySaved += (safeRange - summary.totalKgCO2);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Calculate Streak
            for (DailySummary summary : summaries) {
                if (summary.totalKgCO2 <= safeRange) {
                    streak++;
                } else {
                    break;
                }
            }

            int treesPlanted = 0; // Placeholder

            if (getActivity() != null) {
                double finalTotalCo2Saved = totalCo2Saved;
                double finalWeeklySaved = weeklySaved;
                int finalCurrentStreak = streak;
                getActivity().runOnUiThread(() -> {
                    // CO2 Saved Card (Weekly)
                    tvCo2Saved.setText(String.format(Locale.US, "%.1fkg", finalWeeklySaved));
                    lblCo2Saved.setText("CO2 Saved (Weekly)");
                    imgCo2Saved.setImageResource(R.drawable.ic_leaf);
                    imgCo2Saved.setColorFilter(getResources().getColor(R.color.matcha_green));

                    // Current Streak Card
                    tvCurrentStreak.setText(String.format(Locale.US, "%d days", finalCurrentStreak));
                    lblCurrentStreak.setText("Current Streak");
                    imgCurrentStreak.setImageResource(R.drawable.ic_streak);
                    imgCurrentStreak.setColorFilter(getResources().getColor(R.color.coral));

                    // Trees Planted Card
                    tvTreesPlanted.setText(String.valueOf(treesPlanted));
                    lblTreesPlanted.setText("Trees Planted");
                    imgTreesPlanted.setImageResource(R.drawable.ic_tree);
                    imgTreesPlanted.setColorFilter(getResources().getColor(R.color.matcha_green));
                });
            }
        });
    }

    private List<AchievementAdapter.Achievement> getAchievements() {
        List<AchievementAdapter.Achievement> items = new ArrayList<>();
        items.add(new AchievementAdapter.Achievement("First Tree", "Awarded for planting your first tree.", R.drawable.ic_leaf, true));
        items.add(new AchievementAdapter.Achievement("Tree Planter", "Awarded for planting 10 trees.", R.drawable.ic_tree, true));
        items.add(new AchievementAdapter.Achievement("Streak Master", "Awarded for maintaining a 7-day streak.", R.drawable.ic_streak, true));
        items.add(new AchievementAdapter.Achievement("Eco Warrior", "Awarded for saving 100kg of CO2.", R.drawable.ic_shield, true));
        items.add(new AchievementAdapter.Achievement("Week Champion", "Finish a week within the carbon limit.", R.drawable.ic_star, false));
        items.add(new AchievementAdapter.Achievement("Carbon Zero", "Have a carbon-neutral day.", R.drawable.ic_check_circle, false));
        items.add(new AchievementAdapter.Achievement("Community Lead", "Start or lead a community group.", R.drawable.ic_community, false));
        items.add(new AchievementAdapter.Achievement("Planet Saver", "Awarded for saving 1,000kg of CO2.", R.drawable.ic_globe, false));
        return items;
    }

    private List<TransactionAdapter.Transaction> getTransactions() {
        List<TransactionAdapter.Transaction> items = new ArrayList<>();
        items.add(new TransactionAdapter.Transaction("Mangrove - Palawan", "Dec 10, 2024", "5 Lives", R.drawable.ic_leaf));
        items.add(new TransactionAdapter.Transaction("LED Light Bulb Set", "Dec 8, 2024", "P450", R.drawable.ic_lightbulb));
        items.add(new TransactionAdapter.Transaction("Narra Tree - Mindanao", "Dec 5, 2024", "8 Lives", R.drawable.ic_leaf));
        items.add(new TransactionAdapter.Transaction("Upcycled Tote Bag", "Dec 2, 2024", "P250", R.drawable.ic_bag));
        items.add(new TransactionAdapter.Transaction("Solar Panel Kit", "Nov 28, 2024", "P15,000", R.drawable.ic_solar_panel));
        items.add(new TransactionAdapter.Transaction("Bamboo Grove", "Nov 25, 2024", "4 Lives", R.drawable.ic_leaf));
        return items;
    }
}
