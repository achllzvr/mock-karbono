package com.achllzvr.mockkarbono.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.achllzvr.mockkarbono.R;
import com.achllzvr.mockkarbono.api.ApiClient;
import com.achllzvr.mockkarbono.api.data.models.LeaderboardEntry;
import com.achllzvr.mockkarbono.ui.adapters.LeaderboardAdapter;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommunityFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LeaderboardAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);

        progressBar = view.findViewById(R.id.progressBar);
        recyclerView = view.findViewById(R.id.recyclerLeaderboard);

        // Setup Recycler
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LeaderboardAdapter();
        recyclerView.setAdapter(adapter);

        // Fetch Data
        fetchLeaderboard();

        return view;
    }

    private void fetchLeaderboard() {
        progressBar.setVisibility(View.VISIBLE);

        ApiClient.getService().getLeaderboard().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (getContext() == null) return;
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    if (body.has("data")) {
                        JsonArray dataArray = body.getAsJsonArray("data");
                        Type listType = new TypeToken<List<LeaderboardEntry>>(){}.getType();
                        List<LeaderboardEntry> entries = new Gson().fromJson(dataArray, listType);

                        if (entries != null) {
                            adapter.setUsers(entries);
                        }
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load rankings", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (getContext() == null) return;
                progressBar.setVisibility(View.GONE);
                // Silent fail or simple toast
            }
        });
    }
}