package com.achllzvr.mockkarbono;

import android.app.Application;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseAuth;

import com.achllzvr.mockkarbono.tracking.SyncWorker;
import com.achllzvr.mockkarbono.tracking.UsageQueryWorker;

import java.util.concurrent.TimeUnit;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Anonymous sign-in (for demo)
        FirebaseAuth.getInstance().signInAnonymously();
        Log.d("(DEBUG) Firebase Auth", "Signed in anonymously");

        // Schedule periodic workers
        PeriodicWorkRequest usageReq = new PeriodicWorkRequest.Builder(UsageQueryWorker.class, 15, TimeUnit.MINUTES)
                .build();

        Constraints syncConstraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        PeriodicWorkRequest syncReq = new PeriodicWorkRequest.Builder(SyncWorker.class, 30, TimeUnit.MINUTES)
                .setConstraints(syncConstraints)
                .build();

        WorkManager.getInstance(this).enqueue(usageReq);
        WorkManager.getInstance(this).enqueue(syncReq);
        Log.d("(DEBUG) Work Manager", "Scheduled workers");
    }
}