package com.achllzvr.mockkarbono.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.achllzvr.mockkarbono.R;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private final List<Transaction> items;

    public TransactionAdapter(List<Transaction> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgTransactionIcon;
        TextView tvTransactionTitle;
        TextView tvTransactionDate;
        TextView tvTransactionValue;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgTransactionIcon = itemView.findViewById(R.id.imgTransactionIcon);
            tvTransactionTitle = itemView.findViewById(R.id.tvTransactionTitle);
            tvTransactionDate = itemView.findViewById(R.id.tvTransactionDate);
            tvTransactionValue = itemView.findViewById(R.id.tvTransactionValue);
        }

        public void bind(Transaction transaction) {
            tvTransactionTitle.setText(transaction.getTitle());
            tvTransactionDate.setText(transaction.getDate());
            tvTransactionValue.setText(transaction.getValue());

            imgTransactionIcon.setImageResource(transaction.getIconResId());

            // Set icon tint based on transaction type
            if (transaction.getValue().contains("Lives")) {
                imgTransactionIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.matcha_green));
            } else {
                imgTransactionIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.marigold));
            }
        }
    }

    public static class Transaction {
        private final String title;
        private final String date;
        private final String value;
        private final int iconResId;

        public Transaction(String title, String date, String value, int iconResId) {
            this.title = title;
            this.date = date;
            this.value = value;
            this.iconResId = iconResId;
        }

        public String getTitle() {
            return title;
        }

        public String getDate() {
            return date;
        }

        public String getValue() {
            return value;
        }

        public int getIconResId() {
            return iconResId;
        }
    }
}
