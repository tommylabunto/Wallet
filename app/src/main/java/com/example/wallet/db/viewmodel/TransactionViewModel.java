package com.example.wallet.db.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.wallet.db.entity.Transaction;
import com.example.wallet.db.repository.TransactionRepository;

import java.util.List;

public class TransactionViewModel extends AndroidViewModel {

    private TransactionRepository transactionRepository;
    // Using LiveData and caching what getAlphabetizedWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    private LiveData<List<Transaction>> allTransactions;
    private LiveData<List<Transaction>> allNonRecurringTransactions;

    public TransactionViewModel(Application application) {
        super(application);
        transactionRepository = new TransactionRepository(application);
        allTransactions = transactionRepository.getAllTransactions();
        allNonRecurringTransactions = transactionRepository.getAllNonRecurringTransactions();
    }

    public LiveData<List<Transaction>> getAllTransactions() {
        return allTransactions;
    }

    public LiveData<List<Transaction>> getAllRecurringTransactions(Long millisecondsToday) {
        return transactionRepository.getAllRecurringTransactions(millisecondsToday);
    }

    public LiveData<List<Transaction>> getExpenseRecurringTransactions(Long millisecondsToday) {
        return transactionRepository.getExpenseRecurringTransactions(millisecondsToday);
    }

    public LiveData<List<Transaction>> getIncomeRecurringTransactions(Long millisecondsToday) {
        return transactionRepository.getIncomeRecurringTransactions(millisecondsToday);
    }

    public LiveData<List<Transaction>> getAllNonRecurringTransactions() {
        return allNonRecurringTransactions;
    }

    public LiveData<List<Transaction>> getAllTransactionsInAMonth(Long millisecondsStart, Long millisecondsEnd) {
        return transactionRepository.getAllTransactionsInAMonth(millisecondsStart, millisecondsEnd);
    }

    public LiveData<List<Transaction>> getAllTransactionsInAMonthView(Long millisecondsStart, Long millisecondsEnd) {
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

    public LiveData<Transaction> getTransaction(Long transactionId) {
        return transactionRepository.getTransaction(transactionId);
    }

    public void deleteAllRecurringTransactions(double value, String name, String typeName, int frequency) {
        transactionRepository.deleteAllRecurringTransactions(value, name, typeName, frequency);
    }

    public void deleteFutureRecurringTransactions(String transactionRecurringId, Long milliseconds) {
        transactionRepository.deleteFutureRecurringTransactions(transactionRecurringId, milliseconds);
    }

    public LiveData<Integer> checkpoint(SupportSQLiteQuery supportSQLiteQuery) {
        return transactionRepository.checkpoint(supportSQLiteQuery);
    }

    public LiveData<List<String>> getAllTransactionNameString() {
        return transactionRepository.getAllTransactionNameString();
    }
}
