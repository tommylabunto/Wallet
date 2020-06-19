package com.xingtingkai.wallet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xingtingkai.wallet.adapter.TypeAdapter;
import com.xingtingkai.wallet.db.entity.Type;
import com.xingtingkai.wallet.db.viewmodel.TypeViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class TypeActivity extends AppCompatActivity {

    protected static final int ADD_TYPE_ACTIVITY_REQUEST_CODE = 1;
    protected static final int EDIT_TYPE_ACTIVITY_REQUEST_CODE = 2;

    private static final int numOfColumns = 2;

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
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToAddEditTypeActivity = new Intent(TypeActivity.this, AddEditTypeActivity.class);
                startActivityForResult(goToAddEditTypeActivity, ADD_TYPE_ACTIVITY_REQUEST_CODE);
            }
        });

        // show transaction in recycler view
        RecyclerView recyclerViewExpense = findViewById(R.id.recyclerview_expense_type);
        typeExpenseAdapter = new TypeAdapter(this);
        recyclerViewExpense.setHasFixedSize(true);
        recyclerViewExpense.setAdapter(typeExpenseAdapter);
        recyclerViewExpense.setLayoutManager(new GridLayoutManager(this, numOfColumns));

        RecyclerView recyclerViewIncome = findViewById(R.id.recyclerview_income_type);
        typeIncomeAdapter = new TypeAdapter(this);
        recyclerViewIncome.setHasFixedSize(true);
        recyclerViewIncome.setAdapter(typeIncomeAdapter);
        recyclerViewIncome.setLayoutManager(new GridLayoutManager(this, numOfColumns));

        initViewModels();

        coordinatorLayout = findViewById(R.id.typeActivity);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initViewModels() {

        typeViewModel = ViewModelProviders.of(this).get(TypeViewModel.class);

        typeViewModel.getAllExpenseTypes().observe(this, new Observer<List<Type>>() {
            @Override
            public void onChanged(@Nullable final List<Type> types) {
                typeExpenseAdapter.submitList(types);
                updateExpenseTypeCount(types);
            }
        });

        // when click on item in recycler view -> populate data and open up to edit
        typeExpenseAdapter.setOnItemClickListener(new TypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Type type) {
                Intent goToAddEditTypeActivity = new Intent(TypeActivity.this, AddEditTypeActivity.class);
                goToAddEditTypeActivity.putExtra(AddEditTypeActivity.EXTRA_ID, type.getTypeId());
                goToAddEditTypeActivity.putExtra(AddEditTypeActivity.EXTRA_NAME, type.getName());
                goToAddEditTypeActivity.putExtra(AddEditTypeActivity.EXTRA_IS_EXPENSE_TYPE, type.isExpenseType());
                startActivityForResult(goToAddEditTypeActivity, EDIT_TYPE_ACTIVITY_REQUEST_CODE);
            }
        });

        typeViewModel.getAllIncomeTypes().observe(this, new Observer<List<Type>>() {
            @Override
            public void onChanged(@Nullable final List<Type> types) {
                typeIncomeAdapter.submitList(types);
                updateIncomeTypeCount(types);
            }
        });

        // when click on item in recycler view -> populate data and open up to edit
        typeIncomeAdapter.setOnItemClickListener(new TypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Type type) {
                Intent goToAddEditTypeActivity = new Intent(TypeActivity.this, AddEditTypeActivity.class);
                goToAddEditTypeActivity.putExtra(AddEditTypeActivity.EXTRA_ID, type.getTypeId());
                goToAddEditTypeActivity.putExtra(AddEditTypeActivity.EXTRA_NAME, type.getName());
                goToAddEditTypeActivity.putExtra(AddEditTypeActivity.EXTRA_IS_EXPENSE_TYPE, type.isExpenseType());
                startActivityForResult(goToAddEditTypeActivity, EDIT_TYPE_ACTIVITY_REQUEST_CODE);
            }
        });
    }

    private void updateExpenseTypeCount(List<Type> expenseTypes) {

        // refresh values because onchanged will keep adding it
        numOfExpenseTypes = 0;

        numOfExpenseTypes = expenseTypes.size();
    }

    private void updateIncomeTypeCount(List<Type> incomeTypes) {

        // refresh values because onchanged will keep adding it
        numOfIncomeTypes = 0;

        numOfIncomeTypes = incomeTypes.size();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == ADD_TYPE_ACTIVITY_REQUEST_CODE) {

                // create type
                if (data.getStringExtra(AddEditTypeActivity.EXTRA_OPERATION).equals("save")) {

                    Type type = extractDataToType(data, 0L);
                    typeViewModel.insertType(type);
                }
            } else {

                long id = data.getLongExtra(AddEditTypeActivity.EXTRA_ID, -1);
                if (id == -1) {
                    showSnackbar("category cannot be updated");
                }

                Type type = extractDataToType(data, id);

                // update type
                // must have at least one of each type
                if (data.getStringExtra(AddEditTypeActivity.EXTRA_OPERATION).equals("save")) {

                    boolean originalIsExpenseType = data.getBooleanExtra(AddEditTypeActivity.EXTRA_ORIGINAL_IS_EXPENSE_TYPE, true);

                    // must have at least one type to create a transaction
                    // switch types (is the problem)
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

    private void showSnackbar(String message) {

        Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG);

        snackbar.show();
    }

    private Type extractDataToType(Intent data, Long id) {

        String name = data.getStringExtra(AddEditTypeActivity.EXTRA_NAME);
        boolean isExpenseType = data.getBooleanExtra(AddEditTypeActivity.EXTRA_IS_EXPENSE_TYPE, true);

        Type type = new Type(name, isExpenseType);

        if (id != 0) {
            type.setTypeId(id);
        }
        return type;
    }
}