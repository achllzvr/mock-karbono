package com.achllzvr.mockkarbono.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsManager {
    private static final String PREF_NAME = "karbono_prefs";

    // Auth Data
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";

    // Legacy/Tutorial Keys (Keeping them is fine for now)
    private static final String KEY_IS_FIRST_RUN = "is_first_run";
    private static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";
    private static final String KEY_TUTORIAL_DASHBOARD_SEEN = "tutorial_dashboard_seen";
    private static final String KEY_TUTORIAL_TRACK_SEEN = "tutorial_track_seen";

    private SharedPreferences prefs;

    public PrefsManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // --- USER DATA MANAGEMENT ---

    public void saveToken(String token) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply();
    }

    public String getToken() {
        return prefs.getString(KEY_AUTH_TOKEN, null);
    }

    public void saveUserDetails(String username, String email) {
        SharedPreferences.Editor editor = prefs.edit();
        if (username != null) editor.putString(KEY_USERNAME, username);
        if (email != null) editor.putString(KEY_EMAIL, email);
        editor.apply();
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "Eco Warrior"); // Default name
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "Guest Account"); // Default email
    }

    public void clear() {
        // Wipes ALL data on logout
        prefs.edit().clear().apply();
    }

    // --- LEGACY / TUTORIALS ---

    public boolean isFirstRun() {
        return prefs.getBoolean(KEY_IS_FIRST_RUN, true);
    }

    public void setFirstRun(boolean isFirst) {
        prefs.edit().putBoolean(KEY_IS_FIRST_RUN, isFirst).apply();
    }

    public boolean isOnboardingCompleted() {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false);
    }

    public void setOnboardingCompleted(boolean completed) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply();
    }

    public boolean isDashboardTutorialSeen() {
        return prefs.getBoolean(KEY_TUTORIAL_DASHBOARD_SEEN, false);
    }

    public void setDashboardTutorialSeen(boolean seen) {
        prefs.edit().putBoolean(KEY_TUTORIAL_DASHBOARD_SEEN, seen).apply();
    }

    public boolean isTrackTutorialSeen() {
        return prefs.getBoolean(KEY_TUTORIAL_TRACK_SEEN, false);
    }

    public void setTrackTutorialSeen(boolean seen) {
        prefs.edit().putBoolean(KEY_TUTORIAL_TRACK_SEEN, seen).apply();
    }
}