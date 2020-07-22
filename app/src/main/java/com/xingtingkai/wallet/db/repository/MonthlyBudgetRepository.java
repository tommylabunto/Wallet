package com.xingtingkai.wallet.db.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.xingtingkai.wallet.db.WalletDatabase;
import com.xingtingkai.wallet.db.dao.MonthlyBudgetDao;
import com.xingtingkai.wallet.db.entity.MonthlyBudget;

import java.util.List;
import java.util.concurrent.Future;

public class MonthlyBudgetRepository {

    private MonthlyBudgetDao monthlyBudgetDao;

    public MonthlyBudgetRepository(Application application) {
        WalletDatabase db = WalletDatabase.getDatabase(application);
        monthlyBudgetDao = db.getMonthlyBudgetDao();
    }

    /*
    Room executes all queries on a separate thread.
    Observed LiveData will notify the observer when the data has changed.
     */
    public LiveData<List<MonthlyBudget>> getAllMonthlyBudgetsInAYear(int year) {
        return monthlyBudgetDao.getAllMonthlyBudgetsInAYear(year);
    }

    /*
     You must call this on a non-UI thread or your app will throw an exception. Room ensures
     that you're not doing any long running operations on the main thread, blocking the UI.
    */
    public void insertMonthlyBudget(MonthlyBudget monthlyBudget) {
        WalletDatabase.databaseWriteExecutor.execute(() ->
                monthlyBudgetDao.insertMonthlyBudget(monthlyBudget));
    }
    public void deleteMonthlyBudget(MonthlyBudget monthlyBudget) {
        WalletDatabase.databaseWriteExecutor.execute(() ->
                monthlyBudgetDao.deleteMonthlyBudget(monthlyBudget));
    }

    public void updateMonthlyBudget(MonthlyBudget monthlyBudget) {
        WalletDatabase.databaseWriteExecutor.execute(() ->
                monthlyBudgetDao.updateMonthlyBudget(monthlyBudget));
    }

    public void updateAllMonthlyBudgets(double budget) {
        WalletDatabase.databaseWriteExecutor.execute(() ->
                monthlyBudgetDao.updateAllMonthlyBudgets(budget));
    }

    public void updateAllFutureMonthlyBudgets(long monthlyBudgetId, double budget) {
        WalletDatabase.databaseWriteExecutor.execute(() ->
                monthlyBudgetDao.updateAllFutureMonthlyBudgets(monthlyBudgetId, budget));
    }

    public Future<MonthlyBudget> getMonthlyBudget(int year, int month) {
        return WalletDatabase.databaseWriteExecutor
                .submit(() -> monthlyBudgetDao.getMonthlyBudget(year, month));
    }
}
