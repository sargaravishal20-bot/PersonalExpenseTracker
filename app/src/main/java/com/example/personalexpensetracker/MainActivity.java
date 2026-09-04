package com.example.personalexpensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalexpensetracker.adapter.TransactionAdapter;
import com.example.personalexpensetracker.database.DatabaseHelper;
import com.example.personalexpensetracker.model.Transaction;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvBalance;
    private TextView tvIncome;
    private TextView tvExpense;
    private TextView tvTransactionsCount;
    private ImageView ivToggleBalance;

    private View emptyState;
    private RecyclerView recyclerTransactions;

    private DatabaseHelper databaseHelper;
    private TransactionAdapter transactionAdapter;

    private boolean isBalanceHidden = false;
    private double currentBalance = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // <-- setContentView pehle aana chahiye

        // =========================
        // Handle Status Bar Insets
        // =========================
        View rootLayout = findViewById(R.id.rootLayout);
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

        // =========================
        // Views
        // =========================
        tvBalance = findViewById(R.id.tvBalance);
        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);
        tvTransactionsCount = findViewById(R.id.tvTransactionsCount);
        ivToggleBalance = findViewById(R.id.ivToggleBalance);
        emptyState = findViewById(R.id.emptyState);

        // ============================================
        // Avatar Click -> Open Profile Activity
        // ============================================
        View layoutAvatar = findViewById(R.id.layoutAvatar);
        if (layoutAvatar != null) {
            layoutAvatar.setOnClickListener(v -> {
                // Agar aapki file ka naam ProfileActivity hai toh ProfileActivity.class rakhein
                Intent intent = new Intent(MainActivity.this, activity_profile.class);
                startActivity(intent);
            });
        }

        // Eye toggle for Balance
        ivToggleBalance.setOnClickListener(v -> {
            isBalanceHidden = !isBalanceHidden;
            if (isBalanceHidden) {
                tvBalance.setText("••••••");
            } else {
                tvBalance.setText(String.format(Locale.getDefault(), "₹%.2f", currentBalance));
            }
        });

        // =========================
        // RecyclerView Setup
        // =========================
        recyclerTransactions = findViewById(R.id.recyclerTransactions);
        recyclerTransactions.setLayoutManager(new LinearLayoutManager(this));

        // =========================
        // Database
        // =========================
        databaseHelper = new DatabaseHelper(this);

        // =========================
        // Quick Action Buttons
        // =========================
        View btnSend = findViewById(R.id.btnSend);
        if (btnSend != null) {
            btnSend.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
                intent.putExtra("preselected_type", "EXPENSE");
                startActivity(intent);
            });
        }

        View btnRequest = findViewById(R.id.btnRequest);
        if (btnRequest != null) {
            btnRequest.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
                intent.putExtra("preselected_type", "INCOME");
                startActivity(intent);
            });
        }

        // Optional backward-compatible fabAdd
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
                startActivity(intent);
            });
        }

        // =========================
        // Initial Load
        // =========================
        loadDashboard();
        loadTransactions();
    }

    // =========================
    // LOAD DASHBOARD
    // =========================
    private void loadDashboard() {
        double income = databaseHelper.getTotalIncome();
        double expense = databaseHelper.getTotalExpense();
        currentBalance = income - expense;

        tvIncome.setText(String.format(Locale.getDefault(), "₹%.2f", income));
        tvExpense.setText(String.format(Locale.getDefault(), "₹%.2f", expense));

        if (isBalanceHidden) {
            tvBalance.setText("••••••");
        } else {
            tvBalance.setText(String.format(Locale.getDefault(), "₹%.2f", currentBalance));
        }
    }

    // =========================
    // LOAD TRANSACTIONS
    // =========================
    private void loadTransactions() {
        List<Transaction> transactionList = databaseHelper.getAllTransactions();

        if (tvTransactionsCount != null) {
            tvTransactionsCount.setText(transactionList.size() + " Items");
        }

        if (transactionList.isEmpty()) {
            recyclerTransactions.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerTransactions.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);

            transactionAdapter = new TransactionAdapter(
                    transactionList,
                    databaseHelper,
                    () -> {
                        loadDashboard();
                        loadTransactions();
                    }
            );

            recyclerTransactions.setAdapter(transactionAdapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (databaseHelper != null) {
            loadDashboard();
            loadTransactions();

            // Profile Avatar icon update on resume
            TextView tvAvatar = findViewById(R.id.tvAvatar);
            if (tvAvatar != null) {
                String[] profile = databaseHelper.getProfile();
                tvAvatar.setText(profile[1]); // Sets saved avatar emoji
            }
        }
    }
}