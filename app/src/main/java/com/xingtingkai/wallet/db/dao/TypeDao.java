package com.xingtingkai.wallet.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.xingtingkai.wallet.db.entity.Type;

import java.util.List;

@Dao
public interface TypeDao {

    @Insert
    public void insertType(Type type);

    @Update
    public void updateType(Type type);

    @Delete
    public void deleteType(Type type);

//    @Query("SELECT name FROM type WHERE name = :selectedType")
//    public LiveData<String> getType(String selectedType);

    @Query("SELECT name FROM type ORDER BY typeId ASC")
    public LiveData<List<String>> getAllTypesString();

    @Query("SELECT name FROM type ORDER BY typeId ASC")
    public List<String> getAllTypesStringTemp();

    @Query("SELECT typeId, name, expenseType FROM type ORDER BY typeId ASC")
    public LiveData<List<Type>> getAllTypes();

    @Query("SELECT typeId, name, expenseType FROM type WHERE expenseType = 1 ORDER BY typeId ASC")
    public LiveData<List<Type>> getAllExpenseTypes();

    @Query("SELECT name FROM type WHERE expenseType = 1 ORDER BY typeId ASC")
    public List<String> getAllExpenseTypesString();

    @Query("SELECT typeId, name, expenseType FROM type WHERE expenseType = 0 ORDER BY typeId ASC")
    public LiveData<List<Type>> getAllIncomeTypes();

    @Query("SELECT name FROM type WHERE expenseType = 0 ORDER BY typeId ASC")
    public List<String> getAllIncomeTypesString();
}
