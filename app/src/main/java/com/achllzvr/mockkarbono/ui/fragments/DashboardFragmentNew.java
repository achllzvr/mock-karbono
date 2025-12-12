package com.achllzvr.mockkarbono.ui.fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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

import com.achllzvr.mockkarbono.R;
import com.achllzvr.mockkarbono.db.AppDatabase;
import com.achllzvr.mockkarbono.db.entities.AppUsage;
import com.achllzvr.mockkarbono.db.entities.ApplianceLog;

import java.util.Calendar;
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
    private ImageButton[] seedButtons = new ImageButton[7];

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
        LinearLayout cardSmartphone = view.findViewById(R.id.cardSmartphone);
        LinearLayout cardAppliances = view.findViewById(R.id.cardAppliances);

        try {
            ViewGroup cs = (ViewGroup) cardSmartphone;
            ViewGroup appRel1 = (ViewGroup) cs.getChildAt(1); // TikTok row
            ViewGroup appRel2 = (ViewGroup) cs.getChildAt(3); // Facebook row
            ViewGroup appRel3 = (ViewGroup) cs.getChildAt(5); // Games row

            tvTopApp1 = (TextView) appRel1.getChildAt(0);
            tvPhoneCarbonValue = (TextView) appRel1.getChildAt(1);

            tvTopApp2 = (TextView) appRel2.getChildAt(0);
            tvTopApp3 = (TextView) appRel3.getChildAt(0);
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
    }

    private void loadDashboardData() {
        // For demo, use mock data
        loadMockData();

        // Start mascot animation based on state
        updateMascotState(2.4); // Mock carbon value
    }

    private void loadMockData() {
        // Mock carbon values
        double phoneCO2 = 0.8;
        double applianceCO2 = 1.6;
        double totalCO2 = phoneCO2 + applianceCO2;

        // Update UI
        tvTodayCarbon.setText(String.format(Locale.US, "%.1f", totalCO2));
        tvPhoneCarbonValue.setText(String.format(Locale.US, "%.1f kg", phoneCO2));
        tvApplianceCarbonValue.setText(String.format(Locale.US, "%.1f kg", applianceCO2));

        // Update status
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

        // Mock top apps
        tvTopApp1.setText("• TikTok: 2h");
        tvTopApp2.setText("• YouTube: 1.5h");
        tvTopApp3.setText("• Instagram: 45m");

        // Mock top appliances
        tvTopAppliance1.setText("• AC: 8h");
        tvTopAppliance2.setText("• Refrigerator: 24h");
        tvTopAppliance3.setText("• TV: 3h");

        // Setup weekly seeds based on current day
        setupWeeklySeeds();
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
        isAnimating = true;
        updateMascotState(2.4); // Resume animations
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
}

