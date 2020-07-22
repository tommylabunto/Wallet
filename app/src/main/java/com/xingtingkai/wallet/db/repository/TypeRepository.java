package com.xingtingkai.wallet.db.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.xingtingkai.wallet.db.WalletDatabase;
import com.xingtingkai.wallet.db.dao.TypeDao;
import com.xingtingkai.wallet.db.entity.Type;

import java.util.List;
import java.util.concurrent.Future;

public class TypeRepository {

    private TypeDao typeDao;
    private LiveData<List<Type>> allExpenseTypes;
    private LiveData<List<Type>> allIncomeTypes;

    public TypeRepository(Application application) {
        WalletDatabase db = WalletDatabase.getDatabase(application);
        typeDao = db.getTypeDao();
        allExpenseTypes = typeDao.getAllExpenseTypes();
        allIncomeTypes = typeDao.getAllIncomeTypes();
    }

    /*
    Room executes all queries on a separate thread.
    Observed LiveData will notify the observer when the data has changed.
     */

    public Future<List<String>> getAllTypesStringFuture() {
        return WalletDatabase.databaseWriteExecutor
                .submit(typeDao::getAllTypesString);
    }

    public LiveData<List<Type>> getAllExpenseTypes() {
        return allExpenseTypes;
    }

    public LiveData<List<Type>> getAllIncomeTypes() {
        return allIncomeTypes;
    }

    public Future<List<String>> getAllExpenseTypesString() {
        return WalletDatabase.databaseWriteExecutor
                .submit(typeDao::getAllExpenseTypesString);
    }

    public Future<List<String>> getAllIncomeTypesString() {
        return WalletDatabase.databaseWriteExecutor
                .submit(typeDao::getAllIncomeTypesString);
    }

    /*
     You must call this on a non-UI thread or your app will throw an exception. Room ensures
     that you're not doing any long running operations on the main thread, blocking the UI.
    */
    public void insertType(Type type) {
        WalletDatabase.databaseWriteExecutor.execute(() ->
                typeDao.insertType(type));
    }

    public void deleteType(Type type) {
        WalletDatabase.databaseWriteExecutor.execute(() ->
                typeDao.deleteType(type));
    }

    public void updateType(Type type) {
        WalletDatabase.databaseWriteExecutor.execute(() ->
                typeDao.updateType(type));
    }
}
