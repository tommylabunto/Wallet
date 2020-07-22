package com.xingtingkai.wallet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.xingtingkai.wallet.adapter.TypeAdapter;
import com.xingtingkai.wallet.db.entity.Type;
import com.xingtingkai.wallet.db.viewmodel.TypeViewModel;

import java.util.List;

public class TypeActivity extends AppCompatActivity {

    protected static final int ADD_TYPE_ACTIVITY_REQUEST_CODE = 1;
    protected static final int EDIT_TYPE_ACTIVITY_REQUEST_CODE = 2;

    private static final int NUMBER_OF_COLUMNS = 2;

    private TypeViewModel typeViewModel;

    private TypeAdapter typeExpenseAdapter;
    private TypeAdapter typeIncomeAdapter;

    private CoordinatorLayout coordinatorLayout;

    private static int numOfExpenseTypes;
    private static int numOfIncomeTypes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type);

        // create type
        FloatingActionButton fab = findViewById(R.id.fab);
        // on click
        fab.setOnClickListener((View view) -> {
            Intent goToAddEditType = new Intent(TypeActivity.this, AddEditTypeActivity.class);
            startActivityForResult(goToAddEditType, ADD_TYPE_ACTIVITY_REQUEST_CODE);
        });

        // show transaction in recycler view
        RecyclerView recyclerViewExpense = findViewById(R.id.recyclerview_expense_type);
        typeExpenseAdapter = new TypeAdapter(this);
        recyclerViewExpense.setHasFixedSize(true);
        recyclerViewExpense.setAdapter(typeExpenseAdapter);
        recyclerViewExpense.setLayoutManager(new GridLayoutManager(this, NUMBER_OF_COLUMNS));

        RecyclerView recyclerViewIncome = findViewById(R.id.recyclerview_income_type);
        typeIncomeAdapter = new TypeAdapter(this);
        recyclerViewIncome.setHasFixedSize(true);
        recyclerViewIncome.setAdapter(typeIncomeAdapter);
        recyclerViewIncome.setLayoutManager(new GridLayoutManager(this, NUMBER_OF_COLUMNS));

        initViewModels();

        coordinatorLayout = findViewById(R.id.typeActivity);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initViewModels() {

        typeViewModel = new ViewModelProvider(this).get(TypeViewModel.class);
        // typeViewModel = ViewModelProviders.of(this).get(TypeViewModel.class);

        // on changed
        typeViewModel.getAllExpenseTypes().observe(this,
                (@Nullable final List<Type> types) -> {
            typeExpenseAdapter.submitList(types);
            updateExpenseTypeCount(types);
        });

        /*
         when click on item in recycler view -> populate data and open up to edit
         on item click
        */
        typeExpenseAdapter.setOnItemClickListener((Type type) -> {
            Intent goToAddEditType = new Intent(TypeActivity.this, AddEditTypeActivity.class);
            goToAddEditType.putExtra(AddEditTypeActivity.EXTRA_ID, type.getTypeId());
            goToAddEditType.putExtra(AddEditTypeActivity.EXTRA_NAME, type.getName());
            goToAddEditType.putExtra(AddEditTypeActivity.EXTRA_IS_EXPENSE_TYPE, type.isExpenseType());
            startActivityForResult(goToAddEditType, EDIT_TYPE_ACTIVITY_REQUEST_CODE);
        });

        // on changed
        typeViewModel.getAllIncomeTypes().observe(this, (@Nullable final List<Type> types) -> {
            typeIncomeAdapter.submitList(types);
            updateIncomeTypeCount(types);
        });

        /*
         when click on item in recycler view -> populate data and open up to edit
         on item click
        */
        typeIncomeAdapter.setOnItemClickListener((Type type) -> {
            Intent goToAddEditType = new Intent(TypeActivity.this, AddEditTypeActivity.class);
            goToAddEditType.putExtra(AddEditTypeActivity.EXTRA_ID, type.getTypeId());
            goToAddEditType.putExtra(AddEditTypeActivity.EXTRA_NAME, type.getName());
            goToAddEditType.putExtra(AddEditTypeActivity.EXTRA_IS_EXPENSE_TYPE, type.isExpenseType());
            startActivityForResult(goToAddEditType, EDIT_TYPE_ACTIVITY_REQUEST_CODE);
        });
    }

    private void updateExpenseTypeCount(@Nullable List<Type> expenseTypes) {

        numOfExpenseTypes = 0;

        if (expenseTypes != null) {
            // refresh values because onchanged will keep adding it
            numOfExpenseTypes = expenseTypes.size();
        }
    }

    private void updateIncomeTypeCount(@Nullable List<Type> incomeTypes) {

        numOfIncomeTypes = 0;

        if (incomeTypes != null) {
            // refresh values because onchanged will keep adding it
            numOfIncomeTypes = incomeTypes.size();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == ADD_TYPE_ACTIVITY_REQUEST_CODE) {

                String operation = data.getStringExtra(AddEditTypeActivity.EXTRA_OPERATION);

                // create type
                if (operation != null && operation.equals("save")) {

                    Type type = extractDataToType(data, 0L);
                    typeViewModel.insertType(type);
                }
            } else {

                long id = data.getLongExtra(AddEditTypeActivity.EXTRA_ID, -1);
                if (id == -1) {
                    showSnackbar("category cannot be updated");
                }

                Type type = extractDataToType(data, id);

                String operation = data.getStringExtra(AddEditTypeActivity.EXTRA_OPERATION);

                if (operation != null) {
                    // update type
                    if (operation.equals("save")) {

                        boolean originalIsExpenseType = data.getBooleanExtra(AddEditTypeActivity.EXTRA_ORIGINAL_IS_EXPENSE_TYPE, true);
                        /*
                         must have at least one type to create a transaction
                         switch types (is the problem)
                        */
                        if (originalIsExpenseType != type.isExpenseType()) {
                            if (originalIsExpenseType) {
                                if (numOfExpenseTypes > 1) {
                                    typeViewModel.updateType(type);
                                } else {
                                    showSnackbar("at least 1 expense category is required");
                                }
                            } else {
                                if (numOfIncomeTypes > 1) {
                                    typeViewModel.updateType(type);
                                } else {
                                    showSnackbar("at least 1 income category is required");
                                }
                            }
                            // stay on same type (not a problem)
                        } else {
                            typeViewModel.updateType(type);
                        }

                        // delete type
                    } else {
                        if (type.isExpenseType()) {
                            if (numOfExpenseTypes > 1) {
                                typeViewModel.deleteType(type);
                            } else {
                                showSnackbar("at least 1 expense category is required");
                            }
                        } else {
                            if (numOfIncomeTypes > 1) {
                                typeViewModel.deleteType(type);
                            } else {
                                showSnackbar("at least 1 income category is required");
                            }
                        }
                    }
                }
            }
        }
    }

    private void showSnackbar(String message) {

        Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private Type extractDataToType(Intent data, long id) {

        String name = data.getStringExtra(AddEditTypeActivity.EXTRA_NAME);
        boolean isExpenseType = data.getBooleanExtra(AddEditTypeActivity.EXTRA_IS_EXPENSE_TYPE, true);

        // if type is new -> id is 0, but sqlite auto generates an id at insertion
        return Type.create(id, name, isExpenseType);
    }
}