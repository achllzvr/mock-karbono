package com.achllzvr.mockkarbono.tracking;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.achllzvr.mockkarbono.api.ApiClient;
import com.achllzvr.mockkarbono.api.KarbonoApiService;
import com.achllzvr.mockkarbono.db.AppDatabase;
import com.achllzvr.mockkarbono.db.entities.CarbonReference;
import com.achllzvr.mockkarbono.utils.PrefsManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Response;

public class SyncWorker extends Worker {

    private static final String TAG = "SyncWorker";
    private final AppDatabase db;
    private final KarbonoApiService api;
    private final PrefsManager prefs;

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        db = AppDatabase.getInstance(context);
        api = ApiClient.getService();
        prefs = new PrefsManager(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Starting Sync...");

        try {
            // 1. Check Authentication
            String token = prefs.getToken();
            if (token == null) {
                Log.e(TAG, "Sync failed: No Auth Token found. User might be logged out.");
                return Result.failure();
            }
            String authHeader = "Bearer " + token;

            // ---------------------------------------------------------
            // TASK A: DOWNLOAD LATEST MATH (The "Brain" Update)
            // ---------------------------------------------------------
            Response<JsonObject> refResponse = api.getCarbonReferences().execute();
            if (refResponse.isSuccessful() && refResponse.body() != null) {
                // Parse the "data" array from the JSON response
                JsonArray data = refResponse.body().getAsJsonArray("data");

                // Convert JSON to List<CarbonReference>
                Type listType = new TypeToken<List<CarbonReference>>(){}.getType();
                List<CarbonReference> references = new Gson().fromJson(data, listType);

                // Save to Local DB
                if (references != null && !references.isEmpty()) {
                    db.carbonReferenceDao().insertAll(references);
                    Log.d(TAG, "Updated Carbon References: " + references.size() + " items.");
                }
            } else {
                Log.w(TAG, "Failed to download references: " + refResponse.code());
            }

            // ---------------------------------------------------------
            // TASK B: REPORT UNKNOWN APPS (Crowdsourcing)
            // ---------------------------------------------------------
            // 1. Get all apps used locally
            List<String> localPackages = db.appUsageDao().getDistinctPackageNames();

            // 2. Get all known apps from the Reference table we just updated
            List<CarbonReference> knownRefs = db.carbonReferenceDao().getAll();
            List<String> knownPackages = knownRefs.stream()
                    .map(ref -> ref.packageName)
                    .collect(Collectors.toList());

            // 3. Find the difference (Local - Known = Unknown)
            List<String> unknownPackages = new ArrayList<>();
            for (String pkg : localPackages) {
                if (!knownPackages.contains(pkg)) {
                    unknownPackages.add(pkg);
                }
            }

            // 4. Upload if any found
            if (!unknownPackages.isEmpty()) {
                Log.d(TAG, "Found " + unknownPackages.size() + " unknown apps. Reporting...");
                api.reportUnknownPackages(unknownPackages).execute();
            }

            // ---------------------------------------------------------
            // TASK C: UPLOAD DAILY LOGS (For Streaks/Leaderboard)
            // ---------------------------------------------------------
            // Note: You would normally query your DailySummaryDao here for unsynced rows.
            // For now, let's assume we are syncing "Today's" data.
            // ... (Logic to bundle daily stats object) ...

            Log.d(TAG, "Sync Complete!");
            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Sync Error", e);
            return Result.retry();
        }
    }
}