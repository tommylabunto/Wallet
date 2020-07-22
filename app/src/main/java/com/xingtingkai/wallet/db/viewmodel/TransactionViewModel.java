package com.xingtingkai.wallet.db.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.xingtingkai.wallet.db.entity.Transaction;
import com.xingtingkai.wallet.db.repository.TransactionRepository;

import java.util.List;
import java.util.concurrent.Future;

public class TransactionViewModel extends AndroidViewModel {

    private TransactionRepository transactionRepository;

    public TransactionViewModel(Application application) {
        super(application);
        transactionRepository = new TransactionRepository(application);
    }

    public LiveData<List<Transaction>> getExpenseRecurringTransactions(long millisecondsToday) {
        return transactionRepository.getExpenseRecurringTransactions(millisecondsToday);
    }

    public LiveData<List<Transaction>> getIncomeRecurringTransactions(long millisecondsToday) {
        return transactionRepository.getIncomeRecurringTransactions(millisecondsToday);
    }

    public LiveData<List<Transaction>> getAllTransactionsInAMonth(long millisecondsStart, long millisecondsEnd) {
        return transactionRepository.getAllTransactionsInAMonth(millisecondsStart, millisecondsEnd);
    }

    public LiveData<List<Transaction>> getAllTransactionsInAMonthView(long millisecondsStart, long millisecondsEnd) {
        return transactionRepository.getAllTransactionsInAMonthView(millisecondsStart, millisecondsEnd);
    }

    public LiveData<List<Transaction>> searchAllTransactions(String searchName) {
        return transactionRepository.searchAllTransactions(searchName);
    }

    public void insertTransaction(Transaction transaction) {
        transactionRepository.insertTransaction(transaction);
    }

    public void updateTransaction(Transaction transaction) {
        transactionRepository.updateTransaction(transaction);
    }

    public void deleteTransaction(Transaction transaction) {
        transactionRepository.deleteTransaction(transaction);
    }

    public void deleteFutureRecurringTransactions(String transactionRecurringId, long milliseconds) {
        transactionRepository.deleteFutureRecurringTransactions(transactionRecurringId, milliseconds);
    }

    public Future<Integer> checkpoint(SupportSQLiteQuery supportSQLiteQuery) {
        return transactionRepository.checkpoint(supportSQLiteQuery);
    }

    public Future<List<String>> getAllTransactionNameString() {
        return transactionRepository.getAllTransactionNameString();
    }

    public Future<Double> calculateExpensesInAMonth(long epochSecondsStart, long epochSecondsEnd){
        return transactionRepository.calculateExpensesInAMonth(epochSecondsStart, epochSecondsEnd);
    }
}
