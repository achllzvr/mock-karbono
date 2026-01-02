package com.achllzvr.mockkarbono.ui.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.achllzvr.mockkarbono.OnboardingActivity;
import com.achllzvr.mockkarbono.R;
import com.google.firebase.auth.FirebaseAuth;

public class AuthFragment extends Fragment {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnAnon;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auth, container, false);

        auth = FirebaseAuth.getInstance();
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnAnon = view.findViewById(R.id.btnAnon);

        btnLogin.setOnClickListener(v -> handleLogin());
        btnAnon.setOnClickListener(v -> handleGuestLogin());

        return view;
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(getContext(), "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mock Login for now (or real Firebase if configured)
        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                proceedToNext();
            } else {
                Toast.makeText(getContext(), "Auth Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleGuestLogin() {
        auth.signInAnonymously().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                proceedToNext();
            } else {
                Toast.makeText(getContext(), "Guest Auth Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void proceedToNext() {
        if (getActivity() instanceof OnboardingActivity) {
            ((OnboardingActivity) getActivity()).goToNextStep();
        }
    }
}