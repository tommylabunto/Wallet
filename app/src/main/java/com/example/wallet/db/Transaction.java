package com.example.wallet.db;

import androidx.annotation.NonNull;
import androidx.annotation.Size;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity
public class Transaction {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    private long transactionId;

    // each recurring transaction will have the same id
    private String transactionRecurringId;

    @NonNull
    private Date date;

    @NonNull
    private double value;

    // name of transaction
    @NonNull
    @Size(min = 1)
    private String name;

    // name of type of transaction (e.g. food, transport)
    @NonNull
    @Size(min = 1, max = 100)
    private String typeName;

    @NonNull
    private boolean isRepeat;

    // number of times in a year
    // to get duration between successive events, use 12 / frequency
    // 12 (monthly)
    // 4 (quarterly)
    // 2 (biannually)
    // 1 (annually)
    private int frequency;

    // min 0 year to max 30 years
    // 0 means no more recurring in current year
    @Size(min = 1, max = 30)
    private int numOfRepeat;

    @NonNull
    private boolean isExpenseTransaction;

    // can be income(+) or expense (-)
    public Transaction() {
        this.isRepeat = false;
    }

    // for non-recurring transaction
    public Transaction(Date date, double value, String name, String typeName, boolean isExpenseTransaction) {
        this();

        this.date = date;
        this.value = value;
        this.typeName = typeName;
        this.name = name;
        this.isExpenseTransaction = isExpenseTransaction;
    }

    // for recurring transaction
    public Transaction(Date date, double value, String name, String typeName, int frequency, int numOfRepeat, boolean isExpenseTransaction) {
        this();

        this.date = date;
        this.value = value;
        this.typeName = typeName;
        this.name = name;
        this.frequency = frequency;
        this.numOfRepeat = numOfRepeat;
        this.isExpenseTransaction = isExpenseTransaction;

        this.isRepeat = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public boolean isRepeat() {
        return isRepeat;
    }

    public void setRepeat(boolean repeat) {
        isRepeat = repeat;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getNumOfRepeat() {
        return numOfRepeat;
    }

    public void setNumOfRepeat(int numOfRepeat) {
        this.numOfRepeat = numOfRepeat;
    }

    public String getTransactionRecurringId() {
        return transactionRecurringId;
    }

    public void setTransactionRecurringId(String transactionRecurringId) {
        this.transactionRecurringId = transactionRecurringId;
    }

    public boolean isExpenseTransaction() {
        return isExpenseTransaction;
    }

    public void setExpenseTransaction(boolean expenseTransaction) {
        isExpenseTransaction = expenseTransaction;
    }
}

