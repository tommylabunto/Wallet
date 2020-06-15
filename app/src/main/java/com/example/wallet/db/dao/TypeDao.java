package com.example.wallet.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.wallet.db.entity.Type;

import java.util.List;

@Dao
public interface TypeDao {

    @Insert
    public void insertType(Type type);

    @Update
    public void updateType(Type type);

    @Delete
    public void deleteType(Type type);

    @Query("SELECT name FROM type WHERE name = :selectedType")
    public LiveData<String> getType(String selectedType);

    @Query("SELECT name FROM type ORDER BY typeId ASC")
    public LiveData<List<String>> getAllTypesString();

    @Query("SELECT typeId, name, isExpenseType FROM type ORDER BY typeId ASC")
    public LiveData<List<Type>> getAllTypes();

    @Query("SELECT typeId, name, isExpenseType FROM type WHERE isExpenseType = 1 ORDER BY typeId ASC")
    public LiveData<List<Type>> getAllExpenseTypes();

    @Query("SELECT typeId, name, isExpenseType FROM type WHERE isExpenseType = 0 ORDER BY typeId ASC")
    public LiveData<List<Type>> getAllIncomeTypes();
}
