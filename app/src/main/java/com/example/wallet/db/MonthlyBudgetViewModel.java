package com.example.wallet.db;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

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

    public void insertMonthlyBudget(MonthlyBudget monthlyBudget) {
        monthlyBudgetRepository.insertMonthlyBudget(monthlyBudget);
    }

    public void updateMonthlyBudget(MonthlyBudget monthlyBudget) {
        monthlyBudgetRepository.updateMonthlyBudget(monthlyBudget);
    }

    public void deleteMonthlyBudget(MonthlyBudget monthlyBudget) {
        monthlyBudgetRepository.deleteMonthlyBudget(monthlyBudget);
    }
}
