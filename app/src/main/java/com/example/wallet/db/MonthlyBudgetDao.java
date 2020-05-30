package com.example.wallet.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MonthlyBudgetDao {

    @Insert
    public void insertMonthlyBudget(MonthlyBudget monthlyBudget);

    @Update
    public void updateMonthlyBudget(MonthlyBudget monthlyBudget);

    @Delete
    public void deleteMonthlyBudget(MonthlyBudget monthlyBudget);

//    @Query("SELECT budget, yearMonth FROM monthlyBudget WHERE yearMonth = :selectedYearMonth")
//    public String getMonthlyBudget(int selectedYearMonth);

    @Query("SELECT monthlyBudgetId, budget, yearMonth FROM monthlyBudget")
    public LiveData<List<MonthlyBudget>> getAllMonthlyBudgets();
}
