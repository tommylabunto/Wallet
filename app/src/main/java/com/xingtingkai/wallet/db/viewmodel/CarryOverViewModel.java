package com.xingtingkai.wallet.db.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.xingtingkai.wallet.db.entity.CarryOver;
import com.xingtingkai.wallet.db.repository.CarryOverRepository;

import java.util.List;

public class CarryOverViewModel extends AndroidViewModel {

    private CarryOverRepository carryOverRepository;
    private LiveData<List<CarryOver>> allCarryOver;

    public CarryOverViewModel(Application application) {
        super(application);
        carryOverRepository = new CarryOverRepository(application);
        allCarryOver = carryOverRepository.getAllCarryOver();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    public LiveData<List<CarryOver>> getAllCarryOver() {
        return allCarryOver;
    }

    public List<CarryOver> getAllCarryOverList() {
        return carryOverRepository.getAllCarryOverList();
    }

    public CarryOver getCarryOver() {
        return carryOverRepository.getCarryOver();
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    public void insertCarryOver(CarryOver carryOver) {
        carryOverRepository.insertCarryOver(carryOver);
    }
    public void deleteCarryOver(CarryOver carryOver) {
        carryOverRepository.deleteCarryOver(carryOver);
    }

    public void updateCarryOver(CarryOver carryOver) {
        carryOverRepository.updateCarryOver(carryOver);
    }
}
