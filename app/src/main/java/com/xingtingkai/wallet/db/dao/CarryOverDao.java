package com.xingtingkai.wallet.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.xingtingkai.wallet.db.entity.CarryOver;

import java.util.List;

@Dao
public interface CarryOverDao {

    @Insert
    public void insertCarryOver(CarryOver carryOver);

    @Update
    public void updateCarryOver(CarryOver carryOver);

    @Delete
    public void deleteCarryOver(CarryOver carryOver);

    @Query("SELECT carryOverId, carryOver FROM carryOver ORDER BY carryOverId ASC")
    public LiveData<List<CarryOver>> getAllCarryOver();

    @Query("SELECT carryOverId, carryOver FROM carryOver ORDER BY carryOverId ASC")
    public List<CarryOver> getAllCarryOverList();

    @Query("SELECT carryOverId, carryOver FROM carryOver ORDER BY carryOverId ASC LIMIT 1")
    public CarryOver getCarryOver();
}
