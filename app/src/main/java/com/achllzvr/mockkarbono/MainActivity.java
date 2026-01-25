package com.achllzvr.mockkarbono;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.achllzvr.mockkarbono.ui.appusage.AppUsageAdapter;
import com.achllzvr.mockkarbono.ui.appusage.AppUsageViewModel;
import com.achllzvr.mockkarbono.ui.fragments.DashboardFragmentNew;
import com.achllzvr.mockkarbono.ui.fragments.MarketplaceFragment;
import com.achllzvr.mockkarbono.ui.fragments.CommunityFragment;
import com.achllzvr.mockkarbono.ui.fragments.ProfileFragment;
import com.achllzvr.mockkarbono.tracking.ScreenReceiver;
import com.achllzvr.mockkarbono.utils.PermissionHelper;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "KarbonoDebug";
    private ScreenReceiver screenReceiver = new ScreenReceiver();
    private TextView txtUnsynced;
    private AppUsageAdapter adapter;
    private AppUsageViewModel vm;
    private ImageView topBarAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("(DEBUG) " +TAG, "MainActivity.onCreate");

        topBarAvatar = findViewById(R.id.imgAvatar);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_dashboard) {
                fragment = new DashboardFragmentNew();
            } else if (itemId == R.id.navigation_marketplace) {
                fragment = new MarketplaceFragment();
            } else if (itemId == R.id.navigation_community) {
                fragment = new CommunityFragment();
            } else if (itemId == R.id.navigation_profile) {
                fragment = new ProfileFragment();
            }

            if (fragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .commit();
            }
            return true;
        });

        // Top bar avatar click listener
        if (topBarAvatar != null) {
            topBarAvatar.setOnClickListener(v -> {
                // Navigate to ProfileFragment
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new ProfileFragment())
                        .addToBackStack(null) // So user can press back
                        .commit();
                // Also select the profile tab in the bottom nav
                bottomNav.setSelectedItemId(R.id.navigation_profile);
            });
        }

        // Load default fragment (Dashboard)
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new DashboardFragmentNew())
                    .commit();
        }

        // Check permissions on startup
        checkAndRequestPermissions();
    }

    public void setTopBarAvatarVisibility(int visibility) {
        if (topBarAvatar != null) {
            topBarAvatar.setVisibility(visibility);
        }
    }

    private void checkAndRequestPermissions() {
        // Delay permission check to not interrupt app startup
        new android.os.Handler().postDelayed(() -> {
            if (!PermissionHelper.hasAllPermissions(this)) {
                PermissionHelper.requestAllPermissions(this);
            }
        }, 2000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("(DEBUG) " +TAG, "MainActivity.onResume - registering ScreenReceiver");
        registerReceiver(screenReceiver, ScreenReceiver.makeFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("(DEBUG) " +TAG, "MainActivity.onPause - unregistering ScreenReceiver");
        try {
            unregisterReceiver(screenReceiver);
        } catch (IllegalArgumentException e) {
            Log.w("(DEBUG) " +TAG, "Receiver already unregistered");
        }
    }

    public boolean isNotificationServiceEnabled(Context context) {
        String pkg = context.getPackageName();
        final String flat = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        Log.d("(DEBUG) Notif Service Enabled (?) - Main Activity", "enabled_notification_listeners=" + flat);
        if (flat != null) {
            final String[] names = flat.split(":");
            for (String name : names) {
                ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null && pkg.equals(cn.getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }
}
