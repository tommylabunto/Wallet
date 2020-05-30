package com.example.wallet.db;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class TransactionRepository {

    private TransactionDao transactionDao;
    private LiveData<List<Transaction>> allTransactions;

    public TransactionRepository(Application application) {
        WalletDatabase db = WalletDatabase.getDatabase(application);
        transactionDao = db.getTransactionDao();
        allTransactions = transactionDao.getAllTransactions();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    public LiveData<List<Transaction>> getAllTransactions() {
        return allTransactions;
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    public void insertTransaction(Transaction transaction) {
        WalletDatabase.databaseWriteExecutor.execute(() -> {
            transactionDao.insertTransaction(transaction);
        });
    }
    public void deleteTransaction(Transaction transaction) {
        WalletDatabase.databaseWriteExecutor.execute(() -> {
            transactionDao.deleteTransaction(transaction);
        });
    }

    public void updateTransaction(Transaction transaction) {
        WalletDatabase.databaseWriteExecutor.execute(() -> {
            transactionDao.updateTransaction(transaction);
        });
    }
}
