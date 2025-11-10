package com.achllzvr.mockkarbono.ui.fragments.tabs;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.achllzvr.mockkarbono.R;
import com.achllzvr.mockkarbono.db.AppDatabase;
import com.achllzvr.mockkarbono.db.entities.NotificationEvent;
import com.achllzvr.mockkarbono.ui.adapters.NotificationAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;

public class NotificationsTabFragment extends Fragment {

    private EditText etSearchNotifications;
    private TextView tvNotificationCount;
    private TextView tvNotificationCarbon;
    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private AppDatabase db;
    private List<NotificationEvent> allNotifications = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_track_notifications, container, false);

        db = AppDatabase.getInstance(requireContext());

        // Bind views
        etSearchNotifications = view.findViewById(R.id.etSearchNotifications);
        tvNotificationCount = view.findViewById(R.id.tvNotificationCount);
        tvNotificationCarbon = view.findViewById(R.id.tvNotificationCarbon);
        rvNotifications = view.findViewById(R.id.rvNotifications);

        // Setup RecyclerView
        adapter = new NotificationAdapter();
        rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvNotifications.setAdapter(adapter);

        // Setup search
        etSearchNotifications.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterNotifications(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Load data
        loadNotifications();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNotifications();
    }

    private void loadNotifications() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<NotificationEvent> notifications = db.notificationEventDao().getUnsynced();

            // Filter for today only
            List<NotificationEvent> todayNotifications = new ArrayList<>();
            double totalCO2 = 0.0;

            for (NotificationEvent notif : notifications) {
                if (isSameDay(notif.clientCreatedAtMs, System.currentTimeMillis())) {
                    todayNotifications.add(notif);
                    totalCO2 += notif.estimatedKgCO2;
                }
            }

            allNotifications = todayNotifications;
            double finalTotalCO2 = totalCO2;

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    tvNotificationCount.setText(String.format("%d notifications today", todayNotifications.size()));
                    tvNotificationCarbon.setText(String.format("%.4f kg CO₂", finalTotalCO2));
                    adapter.setItems(todayNotifications);
                });
            }
        });
    }

    private void filterNotifications(String query) {
        if (query.isEmpty()) {
            adapter.setItems(allNotifications);
            return;
        }

        List<NotificationEvent> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (NotificationEvent notif : allNotifications) {
            if (notif.packageName.toLowerCase().contains(lowerQuery) ||
                notif.category.toLowerCase().contains(lowerQuery)) {
                filtered.add(notif);
            }
        }

        adapter.setItems(filtered);
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

