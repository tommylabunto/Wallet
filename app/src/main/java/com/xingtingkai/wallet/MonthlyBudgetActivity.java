package com.xingtingkai.wallet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.xingtingkai.wallet.adapter.MonthlyBudgetAdapter;
import com.xingtingkai.wallet.db.entity.MonthlyBudget;
import com.xingtingkai.wallet.db.viewmodel.MonthlyBudgetViewModel;

import java.time.ZonedDateTime;
import java.util.List;

public class MonthlyBudgetActivity extends AppCompatActivity {

    protected static final int ADD_MONTHLY_BUDGET_ACTIVITY_REQUEST_CODE = 1;
    protected static final int EDIT_MONTHLY_BUDGET_ACTIVITY_REQUEST_CODE = 2;

    private MonthlyBudgetViewModel monthlyBudgetViewModel;

    private MonthlyBudgetAdapter monthlyBudgetAdapter;

    private CoordinatorLayout coordinatorLayout;

    private TextView textViewYear;

    private static ZonedDateTime zonedMonthDate = ZonedDateTime.now();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_budget);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // show transaction in recycler view
        RecyclerView recyclerView = findViewById(R.id.recyclerview_monthly_budget);
        monthlyBudgetAdapter = new MonthlyBudgetAdapter(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(monthlyBudgetAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        textViewYear = findViewById(R.id.textView_year);

        initViewModels();

        coordinatorLayout = findViewById(R.id.monthlyBudgetActivity);

        ImageButton prevYear = findViewById(R.id.left_button);
        // on item click
        prevYear.setOnClickListener((View view) -> {
            zonedMonthDate = zonedMonthDate.minusYears(1);
            updateMonthlyBudgetsInAYear();
        });

        ImageButton nextYear = findViewById(R.id.right_button);
        // on item click
        nextYear.setOnClickListener((View view) -> {
            zonedMonthDate = zonedMonthDate.plusYears(1);
            updateMonthlyBudgetsInAYear();
        });
    }

    private void updateMonthlyBudgetsInAYear() {

        int yearInt = zonedMonthDate.getYear();

        // on changed
        monthlyBudgetViewModel.getAllMonthlyBudgetsInAYear(yearInt).observe(this,
                (@Nullable final List<MonthlyBudget> monthlyBudgets) ->
                monthlyBudgetAdapter.submitList(monthlyBudgets));

        String year = getString(R.string.single_string_param, yearInt + "");
        textViewYear.setText(year);
    }

    private void initViewModels() {

        ZonedDateTime tempZonedMonthDate = ZonedDateTime.now();
        int yearInt = tempZonedMonthDate.getYear();

        String year = getString(R.string.single_string_param, yearInt + "");
        textViewYear.setText(year);

        monthlyBudgetViewModel = new ViewModelProvider(this).get(MonthlyBudgetViewModel.class);

        // on changed
        monthlyBudgetViewModel.getAllMonthlyBudgetsInAYear(yearInt).observe(this,
                (@Nullable final List<MonthlyBudget> monthlyBudgets) ->
                        monthlyBudgetAdapter.submitList(monthlyBudgets));

        /*
         when click on item in recycler view -> populate data and open up to edit
         on item click
        */

        monthlyBudgetAdapter.setOnItemClickListener((MonthlyBudget monthlyBudget) -> {
            Intent goToAddEditMonthlyBudget = new Intent(MonthlyBudgetActivity.this, AddEditMonthlyBudgetActivity.class);
            goToAddEditMonthlyBudget.putExtra(AddEditMonthlyBudgetActivity.EXTRA_ID, monthlyBudget.getMonthlyBudgetId());
            goToAddEditMonthlyBudget.putExtra(AddEditMonthlyBudgetActivity.EXTRA_BUDGET, monthlyBudget.getBudget());
            goToAddEditMonthlyBudget.putExtra(AddEditMonthlyBudgetActivity.EXTRA_YEAR, monthlyBudget.getYear());
            goToAddEditMonthlyBudget.putExtra(AddEditMonthlyBudgetActivity.EXTRA_MONTH, monthlyBudget.getMonth());
            startActivityForResult(goToAddEditMonthlyBudget, EDIT_MONTHLY_BUDGET_ACTIVITY_REQUEST_CODE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == ADD_MONTHLY_BUDGET_ACTIVITY_REQUEST_CODE) {

                String operation = data.getStringExtra(AddEditMonthlyBudgetActivity.EXTRA_OPERATION);

                // create Monthly Budget
                if (operation != null && operation.equals("save")) {

                    MonthlyBudget monthlyBudget = extractDataToMonthlyBudget(data, 0L);
                    monthlyBudgetViewModel.insertMonthlyBudget(monthlyBudget);
                }
            } else {

                long id = data.getLongExtra(AddEditMonthlyBudgetActivity.EXTRA_ID, -1);
                if (id == -1) {
                    showSnackbarMessage("budget cannot be updated");
                }

                MonthlyBudget monthlyBudget = extractDataToMonthlyBudget(data, id);

                String operation = data.getStringExtra(AddEditMonthlyBudgetActivity.EXTRA_OPERATION);

                if (operation != null) {
                    // update Monthly Budget
                    switch (operation) {
                        case "save next":
                            monthlyBudgetViewModel.updateAllFutureMonthlyBudgets(monthlyBudget.getMonthlyBudgetId(), monthlyBudget.getBudget());
                            break;
                        case "save all":
                            monthlyBudgetViewModel.updateAllMonthlyBudgets(monthlyBudget.getBudget());
                            break;
                        default:
                            monthlyBudgetViewModel.updateMonthlyBudget(monthlyBudget);
                            // monthlyBudgetViewModel.deleteMonthlyBudget(monthlyBudget);
                            //showSnackbar(monthlyBudget);
                            break;
                    }
                }
            }
        }
    }

    private void showSnackbarMessage(String message) {

        Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG);

        snackbar.show();
    }

    private MonthlyBudget extractDataToMonthlyBudget(Intent data, long id) {

        int budget = data.getIntExtra(AddEditMonthlyBudgetActivity.EXTRA_BUDGET, 0);
        int year = data.getIntExtra(AddEditMonthlyBudgetActivity.EXTRA_YEAR, 0);
        int month = data.getIntExtra(AddEditMonthlyBudgetActivity.EXTRA_MONTH, 1);

        return MonthlyBudget.create(id, budget, year, month);
    }

//    private void showSnackbar(MonthlyBudget monthlyBudget) {
//
//        Snackbar snackbar = Snackbar.make(coordinatorLayout, "Monthly Budget deleted", Snackbar.LENGTH_LONG)
//                // on click
//                .setAction("UNDO", (View v) -> {
//                    // insert back
//                    monthlyBudgetViewModel.insertMonthlyBudget(monthlyBudget);
//
//                    Snackbar snackbar1 = Snackbar.make(coordinatorLayout, "Undo successful", Snackbar.LENGTH_SHORT);
//                    snackbar1.show();
//                });
//
//        snackbar.show();
//    }
}