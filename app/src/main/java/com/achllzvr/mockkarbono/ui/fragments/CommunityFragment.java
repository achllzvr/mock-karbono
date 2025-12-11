package com.achllzvr.mockkarbono.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.achllzvr.mockkarbono.R;

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

        // Load mock data
        loadMockLeaderboardData();

        return view;
    }

    private void loadMockLeaderboardData() {
        // Mock data for the podium - in real app, this would come from Firestore
        tvFirstName.setText("Juan");
        tvFirstScore.setText("3.8 kg");

        tvSecondName.setText("Maria");
        tvSecondScore.setText("4.2 kg");

        tvThirdName.setText("Anna");
        tvThirdScore.setText("4.5 kg");

        // Your score
        tvYourScore.setText("5.8 kg");
    }
}

