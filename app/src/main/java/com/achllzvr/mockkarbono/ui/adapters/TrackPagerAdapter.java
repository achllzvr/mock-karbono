package com.achllzvr.mockkarbono.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.achllzvr.mockkarbono.ui.fragments.tabs.AppliancesTabFragment;
import com.achllzvr.mockkarbono.ui.fragments.tabs.LiveTrackingTabFragment;
import com.achllzvr.mockkarbono.ui.fragments.tabs.NotificationsTabFragment;

public class TrackPagerAdapter extends FragmentStateAdapter {

    public TrackPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new LiveTrackingTabFragment();
            case 1:
                return new NotificationsTabFragment();
            case 2:
                return new AppliancesTabFragment();
            default:
                return new LiveTrackingTabFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
