package com.achllzvr.mockkarbono;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.achllzvr.mockkarbono.db.entities.AppUsage;
import com.achllzvr.mockkarbono.db.entities.NotificationEvent;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.achllzvr.mockkarbono.ui.AppUsageAdapter;
import com.achllzvr.mockkarbono.ui.AppUsageViewModel;
import com.achllzvr.mockkarbono.ui.fragments.DashboardFragment;
import com.achllzvr.mockkarbono.ui.fragments.DashboardFragmentNew;
import com.achllzvr.mockkarbono.ui.fragments.TrackFragment;
import com.achllzvr.mockkarbono.ui.fragments.AppliancesFragment;
import com.achllzvr.mockkarbono.ui.fragments.SyncSettingsFragment;
import com.achllzvr.mockkarbono.ui.fragments.MarketplaceFragment;
import com.achllzvr.mockkarbono.ui.fragments.CommunityFragment;
import com.achllzvr.mockkarbono.ui.fragments.ProfileFragment;
import com.achllzvr.mockkarbono.tracking.SyncWorker;
import com.achllzvr.mockkarbono.tracking.ScreenReceiver;
import com.achllzvr.mockkarbono.db.AppDatabase;
import com.achllzvr.mockkarbono.utils.PermissionHelper;

import java.util.UUID;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "KarbonoDebug";
    private ScreenReceiver screenReceiver = new ScreenReceiver();
    private TextView txtUnsynced;
    private AppUsageAdapter adapter;
    private AppUsageViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("(DEBUG) " +TAG, "MainActivity.onCreate");

        // Firebase anonymous sign-in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("(DEBUG) " +TAG, "Firebase anonymous sign-in successful, uid=" + FirebaseAuth.getInstance().getUid());
                } else {
                    Log.e("(DEBUG) " +TAG, "Firebase sign-in failed", task.getException());
                }
            });
        }

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