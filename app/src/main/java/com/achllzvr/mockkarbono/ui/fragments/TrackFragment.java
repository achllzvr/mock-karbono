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
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class TrackFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_track, container, false);

        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        setupViewPager();

        // Check for usage stats permission
        checkPermissions();

        return view;
    }

    private void setupViewPager() {
        TrackPagerAdapter adapter = new TrackPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.tab_live);
                    break;
                case 1:
                    tab.setText(R.string.tab_notifications);
                    break;
                case 2:
                    tab.setText(R.string.tab_appliances);
                    break;
            }
        }).attach();
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

