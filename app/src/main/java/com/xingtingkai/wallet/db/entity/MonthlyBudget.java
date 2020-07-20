package com.xingtingkai.wallet.db.entity;

import androidx.annotation.Size;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.auto.value.AutoValue;

@AutoValue
@Entity
public abstract class MonthlyBudget {

    @AutoValue.CopyAnnotations
    @PrimaryKey(autoGenerate = true)
    public abstract long getMonthlyBudgetId();

    @AutoValue.CopyAnnotations
    @Size(min = 0)
    public abstract int getBudget();

    @AutoValue.CopyAnnotations
    @Size(min = 0)
    public abstract int getYear();

    @AutoValue.CopyAnnotations
    // start from 1 to 12
    @Size(min = 0)
    public abstract int getMonth();

    // Room uses this factory method to create MonthlyBudget objects.
    public static MonthlyBudget create(long monthlyBudgetId, int budget, int year, int month) {
        return new AutoValue_MonthlyBudget(monthlyBudgetId, budget, year, month);
    }

//    @PrimaryKey(autoGenerate = true)
//    @NonNull
//    private long monthlyBudgetId;
//
//    @NonNull
//    @Size(min = 0)
//    private int budget;
//
//    @NonNull
//    @Size(min = 0)
//    private int year;
//
//    // start from 0 to 11
//    @NonNull
//    @Size(min = 0)
//    private int month;
//
//    // instantiated every month
//    public MonthlyBudget() {
//    }
//
//    public MonthlyBudget(int budget, int year, int month) {
//        this();
//        this.budget = budget;
//        this.year = year;
//        this.month = month;
//    }
//
//    public long getMonthlyBudgetId() {
//        return monthlyBudgetId;
//    }
//
//    public void setMonthlyBudgetId(long monthlyBudgetId) {
//        this.monthlyBudgetId = monthlyBudgetId;
//    }
//
//    public int getBudget() {
//        return budget;
//    }
//
//    public void setBudget(int budget) {
//        this.budget = budget;
//    }
//
//    public int getMonth() {
//        return month;
//    }
//
//    public void setMonth(int month) {
//        this.month = month;
//    }
//
//    public int getYear() {
//        return year;
//    }
//
//    public void setYear(int year) {
//        this.year = year;
//    }
//
//    @NonNull
//    @Override
//    public String toString() {
//        return "Monthly Budget Id: " + getMonthlyBudgetId()
//                + ", " + "Month: " + getMonth()
//                + ", " + "Year: " + getYear();
//    }
}
