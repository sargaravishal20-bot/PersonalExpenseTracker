package com.example.personalexpensetracker;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.personalexpensetracker.database.DatabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {

    private TextInputEditText etAmount, etDate, etNote;
    private AutoCompleteTextView actCategory;
    private TextView tvHeaderTitle;

    private MaterialButton btnSave;
    private MaterialButtonToggleGroup typeToggle;

    private DatabaseHelper databaseHelper;

    private String transactionType = "EXPENSE";

    // Edit mode
    private boolean isEditMode = false;
    private int transactionId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        // Status bar insets
        View rootLayout = findViewById(R.id.rootScrollView);
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
            );
            view.setPadding(
                    view.getPaddingLeft(),
                    systemBars.top,
                    view.getPaddingRight(),
                    view.getPaddingBottom()
            );
            return windowInsets;
        });

        // Find Views
        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        typeToggle = findViewById(R.id.typeToggle);
        etAmount = findViewById(R.id.etAmount);
        actCategory = findViewById(R.id.actCategory);
        etDate = findViewById(R.id.etDate);
        etNote = findViewById(R.id.etNote);
        btnSave = findViewById(R.id.btnSave);

        databaseHelper = new DatabaseHelper(this);

        setupCategories();
        setupDatePicker();

        // Check if coming from "Send" or "Request" buttons
        String preselectedType = getIntent().getStringExtra("preselected_type");
        if ("INCOME".equals(preselectedType)) {
            transactionType = "INCOME";
            typeToggle.check(R.id.btnIncome);
        } else {
            transactionType = "EXPENSE";
            typeToggle.check(R.id.btnExpense);
        }

        // Check whether this is Edit mode
        checkEditMode();

        if (!isEditMode) {
            setupDate();
        }

        // Toggle Listener
        typeToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnExpense) {
                    transactionType = "EXPENSE";
                } else if (checkedId == R.id.btnIncome) {
                    transactionType = "INCOME";
                }
            }
        });

        // Save Click
        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void setupCategories() {
        String[] categories = {
                "Food",
                "Travel",
                "Shopping",
                "Bills",
                "Health",
                "Entertainment",
                "Education",
                "Salary",
                "Other"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categories
        );

        actCategory.setAdapter(adapter);
    }

    private void setupDate() {
        String currentDate = new SimpleDateFormat(
                "dd-MM-yyyy",
                Locale.getDefault()
        ).format(new Date());

        etDate.setText(currentDate);
    }

    private void setupDatePicker() {
        etDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            String currentDate = etDate.getText().toString().trim();

            if (!currentDate.isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    Date date = sdf.parse(currentDate);
                    if (date != null) {
                        calendar.setTime(date);
                    }
                } catch (Exception ignored) {
                }
            }

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        String selectedDate = String.format(
                                Locale.getDefault(),
                                "%02d-%02d-%04d",
                                dayOfMonth,
                                month + 1,
                                year
                        );
                        etDate.setText(selectedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        });
    }

    private void checkEditMode() {
        if (getIntent().hasExtra("transaction_id")) {
            isEditMode = true;
            transactionId = getIntent().getIntExtra("transaction_id", -1);

            String type = getIntent().getStringExtra("transaction_type");
            double amount = getIntent().getDoubleExtra("transaction_amount", 0);
            String category = getIntent().getStringExtra("transaction_category");
            String date = getIntent().getStringExtra("transaction_date");
            String note = getIntent().getStringExtra("transaction_note");

            if ("INCOME".equals(type)) {
                transactionType = "INCOME";
                typeToggle.check(R.id.btnIncome);
            } else {
                transactionType = "EXPENSE";
                typeToggle.check(R.id.btnExpense);
            }

            etAmount.setText(String.valueOf(amount));
            actCategory.setText(category, false);
            etDate.setText(date);
            etNote.setText(note);

            btnSave.setText("Update Transaction");
            if (tvHeaderTitle != null) {
                tvHeaderTitle.setText("Edit Transaction");
            }
        }
    }

    private void saveTransaction() {
        String amountText = etAmount.getText().toString().trim();
        String category = actCategory.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String note = etNote.getText().toString().trim();

        if (amountText.isEmpty()) {
            etAmount.setError("Enter amount");
            return;
        }

        if (category.isEmpty()) {
            actCategory.setError("Select category");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            etAmount.setError("Enter a valid amount");
            return;
        }

        if (isEditMode) {
            boolean updated = databaseHelper.updateTransaction(
                    transactionId,
                    transactionType,
                    amount,
                    category,
                    date,
                    note
            );

            if (updated) {
                Toast.makeText(this, "Transaction Updated", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to update transaction", Toast.LENGTH_SHORT).show();
            }
        } else {
            boolean inserted = databaseHelper.addTransaction(
                    transactionType,
                    amount,
                    category,
                    date,
                    note
            );

            if (inserted) {
                Toast.makeText(this, "Transaction Added", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to save transaction", Toast.LENGTH_SHORT).show();
            }
        }
    }
}