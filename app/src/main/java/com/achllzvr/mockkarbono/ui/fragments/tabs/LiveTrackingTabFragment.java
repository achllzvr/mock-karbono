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
import com.achllzvr.mockkarbono.ui.appusage.AppUsageAdapter;
import com.achllzvr.mockkarbono.ui.appusage.AppUsageViewModel;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class LiveTrackingTabFragment extends Fragment {

    private TextView tvScreenTime;
    private TextView tvScreenCarbon;
    private SwitchMaterial switchAppUsage;
    private SwitchMaterial switchNotifications;
    private RecyclerView rvAppUsageList;
    private View emptyState;
    private AppUsageAdapter adapter;
    private AppUsageViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_track_live, container, false);

        // Bind views
        tvScreenTime = view.findViewById(R.id.tvScreenTime);
        tvScreenCarbon = view.findViewById(R.id.tvScreenCarbon);
        switchAppUsage = view.findViewById(R.id.switchAppUsage);
        switchNotifications = view.findViewById(R.id.switchNotifications);
        rvAppUsageList = view.findViewById(R.id.rvAppUsageList);
        emptyState = view.findViewById(R.id.emptyState);

        // Setup RecyclerView
        adapter = new AppUsageAdapter(requireContext());
        rvAppUsageList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvAppUsageList.setAdapter(adapter);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(AppUsageViewModel.class);

        // Observe today's app usage
        viewModel.getTodayAppUsage().observe(getViewLifecycleOwner(), usageList -> {
            if (usageList == null || usageList.isEmpty()) {
                rvAppUsageList.setVisibility(View.GONE);
                if (emptyState != null) {
                    emptyState.setVisibility(View.VISIBLE);
                }
            } else {
                rvAppUsageList.setVisibility(View.VISIBLE);
                if (emptyState != null) {
                    emptyState.setVisibility(View.GONE);
                }
                adapter.submitList(usageList);
            }
        });

        // Observe total duration
        viewModel.getTodayTotalDuration().observe(getViewLifecycleOwner(), duration -> {
            if (duration == null) duration = 0L;
            tvScreenTime.setText(formatDuration(duration));
        });

        // Observe total carbon
        viewModel.getTodayTotalCarbon().observe(getViewLifecycleOwner(), carbon -> {
            if (carbon == null) carbon = 0.0;
            tvScreenCarbon.setText(String.format(Locale.getDefault(), "≈ %.3f kg CO₂", carbon));
        });

        // Setup switches
        switchAppUsage.setChecked(true);
        switchNotifications.setChecked(true);

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


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.loadTodayData();
        }
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

}

