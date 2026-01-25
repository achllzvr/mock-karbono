package com.achllzvr.mockkarbono.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.achllzvr.mockkarbono.ui.fragments.TrackFragment;
import com.achllzvr.mockkarbono.ui.fragments.tabs.LiveTrackingTabFragment;
import com.achllzvr.mockkarbono.ui.fragments.tabs.NotificationsTabFragment;

public class TrackPagerAdapter extends FragmentStateAdapter {

    public TrackPagerAdapter(@NonNull TrackFragment fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return correct fragment based on position
        if (position == 1) {
            return new NotificationsTabFragment();
        }
        return new LiveTrackingTabFragment();
    }

    @Override
    public int getItemCount() {
        return 2; // Reduced from 3 to 2 (Live & Notifications only)
    }
}