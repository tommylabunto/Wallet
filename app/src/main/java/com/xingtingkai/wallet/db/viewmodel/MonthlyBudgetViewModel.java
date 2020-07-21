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
    // Using LiveData and caching what getAlphabetizedWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    private LiveData<List<MonthlyBudget>> allMonthlyBudgets;

    public MonthlyBudgetViewModel(Application application) {
        super(application);
        monthlyBudgetRepository = new MonthlyBudgetRepository(application);
        allMonthlyBudgets = monthlyBudgetRepository.getAllMonthlyBudgets();
    }

    public LiveData<List<MonthlyBudget>> getAllMonthlyBudgets() {
        return allMonthlyBudgets;
    }

    public LiveData<List<MonthlyBudget>> getAllMonthlyBudgetsInAYear(int year) {
        return monthlyBudgetRepository.getAllMonthlyBudgetsInAYear(year);
    }

    public LiveData<MonthlyBudget> getMonthlyBudget(int year, int month) {
        return monthlyBudgetRepository.getMonthlyBudget(year, month);
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

    public Future<MonthlyBudget> getMonthlyBudgetTemp(int year, int month) {
        return monthlyBudgetRepository.getMonthlyBudgetTemp(year, month);
    }
}
