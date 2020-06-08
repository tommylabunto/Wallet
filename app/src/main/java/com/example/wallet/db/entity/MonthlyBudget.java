package com.example.wallet.db.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Size;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class MonthlyBudget {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    private long monthlyBudgetId;

    @NonNull
    @Size(min = 0)
    private double budget;

    @NonNull
    @Size(min = 0)
    private int year;

    // start from 0 to 11
    @NonNull
    @Size(min = 0)
    private int month;

    // instantiated every month
    public MonthlyBudget() {
    }

    public MonthlyBudget(double budget, int year, int month) {
        this();
        this.budget = budget;
        this.year = year;
        this.month = month;
    }

    public long getMonthlyBudgetId() {
        return monthlyBudgetId;
    }

    public void setMonthlyBudgetId(long monthlyBudgetId) {
        this.monthlyBudgetId = monthlyBudgetId;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
