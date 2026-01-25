package com.achllzvr.mockkarbono.ui.fragments;

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

import com.achllzvr.mockkarbono.R;
import com.achllzvr.mockkarbono.api.ApiClient;
import com.achllzvr.mockkarbono.api.data.models.MarketplaceGoal;
import com.achllzvr.mockkarbono.ui.adapters.MarketplaceAdapter;
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
        adapter = new MarketplaceAdapter();
        recyclerView.setAdapter(adapter);

        // Setup Refresh
        swipeRefresh.setOnRefreshListener(this::fetchGoals);

        // Initial Load
        fetchGoals();

        return view;
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