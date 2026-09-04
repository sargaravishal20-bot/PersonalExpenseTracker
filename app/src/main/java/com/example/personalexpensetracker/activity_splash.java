package com.example.personalexpensetracker;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.personalexpensetracker.database.DatabaseHelper;

import java.util.Locale;

public class activity_splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // <-- setContentView pehle aayega!

        // ============================================
        // 1. DYNAMIC PROFILE DATA LOAD FROM DATABASE
        // ============================================
        DatabaseHelper db = new DatabaseHelper(this);
        String[] profile = db.getProfile();

        TextView tvCardName = findViewById(R.id.tvCardName);
        TextView tvCardAvatar = findViewById(R.id.tvCardAvatar);
        TextView tvFloatingAmount = findViewById(R.id.tvFloatingAmount);

        if (tvCardName != null) {
            tvCardName.setText(profile  [0]); // Saved Name
        }
        if (tvCardAvatar != null) {
            tvCardAvatar.setText(profile[1]); // Saved Emoji
        }
        if (tvFloatingAmount != null) {
            try {
                double amt = Double.parseDouble(profile[2]);
                tvFloatingAmount.setText(String.format(Locale.getDefault(), "₹%.2f", amt)); // Saved Amount
            } catch (Exception e) {
                tvFloatingAmount.setText("₹" + profile[2]);
            }
        }

        // ============================================
        // 2. FIND ANIMATION VIEWS
        // ============================================
        View tvBackgroundWatermark = findViewById(R.id.tvBackgroundWatermark);
        View cardFloatingTransaction = findViewById(R.id.cardFloatingTransaction);
        View layoutCurrencyBadge = findViewById(R.id.layoutCurrencyBadge);
        View layoutBottomContent = findViewById(R.id.layoutBottomContent);
        View btnGetStarted = findViewById(R.id.btnGetStarted);

        // ============================================
        // 3. INITIAL STATES (Chhipa kar start karenge)
        // ============================================
        if (tvBackgroundWatermark != null) {
            tvBackgroundWatermark.setAlpha(0f);
            tvBackgroundWatermark.setScaleX(0.85f);
            tvBackgroundWatermark.setScaleY(0.85f);
        }

        if (cardFloatingTransaction != null) {
            cardFloatingTransaction.setAlpha(0f);
            cardFloatingTransaction.setTranslationY(120f);
        }

        if (layoutCurrencyBadge != null) {
            layoutCurrencyBadge.setAlpha(0f);
            layoutCurrencyBadge.setScaleX(0f);
            layoutCurrencyBadge.setScaleY(0f);
        }

        if (layoutBottomContent != null) {
            layoutBottomContent.setAlpha(0f);
            layoutBottomContent.setTranslationY(60f);
        }

        // ============================================
        // 4. ENTRANCE ANIMATIONS
        // ============================================

        // A. Background "Tracker" Watermark
        if (tvBackgroundWatermark != null) {
            tvBackgroundWatermark.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(900)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        // B. Floating Card Slide Up & Fade In
        if (cardFloatingTransaction != null) {
            cardFloatingTransaction.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(900)
                    .setStartDelay(200)
                    .setInterpolator(new DecelerateInterpolator())
                    .withEndAction(() -> {
                        startGentleFloating(cardFloatingTransaction, layoutCurrencyBadge);
                    })
                    .start();
        }

        // C. Currency Coins Pop-Up with Bounce
        if (layoutCurrencyBadge != null) {
            layoutCurrencyBadge.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(600)
                    .setStartDelay(650)
                    .setInterpolator(new OvershootInterpolator(2.0f))
                    .start();
        }

        // D. Bottom Content Slide Up
        if (layoutBottomContent != null) {
            layoutBottomContent.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(800)
                    .setStartDelay(500)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        // ============================================
        // 5. GET STARTED BUTTON CLICK
        // ============================================
        if (btnGetStarted != null) {
            btnGetStarted.setOnClickListener(v -> {
                btnGetStarted.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(100)
                        .withEndAction(() -> {
                            btnGetStarted.animate().scaleX(1f).scaleY(1f).setDuration(100);
                            Intent intent = new Intent(activity_splash.this, MainActivity.class);
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            finish();
                        })
                        .start();
            });
        }
    }

    // ============================================
    // CONTINUOUS GENTLE FLOATING (Hawa mein float hona)
    // ============================================
    private void startGentleFloating(View card, View badge) {
        ObjectAnimator floatCard = ObjectAnimator.ofFloat(card, "translationY", -12f, 12f);
        floatCard.setDuration(2400);
        floatCard.setRepeatMode(ValueAnimator.REVERSE);
        floatCard.setRepeatCount(ValueAnimator.INFINITE);
        floatCard.setInterpolator(new AccelerateDecelerateInterpolator());
        floatCard.start();

        if (badge != null) {
            ObjectAnimator floatBadge = ObjectAnimator.ofFloat(badge, "translationY", -16f, 8f);
            floatBadge.setDuration(2400);
            floatBadge.setRepeatMode(ValueAnimator.REVERSE);
            floatBadge.setRepeatCount(ValueAnimator.INFINITE);
            floatBadge.setInterpolator(new AccelerateDecelerateInterpolator());
            floatBadge.start();
        }
    }
}