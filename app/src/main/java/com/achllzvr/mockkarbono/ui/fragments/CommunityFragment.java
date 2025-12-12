package com.achllzvr.mockkarbono.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.achllzvr.mockkarbono.R;
import com.achllzvr.mockkarbono.ui.adapters.CommunityAdapter;
import com.achllzvr.mockkarbono.ui.adapters.FriendStreakAdapter;
import com.achllzvr.mockkarbono.ui.adapters.Community;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Community Fragment - Soft Pop / Duolingo Style
 * Displays leaderboard with podium and community forum topics
 */
public class CommunityFragment extends Fragment {

    // Podium views
    private ImageView imgFirstPlace;
    private ImageView imgSecondPlace;
    private ImageView imgThirdPlace;
    private TextView tvFirstName;
    private TextView tvSecondName;
    private TextView tvThirdName;
    private TextView tvFirstScore;
    private TextView tvSecondScore;
    private TextView tvThirdScore;
    private TextView tvYourScore;
    private RecyclerView rvCommunities;
    private RecyclerView rvFriendStreaks;
    private CommunityAdapter adapter;
    private FriendStreakAdapter friendStreakAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);

        // Bind podium views
        imgFirstPlace = view.findViewById(R.id.imgFirstPlace);
        imgSecondPlace = view.findViewById(R.id.imgSecondPlace);
        imgThirdPlace = view.findViewById(R.id.imgThirdPlace);
        tvFirstName = view.findViewById(R.id.tvFirstName);
        tvSecondName = view.findViewById(R.id.tvSecondName);
        tvThirdName = view.findViewById(R.id.tvThirdName);
        tvFirstScore = view.findViewById(R.id.tvFirstScore);
        tvSecondScore = view.findViewById(R.id.tvSecondScore);
        tvThirdScore = view.findViewById(R.id.tvThirdScore);
        tvYourScore = view.findViewById(R.id.tvYourScore);
        rvCommunities = view.findViewById(R.id.rvCommunities);
        rvFriendStreaks = view.findViewById(R.id.rvFriendStreaks);

        // Load mock data
        loadMockLeaderboardData();

        // Setup Friend Streaks RecyclerView
        rvFriendStreaks.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        friendStreakAdapter = new FriendStreakAdapter(getFriendStreaks());
        rvFriendStreaks.setAdapter(friendStreakAdapter);

        // Setup Communities RecyclerView
        rvCommunities.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(requireContext(), 2));
        adapter = new CommunityAdapter(getCommunities(), item -> {
            Toast.makeText(requireContext(), "Joined " + item.getTitle(), Toast.LENGTH_SHORT).show();
        });
        rvCommunities.setAdapter(adapter);

        return view;
    }

    private void loadMockLeaderboardData() {
        // Mock data for the podium - in real app, this would come from Firestore
        tvFirstName.setText("Juan");
        tvFirstScore.setText("3.8 kg");
        Glide.with(this)
            .load(R.drawable.img_male_avatar)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imgFirstPlace);

        tvSecondName.setText("Maria");
        tvSecondScore.setText("4.2 kg");
        Glide.with(this)
            .load(R.drawable.img_female_avatar)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imgSecondPlace);

        tvThirdName.setText("Anna");
        tvThirdScore.setText("4.5 kg");
        Glide.with(this)
            .load(R.drawable.img_asian_female_avatar)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imgThirdPlace);

        // Your score
        tvYourScore.setText("5.8 kg");
    }

    private List<FriendStreakAdapter.FriendStreakItem> getFriendStreaks() {
        List<FriendStreakAdapter.FriendStreakItem> items = new ArrayList<>();
        items.add(new FriendStreakAdapter.FriendStreakItem("Alex", 5, R.drawable.img_male_avatar_2));
        items.add(new FriendStreakAdapter.FriendStreakItem("Sarah", 3, R.drawable.img_female_avatar_2));
        items.add(new FriendStreakAdapter.FriendStreakItem("Ben", 7, R.drawable.img_asian_male_avatar));
        items.add(new FriendStreakAdapter.FriendStreakItem("Chloe", 2, R.drawable.img_lgbt_avatar));
        items.add(new FriendStreakAdapter.FriendStreakItem("David", 4, R.drawable.img_afr_avatar));
        return items;
    }

    // Mock data: Community items
    private List<Community> getCommunities() {
        List<Community> items = new ArrayList<>();
        items.add(new Community("NULP Eco Club", "5,324 members", R.drawable.img_nulp));
        items.add(new Community("SM Cares", "12,120 members", R.drawable.img_sm_cares));
        items.add(new Community("Kaya Founders", "1,500 members", R.drawable.img_kaya_founders));
        items.add(new Community("QBO Innovation", "4,200 members", R.drawable.img_qbo));
        items.add(new Community("Evertreen", "8,900 members", R.drawable.img_evertreen));
        return items;
    }
}
