package com.xingtingkai.wallet.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.xingtingkai.wallet.db.entity.Transaction;

import java.util.List;

@Dao
public interface TransactionDao {

    @Insert
    public void insertTransaction(Transaction transaction);

    @Update
    public void updateTransaction(Transaction transaction);

    @Delete
    public void deleteTransaction(Transaction transaction);

    @Query("SELECT transactionId, transactionRecurringId, instant, zoneId, value, name, typeName, repeat, frequency, numOfRepeat, expenseTransaction FROM `transaction` WHERE instant >= :epochSecondsStart AND instant <= :epochSecondsEnd ORDER BY instant DESC")
    public LiveData<List<Transaction>> getAllTransactionsInAMonth(long epochSecondsStart, long epochSecondsEnd);

    @Query("SELECT transactionId, transactionRecurringId, instant, zoneId, sum(value) value, name, typeName, repeat, frequency, numOfRepeat, expenseTransaction FROM `transaction` WHERE instant >= :epochSecondsStart AND instant <= :epochSecondsEnd GROUP BY typeName ORDER BY instant ASC")
    public LiveData<List<Transaction>> getAllTransactionsInAMonthView(long epochSecondsStart, long epochSecondsEnd);

    @Query("SELECT sum(value) value FROM `transaction` WHERE expenseTransaction = 1 AND instant >= :epochSecondsStart AND instant <= :epochSecondsEnd")
    public double calculateExpensesInAMonth(long epochSecondsStart, long epochSecondsEnd);

    /*
    cannot use distinct (all are distinct), group by (don't know which row sqlite selects)
    so list is sorted by recurring id when RepeatTransactionActivity receives it
     */

    @Query("SELECT transactionId, transactionRecurringId, instant, zoneId, value, name, typeName, repeat, frequency, numOfRepeat, expenseTransaction FROM `transaction` WHERE repeat = 1 AND instant >= :epochSecondsToday AND expenseTransaction = 1 ORDER BY transactionRecurringId ASC, instant ASC")
    public LiveData<List<Transaction>> getExpenseRecurringTransactions(long epochSecondsToday);

    @Query("SELECT transactionId, transactionRecurringId, instant, zoneId, value, name, typeName, repeat, frequency, numOfRepeat, expenseTransaction FROM `transaction` WHERE repeat = 1 AND instant >= :epochSecondsToday AND expenseTransaction = 0 ORDER BY transactionRecurringId ASC, instant ASC")
    public LiveData<List<Transaction>> getIncomeRecurringTransactions(long epochSecondsToday);


    @Query("SELECT transactionId, transactionRecurringId, instant, zoneId, value, name, typeName, repeat, frequency, numOfRepeat, expenseTransaction FROM `transaction` WHERE name LIKE :searchName ORDER BY instant ASC")
    public LiveData<List<Transaction>> searchAllTransactions(String searchName);

    @Query("DELETE FROM `transaction`")
    public void deleteAllTransactions();

    // instant is stored as long in sqlite (seen in converters)
    @Query("DELETE FROM `transaction` WHERE transactionRecurringId = :transactionRecurringId AND instant >= :epochSeconds")
    public void deleteFutureRecurringTransactions(String transactionRecurringId, long epochSeconds);

    // sync DB data
    @RawQuery(observedEntities = Transaction.class)
    public Integer checkpoint(SupportSQLiteQuery supportSQLiteQuery);

    @Query("SELECT DISTINCT name FROM `transaction` ORDER BY name ASC")
    public List<String> getAllTransactionNameString();
}
