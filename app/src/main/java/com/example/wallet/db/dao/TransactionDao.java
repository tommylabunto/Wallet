package com.example.wallet.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.wallet.db.entity.Transaction;

import java.util.List;

@Dao
public interface TransactionDao {

    @Insert
    public void insertTransaction(Transaction transaction);

    @Update
    public void updateTransaction(Transaction transaction);

    @Delete
    public void deleteTransaction(Transaction transaction);

    @Query("SELECT transactionId, transactionRecurringId, date, value, name, typeName, isRepeat, frequency, numOfRepeat, isExpenseTransaction FROM `transaction` WHERE transactionId = :transactionId ORDER BY date ASC")
    public LiveData<Transaction> getTransaction(Long transactionId);

    @Query("SELECT transactionId, transactionRecurringId, date, value, name, typeName, isRepeat, frequency, numOfRepeat, isExpenseTransaction FROM `transaction` WHERE date >= :millisecondsStart AND date <= :millisecondsEnd ORDER BY date DESC")
    public LiveData<List<Transaction>> getAllTransactionsInAMonth(Long millisecondsStart, Long millisecondsEnd);

    @Query("SELECT transactionId, transactionRecurringId, date, sum(value) value, name, typeName, isRepeat, frequency, numOfRepeat, isExpenseTransaction FROM `transaction` WHERE date >= :millisecondsStart AND date <= :millisecondsEnd GROUP BY typeName ORDER BY date ASC")
    public LiveData<List<Transaction>> getAllTransactionsInAMonthView(Long millisecondsStart, Long millisecondsEnd);

    /*
    cannot use distinct (all are distinct), group by (don't know which row sqlite selects)
    so list is sorted by recurring id when RepeatTransactionActivity receives it
     */
    @Query("SELECT transactionId, transactionRecurringId, date, value, name, typeName, isRepeat, frequency, numOfRepeat, isExpenseTransaction FROM `transaction` WHERE isRepeat = 1 AND date >= :millisecondsToday ORDER BY transactionRecurringId ASC, date ASC")
    public LiveData<List<Transaction>> getAllRecurringTransactions(Long millisecondsToday);

    @Query("SELECT transactionId, transactionRecurringId, date, value, name, typeName, isRepeat, frequency, numOfRepeat, isExpenseTransaction FROM `transaction` WHERE isRepeat = 1 AND date >= :millisecondsToday AND isExpenseTransaction = 1 ORDER BY transactionRecurringId ASC, date ASC")
    public LiveData<List<Transaction>> getExpenseRecurringTransactions(Long millisecondsToday);

    @Query("SELECT transactionId, transactionRecurringId, date, value, name, typeName, isRepeat, frequency, numOfRepeat, isExpenseTransaction FROM `transaction` WHERE isRepeat = 1 AND date >= :millisecondsToday AND isExpenseTransaction = 0 ORDER BY transactionRecurringId ASC, date ASC")
    public LiveData<List<Transaction>> getIncomeRecurringTransactions(Long millisecondsToday);

    @Query("SELECT transactionId, transactionRecurringId, date, value, name, typeName, isRepeat, frequency, numOfRepeat, isExpenseTransaction FROM `transaction` WHERE isRepeat = 0 ORDER BY date ASC")
    public LiveData<List<Transaction>> getAllNonRecurringTransactions();

    @Query("SELECT transactionId, transactionRecurringId, date, value, name, typeName, isRepeat, frequency, numOfRepeat, isExpenseTransaction FROM `transaction` ORDER BY date ASC")
    public LiveData<List<Transaction>> getAllTransactions();

    @Query("SELECT transactionId, transactionRecurringId, date, value, name, typeName, isRepeat, frequency, numOfRepeat, isExpenseTransaction FROM `transaction` WHERE name LIKE :searchName ORDER BY date ASC")
    public LiveData<List<Transaction>> searchAllTransactions(String searchName);

    @Query("DELETE FROM `transaction`")
    public void deleteAllTransactions();

    @Query("DELETE FROM `transaction` WHERE value = :value AND name = :name AND typeName = :typeName AND frequency = :frequency")
    public void deleteAllRecurringTransactions(double value, String name, String typeName, int frequency);

    // date is stored as Long in sqlite (seen in converters)
    @Query("DELETE FROM `transaction` WHERE transactionRecurringId = :transactionRecurringId AND date >= :milliseconds")
    public void deleteFutureRecurringTransactions(String transactionRecurringId, Long milliseconds);

    // sync DB data
    @RawQuery(observedEntities = Transaction.class)
    public LiveData<Integer> checkpoint(SupportSQLiteQuery supportSQLiteQuery);

    @Query("SELECT DISTINCT name FROM `transaction` ORDER BY name ASC")
    public LiveData<List<String>> getAllTransactionNameString();
}
