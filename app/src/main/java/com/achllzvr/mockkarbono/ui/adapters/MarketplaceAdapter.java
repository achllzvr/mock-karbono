package com.achllzvr.mockkarbono.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.achllzvr.mockkarbono.R;
import com.achllzvr.mockkarbono.api.data.models.TreeModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MarketplaceAdapter extends RecyclerView.Adapter<MarketplaceAdapter.TreeViewHolder> {

    // Initialize with empty list to prevent initial null crash
    private List<TreeModel> treeList = new ArrayList<>();
    private Context context;

    public void setTrees(List<TreeModel> trees) {
        // SAFETY CHECK: If trees is null, use an empty list instead
        this.treeList = (trees != null) ? trees : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TreeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_marketplace_grid, parent, false);
        return new TreeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TreeViewHolder holder, int position) {
        TreeModel tree = treeList.get(position);

        holder.tvName.setText(tree.name);

        // Handle Price
        if (tree.price != null) {
            double priceUsd = tree.price.usdCents / 100.0;
            holder.tvPrice.setText(String.format(Locale.US, "$%.2f", priceUsd));
        } else {
            holder.tvPrice.setText("$0.00");
        }

        // Handle CO2 Text
        holder.tvImpact.setText(String.format(Locale.US, "-%.0f kg CO₂", tree.co2Kg));

        // Handle Location
        if (tree.location != null) {
            holder.tvLocation.setText(tree.location.country);
        } else {
            holder.tvLocation.setText("Global");
        }

        // Image Loading with Glide
        if (tree.imageUrl != null && !tree.imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(tree.imageUrl)
                    .placeholder(R.drawable.ic_tree) // Make sure this drawable exists!
                    .centerCrop()
                    .into(holder.imgTree);
        }
    }

    @Override
    public int getItemCount() {
        // Double safety check
        return (treeList != null) ? treeList.size() : 0;
    }

    static class TreeViewHolder extends RecyclerView.ViewHolder {
        ImageView imgTree;
        TextView tvName, tvPrice, tvImpact, tvLocation;
        Button btnPlant;

        public TreeViewHolder(@NonNull View itemView) {
            super(itemView);
            imgTree = itemView.findViewById(R.id.imgTree);
            tvName = itemView.findViewById(R.id.tvTreeName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvImpact = itemView.findViewById(R.id.tvImpact);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            btnPlant = itemView.findViewById(R.id.btnPlant);
        }
    }
}