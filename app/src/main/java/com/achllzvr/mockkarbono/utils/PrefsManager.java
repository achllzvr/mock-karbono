package com.achllzvr.mockkarbono.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsManager {
    private static final String PREF_NAME = "karbono_prefs";
    private static final String KEY_IS_FIRST_RUN = "is_first_run";
    private static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";

    // New keys for tutorials
    private static final String KEY_TUTORIAL_DASHBOARD_SEEN = "tutorial_dashboard_seen";
    private static final String KEY_TUTORIAL_TRACK_SEEN = "tutorial_track_seen";

    private SharedPreferences prefs;

    public PrefsManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

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

    // Tutorial Getters/Setters
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