package com.achllzvr.mockkarbono.ui.notifications;

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

import java.util.Locale;

public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerNotifications;
    private View emptyState;
    private TextView tvNotificationCount;
    private TextView tvTotalCarbon;
    private NotificationAdapter adapter;
    private NotificationsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerNotifications = view.findViewById(R.id.recyclerNotifications);
        emptyState = view.findViewById(R.id.emptyState);
        tvNotificationCount = view.findViewById(R.id.tvNotificationCount);
        tvTotalCarbon = view.findViewById(R.id.tvTotalCarbon);

        adapter = new NotificationAdapter(requireContext());
        recyclerNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerNotifications.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);

        viewModel.getTodayNotifications().observe(getViewLifecycleOwner(), notifications -> {
            if (notifications == null || notifications.isEmpty()) {
                recyclerNotifications.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
                tvNotificationCount.setText("0 notifications today");
            } else {
                recyclerNotifications.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);
                adapter.submitList(notifications);

                String count = notifications.size() == 1
                    ? "1 notification today"
                    : notifications.size() + " notifications today";
                tvNotificationCount.setText(count);
            }
        });

        viewModel.getTodayTotalCarbon().observe(getViewLifecycleOwner(), total -> {
            if (total == null) total = 0.0;
            tvTotalCarbon.setText(String.format(Locale.getDefault(), "%.3f kg CO₂", total));
        });
    }
}