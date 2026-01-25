package com.achllzvr.mockkarbono.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.achllzvr.mockkarbono.OnboardingActivity;
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
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MarketplaceFragment extends Fragment {

    private RecyclerView recyclerView;
    private MarketplaceAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_marketplace, container, false);

        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        recyclerView = view.findViewById(R.id.recyclerMarketplace);

        // Setup Recycler
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new MarketplaceAdapter(this::handleContribute);
        recyclerView.setAdapter(adapter);

        // Setup Refresh
        swipeRefresh.setOnRefreshListener(this::fetchGoals);

        // Initial Load
        fetchGoals();

        return view;
    }

    private void handleContribute(MarketplaceGoal goal) {
        if (isGuest()) {
            showLoginPrompt();
        } else {
            // Logic for logged-in users (e.g., Open payment dialog)
            Toast.makeText(getContext(), "Contribute to: " + goal.title, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isGuest() {
        com.achllzvr.mockkarbono.utils.PrefsManager prefs = new com.achllzvr.mockkarbono.utils.PrefsManager(getContext());
        return prefs.getToken() == null || prefs.getToken().isEmpty();
    }

    private void showLoginPrompt() {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Login Required")
                .setMessage("You need a Karbono account to contribute.")
                .setPositiveButton("Log In", (dialog, which) -> {
                    android.content.Intent intent = new android.content.Intent(getActivity(), com.achllzvr.mockkarbono.OnboardingActivity.class);
                    intent.putExtra("force_login", true);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void fetchGoals() {
        swipeRefresh.setRefreshing(true);

        ApiClient.getService().getMarketplaceGoals().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (getContext() == null) return;
                swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    if (body.has("data")) {
                        JsonArray dataArray = body.getAsJsonArray("data");
                        Type listType = new TypeToken<List<MarketplaceGoal>>(){}.getType();
                        List<MarketplaceGoal> goals = new Gson().fromJson(dataArray, listType);

                        if (goals != null) {
                            adapter.setGoals(goals);
                        }
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load goals", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (getContext() == null) return;
                swipeRefresh.setRefreshing(false);
                Toast.makeText(getContext(), "Connection Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}