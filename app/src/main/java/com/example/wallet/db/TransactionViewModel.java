package com.example.wallet.db;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class TransactionViewModel extends AndroidViewModel {

    private TransactionRepository transactionRepository;
    // Using LiveData and caching what getAlphabetizedWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    private LiveData<List<Transaction>> allTransactions;
    private LiveData<List<Transaction>> allRecurringTransactions;
    private LiveData<List<Transaction>> allNonRecurringTransactions;

    public TransactionViewModel(Application application) {
        super(application);
        transactionRepository = new TransactionRepository(application);
        allTransactions = transactionRepository.getAllTransactions();
        allRecurringTransactions = transactionRepository.getAllRecurringTransactions();
        allNonRecurringTransactions = transactionRepository.getAllNonRecurringTransactions();
    }

    public LiveData<List<Transaction>> getAllTransactions() {
        return allTransactions;
    }

    public LiveData<List<Transaction>> getAllRecurringTransactions() {
        return allRecurringTransactions;
    }

    public LiveData<List<Transaction>> getAllNonRecurringTransactions() {
        return allNonRecurringTransactions;
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
}
