package com.example.personalexpensetracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.personalexpensetracker.model.Transaction;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "expense_tracker.db";
    // Version 2 kar diya hai taaki naya profile table bina error ke ban jaye
    private static final int DATABASE_VERSION = 2;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // 1. Transactions Table
        String createTable = "CREATE TABLE transactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "type TEXT, " +
                "amount REAL, " +
                "category TEXT, " +
                "date TEXT, " +
                "note TEXT" +
                ")";
        db.execSQL(createTable);

        // 2. User Profile Table
        String createProfileTable = "CREATE TABLE IF NOT EXISTS user_profile (" +
                "id INTEGER PRIMARY KEY, " +
                "name TEXT, " +
                "avatar TEXT, " +
                "amount REAL" +
                ")";
        db.execSQL(createProfileTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Naya Profile Table add karna (Purana data delete kiye bina)
        if (oldVersion < 2) {
            String createProfileTable = "CREATE TABLE IF NOT EXISTS user_profile (" +
                    "id INTEGER PRIMARY KEY, " +
                    "name TEXT, " +
                    "avatar TEXT, " +
                    "amount REAL" +
                    ")";
            db.execSQL(createProfileTable);
        }
    }

    // ===================================================
    // PROFILE METHODS (Name, Avatar, Amount)
    // ===================================================

    public boolean saveProfile(String name, String avatar, double amount) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Safety check: ensure table exists
        db.execSQL("CREATE TABLE IF NOT EXISTS user_profile (" +
                "id INTEGER PRIMARY KEY, " +
                "name TEXT, " +
                "avatar TEXT, " +
                "amount REAL" +
                ")");

        ContentValues values = new ContentValues();
        values.put("id", 1);
        values.put("name", name);
        values.put("avatar", avatar);
        values.put("amount", amount);

        long result = db.insertWithOnConflict(
                "user_profile",
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );

        db.close();
        return result != -1;
    }

    public String[] getProfile() {
        SQLiteDatabase db = this.getReadableDatabase();

        // Default values agar user ne abhi profile save na ki ho
        String[] profileData = new String[]{"My Wallet", "😎", "2085.00"};

        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS user_profile (" +
                    "id INTEGER PRIMARY KEY, " +
                    "name TEXT, " +
                    "avatar TEXT, " +
                    "amount REAL" +
                    ")");

            Cursor cursor = db.rawQuery("SELECT name, avatar, amount FROM user_profile WHERE id = 1", null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    profileData[0] = cursor.getString(0); // Name
                    profileData[1] = cursor.getString(1); // Avatar
                    profileData[2] = String.valueOf(cursor.getDouble(2)); // Amount
                }
                cursor.close();
            }
        } catch (Exception ignored) {
        }

        db.close();
        return profileData;
    }

    // ===================================================
    // TRANSACTION METHODS (Purane saare methods safe hain)
    // ===================================================

    public boolean addTransaction(String type, double amount,
                                  String category, String date, String note) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("type", type);
        values.put("amount", amount);
        values.put("category", category);
        values.put("date", date);
        values.put("note", note);

        long result = db.insert("transactions", null, values);
        db.close();

        return result != -1;
    }

    public List<Transaction> getAllTransactions() {

        List<Transaction> transactionList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM transactions ORDER BY id DESC",
                null
        );

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                String note = cursor.getString(cursor.getColumnIndexOrThrow("note"));

                Transaction transaction = new Transaction(
                        id,
                        type,
                        amount,
                        category,
                        date,
                        note
                );

                transactionList.add(transaction);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return transactionList;
    }

    public boolean updateTransaction(int id, String type, double amount,
                                     String category, String date, String note) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("type", type);
        values.put("amount", amount);
        values.put("category", category);
        values.put("date", date);
        values.put("note", note);

        int result = db.update(
                "transactions",
                values,
                "id = ?",
                new String[]{String.valueOf(id)}
        );

        db.close();
        return result > 0;
    }

    public boolean deleteTransaction(int id) {

        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete(
                "transactions",
                "id = ?",
                new String[]{String.valueOf(id)}
        );

        db.close();
        return result > 0;
    }

    public double getTotalIncome() {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT SUM(amount) FROM transactions WHERE type = ?",
                new String[]{"INCOME"}
        );

        double total = 0;

        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }

        cursor.close();
        db.close();

        return total;
    }

    public double getTotalExpense() {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT SUM(amount) FROM transactions WHERE type = ?",
                new String[]{"EXPENSE"}
        );

        double total = 0;

        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }

        cursor.close();
        db.close();

        return total;
    }
}