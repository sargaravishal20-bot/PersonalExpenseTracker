package com.example.personalexpensetracker.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalexpensetracker.AddTransactionActivity;
import com.example.personalexpensetracker.R;
import com.example.personalexpensetracker.database.DatabaseHelper;
import com.example.personalexpensetracker.model.Transaction;

import java.util.List;
import java.util.Locale;

public class TransactionAdapter
        extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList;
    private DatabaseHelper databaseHelper;

    public interface OnTransactionChangeListener {
        void onTransactionChanged();
    }

    private OnTransactionChangeListener changeListener;

    public TransactionAdapter(
            List<Transaction> transactionList,
            DatabaseHelper databaseHelper,
            OnTransactionChangeListener changeListener) {

        this.transactionList = transactionList;
        this.databaseHelper = databaseHelper;
        this.changeListener = changeListener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);

        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull TransactionViewHolder holder,
            int position) {

        Transaction transaction = transactionList.get(position);

        holder.tvCategory.setText(transaction.getCategory());

        // Category Emoji Icon
        String category = transaction.getCategory();
        switch (category != null ? category : "") {
            case "Food":
                holder.tvCategoryIcon.setText("🍔");
                break;
            case "Travel":
                holder.tvCategoryIcon.setText("🚗");
                break;
            case "Shopping":
                holder.tvCategoryIcon.setText("🛍️");
                break;
            case "Bills":
                holder.tvCategoryIcon.setText("💡");
                break;
            case "Health":
                holder.tvCategoryIcon.setText("❤️");
                break;
            case "Entertainment":
                holder.tvCategoryIcon.setText("🎬");
                break;
            case "Education":
                holder.tvCategoryIcon.setText("📚");
                break;
            case "Salary":
                holder.tvCategoryIcon.setText("💰");
                break;
            default:
                holder.tvCategoryIcon.setText("📦");
                break;
        }

        // Note & Date
        holder.tvNote.setText(transaction.getNote());
        holder.tvDate.setText(transaction.getDate());

        // Amount Formatting
        String amount = String.format(
                Locale.getDefault(),
                "₹%.2f",
                transaction.getAmount()
        );

        if ("EXPENSE".equals(transaction.getType())) {
            holder.tvAmount.setText("- " + amount);
            holder.tvAmount.setTextColor(0xFFFF5252); // Nova Red
        } else {
            holder.tvAmount.setText("+ " + amount);
            holder.tvAmount.setTextColor(0xFF10B981); // Nova Green
        }

        // Tap item row directly to edit
        holder.itemView.setOnClickListener(v -> openEditActivity(v.getContext(), transaction));

        // Explicit edit button
        holder.btnEdit.setOnClickListener(v -> openEditActivity(v.getContext(), transaction));

        // Delete button
        holder.btnDelete.setOnClickListener(v -> confirmDelete(v.getContext(), holder.getAdapterPosition()));

        // Long press on item row to delete
        holder.itemView.setOnLongClickListener(v -> {
            confirmDelete(v.getContext(), holder.getAdapterPosition());
            return true;
        });
    }

    private void openEditActivity(Context context, Transaction transaction) {
        Intent intent = new Intent(context, AddTransactionActivity.class);
        intent.putExtra("transaction_id", transaction.getId());
        intent.putExtra("transaction_type", transaction.getType());
        intent.putExtra("transaction_amount", transaction.getAmount());
        intent.putExtra("transaction_category", transaction.getCategory());
        intent.putExtra("transaction_date", transaction.getDate());
        intent.putExtra("transaction_note", transaction.getNote());
        context.startActivity(intent);
    }

    private void confirmDelete(Context context, int currentPosition) {
        if (currentPosition == RecyclerView.NO_POSITION || currentPosition >= transactionList.size()) {
            return;
        }

        Transaction selectedTransaction = transactionList.get(currentPosition);

        new AlertDialog.Builder(context)
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean deleted = databaseHelper.deleteTransaction(selectedTransaction.getId());
                    if (deleted) {
                        transactionList.remove(currentPosition);
                        notifyItemRemoved(currentPosition);
                        notifyItemRangeChanged(currentPosition, transactionList.size());
                        Toast.makeText(context, "Transaction deleted", Toast.LENGTH_SHORT).show();
                        if (changeListener != null) {
                            changeListener.onTransactionChanged();
                        }
                    } else {
                        Toast.makeText(context, "Failed to delete transaction", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryIcon;
        TextView tvCategory;
        TextView tvNote;
        TextView tvDate;
        TextView tvAmount;
        ImageButton btnEdit;
        ImageButton btnDelete;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryIcon = itemView.findViewById(R.id.tvCategoryIcon);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvNote = itemView.findViewById(R.id.tvNote);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}