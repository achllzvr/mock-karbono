package com.achllzvr.mockkarbono.utils;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ScrollView;

import com.achllzvr.mockkarbono.R;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;

import java.util.ArrayList;
import java.util.List;

public class TutorialManager {

    private final Activity activity;
    private final List<TutorialStep> steps = new ArrayList<>();
    private Runnable onCompleteAction;
    private ScrollView scrollView; // Added reference

    public TutorialManager(Activity activity) {
        this.activity = activity;
    }

    // New method to attach the ScrollView
    public TutorialManager withScrollView(ScrollView scrollView) {
        this.scrollView = scrollView;
        return this;
    }

    public TutorialManager addStep(View target, String title, String description) {
        steps.add(new TutorialStep(target, title, description));
        return this;
    }

    public void start(Runnable onComplete) {
        this.onCompleteAction = onComplete;
        if (!steps.isEmpty()) {
            showStep(0);
        }
    }

    private void showStep(int index) {
        if (index >= steps.size()) {
            if (onCompleteAction != null) onCompleteAction.run();
            return;
        }

        TutorialStep step = steps.get(index);
        View targetView = step.target;

        // --- NEW SCROLL LOGIC ---
        if (scrollView != null && targetView != null) {
            scrollView.post(() -> {
                int[] targetLoc = new int[2];
                targetView.getLocationOnScreen(targetLoc); // Absolute Y on screen

                int[] scrollLoc = new int[2];
                scrollView.getLocationOnScreen(scrollLoc); // Absolute Y of ScrollView container

                int currentScrollY = scrollView.getScrollY();

                // Calculate where the view is relative to the VISIBLE top of the scrollview
                int relativeY = targetLoc[1] - scrollLoc[1];

                // Calculate destination to center the view:
                // TargetScroll = CurrentScroll + RelativeY - (ScreenHalfHeight) + (ViewHalfHeight)
                int destY = currentScrollY + relativeY - (scrollView.getHeight() / 2) + (targetView.getHeight() / 2);

                scrollView.smoothScrollTo(0, destY);
            });
        }
        // ------------------------

        // Delay slightly to let scroll finish before showing bubble
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            showTapTarget(step, index);
        }, 400); // Increased to 400ms for smoother feel
    }

    private void showTapTarget(TutorialStep step, int nextIndex) {
        TapTargetView.showFor(activity,
                TapTarget.forView(step.target, step.title, step.description)
                        .outerCircleColor(R.color.matcha_green)
                        .outerCircleAlpha(0.96f)
                        .targetCircleColor(R.color.white)
                        .titleTextSize(24)
                        .titleTextColor(R.color.white)
                        .descriptionTextSize(16)
                        .descriptionTextColor(R.color.white)
                        .textColor(R.color.white)
                        .textTypeface(Typeface.SANS_SERIF)
                        .dimColor(R.color.black)
                        .drawShadow(true)
                        .cancelable(false)
                        .tintTarget(false)
                        .transparentTarget(false)
                        .targetRadius(60),

                new TapTargetView.Listener() {
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                        showStep(nextIndex + 1);
                    }
                });
    }

    private static class TutorialStep {
        View target;
        String title;
        String description;

        TutorialStep(View target, String title, String description) {
            this.target = target;
            this.title = title;
            this.description = description;
        }
    }
}