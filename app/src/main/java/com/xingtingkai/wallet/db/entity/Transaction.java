package com.xingtingkai.wallet.db.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Size;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.auto.value.AutoValue;

import java.time.Instant;
import java.time.ZoneId;

@AutoValue
@Entity
public abstract class Transaction {

    @AutoValue.CopyAnnotations
    @PrimaryKey(autoGenerate = true)
    public abstract long getTransactionId();

    // each recurring transaction will have the same id
    @AutoValue.CopyAnnotations
    public abstract String getTransactionRecurringId();

    /*
    cannot store as ZonedDateTime directly
    - because needs at least 2 parameters to construct, so not applicable in TypeConverter
    cannot store ZonedDateTime as String
    - because dao compares time in seconds
     */
    @AutoValue.CopyAnnotations
    @NonNull
    public abstract Instant getInstant();

    @AutoValue.CopyAnnotations
    @NonNull
    public abstract ZoneId getZoneId();

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

    /*
     number of times in a year
     to get duration between successive events, use 12 / frequency
     12 (monthly)
     4 (quarterly)
     2 (biannually)
     1 (annually)
    */
    @AutoValue.CopyAnnotations
    @Size(min = 1)
    public abstract int getFrequency();

    /*
     min 0 year to max 30 years
     0 means no more recurring in current year
    */
    @AutoValue.CopyAnnotations
    @Size(min = 0, max = 30)
    public abstract int getNumOfRepeat();

    @AutoValue.CopyAnnotations
    public abstract boolean isExpenseTransaction();

    @Ignore
    public static Transaction createNonRecurringTransaction(long transactionId, @NonNull Instant instant, @NonNull ZoneId zoneId, double value, @NonNull String name, @NonNull String typeName, boolean expenseTransaction) {
        return createTransaction(transactionId, "", instant, zoneId, value, name, typeName, false, 1, 0, expenseTransaction);
    }

    @Ignore
    public static Transaction createRecurringTransaction(long transactionId, @NonNull String transactionRecurringId, @NonNull Instant instant, @NonNull ZoneId zoneId, double value, @NonNull String name, @NonNull String typeName, int frequency, int numOfRepeat, boolean expenseTransaction) {
        return createTransaction(transactionId, transactionRecurringId, instant, zoneId, value, name, typeName, true, frequency, numOfRepeat, expenseTransaction);
    }

    public static Transaction createTransaction(long transactionId, String transactionRecurringId, Instant instant, ZoneId zoneId, double value, String name, String typeName, boolean repeat, int frequency, int numOfRepeat, boolean expenseTransaction) {
        return new AutoValue_Transaction(transactionId, transactionRecurringId, instant, zoneId, value, name, typeName, repeat, frequency, numOfRepeat, expenseTransaction);
    }
}

