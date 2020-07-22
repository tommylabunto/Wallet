package com.xingtingkai.wallet.db.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.xingtingkai.wallet.db.entity.Type;
import com.xingtingkai.wallet.db.repository.TypeRepository;

import java.util.List;
import java.util.concurrent.Future;

public class TypeViewModel extends AndroidViewModel {

    private TypeRepository typeRepository;
    private LiveData<List<Type>> allExpenseTypes;
    private LiveData<List<Type>> allIncomeTypes;

    public TypeViewModel(Application application) {
        super(application);
        typeRepository = new TypeRepository(application);
        allExpenseTypes = typeRepository.getAllExpenseTypes();
        allIncomeTypes = typeRepository.getAllIncomeTypes();
    }

    public Future<List<String>> getAllTypesStringFuture() {
        return typeRepository.getAllTypesStringFuture();
    }

    public LiveData<List<Type>> getAllExpenseTypes() {
        return allExpenseTypes;
    }

    public LiveData<List<Type>> getAllIncomeTypes() {
        return allIncomeTypes;
    }

    public Future<List<String>> getAllExpenseTypesString() {
        return typeRepository.getAllExpenseTypesString();
    }

    public Future<List<String>> getAllIncomeTypesString() {
        return typeRepository.getAllIncomeTypesString();
    }

    public void insertType(Type type) {
        typeRepository.insertType(type);
    }

    public void updateType(Type type) {
        typeRepository.updateType(type);
    }

    public void deleteType(Type type) {
        typeRepository.deleteType(type);
    }
}
