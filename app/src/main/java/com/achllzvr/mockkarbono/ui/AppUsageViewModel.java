package com.achllzvr.mockkarbono.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.achllzvr.mockkarbono.db.AppDatabase;
import com.achllzvr.mockkarbono.db.entities.AppUsage;

import java.util.List;

public class AppUsageViewModel extends AndroidViewModel {
    private final AppDatabase db;
    private final LiveData<List<AppUsage>> liveList;

    public AppUsageViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
        // observe LiveData directly from Room (ensure DAO has observeAll method returning LiveData)
        liveList = db.appUsageDao().observeAll();
    }

    public LiveData<List<AppUsage>> getList() {
        return liveList;
    }
}