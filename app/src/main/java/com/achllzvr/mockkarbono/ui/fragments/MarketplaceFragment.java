package com.achllzvr.mockkarbono.ui.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.achllzvr.mockkarbono.R;
import com.achllzvr.mockkarbono.api.ApiClient;
import com.achllzvr.mockkarbono.api.data.models.TreeModel;
import com.achllzvr.mockkarbono.api.data.models.TreeResponse;
import com.achllzvr.mockkarbono.ui.adapters.MarketplaceAdapter;
import com.achllzvr.mockkarbono.BuildConfig;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MarketplaceFragment extends Fragment {

    // IMPORTANT: Make sure this is your generated key from Evertreen Dashboard
    private static final String API_KEY = BuildConfig.EVERTREEN_API_KEY;

    // Views
    private RecyclerView recyclerView;
    private MarketplaceAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvError;
    private TextView tvEmpty;
    private LinearLayout llPartnerLabel;

    // Tabs
    private TextView tabPlantTrees;
    private TextView tabAppliances;
    private TextView tabThrift;

    // State
    private int currentTab = 0; // 0=Trees, 1=Appliances, 2=Thrift

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_marketplace, container, false);

        // Bind Views
        recyclerView = view.findViewById(R.id.recyclerMarketplace);
        progressBar = view.findViewById(R.id.progressBar);
        tvError = view.findViewById(R.id.tvError);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        llPartnerLabel = view.findViewById(R.id.llPartnerLabel);

        // Bind Tabs
        tabPlantTrees = view.findViewById(R.id.tabPlantTrees);
        tabAppliances = view.findViewById(R.id.tabAppliances);
        tabThrift = view.findViewById(R.id.tabThrift);

        // Setup Recycler
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new MarketplaceAdapter();
        recyclerView.setAdapter(adapter);

        // Setup Tab Clicks
        setupTabs();

        // Load Initial Data (Trees)
        selectTab(0);

        return view;
    }

    private void setupTabs() {
        tabPlantTrees.setOnClickListener(v -> selectTab(0));
        tabAppliances.setOnClickListener(v -> selectTab(1));
        tabThrift.setOnClickListener(v -> selectTab(2));
    }

    private void selectTab(int index) {
        currentTab = index;

        // 1. Reset UI State
        resetTabStyles();
        tvError.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        // 2. Highlight Selected Tab
        TextView selectedView = null;
        switch (index) {
            case 0: selectedView = tabPlantTrees; break;
            case 1: selectedView = tabAppliances; break;
            case 2: selectedView = tabThrift; break;
        }

        if (selectedView != null) {
            selectedView.setTextColor(ContextCompat.getColor(requireContext(), R.color.matcha_green));
            selectedView.setBackgroundResource(R.drawable.bg_pill_green); // Ensure you have a selected background drawable or just change text color
            // For simple text-only switching without custom drawables:
            selectedView.setTypeface(null, android.graphics.Typeface.BOLD);
        }

        // 3. Load Content
        switch (index) {
            case 0: // Trees
                llPartnerLabel.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
                fetchTrees();
                break;
            case 1: // Appliances
                llPartnerLabel.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                showComingSoon("Eco-Friendly Appliances coming soon!");
                break;
            case 2: // Thrift
                llPartnerLabel.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                showComingSoon("Local Thrift Shops coming soon!");
                break;
        }
    }

    private void resetTabStyles() {
        int defaultColor = ContextCompat.getColor(requireContext(), R.color.text_secondary);

        tabPlantTrees.setTextColor(defaultColor);
        tabPlantTrees.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabPlantTrees.setBackground(null);

        tabAppliances.setTextColor(defaultColor);
        tabAppliances.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabAppliances.setBackground(null);

        tabThrift.setTextColor(defaultColor);
        tabThrift.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabThrift.setBackground(null);
    }

    private void showComingSoon(String message) {
        tvEmpty.setText(message);
        tvEmpty.setVisibility(View.VISIBLE);
        // Optional: Set a specific icon for empty state if you have one
    }

    private void fetchTrees() {
        // Only fetch if we are on the tree tab
        if (currentTab != 0) return;

        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE); // Hide while loading

        ApiClient.getService().getTrees(API_KEY).enqueue(new Callback<TreeResponse>() {
            @Override
            public void onResponse(Call<TreeResponse> call, Response<TreeResponse> response) {
                // Check if user switched tabs while loading
                if (currentTab != 0) return;

                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<TreeModel> trees = response.body().trees;

                    if (trees != null && !trees.isEmpty()) {
                        adapter.setTrees(trees);
                        recyclerView.setVisibility(View.VISIBLE);
                    } else {
                        tvEmpty.setText("No trees available right now.");
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                } else {
                    String errorMsg = "Error: " + response.code();
                    if (response.code() == 401) errorMsg += "\n(Check API Key)";
                    tvError.setText(errorMsg);
                    tvError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<TreeResponse> call, Throwable t) {
                if (currentTab != 0) return;

                progressBar.setVisibility(View.GONE);
                tvError.setText("Connection Failed");
                tvError.setVisibility(View.VISIBLE);
            }
        });
    }
}