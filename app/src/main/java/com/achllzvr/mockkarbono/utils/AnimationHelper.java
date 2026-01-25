package com.achllzvr.mockkarbono.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Centralized animation utilities for consistent UI polish across the app
 */
public class AnimationHelper {

    /**
     * Animate card entrance with scale and fade
     * @param view The view to animate
     * @param delay Start delay in milliseconds
     */
    public static void animateCardEntrance(View view, int delay) {
        view.setAlpha(0f);
        view.setScaleX(0.9f);
        view.setScaleY(0.9f);

        view.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .setStartDelay(delay)
                .setInterpolator(new OvershootInterpolator(1.1f))
                .start();
    }

    /**
     * Animate list item entrance from bottom
     * @param view The view to animate
     * @param delay Start delay in milliseconds
     */
    public static void animateListItemEntrance(View view, int delay) {
        view.setAlpha(0f);
        view.setTranslationY(40f);

        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(350)
                .setStartDelay(delay)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    /**
     * Stagger animate children of a ViewGroup
     * @param parent ViewGroup containing children to animate
     * @param delayIncrement Delay between each child animation
     */
    public static void staggerAnimateChildren(ViewGroup parent, int delayIncrement) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child.getVisibility() == View.VISIBLE) {
                animateListItemEntrance(child, i * delayIncrement);
            }
        }
    }

    /**
     * Add button press animation (scale down on press)
     * @param button The button to add animation to
     */
    public static void addButtonPressAnimation(View button) {
        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.animate()
                            .scaleX(0.95f)
                            .scaleY(0.95f)
                            .setDuration(100)
                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            .start();
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            .start();
                    v.performClick();
                    break;
            }
            return true;
        });
    }

    /**
     * Bounce animation for emphasis
     * @param view The view to bounce
     */
    public static void bounce(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(300);
        animatorSet.setInterpolator(new OvershootInterpolator(2f));
        animatorSet.start();
    }

    /**
     * Shake animation for error states
     * @param view The view to shake
     */
    public static void shake(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX",
                0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
        animator.setDuration(500);
        animator.start();
    }

    /**
     * Pulse animation for notifications/badges. Repeats infinitely.
     * @param view The view to pulse
     */
    public static void pulse(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.2f, 1f);

        scaleX.setRepeatCount(ObjectAnimator.INFINITE);
        scaleY.setRepeatCount(ObjectAnimator.INFINITE);
        scaleX.setRepeatMode(ObjectAnimator.RESTART);
        scaleY.setRepeatMode(ObjectAnimator.RESTART);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(600);
        animatorSet.start();
    }

    /**
     * Fade in animation
     * @param view The view to fade in
     * @param duration Duration in milliseconds
     */
    public static void fadeIn(View view, int duration) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(duration)
                .setListener(null)
                .start();
    }

    /**
     * Fade out animation
     * @param view The view to fade out
     * @param duration Duration in milliseconds
     */
    public static void fadeOut(View view, int duration) {
        view.animate()
                .alpha(0f)
                .setDuration(duration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.GONE);
                    }
                })
                .start();
    }

    /**
     * Cross-fade between two views
     * @param viewOut View to fade out
     * @param viewIn View to fade in
     * @param duration Duration in milliseconds
     */
    public static void crossFade(View viewOut, View viewIn, int duration) {
        fadeOut(viewOut, duration);
        fadeIn(viewIn, duration);
    }

    /**
     * Reveal animation (expand from center)
     * @param view The view to reveal
     */
    public static void reveal(View view) {
        view.setScaleX(0f);
        view.setScaleY(0f);
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);

        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(400)
                .setInterpolator(new OvershootInterpolator(1.5f))
                .start();
    }

    /**
     * Collapse animation (shrink to center)
     * @param view The view to collapse
     */
    public static void collapse(View view) {
        view.animate()
                .scaleX(0f)
                .scaleY(0f)
                .alpha(0f)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.GONE);
                    }
                })
                .start();
    }
}
