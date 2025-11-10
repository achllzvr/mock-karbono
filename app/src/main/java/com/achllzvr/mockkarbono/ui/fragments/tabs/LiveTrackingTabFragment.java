package com.achllzvr.mockkarbono.ui.fragments.tabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.achllzvr.mockkarbono.R;
import com.achllzvr.mockkarbono.db.AppDatabase;
import com.achllzvr.mockkarbono.db.entities.AppUsage;
import com.achllzvr.mockkarbono.ui.AppUsageAdapter;
import com.achllzvr.mockkarbono.ui.AppUsageViewModel;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LiveTrackingTabFragment extends Fragment {

    private TextView tvScreenTime;
    private TextView tvScreenCarbon;
    private SwitchMaterial switchAppUsage;
    private SwitchMaterial switchNotifications;
    private RecyclerView rvAppUsageList;
    private AppUsageAdapter adapter;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_track_live, container, false);

        db = AppDatabase.getInstance(requireContext());

        // Bind views
        tvScreenTime = view.findViewById(R.id.tvScreenTime);
        tvScreenCarbon = view.findViewById(R.id.tvScreenCarbon);
        switchAppUsage = view.findViewById(R.id.switchAppUsage);
        switchNotifications = view.findViewById(R.id.switchNotifications);
        rvAppUsageList = view.findViewById(R.id.rvAppUsageList);

        // Setup RecyclerView
        adapter = new AppUsageAdapter();
        rvAppUsageList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvAppUsageList.setAdapter(adapter);

        // Setup switches
        switchAppUsage.setChecked(true); // Default on
        switchNotifications.setChecked(true); // Default on

        switchAppUsage.setOnCheckedChangeListener((buttonView, isChecked) -> {
            android.widget.Toast.makeText(requireContext(),
                isChecked ? "App usage tracking enabled" : "App usage tracking disabled",
                android.widget.Toast.LENGTH_SHORT).show();
        });

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            android.widget.Toast.makeText(requireContext(),
                isChecked ? "Notification tracking enabled" : "Notification tracking disabled",
                android.widget.Toast.LENGTH_SHORT).show();
        });

        // Load data
        loadLiveData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadLiveData();
    }

    private void loadLiveData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Get today's screen usage
            List<AppUsage> allUsage = db.appUsageDao().getLatest(100);

            long totalScreenTimeMs = 0;
            double totalScreenCO2 = 0.0;

            for (AppUsage usage : allUsage) {
                if (isSameDay(usage.clientCreatedAtMs, System.currentTimeMillis())) {
                    if ("screen".equals(usage.category)) {
                        totalScreenTimeMs += usage.durationMs;
                        totalScreenCO2 += usage.estimatedKgCO2;
                    }
                }
            }

            long finalScreenTimeMs = totalScreenTimeMs;
            double finalScreenCO2 = totalScreenCO2;

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    tvScreenTime.setText(formatDuration(finalScreenTimeMs));
                    tvScreenCarbon.setText(String.format("≈ %.3f kg CO₂", finalScreenCO2));

                    // Filter only non-screen apps for the list
                    List<AppUsage> appOnlyUsage = new java.util.ArrayList<>();
                    for (AppUsage usage : allUsage) {
                        if (isSameDay(usage.clientCreatedAtMs, System.currentTimeMillis()) &&
                            !"screen".equals(usage.category)) {
                            appOnlyUsage.add(usage);
                        }
                    }
                    adapter.setItems(appOnlyUsage);
                });
            }
        });
    }

    private String formatDuration(long ms) {
        long hours = TimeUnit.MILLISECONDS.toHours(ms);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60;

        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }

    private boolean isSameDay(long timestamp1, long timestamp2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTimeInMillis(timestamp1);
        cal2.setTimeInMillis(timestamp2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}

