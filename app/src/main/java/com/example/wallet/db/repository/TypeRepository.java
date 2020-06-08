package com.example.wallet.db.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.wallet.db.WalletDatabase;
import com.example.wallet.db.dao.TypeDao;
import com.example.wallet.db.entity.Type;

import java.util.List;

public class TypeRepository {

    private TypeDao typeDao;
    private LiveData<List<String>> allTypesString;
    private LiveData<List<Type>> allTypes;

    public TypeRepository(Application application) {
        WalletDatabase db = WalletDatabase.getDatabase(application);
        typeDao = db.getTypeDao();
        allTypesString = typeDao.getAllTypesString();
        allTypes = typeDao.getAllTypes();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    public LiveData<List<String>> getAllTypesString() {
        return allTypesString;
    }

    public LiveData<List<Type>> getAllTypes() {
        return allTypes;
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    public void insertType(Type type) {
        WalletDatabase.databaseWriteExecutor.execute(() -> {
            typeDao.insertType(type);
        });
    }
    public void deleteType(Type type) {
        WalletDatabase.databaseWriteExecutor.execute(() -> {
            typeDao.deleteType(type);
        });
    }

    public void updateType(Type type) {
        WalletDatabase.databaseWriteExecutor.execute(() -> {
            typeDao.updateType(type);
        });
    }
}
