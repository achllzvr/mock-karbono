package com.achllzvr.mockkarbono.ui.fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.achllzvr.mockkarbono.R;
import com.achllzvr.mockkarbono.db.AppDatabase;
import com.achllzvr.mockkarbono.db.entities.AppUsage;
import com.achllzvr.mockkarbono.db.entities.NotificationEvent;
import com.achllzvr.mockkarbono.utils.PrefsManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.achllzvr.mockkarbono.utils.TutorialManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;

/**
 * New Dashboard Fragment - Soft Pop / Duolingo Style
 * Features mascot with state-based animations and Bento box cards
 */
public class DashboardFragmentNew extends Fragment {

    // Mascot states
    private static final String STATE_HAPPY = "happy";
    private static final String STATE_WARNING = "warning";
    private static final String STATE_CRITICAL = "critical";

    // Carbon thresholds (kg CO2/day)
    private static final double THRESHOLD_WARNING = 4.5;
    private static final double THRESHOLD_CRITICAL = 6.8;

    // Views
    private ImageView imgMascot;
    private TextView tvMascotMessage;
    private FrameLayout leavesContainer;
    private TextView tvTodayCarbon;
    private TextView tvCarbonStatus;

    // Smartphone Card Views
    private LinearLayout cardSmartphone;
    private TextView tvPhoneCarbonValue;

    // App Rows (Views to hide/show)
    private View rowApp1, rowApp2, rowApp3;
    private TextView tvTopApp1, tvTopApp2, tvTopApp3;
    private ImageView imgTopApp1, imgTopApp2, imgTopApp3;

    // Appliance Card Views
    private LinearLayout cardAppliances;
    private TextView tvApplianceCarbonValue;

    // Appliance Rows (Views to hide/show)
    private ViewGroup rowAppliance1, rowAppliance2, rowAppliance3;
    private TextView tvTopAppliance1, tvTopAppliance2, tvTopAppliance3;

    private ImageButton[] seedButtons = new ImageButton[7];
    private CardView btnReferences;

    // Animation handlers
    private Handler animationHandler = new Handler(Looper.getMainLooper());
    private AnimatorSet breathingAnimator;
    private boolean isAnimating = false;
    private String currentState = STATE_HAPPY;
    private double currentCarbonUsage = 0.0;
    private RelativeLayout mascotContainer;
    private PrefsManager prefsManager;
    private ScrollView dashboardScrollView;

    // Blinking handler
    private Handler blinkHandler = new Handler(Looper.getMainLooper());
    private Runnable blinkRunnable;

    private AppDatabase db;
    private Random random = new Random();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard_new, container, false);

        db = AppDatabase.getInstance(requireContext());

        prefsManager = new PrefsManager(requireContext());

        // --- ADD THIS LINE TEMPORARILY ---
        prefsManager.setDashboardTutorialSeen(false);
        // ---------------------------------

        // Initialize Views (Ensure these IDs exist in your XML)
        mascotContainer = view.findViewById(R.id.mascotContainer);
        tvTodayCarbon = view.findViewById(R.id.tvTodayCarbon);
        cardSmartphone = view.findViewById(R.id.cardSmartphone);
        dashboardScrollView = view.findViewById(R.id.dashboardScrollView);

        // Bind views
        bindViews(view);

        // Setup click listeners
        setupClickListeners();

        // Load data and update mascot
        loadDashboardData();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Check and Show Tutorial
        if (!prefsManager.isDashboardTutorialSeen()) {
            // We use a small delay to ensure views are laid out and ready
            view.postDelayed(this::startTutorial, 500);
        }
    }

    private void startTutorial() {
        if (getActivity() == null) return;

        new TutorialManager(getActivity())
                .withScrollView(dashboardScrollView) // <--- CRITICAL UPDATE
                .addStep(mascotContainer,
                        "Your Eco-Companion",
                        "Karbo reacts to your habits! Tap here to see his status.")
                .addStep(tvTodayCarbon,
                        "Daily Limit",
                        "Keep your emissions below 6.8kg today.")
                .addStep(cardSmartphone,
                        "Track Activity",
                        "Tap a card to log usage manually or view details.")
                .start(() -> {
                    prefsManager.setDashboardTutorialSeen(true);
                });
    }

    private void bindViews(View view) {
        imgMascot = view.findViewById(R.id.imgMascot);
        tvMascotMessage = view.findViewById(R.id.tvMascotMessage);
        leavesContainer = view.findViewById(R.id.leavesContainer);
        tvTodayCarbon = view.findViewById(R.id.tvTodayCarbon);
        tvCarbonStatus = view.findViewById(R.id.tvCarbonStatus);

        btnReferences = view.findViewById(R.id.btnReferences);

        // --- Smartphone Card Binding ---
        cardSmartphone = view.findViewById(R.id.cardSmartphone);
        try {
            // App 1 Elements
            imgTopApp1 = view.findViewById(R.id.imgTopApp1);
            tvTopApp1 = view.findViewById(R.id.tvTopApp1);
            tvPhoneCarbonValue = view.findViewById(R.id.tvPhoneCarbonValue);
            if (imgTopApp1 != null && imgTopApp1.getParent() instanceof View) {
                rowApp1 = (View) imgTopApp1.getParent();
            }

            // App 2 Elements
            imgTopApp2 = view.findViewById(R.id.imgTopApp2);
            tvTopApp2 = view.findViewById(R.id.tvTopApp2);
            if (imgTopApp2 != null && imgTopApp2.getParent() instanceof View) {
                rowApp2 = (View) imgTopApp2.getParent();
            }

            // App 3 Elements
            imgTopApp3 = view.findViewById(R.id.imgTopApp3);
            tvTopApp3 = view.findViewById(R.id.tvTopApp3);
            if (imgTopApp3 != null && imgTopApp3.getParent() instanceof View) {
                rowApp3 = (View) imgTopApp3.getParent();
            }
        } catch (Exception ignored) {}

        // --- Appliance Card Binding ---
        cardAppliances = view.findViewById(R.id.cardAppliances);
        try {
            ViewGroup ca = (ViewGroup) cardAppliances;
            rowAppliance1 = (ViewGroup) ca.getChildAt(1); // Row 1 Container
            if (rowAppliance1 != null) {
                tvTopAppliance1 = (TextView) rowAppliance1.getChildAt(0);
                tvApplianceCarbonValue = (TextView) rowAppliance1.getChildAt(1);
            }

            rowAppliance2 = (ViewGroup) ca.getChildAt(3); // Row 2 Container
            if (rowAppliance2 != null) {
                tvTopAppliance2 = (TextView) rowAppliance2.getChildAt(0);
            }

            rowAppliance3 = (ViewGroup) ca.getChildAt(5); // Row 3 Container
            if (rowAppliance3 != null) {
                tvTopAppliance3 = (TextView) rowAppliance3.getChildAt(0);
            }
        } catch (Exception ignored) {}

        // Bind seed buttons
        seedButtons[0] = view.findViewById(R.id.seedDay1);
        seedButtons[1] = view.findViewById(R.id.seedDay2);
        seedButtons[2] = view.findViewById(R.id.seedDay3);
        seedButtons[3] = view.findViewById(R.id.seedDay4);
        seedButtons[4] = view.findViewById(R.id.seedDay5);
        seedButtons[5] = view.findViewById(R.id.seedDay6);
        seedButtons[6] = view.findViewById(R.id.seedDay7);
    }

    private void setupClickListeners() {
        // Seed button clicks
        for (int i = 0; i < seedButtons.length; i++) {
            int dayIndex = i;
            seedButtons[i].setOnClickListener(v -> {
                // Future enhancement: Show popup summary for that specific day
                Toast.makeText(requireContext(), "Day " + (dayIndex + 1) + " status", Toast.LENGTH_SHORT).show();
            });
        }

        // Mascot tap
        imgMascot.setOnClickListener(v -> {
            tvMascotMessage.setText(getMascotMessage(currentState, currentCarbonUsage));
            startBounceAnimation();
        });

        // Expand Smartphone card
        if (cardSmartphone != null) {
            View child = cardSmartphone.getChildAt(cardSmartphone.getChildCount() - 1);
            if (child instanceof TextView) {
                child.setOnClickListener(v -> {
                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragmentContainer, new TrackFragment());
                    transaction.addToBackStack(null);
                    transaction.commit();
                });
            }
        }

        // Show references bottom sheet
        if (btnReferences != null) {
            btnReferences.setOnClickListener(v -> showReferencesBottomSheet());
        }
    }

    private void showReferencesBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.layout_references_bottom_sheet, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        ImageView btnClose = bottomSheetView.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();
    }

    private void loadDashboardData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            // 1. Load Today's Data
            long dayStartMs = getTodayStartTimestamp();
            long dayEndMs = dayStartMs + (24 * 60 * 60 * 1000);

            // Fetch Usage & Aggregate
            List<AppUsage> todayUsage = db.appUsageDao().getLatest(2000);
            Map<String, AppUsage> aggregatedUsage = new HashMap<>();

            double phoneCO2 = 0.0;
            for (AppUsage usage : todayUsage) {
                if (usage.clientCreatedAtMs >= dayStartMs && usage.clientCreatedAtMs < dayEndMs) {
                    phoneCO2 += usage.estimatedKgCO2;
                    if (aggregatedUsage.containsKey(usage.packageName)) {
                        AppUsage existingUsage = aggregatedUsage.get(usage.packageName);
                        existingUsage.durationMs += usage.durationMs;
                        existingUsage.estimatedKgCO2 += usage.estimatedKgCO2;
                    } else {
                        aggregatedUsage.put(usage.packageName, usage);
                    }
                }
            }

            // Fetch Notifications
            List<NotificationEvent> todayNotifs = db.notificationEventDao().getAll();
            double notifCO2 = 0.0;
            for (NotificationEvent notif : todayNotifs) {
                if (notif.clientCreatedAtMs >= dayStartMs && notif.clientCreatedAtMs < dayEndMs) {
                    notifCO2 += notif.estimatedKgCO2;
                }
            }
            phoneCO2 += notifCO2;

            double totalCO2 = phoneCO2;

            // Sort Top Apps & Appliances
            List<AppUsage> topApps = new ArrayList<>(aggregatedUsage.values());
            Collections.sort(topApps, (a, b) -> Double.compare(b.durationMs, a.durationMs));

            // Final Values for UI
            double finalPhoneCO2 = phoneCO2;
            double finalTotalCO2 = totalCO2;

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    updateDashboardUI(finalTotalCO2, finalPhoneCO2, topApps);
                });
            }

            // 2. Load Weekly History (Background)
            loadWeeklyProgress();
        });
    }

    /**
     * Calculates carbon for each day of the current week (Sun-Sat) and updates icons.
     */
    private void loadWeeklyProgress() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long weekStartMs = calendar.getTimeInMillis();
        double[] dailyTotals = new double[7];

        // Retrieve broad datasets once to avoid 7 separate DB queries per table
        List<AppUsage> allUsage = db.appUsageDao().getLatest(5000); // Grab enough history
        List<NotificationEvent> allNotifs = db.notificationEventDao().getAll();

        // Loop through 7 days
        for (int i = 0; i < 7; i++) {
            long dayStart = weekStartMs + (i * 24 * 60 * 60 * 1000L);
            long dayEnd = dayStart + (24 * 60 * 60 * 1000L);

            // Filter App Usage
            double dayUsageCO2 = 0;
            for (AppUsage u : allUsage) {
                if (u.clientCreatedAtMs >= dayStart && u.clientCreatedAtMs < dayEnd) {
                    dayUsageCO2 += u.estimatedKgCO2;
                }
            }

            // Filter Notifications
            double dayNotifCO2 = 0;
            for (NotificationEvent n : allNotifs) {
                if (n.clientCreatedAtMs >= dayStart && n.clientCreatedAtMs < dayEnd) {
                    dayNotifCO2 += n.estimatedKgCO2;
                }
            }

            // Total for Day i
            // Only add appliance cost if there was ANY activity that day or if it's today/past
            if (dayEnd <= System.currentTimeMillis() || dayUsageCO2 > 0) {
                dailyTotals[i] = dayUsageCO2 + dayNotifCO2;
            } else {
                dailyTotals[i] = 0; // Future day or no data
            }
        }

        // Update UI
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> updateWeeklySeedsUI(dailyTotals));
        }
    }

    private void updateWeeklySeedsUI(double[] dailyTotals) {
        Calendar cal = Calendar.getInstance();
        int todayIndex = cal.get(Calendar.DAY_OF_WEEK) - 1; // Sunday = 0

        for (int i = 0; i < 7; i++) {
            double co2 = dailyTotals[i];

            // Logic: Show "Lit" (Colored) icon ONLY if there is data (> 0.01)

            if (co2 > 0.01) {
                // We have data -> Determine Color Status
                int colorRes;
                if (co2 <= THRESHOLD_WARNING) {
                    colorRes = R.color.matcha_green; // Safe
                } else if (co2 <= THRESHOLD_CRITICAL) {
                    colorRes = R.color.marigold; // Warning
                } else {
                    colorRes = R.color.coral; // Exceeded
                }

                // 1. Set Background to Active Shape
                seedButtons[i].setBackgroundResource(R.drawable.bg_day_seed_active);

                // 2. Tint the BACKGROUND to the status color (Green/Yellow/Red)
                seedButtons[i].setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(colorRes, null)));

                // 3. Tint the ICON (Leaf) to WHITE so it contrasts with the background
                ImageViewCompat.setImageTintList(seedButtons[i], ColorStateList.valueOf(getResources().getColor(android.R.color.white, null)));

            } else {
                // No Data or Future -> Grey/Inactive

                // 1. Set Background to Inactive Shape (Grey Outline)
                seedButtons[i].setBackgroundResource(R.drawable.bg_day_seed_inactive);

                // 2. Clear Background Tint (so it shows the original grey drawable color)
                seedButtons[i].setBackgroundTintList(null);

                // 3. Tint the ICON to Tertiary Grey
                ImageViewCompat.setImageTintList(seedButtons[i], ColorStateList.valueOf(getResources().getColor(R.color.text_tertiary, null)));
            }
        }
    }

    private void updateDashboardUI(double totalCO2, double phoneCO2,
                                   List<AppUsage> topApps) {

        // Update carbon values
        tvTodayCarbon.setText(String.format(Locale.US, "%.3f", totalCO2));
        if (tvPhoneCarbonValue != null) {
            tvPhoneCarbonValue.setText(String.format(Locale.US, "%.1f %%", (totalCO2 > 0 ? (phoneCO2 / totalCO2) * 100 : 0)));
        }

        // Update status text
        if (totalCO2 <= THRESHOLD_WARNING) {
            tvCarbonStatus.setText("✓ Great job! Within daily limit");
            tvCarbonStatus.setTextColor(getResources().getColor(R.color.matcha_green, null));
            tvTodayCarbon.setTextColor(getResources().getColor(R.color.matcha_green, null));
        } else if (totalCO2 <= THRESHOLD_CRITICAL) {
            tvCarbonStatus.setText("⚠ Getting close to limit");
            tvCarbonStatus.setTextColor(getResources().getColor(R.color.marigold, null));
            tvTodayCarbon.setTextColor(getResources().getColor(R.color.marigold, null));
        } else {
            tvCarbonStatus.setText("⚠ Above daily limit!");
            tvCarbonStatus.setTextColor(getResources().getColor(R.color.coral, null));
            tvTodayCarbon.setTextColor(getResources().getColor(R.color.coral, null));
        }

        PackageManager pm = requireContext().getPackageManager();

        // --- Logic for Smartphone App Rows ---
        if (topApps.size() > 0) {
            setAppRowData(0, topApps.get(0), pm, tvTopApp1, imgTopApp1);
            if (rowApp1 != null) rowApp1.setVisibility(View.VISIBLE);
        } else {
            if (tvTopApp1 != null) tvTopApp1.setText("No usage today");
            if (imgTopApp1 != null) imgTopApp1.setImageResource(R.drawable.ic_track);
            if (rowApp1 != null) rowApp1.setVisibility(View.VISIBLE);
        }

        if (topApps.size() > 1) {
            setAppRowData(1, topApps.get(1), pm, tvTopApp2, imgTopApp2);
            if (rowApp2 != null) rowApp2.setVisibility(View.VISIBLE);
        } else {
            if (rowApp2 != null) rowApp2.setVisibility(View.GONE);
        }

        if (topApps.size() > 2) {
            setAppRowData(2, topApps.get(2), pm, tvTopApp3, imgTopApp3);
            if (rowApp3 != null) rowApp3.setVisibility(View.VISIBLE);
        } else {
            if (rowApp3 != null) rowApp3.setVisibility(View.GONE);
        }

        // Update mascot state
        updateMascotState(totalCO2);
    }

    private void setAppRowData(int index, AppUsage app, PackageManager pm, TextView tvName, ImageView imgIcon) {
        if (tvName == null) return;
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(app.packageName, 0);
            tvName.setText(pm.getApplicationLabel(appInfo));
            if (imgIcon != null) imgIcon.setImageDrawable(pm.getApplicationIcon(appInfo));
        } catch (PackageManager.NameNotFoundException e) {
            tvName.setText(formatPackageName(app.packageName));
            if (imgIcon != null) imgIcon.setImageResource(R.drawable.ic_app_placeholder);
        }
    }

    private String formatPackageName(String packageName) {
        String[] parts = packageName.split("\\.");
        if (parts.length > 0) {
            String name = parts[parts.length - 1];
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        return packageName;
    }

    public void updateMascotState(double carbonUsage) {
        this.currentCarbonUsage = carbonUsage;
        String newState;
        int mascotRes;

        if (carbonUsage <= THRESHOLD_WARNING) {
            newState = STATE_HAPPY;
            mascotRes = R.drawable.mascot_happy;
        } else if (carbonUsage <= THRESHOLD_CRITICAL) {
            newState = STATE_WARNING;
            mascotRes = R.drawable.mascot_warning;
        } else {
            newState = STATE_CRITICAL;
            mascotRes = R.drawable.mascot_critical;
        }

        imgMascot.setImageResource(mascotRes);
        tvMascotMessage.setText(getMascotMessage(newState, carbonUsage));
        currentState = newState;

        stopAllAnimations();

        switch (newState) {
            case STATE_HAPPY:
                startBreathingAnimation();
                startFallingLeaves(STATE_HAPPY);
                startBlinkingAnimation(R.drawable.mascot_happy, R.drawable.mascot_happy_blink);
                break;
            case STATE_WARNING:
                startBreathingAnimation();
                startFallingLeaves(STATE_WARNING);
                startBlinkingAnimation(R.drawable.mascot_warning, R.drawable.mascot_warning_blink);
                break;
            case STATE_CRITICAL:
                startShakeAnimation();
                startFallingLeaves(STATE_CRITICAL);
                startBlinkingAnimation(R.drawable.mascot_critical, R.drawable.mascot_critical_blink);
                break;
        }
    }

    // --- Animation & Mascot Logic (Unchanged) ---
    private void startBlinkingAnimation(int normalRes, int blinkRes) {
        if (blinkHandler == null) blinkHandler = new Handler(Looper.getMainLooper());
        blinkHandler.removeCallbacksAndMessages(null);
        blinkRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isAdded() || imgMascot == null) return;
                imgMascot.setImageResource(blinkRes);
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (isAdded() && imgMascot != null) {
                        imgMascot.setImageResource(normalRes);
                    }
                }, 150);
                int nextBlinkDelay = 2000 + random.nextInt(2000);
                blinkHandler.postDelayed(this, nextBlinkDelay);
            }
        };
        blinkHandler.post(blinkRunnable);
    }

    public void startBreathingAnimation() {
        if (imgMascot == null) return;
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(imgMascot, "scaleX", 1f, 1.05f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(imgMascot, "scaleY", 1f, 1.05f, 1f);
        breathingAnimator = new AnimatorSet();
        breathingAnimator.playTogether(scaleX, scaleY);
        breathingAnimator.setDuration(2000);
        breathingAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (isAnimating && (currentState.equals(STATE_HAPPY) || currentState.equals(STATE_WARNING))) {
                    breathingAnimator.start();
                }
            }
        });
        isAnimating = true;
        breathingAnimator.start();
    }

    private void startShakeAnimation() {
        if (imgMascot == null) return;
        ObjectAnimator shake = ObjectAnimator.ofFloat(imgMascot, "translationX", 0, 10, -10, 10, -10, 5, -5, 0);
        shake.setDuration(500);
        shake.setRepeatCount(ObjectAnimator.INFINITE);
        shake.setRepeatMode(ObjectAnimator.RESTART);
        isAnimating = true;
        shake.start();
    }

    private void startBounceAnimation() {
        if (imgMascot == null) return;
        ObjectAnimator bounce = ObjectAnimator.ofFloat(imgMascot, "translationY", 0, -20, 0);
        bounce.setDuration(300);
        bounce.start();
    }

    public void startFallingLeaves(String state) {
        if (leavesContainer == null || !isAdded()) return;
        int leafRes;
        int leafCount;
        switch (state) {
            case STATE_HAPPY: leafRes = R.drawable.leaf_standard; leafCount = 3; break;
            case STATE_WARNING: leafRes = R.drawable.leaf_petal; leafCount = 2; break;
            case STATE_CRITICAL: leafRes = R.drawable.leaf_dry; leafCount = 4; break;
            default: return;
        }
        for (int i = 0; i < leafCount; i++) {
            final int delay = i * 800;
            animationHandler.postDelayed(() -> spawnLeaf(leafRes), delay);
        }
        animationHandler.postDelayed(() -> {
            if (isAnimating && isAdded()) {
                startFallingLeaves(state);
            }
        }, leafCount * 800 + 2000);
    }

    private void spawnLeaf(int leafRes) {
        if (leavesContainer == null || !isAdded()) return;
        ImageView leaf = new ImageView(requireContext());
        leaf.setImageResource(leafRes);
        leaf.setLayoutParams(new FrameLayout.LayoutParams(40, 40));
        int containerWidth = leavesContainer.getWidth();
        if (containerWidth == 0) containerWidth = 300;
        int startX = random.nextInt(containerWidth);
        leaf.setX(startX);
        leaf.setY(-40);
        leavesContainer.addView(leaf);
        int containerHeight = leavesContainer.getHeight();
        if (containerHeight == 0) containerHeight = 200;
        ObjectAnimator fallY = ObjectAnimator.ofFloat(leaf, "translationY", -40, containerHeight + 40);
        ObjectAnimator sway = ObjectAnimator.ofFloat(leaf, "translationX", startX, startX + 30, startX - 30, startX);
        ObjectAnimator rotate = ObjectAnimator.ofFloat(leaf, "rotation", 0, 360);
        ObjectAnimator fade = ObjectAnimator.ofFloat(leaf, "alpha", 1f, 0f);
        AnimatorSet leafAnim = new AnimatorSet();
        leafAnim.playTogether(fallY, sway, rotate, fade);
        leafAnim.setDuration(3000);
        leafAnim.setInterpolator(new AccelerateInterpolator(0.5f));
        leafAnim.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                leavesContainer.removeView(leaf);
            }
        });
        leafAnim.start();
    }

    private void stopAllAnimations() {
        isAnimating = false;
        if (breathingAnimator != null) breathingAnimator.cancel();
        animationHandler.removeCallbacksAndMessages(null);
        if (blinkHandler != null) blinkHandler.removeCallbacksAndMessages(null);
        if (leavesContainer != null) leavesContainer.removeAllViews();
    }

    private String getMascotMessage(String state, double carbonUsage) {
        int index = random.nextInt(3);
        double saved = Math.max(0, THRESHOLD_CRITICAL - carbonUsage);
        double drivingKm = saved / 0.2;
        double smartphones = saved / 0.015;
        double trees = saved / 0.06;
        double usageDrivingKm = carbonUsage / 0.2;

        switch (state) {
            case STATE_HAPPY:
                if (index == 0) return String.format(Locale.US, "You're saving %.1f kg CO2! That's like not driving for %.1f kms 🚗", saved, drivingKm);
                if (index == 1) return String.format(Locale.US, "Great job! %.1f kg CO2 saved is equivalent to charging %.0f smartphones! 📱", saved, smartphones);
                return String.format(Locale.US, "You've saved %.1f kg CO2 today! That's the same as planting %.1f trees! 🌳", saved, trees);
            case STATE_WARNING:
                if (index == 0) return "Did you know using Tiktok for 30 mins equals to driving 0.2 kms? No? I see... 📉";
                if (index == 1) return String.format(Locale.US, "You've emitted %.1f kg CO2. That's like driving %.1f km! Time to slow down. 🚶", carbonUsage, usageDrivingKm);
                return "Streaming HD video for 1 hour creates ~0.4 kg CO2. Watch out! 📺";
            case STATE_CRITICAL:
                if (index == 0) return "I need... tree... revive... buy one from the shop... 😵";
                if (index == 1) return String.format(Locale.US, "%.1f kg CO2... that's like driving %.1f km... I'm choking... 🌫️", carbonUsage, usageDrivingKm);
                return "System overheating... please... reduce... usage... 🔥";
        }
        return "🌱";
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardData();
        isAnimating = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAllAnimations();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopAllAnimations();
    }

    private String getTodayDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return sdf.format(Calendar.getInstance().getTime());
    }

    private long getTodayStartTimestamp() {
        Calendar dayStart = Calendar.getInstance();
        dayStart.set(Calendar.HOUR_OF_DAY, 0);
        dayStart.set(Calendar.MINUTE, 0);
        dayStart.set(Calendar.SECOND, 0);
        dayStart.set(Calendar.MILLISECOND, 0);
        return dayStart.getTimeInMillis();
    }
}