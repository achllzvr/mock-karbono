package com.achllzvr.mockkarbono.ui.onboarding;

import android.content.Intent;
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

import com.achllzvr.mockkarbono.MainActivity;
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
                if (getContext() == null) return;

                btnLogin.setEnabled(true);
                btnLogin.setText("Log In");

                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    Log.d("LOGIN_DEBUG", "Server Response: " + body.toString());

                    // 1. Check for Token
                    if (body.has("token")) {
                        String token = body.get("token").getAsString();
                        prefsManager.saveToken(token);

                        // 2. Save User Details (NEW)
                        // Extract username if sent by backend, otherwise default
                        String username = body.has("username") ? body.get("username").getAsString() : "Eco Warrior";
                        prefsManager.saveUserDetails(username, email);

                        // 3. TRIGGER SYNC IMMEDIATELY
                        // This effectively "Imports" guest data to the new account
                        androidx.work.OneTimeWorkRequest syncRequest =
                                new androidx.work.OneTimeWorkRequest.Builder(com.achllzvr.mockkarbono.tracking.SyncWorker.class)
                                        .build();
                        androidx.work.WorkManager.getInstance(getContext()).enqueue(syncRequest);

                        // 4. Navigate to Main Dashboard
                        Toast.makeText(getContext(), "Welcome back, " + username + "!", Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        Toast.makeText(getContext(), "Login failed: Invalid response", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Login Failed: Check credentials", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (getContext() == null) return;

                btnLogin.setEnabled(true);
                btnLogin.setText("Log In");
                Log.e("AuthFragment", "Network Error", t);
                Toast.makeText(getContext(), "Network Error. Please try again.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleGuestLogin() {
        // Just go back to Main Activity
        // Ideally, we don't clear token here so we don't lose data if they clicked this by accident,
        // but typically "Guest" implies no account.
        Toast.makeText(getContext(), "Continuing as Guest", Toast.LENGTH_SHORT).show();
        navigateToMain();
    }

    private void navigateToMain() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            // Clear back stack so they can't press back to return to login
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }
}