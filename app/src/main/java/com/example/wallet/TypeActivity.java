package com.example.wallet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wallet.adapter.TypeAdapter;
import com.example.wallet.db.Type;
import com.example.wallet.db.TypeViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class TypeActivity extends AppCompatActivity {

    public static final int ADD_TYPE_ACTIVITY_REQUEST_CODE = 1;
    public static final int EDIT_TYPE_ACTIVITY_REQUEST_CODE = 2;

    private TypeViewModel typeViewModel;

    private TypeAdapter typeAdapter;

    private CoordinatorLayout coordinatorLayout;

    private static int numOfExpenseTypes;
    private static int numOfIncomeTypes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type);

        // create transaction
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToAddEditTypeActivity = new Intent(TypeActivity.this, AddEditTypeActivity.class);
                startActivityForResult(goToAddEditTypeActivity, ADD_TYPE_ACTIVITY_REQUEST_CODE);
            }
        });

        // show transaction in recycler view
        RecyclerView recyclerView = findViewById(R.id.recyclerview_type);
        typeAdapter = new TypeAdapter(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(typeAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        initViewModels();

        coordinatorLayout = findViewById(R.id.typeActivity);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initViewModels() {

        typeViewModel = ViewModelProviders.of(this).get(TypeViewModel.class);

        typeViewModel.getAllTypes().observe(this, new Observer<List<Type>>() {
            @Override
            public void onChanged(@Nullable final List<Type> types) {
                typeAdapter.submitList(types);
                updateTypeCount(types);
                Log.d("numOfExpenseTypes", numOfExpenseTypes + "");
                Log.d("numOfIncomeTypes", numOfIncomeTypes + "");
            }
        });

        // when click on item in recycler view -> populate data and open up to edit
        typeAdapter.setOnItemClickListener(new TypeAdapter.OnItemClickListener() {
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

    private void updateTypeCount(List<Type> types) {

        // refresh values because onchanged will keep adding it
        numOfExpenseTypes = 0;
        numOfIncomeTypes = 0;

        for (Type type : types) {
            if (type.isExpenseType()) {
                numOfExpenseTypes++;
            } else {
                numOfIncomeTypes++;
            }
        }
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
                    Toast.makeText(this, "Type saved", Toast.LENGTH_LONG).show();
                }
            } else {

                Long id = data.getLongExtra(AddEditTypeActivity.EXTRA_ID, -1);
                if (id == -1) {
                    Toast.makeText(this, "Type can't be updated", Toast.LENGTH_SHORT).show();
                }

                Type type = extractDataToType(data, id);

                // update type
                // must have at least one of each type
                if (data.getStringExtra(AddEditTypeActivity.EXTRA_OPERATION).equals("save")) {
                    if (numOfExpenseTypes > 1 && numOfIncomeTypes > 1) {
                        typeViewModel.updateType(type);
                        Toast.makeText(this, "Type updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "number of income or expense types must be greater than 1", Toast.LENGTH_SHORT).show();
                    }

                    // delete type
                } else {
                    // must have at least one type to create a transaction
                    if (type.isExpenseType()) {
                        if (numOfExpenseTypes > 1) {
                            typeViewModel.deleteType(type);
                            showSnackbar(type);
                        } else {
                            Toast.makeText(this, "number of expense types must be greater than 1", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (numOfIncomeTypes > 1) {
                            typeViewModel.deleteType(type);
                            showSnackbar(type);
                        } else {
                            Toast.makeText(this, "number of income types must be greater than 1", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
    }

    private void showSnackbar(Type type) {

        Snackbar snackbar = Snackbar.make(coordinatorLayout, "Type deleted", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // insert back
                        typeViewModel.insertType(type);

                        Snackbar snackbar1 = Snackbar.make(coordinatorLayout, "Undo successful", Snackbar.LENGTH_SHORT);
                        snackbar1.show();
                    }
                });

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