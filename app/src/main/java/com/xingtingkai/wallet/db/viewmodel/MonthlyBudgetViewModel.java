package com.xingtingkai.wallet.db.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.xingtingkai.wallet.db.entity.MonthlyBudget;
import com.xingtingkai.wallet.db.repository.MonthlyBudgetRepository;

import java.util.List;
import java.util.concurrent.Future;

public class MonthlyBudgetViewModel extends AndroidViewModel {

    private MonthlyBudgetRepository monthlyBudgetRepository;

    public MonthlyBudgetViewModel(Application application) {
        super(application);
        monthlyBudgetRepository = new MonthlyBudgetRepository(application);
    }

    public LiveData<List<MonthlyBudget>> getAllMonthlyBudgetsInAYear(int year) {
        return monthlyBudgetRepository.getAllMonthlyBudgetsInAYear(year);
    }

    public void insertMonthlyBudget(MonthlyBudget monthlyBudget) {
        monthlyBudgetRepository.insertMonthlyBudget(monthlyBudget);
    }

    public void updateMonthlyBudget(MonthlyBudget monthlyBudget) {
        monthlyBudgetRepository.updateMonthlyBudget(monthlyBudget);
    }

    public void deleteMonthlyBudget(MonthlyBudget monthlyBudget) {
        monthlyBudgetRepository.deleteMonthlyBudget(monthlyBudget);
    }

    public void updateAllMonthlyBudgets(double budget) {
        monthlyBudgetRepository.updateAllMonthlyBudgets(budget);
    }

    public void updateAllFutureMonthlyBudgets(long monthlyBudgetId, double budget) {
        monthlyBudgetRepository.updateAllFutureMonthlyBudgets(monthlyBudgetId, budget);
    }

    public Future<MonthlyBudget> getMonthlyBudget(int year, int month) {
        return monthlyBudgetRepository.getMonthlyBudget(year, month);
    }
}
