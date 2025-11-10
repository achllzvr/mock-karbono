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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.achllzvr.mockkarbono.db.entities.AppUsage;
import com.achllzvr.mockkarbono.db.entities.NotificationEvent;
import com.google.firebase.auth.FirebaseAuth;
import com.achllzvr.mockkarbono.ui.AppUsageAdapter;
import com.achllzvr.mockkarbono.ui.AppUsageViewModel;
import com.achllzvr.mockkarbono.tracking.SyncWorker;
import com.achllzvr.mockkarbono.tracking.ScreenReceiver;
import com.achllzvr.mockkarbono.db.AppDatabase;

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

        Button btnUsage = findViewById(R.id.btnUsageAccess);
        Button btnNotif = findViewById(R.id.btnNotifAccess);
        Button btnForceSync = findViewById(R.id.btnForceSync);
        Button btnShowCounts = findViewById(R.id.btnShowCounts);
        txtUnsynced = findViewById(R.id.txtUnsynced);

        btnUsage.setOnClickListener(v -> {
            Log.d("(DEBUG) " +TAG, "Usage access button clicked");
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        });

        btnNotif.setOnClickListener(v -> {
            Log.d("(DEBUG) " +TAG, "Notification access button clicked");
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        });

        btnForceSync.setOnClickListener(v -> {
            OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(SyncWorker.class).build();
            WorkManager.getInstance(this).enqueue(req);
            Log.d("(DEBUG) " +TAG, "Manual sync enqueued");
            Toast.makeText(this, "Sync started", Toast.LENGTH_SHORT).show();
        });

        btnShowCounts.setOnClickListener(v -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                int u = AppDatabase.getInstance(getApplicationContext()).appUsageDao().countUnsynced();
                int n = AppDatabase.getInstance(getApplicationContext()).notificationEventDao().countUnsynced();
                int a = AppDatabase.getInstance(getApplicationContext()).applianceDao().countUnsynced();
                runOnUiThread(() -> {
                    String text = "Unsynced: usage=" + u + " notifs=" + n + " appliances=" + a;
                    txtUnsynced.setText(text);
                    Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
                    Log.d("(DEBUG) " +TAG, text);
                });
            });
        });

        // inside onCreate, wire a btnSimulate
        Button btnSimulate = findViewById(R.id.btnSimulate); // add a button in layout
        btnSimulate.setOnClickListener(v -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    AppUsage u = new AppUsage();
                    u.uuid = UUID.randomUUID().toString();
                    u.packageName = "com.test.fake";
                    u.category = "test";
                    u.startTimeMs = System.currentTimeMillis() - 60000;
                    u.endTimeMs = System.currentTimeMillis();
                    u.durationMs = 60000;
                    u.estimatedWh = 5.0 * (1.0/60.0);
                    u.estimatedKgCO2 = (u.estimatedWh / 1000.0) * 0.8;
                    u.clientCreatedAtMs = System.currentTimeMillis();
                    u.synced = false;
                    long id = AppDatabase.getInstance(getApplicationContext()).appUsageDao().insert(u);
                    Log.d("KarbonoDebug", "Simulated AppUsage inserted id=" + id + " uuid=" + u.uuid);

                    NotificationEvent n = new NotificationEvent();
                    n.uuid = UUID.randomUUID().toString();
                    n.packageName = "com.test.fake";
                    n.category = "test";
                    n.timestampMs = System.currentTimeMillis();
                    n.estimatedKgCO2 = 0.0001;
                    n.clientCreatedAtMs = System.currentTimeMillis();
                    n.synced = false;
                    long nid = AppDatabase.getInstance(getApplicationContext()).notificationEventDao().insert(n);
                    Log.d("KarbonoDebug", "Simulated NotificationEvent inserted id=" + nid + " uuid=" + n.uuid);

                    int ucount = AppDatabase.getInstance(getApplicationContext()).appUsageDao().countUnsynced();
                    int ncount = AppDatabase.getInstance(getApplicationContext()).notificationEventDao().countUnsynced();
                    Log.d("KarbonoDebug", "After simulate unsynced counts usage=" + ucount + " notifs=" + ncount);
                } catch (Exception e) {
                    Log.e("KarbonoDebug", "Simulation insert failed", e);
                }
            });
        });

        // RecyclerView + adapter + ViewModel
        RecyclerView rv = findViewById(R.id.recycler);
        adapter = new AppUsageAdapter();
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(this));
        vm = new ViewModelProvider(this).get(AppUsageViewModel.class);
        vm.getList().observe(this, list -> {
            adapter.setItems(list);
        });
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