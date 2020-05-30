package com.example.wallet.db;

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
    private double budget;

    @NonNull
    @Size(min = 0)
    private int yearMonth;

    // instantiated every month
    public MonthlyBudget() {
    }

    public MonthlyBudget(double budget, int yearMonth) {
        this();
        this.budget = budget;
        this.yearMonth = yearMonth;
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

    public int getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(int yearMonth) {
        this.yearMonth = yearMonth;
    }
}
