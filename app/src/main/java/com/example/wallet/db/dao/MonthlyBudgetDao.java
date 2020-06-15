package com.example.wallet.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.wallet.db.entity.MonthlyBudget;

import java.util.List;

@Dao
public interface MonthlyBudgetDao {

    @Insert
    public void insertMonthlyBudget(MonthlyBudget monthlyBudget);

    @Update
    public void updateMonthlyBudget(MonthlyBudget monthlyBudget);

    @Delete
    public void deleteMonthlyBudget(MonthlyBudget monthlyBudget);

    @Query("SELECT monthlyBudgetId, budget, year, month FROM monthlyBudget WHERE year = :year AND month = :month")
    public LiveData<MonthlyBudget> getMonthlyBudget(int year, int month);

    @Query("UPDATE monthlyBudget SET budget = :budget")
    public void updateAllMonthlyBudgets(double budget);

    @Query("UPDATE monthlyBudget SET budget = :budget WHERE monthlyBudgetId >= :monthlyBudgetId")
    public void updateAllFutureMonthlyBudgets(Long monthlyBudgetId, double budget);

    @Query("SELECT monthlyBudgetId, budget, year, month FROM monthlyBudget ORDER BY monthlyBudgetId ASC")
    public LiveData<List<MonthlyBudget>> getAllMonthlyBudgets();

    @Query("SELECT monthlyBudgetId, budget, year, month FROM monthlyBudget WHERE year > :year AND month > :month ORDER BY monthlyBudgetId ASC")
    public List<MonthlyBudget> getAllFutureMonthlyBudgetsList(int year, int month);

    @Query("SELECT monthlyBudgetId, budget, year, month FROM monthlyBudget ORDER BY monthlyBudgetId ASC")
    public List<MonthlyBudget> getAllMonthlyBudgetsList();

    @Query("SELECT monthlyBudgetId, budget, year, month FROM monthlyBudget WHERE year = :year ORDER BY monthlyBudgetId ASC")
    public LiveData<List<MonthlyBudget>> getAllMonthlyBudgetsInAYear(int year);
}
