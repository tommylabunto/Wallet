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
}
