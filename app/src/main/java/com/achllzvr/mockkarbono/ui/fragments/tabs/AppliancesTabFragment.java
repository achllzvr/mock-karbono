package com.achllzvr.mockkarbono.ui.fragments.tabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.achllzvr.mockkarbono.R;
import com.achllzvr.mockkarbono.db.AppDatabase;
import com.achllzvr.mockkarbono.db.entities.ApplianceLog;
import com.achllzvr.mockkarbono.ui.adapters.ApplianceAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.concurrent.Executors;

public class AppliancesTabFragment extends Fragment {

    private TextView tvAppliancesCarbon;
    private RecyclerView rvAppliancesPreview;
    private FloatingActionButton fabAddAppliance;
    private ApplianceAdapter adapter;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_track_appliances, container, false);

        db = AppDatabase.getInstance(requireContext());

        // Bind views
        tvAppliancesCarbon = view.findViewById(R.id.tvAppliancesCarbon);
        rvAppliancesPreview = view.findViewById(R.id.rvAppliancesPreview);
        fabAddAppliance = view.findViewById(R.id.fabAddAppliance);

        // Setup RecyclerView (preview mode - show top 3)
        adapter = new ApplianceAdapter(null, null); // No edit/delete in preview
        rvAppliancesPreview.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvAppliancesPreview.setAdapter(adapter);

        // FAB click - navigate to full appliances page
        fabAddAppliance.setOnClickListener(v -> {
            // Navigate to AppliancesFragment
            if (getParentFragment() != null) {
                requireActivity().findViewById(R.id.bottomNavigation).performClick();
                // Trigger navigation via bottom nav
                ((com.google.android.material.bottomnavigation.BottomNavigationView)
                    requireActivity().findViewById(R.id.bottomNavigation))
                    .setSelectedItemId(R.id.navigation_appliances);
            }
        });

        // Load data
        loadAppliances();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAppliances();
    }

    private void loadAppliances() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<ApplianceLog> appliances = db.applianceDao().getAll();

            double totalCO2 = 0.0;
            for (ApplianceLog app : appliances) {
                totalCO2 += app.estimatedKgCO2PerDay;
            }

            // Get top 3 for preview
            List<ApplianceLog> preview = appliances.size() > 3
                ? appliances.subList(0, 3)
                : appliances;

            double finalTotalCO2 = totalCO2;

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    tvAppliancesCarbon.setText(String.format("%.2f kg CO₂", finalTotalCO2));
                    adapter.setItems(preview);
                });
            }
        });
    }
}

