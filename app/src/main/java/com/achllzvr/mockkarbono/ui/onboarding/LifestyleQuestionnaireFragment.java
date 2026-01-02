package com.achllzvr.mockkarbono.ui.onboarding;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.achllzvr.mockkarbono.OnboardingActivity;
import com.achllzvr.mockkarbono.R;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class LifestyleQuestionnaireFragment extends Fragment {

    private ImageView imgMascot;
    private TextView tvQuestion;
    private LinearLayout llChoicesContainer;
    private LinearProgressIndicator progressBar;
    private Button btnBack;

    private int currentQuestionIndex = 0;
    private List<Question> questions = new ArrayList<>();

    // Tracking specific habits for the "Tips" section
    private boolean isHeavyGamer = false;
    private boolean hasHeavyAppliances = false;
    private boolean isHeavySocialUser = false;
    private double totalEstimatedCo2 = 0.0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lifestyle_questionnaire, container, false);

        imgMascot = view.findViewById(R.id.imgMascot);
        tvQuestion = view.findViewById(R.id.tvQuestion);
        llChoicesContainer = view.findViewById(R.id.llChoicesContainer);
        progressBar = view.findViewById(R.id.progressBar);
        btnBack = view.findViewById(R.id.btnBack);

        setupQuestions();
        showQuestion(0);

        btnBack.setOnClickListener(v -> {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--;
                showQuestion(currentQuestionIndex);
            }
        });

        return view;
    }

    private void setupQuestions() {
        // 1. Occupation
        questions.add(new Question(
                "First things first! What occupies most of your day?",
                "occupation",
                new Choice("Student (IT/Tech)", 0.5),
                new Choice("Office Worker", 0.4),
                new Choice("Field / Outdoor", 0.1)
        ));

        // 2. Smartphone Usage
        questions.add(new Question(
                "Be honest! How much time do you spend on your phone daily?",
                "phone_usage",
                new Choice("1-2 Hours", 1.5),
                new Choice("3-5 Hours", 3.0),
                new Choice("6+ Hours (Power User)", 5.5) // This will tag as Heavy Social
        ));

        // 3. Gaming Habits
        questions.add(new Question(
                "Do you play video games?",
                "gaming",
                new Choice("No, not really", 0.0),
                new Choice("Mobile Games (Casual)", 0.2),
                new Choice("PC / Console (Hardcore)", 1.5) // This will tag as Heavy Gamer
        ));

        // 4. Appliances
        questions.add(new Question(
                "Do you have heavy appliances running often (AC, Gaming PC)?",
                "appliances",
                new Choice("Yes, AC + High Tech", 9.6),
                new Choice("Just basic appliances", 2.5),
                new Choice("Minimal / Shared Dorm", 1.0)
        ));
    }

    private void showQuestion(int index) {
        if (index >= questions.size()) {
            finishQuestionnaire();
            return;
        }

        Question q = questions.get(index);
        tvQuestion.setText(q.text);

        int progress = (int) (((float) index / questions.size()) * 100);
        progressBar.setProgress(progress);

        // Mascot Reactivity
        if (index == 0) imgMascot.setImageResource(R.drawable.mascot_happy);
        else if (index == 3) imgMascot.setImageResource(R.drawable.mascot_warning);
        else imgMascot.setImageResource(R.drawable.mascot_happy_blink);

        llChoicesContainer.removeAllViews();

        for (Choice choice : q.choices) {
            Button btn = new Button(getContext());
            btn.setText(choice.label);
            btn.setBackgroundResource(R.drawable.bg_button_green);
            btn.setTextColor(Color.WHITE);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 16, 0, 16);
            btn.setLayoutParams(params);

            btn.setOnClickListener(v -> {
                // Logic to tag user habits
                totalEstimatedCo2 += choice.emissionFactor;

                // Logic to flag specific "Weakness" areas for the AI Tip
                if (q.id.equals("phone_usage") && choice.emissionFactor > 4.0) isHeavySocialUser = true;
                if (q.id.equals("gaming") && choice.emissionFactor > 1.0) isHeavyGamer = true;
                if (q.id.equals("appliances") && choice.emissionFactor > 5.0) hasHeavyAppliances = true;

                currentQuestionIndex++;
                showQuestion(currentQuestionIndex);
            });

            llChoicesContainer.addView(btn);
        }

        btnBack.setVisibility(index == 0 ? View.INVISIBLE : View.VISIBLE);
    }

    private void finishQuestionnaire() {
        SharedPreferences prefs = requireContext().getSharedPreferences("karbono_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Save calculation results
        editor.putFloat("calc_user_estimate", (float) totalEstimatedCo2);
        editor.putBoolean("calc_heavy_gamer", isHeavyGamer);
        editor.putBoolean("calc_heavy_appliances", hasHeavyAppliances);
        editor.putBoolean("calc_heavy_social", isHeavySocialUser);
        editor.apply();

        if (getActivity() instanceof OnboardingActivity) {
            ((OnboardingActivity) getActivity()).goToNextStep();
        }
    }

    // Helper classes
    private static class Question {
        String text;
        String id;
        Choice[] choices;
        Question(String text, String id, Choice... choices) {
            this.text = text;
            this.id = id;
            this.choices = choices;
        }
    }

    private static class Choice {
        String label;
        double emissionFactor;
        Choice(String label, double emissionFactor) {
            this.label = label;
            this.emissionFactor = emissionFactor;
        }
    }
}