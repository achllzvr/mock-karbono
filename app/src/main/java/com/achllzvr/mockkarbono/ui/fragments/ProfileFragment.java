package com.achllzvr.mockkarbono.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.achllzvr.mockkarbono.OnboardingActivity;
import com.achllzvr.mockkarbono.R;
import com.achllzvr.mockkarbono.utils.PrefsManager;

public class ProfileFragment extends Fragment {

    private PrefsManager prefs;
    private LinearLayout layoutGuest, layoutUser;
    private TextView tvName, tvEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        prefs = new PrefsManager(getContext());

        // Find Views
        layoutGuest = view.findViewById(R.id.layoutGuestState);
        layoutUser = view.findViewById(R.id.layoutUserState);
        tvName = view.findViewById(R.id.tvDisplayName);
        tvEmail = view.findViewById(R.id.tvEmail);
        Button btnLogin = view.findViewById(R.id.btnLogin);
        Button btnLogout = view.findViewById(R.id.btnLogout);

        // Check Auth State
        updateUI();

        // Listeners
        btnLogin.setOnClickListener(v -> {
            // Launch Login Flow
            Intent intent = new Intent(getActivity(), OnboardingActivity.class);
            intent.putExtra("force_login", true); // Pass flag to jump straight to auth
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            prefs.saveToken(null); // Clear Token
            updateUI();
            Toast.makeText(getContext(), "Logged out", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    private void updateUI() {
        String token = prefs.getToken();

        if (token != null && !token.isEmpty()) {
            // LOGGED IN
            layoutGuest.setVisibility(View.GONE);
            layoutUser.setVisibility(View.VISIBLE);

            // FETCH FROM PREFS
            tvName.setText(prefs.getUsername());
            tvEmail.setText(prefs.getEmail());

        } else {
            // GUEST STATE
            layoutGuest.setVisibility(View.VISIBLE);
            layoutUser.setVisibility(View.GONE);
            tvName.setText("Guest Explorer");
            tvEmail.setText("Unregistered");
        }
    }
}