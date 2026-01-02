package com.achllzvr.mockkarbono.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.achllzvr.mockkarbono.ui.onboarding.AILimitAnalysisFragment;
import com.achllzvr.mockkarbono.ui.onboarding.AuthFragment;
import com.achllzvr.mockkarbono.ui.onboarding.LifestyleQuestionnaireFragment;
import com.achllzvr.mockkarbono.ui.onboarding.TutorialFragment;

public class OnboardingPagerAdapter extends FragmentStateAdapter {

    public OnboardingPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                // Step 1: Authentication & Mascot Intro
                return new AuthFragment();
            case 1:
                // Step 2: Lifestyle Questionnaire (Wizard Style)
                return new LifestyleQuestionnaireFragment();
            case 2:
                // Step 3: AI Cloud Analysis Simulation
                return new AILimitAnalysisFragment();
            case 3:
                // Step 4: Final Tutorial / Feature Tour
                return new TutorialFragment();
            default:
                return new AuthFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4; // Total number of steps
    }
}