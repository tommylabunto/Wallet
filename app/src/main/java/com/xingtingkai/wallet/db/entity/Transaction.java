package com.xingtingkai.wallet.db.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Size;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.auto.value.AutoValue;

import java.util.Date;

@AutoValue
@Entity
public abstract class Transaction {

    @AutoValue.CopyAnnotations
    @PrimaryKey(autoGenerate = true)
    public abstract long getTransactionId();

    // each recurring transaction will have the same id
    @AutoValue.CopyAnnotations
    public abstract String getTransactionRecurringId();

    @AutoValue.CopyAnnotations
    @NonNull
    public abstract Date getDate();

    @AutoValue.CopyAnnotations
    @Size(min = 0)
    public abstract double getValue();

    // name of transaction
    @AutoValue.CopyAnnotations
    @NonNull
    @Size(min = 1)
    public abstract String getName();

    // name of type of transaction (e.g. food, transport)
    @AutoValue.CopyAnnotations
    @NonNull
    @Size(min = 1, max = 100)
    public abstract String getTypeName();

    @AutoValue.CopyAnnotations
    public abstract boolean isRepeat();

    // number of times in a year
    // to get duration between successive events, use 12 / frequency
    // 12 (monthly)
    // 4 (quarterly)
    // 2 (biannually)
    // 1 (annually)
    @AutoValue.CopyAnnotations
    @Size(min = 1)
    public abstract int getFrequency();

    // min 0 year to max 30 years
    // 0 means no more recurring in current year
    @AutoValue.CopyAnnotations
    @Size(min = 0, max = 30)
    public abstract int getNumOfRepeat();

    @AutoValue.CopyAnnotations
    public abstract boolean isExpenseTransaction();

    @Ignore
    public static Transaction createNonRecurringTransaction(Long transactionId, Date date, double value, String name, String typeName, boolean expenseTransaction) {
        return createTransaction(transactionId, "", date, value, name, typeName, false, 1, 0, expenseTransaction);
    }

    @Ignore
    public static Transaction createRecurringTransaction(Long transactionId, String transactionRecurringId, Date date, double value, String name, String typeName, int frequency, int numOfRepeat, boolean expenseTransaction) {
        return createTransaction(transactionId, transactionRecurringId, date, value, name, typeName, true, frequency, numOfRepeat, expenseTransaction);
    }

    public static Transaction createTransaction(Long transactionId, String transactionRecurringId, Date date, double value, String name, String typeName, boolean repeat, int frequency, int numOfRepeat, boolean expenseTransaction) {
        return new AutoValue_Transaction(transactionId, transactionRecurringId, date, value, name, typeName, repeat, frequency, numOfRepeat, expenseTransaction);
    }

//    @PrimaryKey(autoGenerate = true)
//    @NonNull
//    private long transactionId;
//
//    // each recurring transaction will have the same id
//    private String transactionRecurringId;
//
//    @NonNull
//    private Date date;
//
//    @NonNull
//    @Size(min = 0)
//    private double value;
//
//    // name of transaction
//    @NonNull
//    @Size(min = 1)
//    private String name;
//
//    // name of type of transaction (e.g. food, transport)
//    @NonNull
//    @Size(min = 1, max = 100)
//    private String typeName;
//
//    @NonNull
//    private boolean isRepeat;
//
//    // number of times in a year
//    // to get duration between successive events, use 12 / frequency
//    // 12 (monthly)
//    // 4 (quarterly)
//    // 2 (biannually)
//    // 1 (annually)
//    @Size(min = 1)
//    private int frequency;
//
//    // min 0 year to max 30 years
//    // 0 means no more recurring in current year
//    @Size(min = 0, max = 30)
//    private int numOfRepeat;
//
//    @NonNull
//    private boolean isExpenseTransaction;
//
//    // can be income(+) or expense (-)
//    public Transaction() {
//        this.isRepeat = false;
//    }
//
//    // for non-recurring transaction
//    public Transaction(Date date, double value, String name, String typeName, boolean isExpenseTransaction) {
//        this();
//
//        this.date = date;
//        this.value = value;
//        this.typeName = typeName;
//        this.name = name;
//        this.isExpenseTransaction = isExpenseTransaction;
//    }
//
//    // for recurring transaction
//    public Transaction(Date date, double value, String name, String typeName, int frequency, int numOfRepeat, boolean isExpenseTransaction) {
//        this();
//
//        this.date = date;
//        this.value = value;
//        this.typeName = typeName;
//        this.name = name;
//        this.frequency = frequency;
//        this.numOfRepeat = numOfRepeat;
//        this.isExpenseTransaction = isExpenseTransaction;
//
//        this.isRepeat = true;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public double getValue() {
//        return value;
//    }
//
//    public void setValue(double value) {
//        this.value = value;
//    }
//
//    public Date getDate() {
//        return new Date(date.getTime());
//    }
//
//    public void setDate(Date date) {
//        this.date = date;
//    }
//
//    public long getTransactionId() {
//        return transactionId;
//    }
//
//    public void setTransactionId(long transactionId) {
//        this.transactionId = transactionId;
//    }
//
//    public String getTypeName() {
//        return typeName;
//    }
//
//    public void setTypeName(String typeName) {
//        this.typeName = typeName;
//    }
//
//    public boolean isRepeat() {
//        return isRepeat;
//    }
//
//    public void setRepeat(boolean repeat) {
//        isRepeat = repeat;
//    }
//
//    public int getFrequency() {
//        return frequency;
//    }
//
//    public void setFrequency(int frequency) {
//        this.frequency = frequency;
//    }
//
//    public int getNumOfRepeat() {
//        return numOfRepeat;
//    }
//
//    public void setNumOfRepeat(int numOfRepeat) {
//        this.numOfRepeat = numOfRepeat;
//    }
//
//    public String getTransactionRecurringId() {
//        return transactionRecurringId;
//    }
//
//    public void setTransactionRecurringId(String transactionRecurringId) {
//        this.transactionRecurringId = transactionRecurringId;
//    }
//
//    public boolean isExpenseTransaction() {
//        return isExpenseTransaction;
//    }
//
//    public void setExpenseTransaction(boolean expenseTransaction) {
//        isExpenseTransaction = expenseTransaction;
//    }
//
//    @NonNull
//    @Override
//    public String toString() {
//
//        return "Transaction Id: " + getTransactionId()
//                + ", " + "Transaction Name: " + getName();
//    }
}

