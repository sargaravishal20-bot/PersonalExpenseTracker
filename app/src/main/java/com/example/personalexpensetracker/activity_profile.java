package com.example.personalexpensetracker;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.personalexpensetracker.database.DatabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class activity_profile extends AppCompatActivity {

    private TextView tvAvatarPreview;
    private TextInputEditText etCustomEmoji, etProfileName, etProfileAmount;
    private MaterialButton btnSaveProfile;
    private DatabaseHelper databaseHelper;

    private String selectedAvatar = "😎";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        databaseHelper = new DatabaseHelper(this);

        tvAvatarPreview = findViewById(R.id.tvAvatarPreview);
        etCustomEmoji = findViewById(R.id.etCustomEmoji);
        etProfileName = findViewById(R.id.etProfileName);
        etProfileAmount = findViewById(R.id.etProfileAmount);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Load existing profile from DB
        loadExistingProfile();

        // Setup quick emoji chips
        setupEmojiChips();

        // Custom emoji typing listener
        etCustomEmoji.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    selectedAvatar = s.toString().trim();
                    tvAvatarPreview.setText(selectedAvatar);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Save Profile Button
        btnSaveProfile.setOnClickListener(v -> saveProfileData());
    }

    private void loadExistingProfile() {
        String[] profile = databaseHelper.getProfile();
        etProfileName.setText(profile[0]);
        selectedAvatar = profile[1];
        tvAvatarPreview.setText(selectedAvatar);
        etProfileAmount.setText(profile[2]);
    }

    private void setupEmojiChips() {
        int[] chipIds = {R.id.emoji1, R.id.emoji2, R.id.emoji3, R.id.emoji4, R.id.emoji5, R.id.emoji6};
        for (int id : chipIds) {
            TextView chip = findViewById(id);
            if (chip != null) {
                chip.setOnClickListener(v -> {
                    selectedAvatar = chip.getText().toString();
                    tvAvatarPreview.setText(selectedAvatar);
                    etCustomEmoji.setText("");
                });
            }
        }
    }

    private void saveProfileData() {
        String name = etProfileName.getText().toString().trim();
        String amountStr = etProfileAmount.getText().toString().trim();

        if (name.isEmpty()) {
            etProfileName.setError("Enter name");
            return;
        }

        double amount = 2085.00;
        if (!amountStr.isEmpty()) {
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException ignored) {}
        }

        boolean saved = databaseHelper.saveProfile(name, selectedAvatar, amount);
        if (saved) {
            Toast.makeText(this, "Profile Saved!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show();
        }
    }
}