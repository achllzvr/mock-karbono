package com.achllzvr.mockkarbono.ui.fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.achllzvr.mockkarbono.R;
import com.achllzvr.mockkarbono.db.AppDatabase;
import com.achllzvr.mockkarbono.db.entities.AppUsage;
import com.achllzvr.mockkarbono.db.entities.ApplianceLog;
import com.achllzvr.mockkarbono.db.entities.NotificationEvent;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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
    private LinearLayout speechBubble;
    private FrameLayout leavesContainer;
    private TextView tvTodayCarbon;
    private TextView tvCarbonStatus;
    private TextView tvPhoneCarbonValue;
    private TextView tvApplianceCarbonValue;
    private TextView tvTopApp1, tvTopApp2, tvTopApp3;
    private TextView tvTopAppliance1, tvTopAppliance2, tvTopAppliance3;
    private ImageView imgTopApp1, imgTopApp2, imgTopApp3;
    private ImageButton[] seedButtons = new ImageButton[7];
    private LinearLayout cardSmartphone;
    private LinearLayout cardAppliances;

    // Animation handlers
    private Handler animationHandler = new Handler(Looper.getMainLooper());
    private AnimatorSet breathingAnimator;
    private boolean isAnimating = false;
    private String currentState = STATE_HAPPY;

    private AppDatabase db;
    private Random random = new Random();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard_new, container, false);

        db = AppDatabase.getInstance(requireContext());

        // Bind views
        bindViews(view);

        // Setup click listeners
        setupClickListeners();

        // Load data and update mascot
        loadDashboardData();

        return view;
    }

    private void bindViews(View view) {
        imgMascot = view.findViewById(R.id.imgMascot);
        tvMascotMessage = view.findViewById(R.id.tvMascotMessage);
        speechBubble = view.findViewById(R.id.speechBubble);
        leavesContainer = view.findViewById(R.id.leavesContainer);
        tvTodayCarbon = view.findViewById(R.id.tvTodayCarbon);
        tvCarbonStatus = view.findViewById(R.id.tvCarbonStatus);

        // Bind top texts inside cards (layout doesn't provide explicit IDs for inner TextViews)
        cardSmartphone = view.findViewById(R.id.cardSmartphone);
        cardAppliances = view.findViewById(R.id.cardAppliances);

        try {
            // Updated binding logic for new dashboard layout with app icons
            imgTopApp1 = view.findViewById(R.id.imgTopApp1);
            tvTopApp1 = view.findViewById(R.id.tvTopApp1);
            tvPhoneCarbonValue = view.findViewById(R.id.tvPhoneCarbonValue);

            imgTopApp2 = view.findViewById(R.id.imgTopApp2);
            tvTopApp2 = view.findViewById(R.id.tvTopApp2);

            imgTopApp3 = view.findViewById(R.id.imgTopApp3);
            tvTopApp3 = view.findViewById(R.id.tvTopApp3);
        } catch (Exception ignored) {}

        try {
            ViewGroup ca = (ViewGroup) cardAppliances;
            ViewGroup apRel1 = (ViewGroup) ca.getChildAt(1); // AC row
            ViewGroup apRel2 = (ViewGroup) ca.getChildAt(3); // Refrigerator row
            ViewGroup apRel3 = (ViewGroup) ca.getChildAt(5); // Computer row

            tvTopAppliance1 = (TextView) apRel1.getChildAt(0);
            tvApplianceCarbonValue = (TextView) apRel1.getChildAt(1);

            tvTopAppliance2 = (TextView) apRel2.getChildAt(0);
            tvTopAppliance3 = (TextView) apRel3.getChildAt(0);
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
                Toast.makeText(requireContext(), "Day " + (dayIndex + 1) + " stats", Toast.LENGTH_SHORT).show();
            });
        }

        // Mascot tap - Easter egg
        imgMascot.setOnClickListener(v -> {
            tvMascotMessage.setText(getRandomMascotMessage());
            startBounceAnimation();
        });

        // Expand Smartphone card
        if (cardSmartphone != null) {
            // Find expand text button at the bottom of the card
            TextView expandText = (TextView) cardSmartphone.getChildAt(cardSmartphone.getChildCount() - 1);
            expandText.setOnClickListener(v -> {
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragmentContainer, new TrackFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            });
        }
        
        // Expand Appliances card
        if (cardAppliances != null) {
            // Find expand text button at the bottom of the card
            TextView expandText = (TextView) cardAppliances.getChildAt(cardAppliances.getChildCount() - 1);
            expandText.setOnClickListener(v -> {
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragmentContainer, new AppliancesFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            });
        }
    }

    private void loadDashboardData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Load real data from DB
            String today = getTodayDate();
            long dayStartMs = getTodayStartTimestamp();
            long dayEndMs = dayStartMs + (24 * 60 * 60 * 1000);

            // App Usage
            List<AppUsage> todayUsage = db.appUsageDao().getLatest(100);
            double phoneCO2 = 0.0;
            for (AppUsage usage : todayUsage) {
                if (usage.clientCreatedAtMs >= dayStartMs && usage.clientCreatedAtMs < dayEndMs) {
                    phoneCO2 += usage.estimatedKgCO2;
                }
            }

            // Notifications
            List<NotificationEvent> todayNotifs = db.notificationEventDao().getAll();
            double notifCO2 = 0.0;
            for (NotificationEvent notif : todayNotifs) {
                if (notif.clientCreatedAtMs >= dayStartMs && notif.clientCreatedAtMs < dayEndMs) {
                    notifCO2 += notif.estimatedKgCO2;
                }
            }
            phoneCO2 += notifCO2; // Combine phone usage and notifications

            // Appliances
            List<ApplianceLog> appliances = db.applianceDao().getAll();
            double applianceCO2 = 0.0;
            for (ApplianceLog app : appliances) {
                applianceCO2 += app.estimatedKgCO2PerDay;
            }

            double totalCO2 = phoneCO2 + applianceCO2;

            // Prepare Top Apps list
            // For now, simple sort or just take first 3 if available
            Collections.sort(todayUsage, (a, b) -> Double.compare(b.durationMs, a.durationMs));
            List<AppUsage> topApps = todayUsage.size() > 3 ? todayUsage.subList(0, 3) : todayUsage;

            // Prepare Top Appliances
            Collections.sort(appliances, (a, b) -> Double.compare(b.estimatedKgCO2PerDay, a.estimatedKgCO2PerDay));
            List<ApplianceLog> topAppliances = appliances.size() > 3 ? appliances.subList(0, 3) : appliances;

            // Pass final values to UI thread
            double finalPhoneCO2 = phoneCO2;
            double finalApplianceCO2 = applianceCO2;
            double finalTotalCO2 = totalCO2;

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    updateDashboardUI(finalTotalCO2, finalPhoneCO2, finalApplianceCO2, topApps, topAppliances);
                });
            }
        });
    }

    private void updateDashboardUI(double totalCO2, double phoneCO2, double applianceCO2, 
                                  List<AppUsage> topApps, List<ApplianceLog> topAppliances) {
        
        // Update carbon values
        tvTodayCarbon.setText(String.format(Locale.US, "%.3f", totalCO2));
        tvPhoneCarbonValue.setText(String.format(Locale.US, "%.1f %%", (totalCO2 > 0 ? (phoneCO2/totalCO2)*100 : 0))); // Showing percentage contribution
        tvApplianceCarbonValue.setText(String.format(Locale.US, "%.1f %%", (totalCO2 > 0 ? (applianceCO2/totalCO2)*100 : 0)));

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

        // Update Top Apps UI
        PackageManager pm = requireContext().getPackageManager();
        if (topApps.size() >= 1 && tvTopApp1 != null) {
            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(topApps.get(0).packageName, 0);
                tvTopApp1.setText(pm.getApplicationLabel(appInfo));
                if (imgTopApp1 != null) imgTopApp1.setImageDrawable(pm.getApplicationIcon(appInfo));
            } catch (PackageManager.NameNotFoundException e) {
                tvTopApp1.setText(formatPackageName(topApps.get(0).packageName));
                if (imgTopApp1 != null) imgTopApp1.setImageResource(R.drawable.ic_app_placeholder);
            }
        }
        if (topApps.size() >= 2 && tvTopApp2 != null) {
            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(topApps.get(1).packageName, 0);
                tvTopApp2.setText(pm.getApplicationLabel(appInfo));
                if (imgTopApp2 != null) imgTopApp2.setImageDrawable(pm.getApplicationIcon(appInfo));
            } catch (PackageManager.NameNotFoundException e) {
                tvTopApp2.setText(formatPackageName(topApps.get(1).packageName));
                if (imgTopApp2 != null) imgTopApp2.setImageResource(R.drawable.ic_app_placeholder);
            }
        }
        if (topApps.size() >= 3 && tvTopApp3 != null) {
            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(topApps.get(2).packageName, 0);
                tvTopApp3.setText(pm.getApplicationLabel(appInfo));
                if (imgTopApp3 != null) imgTopApp3.setImageDrawable(pm.getApplicationIcon(appInfo));
            } catch (PackageManager.NameNotFoundException e) {
                tvTopApp3.setText(formatPackageName(topApps.get(2).packageName));
                if (imgTopApp3 != null) imgTopApp3.setImageResource(R.drawable.ic_app_placeholder);
            }
        }

        // Update Top Appliances UI
        if (topAppliances.size() >= 1 && tvTopAppliance1 != null) tvTopAppliance1.setText(topAppliances.get(0).name);
        if (topAppliances.size() >= 2 && tvTopAppliance2 != null) tvTopAppliance2.setText(topAppliances.get(1).name);
        if (topAppliances.size() >= 3 && tvTopAppliance3 != null) tvTopAppliance3.setText(topAppliances.get(2).name);

        // Update mascot state
        updateMascotState(totalCO2);

        // Setup weekly seeds based on current day
        setupWeeklySeeds();
    }
    
    private String formatPackageName(String packageName) {
        // Remove domain prefix (com.whatsapp → WhatsApp)
        String[] parts = packageName.split("\\.");
        if (parts.length > 0) {
            String name = parts[parts.length - 1];
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        return packageName;
    }

    private void setupWeeklySeeds() {
        Calendar calendar = Calendar.getInstance();
        int todayDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); // Sunday = 1

        for (int i = 0; i < 7; i++) {
            int dayNum = i + 1; // Sunday = 1, Saturday = 7

            if (dayNum < todayDayOfWeek) {
                // Past days - show completed (green)
                seedButtons[i].setBackgroundResource(R.drawable.bg_day_seed_active);
                seedButtons[i].setColorFilter(getResources().getColor(android.R.color.white, null));
            } else if (dayNum == todayDayOfWeek) {
                // Today - show active
                seedButtons[i].setBackgroundResource(R.drawable.bg_day_seed_active);
                seedButtons[i].setColorFilter(getResources().getColor(android.R.color.white, null));
            } else {
                // Future days - show inactive
                seedButtons[i].setBackgroundResource(R.drawable.bg_day_seed_inactive);
                seedButtons[i].setColorFilter(getResources().getColor(R.color.text_tertiary, null));
            }
        }
    }

    /**
     * Updates mascot image and starts appropriate animation based on carbon usage
     */
    public void updateMascotState(double carbonUsage) {
        String newState;
        String message;
        int mascotRes;

        if (carbonUsage <= THRESHOLD_WARNING) {
            newState = STATE_HAPPY;
            message = getHappyMessage();
            mascotRes = R.drawable.mascot_happy;
        } else if (carbonUsage <= THRESHOLD_CRITICAL) {
            newState = STATE_WARNING;
            message = getWarningMessage();
            mascotRes = R.drawable.mascot_warning;
        } else {
            newState = STATE_CRITICAL;
            message = getCriticalMessage();
            mascotRes = R.drawable.mascot_critical;
        }

        // Update mascot image
        imgMascot.setImageResource(mascotRes);
        tvMascotMessage.setText(message);
        currentState = newState;

        // Start appropriate animation
        stopAllAnimations();

        switch (newState) {
            case STATE_HAPPY:
                startBreathingAnimation();
                startFallingLeaves(STATE_HAPPY);
                break;
            case STATE_WARNING:
                startBreathingAnimation();
                startFallingLeaves(STATE_WARNING);
                break;
            case STATE_CRITICAL:
                startShakeAnimation();
                startFallingLeaves(STATE_CRITICAL);
                break;
        }
    }

    /**
     * Breathing animation - gentle scale up/down for happy/warning states
     */
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
                if (isAnimating && currentState.equals(STATE_HAPPY) || currentState.equals(STATE_WARNING)) {
                    breathingAnimator.start();
                }
            }
        });

        isAnimating = true;
        breathingAnimator.start();
    }

    /**
     * Shake animation for critical state
     */
    private void startShakeAnimation() {
        if (imgMascot == null) return;

        ObjectAnimator shake = ObjectAnimator.ofFloat(imgMascot, "translationX", 0, 10, -10, 10, -10, 5, -5, 0);
        shake.setDuration(500);
        shake.setRepeatCount(ObjectAnimator.INFINITE);
        shake.setRepeatMode(ObjectAnimator.RESTART);

        isAnimating = true;
        shake.start();
    }

    /**
     * Bounce animation when mascot is tapped
     */
    private void startBounceAnimation() {
        if (imgMascot == null) return;

        ObjectAnimator bounce = ObjectAnimator.ofFloat(imgMascot, "translationY", 0, -20, 0);
        bounce.setDuration(300);
        bounce.start();
    }

    /**
     * Spawns falling leaf particles based on mascot state
     */
    public void startFallingLeaves(String state) {
        if (leavesContainer == null || !isAdded()) return;

        int leafRes;
        int leafCount;

        switch (state) {
            case STATE_HAPPY:
                leafRes = R.drawable.leaf_standard;
                leafCount = 3;
                break;
            case STATE_WARNING:
                leafRes = R.drawable.leaf_petal;
                leafCount = 2;
                break;
            case STATE_CRITICAL:
                leafRes = R.drawable.leaf_dry;
                leafCount = 4;
                break;
            default:
                return;
        }

        // Spawn leaves with delay
        for (int i = 0; i < leafCount; i++) {
            final int delay = i * 800;
            animationHandler.postDelayed(() -> spawnLeaf(leafRes), delay);
        }

        // Repeat the falling leaves animation
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

        // Random starting position at top
        int containerWidth = leavesContainer.getWidth();
        if (containerWidth == 0) containerWidth = 300;
        int startX = random.nextInt(containerWidth);

        leaf.setX(startX);
        leaf.setY(-40);

        leavesContainer.addView(leaf);

        // Fall animation
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
        if (breathingAnimator != null) {
            breathingAnimator.cancel();
        }
        animationHandler.removeCallbacksAndMessages(null);
        if (leavesContainer != null) {
            leavesContainer.removeAllViews();
        }
    }

    private String getHappyMessage() {
        String[] messages = {
            "Looking good! 🌱",
            "Keep it up! 🌿",
            "Eco warrior! 💚",
            "Planet thanks you! 🌍"
        };
        return messages[random.nextInt(messages.length)];
    }

    private String getWarningMessage() {
        String[] messages = {
            "Slow down a bit 🌻",
            "Take a break? ☀️",
            "Almost at limit! 📊"
        };
        return messages[random.nextInt(messages.length)];
    }

    private String getCriticalMessage() {
        String[] messages = {
            "Too much carbon! 🔥",
            "Time to unplug! ⚡",
            "Let's reduce! 🌡️"
        };
        return messages[random.nextInt(messages.length)];
    }

    private String getRandomMascotMessage() {
        String[] messages = {
            "Hi there! 👋",
            "You got this! 💪",
            "*happy plant noises*",
            "🌱🌱🌱"
        };
        return messages[random.nextInt(messages.length)];
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardData(); // Reload data when returning to fragment
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
