package com.xingtingkai.wallet.db.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.xingtingkai.wallet.db.WalletDatabase;
import com.xingtingkai.wallet.db.dao.CarryOverDao;
import com.xingtingkai.wallet.db.entity.CarryOver;

import java.util.List;

public class CarryOverRepository {

    private CarryOverDao carryOverDao;
    private LiveData<List<CarryOver>> allCarryOver;

    public CarryOverRepository(Application application) {
        WalletDatabase db = WalletDatabase.getDatabase(application);
        carryOverDao = db.getCarryOverDao();
        allCarryOver = carryOverDao.getAllCarryOver();
    }

    /*
     Room executes all queries on a separate thread.
     Observed LiveData will notify the observer when the data has changed.
     */
    public LiveData<List<CarryOver>> getAllCarryOver() {
        return allCarryOver;
    }

    public List<CarryOver> getAllCarryOverList() {
        return carryOverDao.getAllCarryOverList();
    }

    public CarryOver getCarryOver() {
        return carryOverDao.getCarryOver();
    }

    /*
     You must call this on a non-UI thread or your app will throw an exception. Room ensures
     that you're not doing any long running operations on the main thread, blocking the UI.
    */
    public void insertCarryOver(CarryOver carryOver) {
        WalletDatabase.databaseWriteExecutor.execute(() ->
                carryOverDao.insertCarryOver(carryOver));
    }

    public void deleteCarryOver(CarryOver carryOver) {
        WalletDatabase.databaseWriteExecutor.execute(() ->
                carryOverDao.deleteCarryOver(carryOver));
    }

    public void updateCarryOver(CarryOver carryOver) {
        WalletDatabase.databaseWriteExecutor.execute(() ->
                carryOverDao.updateCarryOver(carryOver));
    }
}
