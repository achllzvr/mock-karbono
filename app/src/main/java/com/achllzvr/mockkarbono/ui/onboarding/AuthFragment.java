package com.achllzvr.mockkarbono.ui.onboarding;

import android.os.Bundle;
import android.util.Log;
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
import com.achllzvr.mockkarbono.api.ApiClient;
import com.achllzvr.mockkarbono.api.KarbonoApiService;
import com.achllzvr.mockkarbono.utils.PrefsManager;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthFragment extends Fragment {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnAnon;
    private KarbonoApiService apiService;
    private PrefsManager prefsManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auth, container, false);

        // Initialize API and Prefs
        apiService = ApiClient.getService();
        prefsManager = new PrefsManager(requireContext());

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

        // 1. Create JSON Body
        JsonObject credentials = new JsonObject();
        credentials.addProperty("email", email);
        credentials.addProperty("password", pass);

        // 2. Call PHP API
        btnLogin.setEnabled(false); // Prevent double clicks
        btnLogin.setText("Logging in...");

        apiService.loginUser(credentials).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Log In");

                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();

                    // 3. Save Token & User ID
                    if (body.has("token")) {
                        String token = body.get("token").getAsString();
                        prefsManager.saveToken(token); // Needed for SyncWorker

                        Toast.makeText(getContext(), "Welcome back!", Toast.LENGTH_SHORT).show();
                        proceedToNext();
                    } else {
                        Toast.makeText(getContext(), "Login failed: Invalid response", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Login Failed: Check credentials", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Log In");
                Log.e("AuthFragment", "Network Error", t);
                Toast.makeText(getContext(), "Network Error. Working offline.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleGuestLogin() {
        // Guest Mode = Offline Mode (No Token)
        // You might want to clear any existing token just in case
        // prefsManager.clearToken();

        Toast.makeText(getContext(), "Continuing as Guest (Offline Mode)", Toast.LENGTH_SHORT).show();
        proceedToNext();
    }

    private void proceedToNext() {
        if (getActivity() instanceof OnboardingActivity) {
            ((OnboardingActivity) getActivity()).goToNextStep();
        }
    }
}