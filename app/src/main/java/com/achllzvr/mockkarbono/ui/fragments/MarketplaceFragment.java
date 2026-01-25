package com.achllzvr.mockkarbono.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.achllzvr.mockkarbono.R;
import com.achllzvr.mockkarbono.api.ApiClient;
import com.achllzvr.mockkarbono.api.data.models.MarketplaceGoal;
import com.achllzvr.mockkarbono.ui.adapters.MarketplaceAdapter;
import com.achllzvr.mockkarbono.utils.PrefsManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MarketplaceFragment extends Fragment {

    // Views
    private RecyclerView recyclerView;
    private MarketplaceAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvError;
    private TextView tvEmpty;
    private LinearLayout llPartnerLabel;

    // Tabs
    private TextView tabPlantTrees;
    private TextView tabAppliances; // Note: You might rename this to "Community" later
    private TextView tabThrift;

    // State
    private int currentTab = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_marketplace, container, false);

        recyclerView = view.findViewById(R.id.recyclerMarketplace);
        progressBar = view.findViewById(R.id.progressBar);
        tvError = view.findViewById(R.id.tvError);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        llPartnerLabel = view.findViewById(R.id.llPartnerLabel);

        tabPlantTrees = view.findViewById(R.id.tabPlantTrees);
        tabAppliances = view.findViewById(R.id.tabAppliances);
        tabThrift = view.findViewById(R.id.tabThrift);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new MarketplaceAdapter();
        recyclerView.setAdapter(adapter);

        setupTabs();
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
        resetTabStyles();
        tvError.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        // Highlight Logic (Simplified)
        TextView selectedView = (index == 0) ? tabPlantTrees : (index == 1) ? tabAppliances : tabThrift;
        selectedView.setTextColor(ContextCompat.getColor(requireContext(), R.color.matcha_green));
        selectedView.setTypeface(null, android.graphics.Typeface.BOLD);

        if (index == 0) {
            llPartnerLabel.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            fetchGoals(); // Call the NEW API method
        } else {
            llPartnerLabel.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            showComingSoon("Feature coming soon!");
        }
    }

    private void resetTabStyles() {
        int defaultColor = ContextCompat.getColor(requireContext(), R.color.text_secondary);
        tabPlantTrees.setTextColor(defaultColor);
        tabPlantTrees.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabAppliances.setTextColor(defaultColor);
        tabAppliances.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabThrift.setTextColor(defaultColor);
        tabThrift.setTypeface(null, android.graphics.Typeface.NORMAL);
    }

    private void showComingSoon(String message) {
        tvEmpty.setText(message);
        tvEmpty.setVisibility(View.VISIBLE);
    }

    private void fetchGoals() {
        if (currentTab != 0) return;

        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        // NEW: Call the Hostinger API instead of Evertreen directly
        ApiClient.getService().getMarketplaceGoals().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (currentTab != 0) return;
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();

                    // Parse the "data" array from JSON
                    if (body.has("data")) {
                        JsonArray dataArray = body.getAsJsonArray("data");
                        Type listType = new TypeToken<List<MarketplaceGoal>>(){}.getType();
                        List<MarketplaceGoal> goals = new Gson().fromJson(dataArray, listType);

                        if (goals != null && !goals.isEmpty()) {
                            adapter.setGoals(goals);
                            recyclerView.setVisibility(View.VISIBLE);
                        } else {
                            showComingSoon("No active campaigns right now.");
                        }
                    } else {
                        showComingSoon("No data found.");
                    }
                } else {
                    tvError.setText("Server Error: " + response.code());
                    tvError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (currentTab != 0) return;
                progressBar.setVisibility(View.GONE);
                tvError.setText("Connection Failed");
                tvError.setVisibility(View.VISIBLE);
            }
        });
    }
}