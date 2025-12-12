package com.achllzvr.mockkarbono.ui.views;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.achllzvr.mockkarbono.R;
import com.achllzvr.mockkarbono.ui.adapters.MarketItem;

import java.util.List;

public class MarketplaceAdapter extends RecyclerView.Adapter<MarketplaceAdapter.ViewHolder> {

    private List<MarketItem> items;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(MarketItem item);
    }

    public MarketplaceAdapter(List<MarketItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    // Update the list when tabs are switched
    public void updateItems(List<MarketItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // IMPORTANT: Inflate the new GRID layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_marketplace_grid, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MarketItem item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgItem;
        TextView tvTitle;
        TextView tvDesc;
        TextView tvLocation; // New Location Badge
        Button btnAction;    // New Green Button

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgItem = itemView.findViewById(R.id.imgMarketItem);
            tvTitle = itemView.findViewById(R.id.tvItemTitle);
            tvDesc = itemView.findViewById(R.id.tvItemDescription);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            btnAction = itemView.findViewById(R.id.btnAction);
        }

        private CharSequence replaceEmojiWithIcon(android.content.Context context, String text, int iconResId) {
            android.text.SpannableString spannable = new android.text.SpannableString(text);
            int index = text.indexOf("🌳"); // The placeholder to replace

            if (index != -1) {
                // 1. Get the drawable
                android.graphics.drawable.Drawable drawable = androidx.core.content.ContextCompat.getDrawable(context, iconResId);

                if (drawable != null) {
                    // 2. Set bounds (Size of the icon inline)
                    // matching the text size (approx 16-20dp)
                    int size = (int) (20 * context.getResources().getDisplayMetrics().density);
                    drawable.setBounds(0, 0, size, size);

                    // 3. Tint it white (since your button is green) or match text color
                    drawable.setTint(android.graphics.Color.WHITE);

                    // 4. Create the ImageSpan
                    android.text.style.ImageSpan imageSpan = new android.text.style.ImageSpan(drawable, android.text.style.ImageSpan.ALIGN_CENTER);

                    // 5. Replace the 1 character (🌳) with the image
                    spannable.setSpan(imageSpan, index, index + 2, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // +2 because emojis are sometimes 2 chars
                }
            }
            return spannable;
        }

        public void bind(final MarketItem item, final OnItemClickListener listener) {
            tvTitle.setText(item.getTitle());
            tvDesc.setText(item.getDescription());
            imgItem.setImageResource(item.getImageResId());

            // Set the price/action text
            if (btnAction != null) {
                CharSequence styledText = replaceEmojiWithIcon(
                        itemView.getContext(),
                        item.getPrice(),
                        R.drawable.ic_tree_line
                );

                btnAction.setText(styledText);

                btnAction.setOnClickListener(v -> listener.onItemClick(item));
            }

            // Hardcoded location logic for demo (or add 'location' field to MarketItem)
            if (tvLocation != null) {
                if (item.getTitle().contains("Palawan")) tvLocation.setText("Palawan");
                else if (item.getTitle().contains("Cebu")) tvLocation.setText("Cebu");
                else if (item.getTitle().contains("Mindanao")) tvLocation.setText("Davao");
                else tvLocation.setText("PH");
            }

            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}
