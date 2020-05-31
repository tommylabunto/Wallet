package com.example.wallet.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.Date;
import java.util.List;

@Dao
public interface TransactionDao {

    @Insert
    public void insertTransaction(Transaction transaction);

    @Update
    public void updateTransaction(Transaction transaction);

    @Delete
    public void deleteTransaction(Transaction transaction);

//    @Query("SELECT dateTime, value, name, typeName FROM `transaction` WHERE name = :selectedName")
//    public String getTransaction(int selectedName);

    @Query("SELECT transactionId, date, value, name, typeName, isRepeat, frequency, numOfRepeat FROM `transaction` WHERE isRepeat = 1")
    public LiveData<List<Transaction>> getAllRecurringTransactions();

    @Query("SELECT transactionId, date, value, name, typeName, isRepeat, frequency, numOfRepeat FROM `transaction` WHERE isRepeat = 0")
    public LiveData<List<Transaction>> getAllNonRecurringTransactions();

    // TODO: check if it works
    @Query("SELECT transactionId, date, value, name, typeName, isRepeat, frequency, numOfRepeat FROM `transaction` WHERE date < :date")
    public LiveData<List<Transaction>> getAllRecurringFutureTransactions(Date date);

    @Query("SELECT transactionId, date, value, name, typeName, isRepeat, frequency, numOfRepeat FROM `transaction`")
    public LiveData<List<Transaction>> getAllTransactions();

    @Query("DELETE FROM `transaction`")
    public void deleteAllTransactions();
}
