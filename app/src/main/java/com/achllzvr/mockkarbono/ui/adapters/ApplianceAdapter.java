package com.achllzvr.mockkarbono.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.achllzvr.mockkarbono.R;
import com.achllzvr.mockkarbono.db.entities.ApplianceLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ApplianceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_FULL = 1;
    private static final int VIEW_TYPE_PREVIEW = 2;

    private List<ApplianceLog> items = new ArrayList<>();
    private final EditListener editListener;
    private final DeleteListener deleteListener;

    public interface EditListener {
        void onEdit(ApplianceLog appliance);
    }

    public interface DeleteListener {
        void onDelete(ApplianceLog appliance);
    }

    public ApplianceAdapter(@Nullable EditListener editListener, @Nullable DeleteListener deleteListener) {
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    public void setItems(List<ApplianceLog> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        // If listeners are null, we are in preview mode
        return (editListener == null && deleteListener == null) ? VIEW_TYPE_PREVIEW : VIEW_TYPE_FULL;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_PREVIEW) {
            View view = inflater.inflate(R.layout.item_appliance_preview, parent, false);
            return new PreviewViewHolder(view);
        }
        // Default to the full view
        View view = inflater.inflate(R.layout.item_appliance_full, parent, false);
        return new FullViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ApplianceLog item = items.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_PREVIEW) {
            ((PreviewViewHolder) holder).bind(item);
        } else {
            ((FullViewHolder) holder).bind(item, editListener, deleteListener);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ViewHolder for the full list item with edit/delete buttons
    static class FullViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDetails, tvCarbon;
        ImageButton btnEdit, btnDelete;

        public FullViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvApplianceName);
            tvDetails = itemView.findViewById(R.id.tvApplianceDetails);
            tvCarbon = itemView.findViewById(R.id.tvApplianceCarbon);
            btnEdit = itemView.findViewById(R.id.btnEditAppliance);
            btnDelete = itemView.findViewById(R.id.btnDeleteAppliance);
        }

        public void bind(final ApplianceLog appliance, final EditListener editListener, final DeleteListener deleteListener) {
            tvName.setText(appliance.name);
            tvDetails.setText(String.format(Locale.US, "%dW %.1f hrs/day", appliance.typicalWattage, appliance.hoursPerDay));
            tvCarbon.setText(String.format(Locale.US, "%.3f kg CO₂/day", appliance.estimatedKgCO2PerDay));

            btnEdit.setOnClickListener(v -> editListener.onEdit(appliance));
            btnDelete.setOnClickListener(v -> deleteListener.onDelete(appliance));
        }
    }

    // ViewHolder for the preview list item with a radio button
    static class PreviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDetails, tvCarbon;
        RadioButton radioActive;

        public PreviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvApplianceName);
            tvDetails = itemView.findViewById(R.id.tvApplianceDetails);
            tvCarbon = itemView.findViewById(R.id.tvApplianceCarbon);
            radioActive = itemView.findViewById(R.id.radioApplianceActive);
        }

        public void bind(final ApplianceLog appliance) {
            tvName.setText(appliance.name);
            tvDetails.setText(String.format(Locale.US, "%dW %.1f hrs/day", appliance.typicalWattage, appliance.hoursPerDay));
            tvCarbon.setText(String.format(Locale.US, "%.3f kg CO₂/day", appliance.estimatedKgCO2PerDay));
            radioActive.setChecked(true); // Default to active in preview
        }
    }
}
