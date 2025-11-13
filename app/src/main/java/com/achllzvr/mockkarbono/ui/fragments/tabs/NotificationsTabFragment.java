package com.achllzvr.mockkarbono.ui.fragments.tabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.achllzvr.mockkarbono.R;
import com.achllzvr.mockkarbono.ui.notifications.NotificationAdapter;
import com.achllzvr.mockkarbono.ui.notifications.NotificationsViewModel;

import java.util.Locale;

public class NotificationsTabFragment extends Fragment {

    private TextView tvNotificationCount;
    private TextView tvNotificationCarbon;
    private RecyclerView rvNotifications;
    private View emptyState;
    private NotificationAdapter adapter;
    private NotificationsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_track_notifications, container, false);

        // Bind views
        tvNotificationCount = view.findViewById(R.id.tvNotificationCount);
        tvNotificationCarbon = view.findViewById(R.id.tvNotificationCarbon);
        rvNotifications = view.findViewById(R.id.rvNotifications);
        emptyState = view.findViewById(R.id.emptyState);

        // Setup RecyclerView
        adapter = new NotificationAdapter(requireContext());
        rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvNotifications.setAdapter(adapter);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);

        // Observe today's notifications (synced and unsynced)
        viewModel.getTodayNotifications().observe(getViewLifecycleOwner(), notifications -> {
            if (notifications == null || notifications.isEmpty()) {
                rvNotifications.setVisibility(View.GONE);
                if (emptyState != null) {
                    emptyState.setVisibility(View.VISIBLE);
                }
                tvNotificationCount.setText("0");
            } else {
                rvNotifications.setVisibility(View.VISIBLE);
                if (emptyState != null) {
                    emptyState.setVisibility(View.GONE);
                }
                adapter.submitList(notifications);
                tvNotificationCount.setText(String.valueOf(notifications.size()));
            }
        });

        // Observe total carbon
        viewModel.getTodayTotalCarbon().observe(getViewLifecycleOwner(), total -> {
            if (total == null) total = 0.0;
            tvNotificationCarbon.setText(String.format(Locale.getDefault(), "%.3f kg", total));
        });

        return view;
    }
}

