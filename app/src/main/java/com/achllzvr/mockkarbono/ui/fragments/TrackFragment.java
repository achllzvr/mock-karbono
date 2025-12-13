package com.achllzvr.mockkarbono.ui.fragments;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.achllzvr.mockkarbono.R;
import com.achllzvr.mockkarbono.ui.adapters.TrackPagerAdapter;

public class TrackFragment extends Fragment {

    private ViewPager2 viewPager;
    private TextView tabLive, tabNotifications, tabAppliances;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_track, container, false);

        viewPager = view.findViewById(R.id.viewPager);
        tabLive = view.findViewById(R.id.tabLive);
        tabNotifications = view.findViewById(R.id.tabNotifications);
        tabAppliances = view.findViewById(R.id.tabAppliances);

        setupViewPager();
        setupTabs();

        // Check for usage stats permission
        checkPermissions();

        return view;
    }

    private void setupViewPager() {
        TrackPagerAdapter adapter = new TrackPagerAdapter(this);
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateTabStyles(position);
            }
        });
    }

    private void setupTabs() {
        tabLive.setOnClickListener(v -> viewPager.setCurrentItem(0));
        tabNotifications.setOnClickListener(v -> viewPager.setCurrentItem(1));
        tabAppliances.setOnClickListener(v -> viewPager.setCurrentItem(2));

        // Set initial style
        updateTabStyles(0);
    }

    private void updateTabStyles(int selectedPosition) {
        TextView[] tabs = {tabLive, tabNotifications, tabAppliances};

        for (int i = 0; i < tabs.length; i++) {
            if (i == selectedPosition) {
                tabs[i].setBackgroundResource(R.drawable.bg_button_green);
                tabs[i].setTextColor(getResources().getColor(android.R.color.white, null));
            } else {
                tabs[i].setBackgroundResource(android.R.color.transparent);
                tabs[i].setTextColor(getResources().getColor(R.color.text_secondary, null));
            }
        }
    }

    private void checkPermissions() {
        if (!hasUsageStatsPermission()) {
            showUsageStatsPermissionDialog();
        }
    }

    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) requireContext().getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), requireContext().getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void showUsageStatsPermissionDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Usage Access Required")
                .setMessage("Karbono needs Usage Access permission to track app usage and calculate carbon footprint.")
                .setPositiveButton("Grant", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Later", null)
                .show();
    }
}
