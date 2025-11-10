package com.achllzvr.mockkarbono.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.achllzvr.mockkarbono.R;
import com.achllzvr.mockkarbono.db.entities.ApplianceLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ApplianceAdapter extends RecyclerView.Adapter<ApplianceAdapter.ViewHolder> {

    private final List<ApplianceLog> items = new ArrayList<>();
    private final OnApplianceClickListener editListener;
    private final OnApplianceClickListener deleteListener;

    public interface OnApplianceClickListener {
        void onClick(ApplianceLog appliance);
    }

    public ApplianceAdapter(OnApplianceClickListener editListener, OnApplianceClickListener deleteListener) {
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    public void setItems(List<ApplianceLog> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_appliance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ApplianceLog appliance = items.get(position);

        holder.tvApplianceName.setText(appliance.name);
        holder.tvApplianceWattage.setText(String.format(Locale.US, "%dW", appliance.typicalWattage));
        holder.tvApplianceHours.setText(String.format(Locale.US, "%.1f hrs/day", appliance.hoursPerDay));
        holder.tvApplianceCO2.setText(String.format(Locale.US, "%.3f kg CO₂/day", appliance.estimatedKgCO2PerDay));

        // Sync status
        if (appliance.synced) {
            holder.tvSyncStatus.setText("✓");
            holder.tvSyncStatus.setTextColor(holder.itemView.getContext()
                    .getResources().getColor(R.color.colorAccent, null));
        } else {
            holder.tvSyncStatus.setText("○");
            holder.tvSyncStatus.setTextColor(holder.itemView.getContext()
                    .getResources().getColor(R.color.colorCarbonGray, null));
        }

        // Edit button
        if (editListener != null) {
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnEdit.setOnClickListener(v -> editListener.onClick(appliance));
        } else {
            holder.btnEdit.setVisibility(View.GONE);
        }

        // Delete button
        if (deleteListener != null) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> deleteListener.onClick(appliance));
        } else {
            holder.btnDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvApplianceName;
        TextView tvApplianceWattage;
        TextView tvApplianceHours;
        TextView tvApplianceCO2;
        TextView tvSyncStatus;
        ImageButton btnEdit;
        ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvApplianceName = itemView.findViewById(R.id.tvApplianceName);
            tvApplianceWattage = itemView.findViewById(R.id.tvApplianceWattage);
            tvApplianceHours = itemView.findViewById(R.id.tvApplianceHours);
            tvApplianceCO2 = itemView.findViewById(R.id.tvApplianceCO2);
            tvSyncStatus = itemView.findViewById(R.id.tvApplianceSyncStatus);
            btnEdit = itemView.findViewById(R.id.btnEditAppliance);
            btnDelete = itemView.findViewById(R.id.btnDeleteAppliance);
        }
    }
}

