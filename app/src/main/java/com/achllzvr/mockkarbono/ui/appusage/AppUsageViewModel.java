package com.achllzvr.mockkarbono.ui.appusage;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.achllzvr.mockkarbono.db.AppDatabase;
import com.achllzvr.mockkarbono.db.dao.AppUsageDao;
import com.achllzvr.mockkarbono.db.entities.AppUsage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;

public class AppUsageViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final MutableLiveData<List<AppUsage>> todayAppUsage = new MutableLiveData<>();
    private final MutableLiveData<Double> todayTotalCarbon = new MutableLiveData<>();
    private final MutableLiveData<Long> todayTotalDuration = new MutableLiveData<>();

    public AppUsageViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
        loadTodayData();
    }

    public LiveData<List<AppUsage>> getTodayAppUsage() {
        return todayAppUsage;
    }

    public LiveData<Double> getTodayTotalCarbon() {
        return todayTotalCarbon;
    }

    public LiveData<Long> getTodayTotalDuration() {
        return todayTotalDuration;
    }

    public void loadTodayData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<AppUsage> allUsage = db.appUsageDao().getLatest(1000);

            List<AppUsage> todayUsage = new ArrayList<>();
            double totalCO2 = 0.0;
            long totalDuration = 0;

            long now = System.currentTimeMillis();
            for (AppUsage usage : allUsage) {
                if (isSameDay(usage.clientCreatedAtMs, now)) {
                    todayUsage.add(usage);
                    totalCO2 += usage.estimatedKgCO2;
                    totalDuration += usage.durationMs;
                }
            }

            // Update LiveData on main thread
            double finalTotalCO2 = totalCO2;
            long finalTotalDuration = totalDuration;

            todayAppUsage.postValue(todayUsage);
            todayTotalCarbon.postValue(finalTotalCO2);
            todayTotalDuration.postValue(finalTotalDuration);
        });
    }

    private boolean isSameDay(long timestamp1, long timestamp2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTimeInMillis(timestamp1);
        cal2.setTimeInMillis(timestamp2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}

