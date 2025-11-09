package com.achllzvr.mockkarbono;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.achllzvr.mockkarbono.tracking.ScreenReceiver;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();

        // Anonymous sign-in
        if (auth.getCurrentUser() == null) {
            auth.signInAnonymously().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // signed in
                } else {
                    // handle failure (for dev, log)
                }
            });
        }

        Button btnUsage = findViewById(R.id.btnUsageAccess);
        Button btnNotif = findViewById(R.id.btnNotifAccess);
        TextView info = findViewById(R.id.txtInfo);
        info.setText("Please enable Usage access and Notification access for Karbono to track app usage and notifications. No message content is collected.");

        btnUsage.setOnClickListener(v -> {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
    });

        btnNotif.setOnClickListener(v -> {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        startActivity(intent);
    });
    }

    private ScreenReceiver screenReceiver = new ScreenReceiver();

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(screenReceiver);
        } catch (IllegalArgumentException e) {
            // ignore
        }
    }

}