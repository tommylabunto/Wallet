package com.example.wallet.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

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

    @Query("SELECT name FROM type")
    public LiveData<List<String>>  getAllTypes();
}
