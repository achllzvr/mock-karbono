package com.achllzvr.mockkarbono.ui.notifications;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.achllzvr.mockkarbono.db.AppDatabase;
import com.achllzvr.mockkarbono.db.entities.NotificationEvent;

import java.util.List;

public class NotificationsViewModel extends AndroidViewModel {

    private final LiveData<List<NotificationEvent>> todayNotifications;
    private final LiveData<Double> todayTotalCarbon;

    public NotificationsViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        todayNotifications = db.notificationEventDao().getTodayNotifications();
        todayTotalCarbon = db.notificationEventDao().getTodayTotalCarbon();
    }

    public LiveData<List<NotificationEvent>> getTodayNotifications() {
        return todayNotifications;
    }

    public LiveData<Double> getTodayTotalCarbon() {
        return todayTotalCarbon;
    }
}