package com.achllzvr.mockkarbono.ui.fragments;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.achllzvr.mockkarbono.utils.CarbonUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

public class AppliancesFragment extends Fragment {

    private TextView tvAppliancesTotalCarbon;
    private RecyclerView rvAllAppliances;
    private FloatingActionButton fabAddApplianceFull;
    private ApplianceAdapter adapter;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appliances, container, false);

        db = AppDatabase.getInstance(requireContext());

        // Bind views
        tvAppliancesTotalCarbon = view.findViewById(R.id.tvAppliancesTotalCarbon);
        rvAllAppliances = view.findViewById(R.id.rvAllAppliances);
        fabAddApplianceFull = view.findViewById(R.id.fabAddApplianceFull);

        // Setup RecyclerView
        adapter = new ApplianceAdapter(this::showEditApplianceDialog, this::deleteAppliance);
        rvAllAppliances.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvAllAppliances.setAdapter(adapter);

        // Setup FAB
        fabAddApplianceFull.setOnClickListener(v -> showAddApplianceDialog());

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

            double finalTotalCO2 = totalCO2;

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    tvAppliancesTotalCarbon.setText(String.format("%.2f kg CO₂", finalTotalCO2));
                    adapter.setItems(appliances);
                });
            }
        });
    }

    private void showAddApplianceDialog() {
        showApplianceDialog(null);
    }

    private void showEditApplianceDialog(ApplianceLog appliance) {
        showApplianceDialog(appliance);
    }

    private void showApplianceDialog(@Nullable ApplianceLog existingAppliance) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_add_appliance);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText etApplianceName = dialog.findViewById(R.id.etApplianceName);
        EditText etDailyHours = dialog.findViewById(R.id.etDailyHours);
        EditText etPowerConsumption = dialog.findViewById(R.id.etPowerConsumption);
        TextView tvEstimatedImpact = dialog.findViewById(R.id.tvEstimatedImpact);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnSave = dialog.findViewById(R.id.btnSave);
        TextView tvDialogTitle = dialog.findViewById(R.id.tvDialogTitle);

        // Pre-fill if editing
        if (existingAppliance != null) {
            tvDialogTitle.setText("Edit Appliance");
            etApplianceName.setText(existingAppliance.name);
            etDailyHours.setText(String.valueOf(existingAppliance.hoursPerDay));
            etPowerConsumption.setText(String.valueOf(existingAppliance.typicalWattage));
        }

        // Update estimate as user types
        android.text.TextWatcher textWatcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateEstimate(etDailyHours, etPowerConsumption, tvEstimatedImpact);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        };

        etDailyHours.addTextChangedListener(textWatcher);
        etPowerConsumption.addTextChangedListener(textWatcher);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name = etApplianceName.getText().toString().trim();
            String hoursStr = etDailyHours.getText().toString().trim();
            String wattsStr = etPowerConsumption.getText().toString().trim();

            if (name.isEmpty() || hoursStr.isEmpty() || wattsStr.isEmpty()) {
                android.widget.Toast.makeText(requireContext(), "Please fill all fields", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double hours = Double.parseDouble(hoursStr);
                int watts = Integer.parseInt(wattsStr);

                ApplianceLog appliance = existingAppliance != null ? existingAppliance : new ApplianceLog();
                if (appliance.uuid == null) {
                    appliance.uuid = UUID.randomUUID().toString();
                }
                appliance.name = name;
                appliance.hoursPerDay = hours;
                appliance.typicalWattage = watts;
                appliance.estimatedKgCO2PerDay = calculateCO2(hours, watts);
                appliance.clientCreatedAtMs = System.currentTimeMillis();
                appliance.synced = false;

                saveAppliance(appliance, existingAppliance != null);
                dialog.dismiss();

            } catch (NumberFormatException e) {
                android.widget.Toast.makeText(requireContext(), "Invalid number format", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void updateEstimate(EditText etDailyHours, EditText etPowerConsumption, TextView tvEstimatedImpact) {
        try {
            String hoursStr = etDailyHours.getText().toString();
            String wattsStr = etPowerConsumption.getText().toString();

            if (!hoursStr.isEmpty() && !wattsStr.isEmpty()) {
                double hours = Double.parseDouble(hoursStr);
                int watts = Integer.parseInt(wattsStr);
                double co2 = calculateCO2(hours, watts);
                tvEstimatedImpact.setText(String.format("Estimated: ~%.2f kg CO₂/day", co2));
            }
        } catch (NumberFormatException e) {
            // Ignore
        }
    }

    private double calculateCO2(double hours, int watts) {
        double wh = watts * hours;
        return CarbonUtils.whToKgCO2(wh);
    }

    private void saveAppliance(ApplianceLog appliance, boolean isUpdate) {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (isUpdate) {
                db.applianceDao().update(appliance);
            } else {
                db.applianceDao().insert(appliance);
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    android.widget.Toast.makeText(requireContext(),
                        isUpdate ? "Appliance updated" : "Appliance added",
                        android.widget.Toast.LENGTH_SHORT).show();
                    loadAppliances();
                });
            }
        });
    }

    private void deleteAppliance(ApplianceLog appliance) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Appliance")
            .setMessage("Are you sure you want to delete " + appliance.name + "?")
            .setPositiveButton("Delete", (dialog, which) -> {
                Executors.newSingleThreadExecutor().execute(() -> {
                    // Room doesn't have delete by default, so we'll need to add it or use update
                    // For now, we'll just reload (in production, add delete method to DAO)
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            android.widget.Toast.makeText(requireContext(), "Delete not implemented yet", android.widget.Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}

