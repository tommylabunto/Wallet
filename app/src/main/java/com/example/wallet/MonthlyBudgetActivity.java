package com.example.wallet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wallet.adapter.MonthlyBudgetAdapter;
import com.example.wallet.db.entity.MonthlyBudget;
import com.example.wallet.db.viewmodel.MonthlyBudgetViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.List;

public class MonthlyBudgetActivity extends AppCompatActivity {

    protected static final int ADD_MONTHLY_BUDGET_ACTIVITY_REQUEST_CODE = 1;
    protected static final int EDIT_MONTHLY_BUDGET_ACTIVITY_REQUEST_CODE = 2;

    private MonthlyBudgetViewModel monthlyBudgetViewModel;

    private MonthlyBudgetAdapter monthlyBudgetAdapter;

    private CoordinatorLayout coordinatorLayout;

    private ImageButton prevYear;
    private ImageButton nextYear;

    private TextView textViewYear;

    private static Calendar calendar = Calendar.getInstance();

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

        prevYear = findViewById(R.id.left_button);
        prevYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar.add(Calendar.YEAR, -1);
                updateMonthlyBudgetsInAYear();
            }
        });

        nextYear = findViewById(R.id.right_button);
        nextYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar.add(Calendar.YEAR, 1);
                updateMonthlyBudgetsInAYear();
            }
        });
    }

    private void updateMonthlyBudgetsInAYear() {

        int year = calendar.get(Calendar.YEAR);

        monthlyBudgetViewModel.getAllMonthlyBudgetsInAYear(year).observe(this, new Observer<List<MonthlyBudget>>() {
            @Override
            public void onChanged(@Nullable final List<MonthlyBudget> monthlyBudgets) {
                monthlyBudgetAdapter.submitList(monthlyBudgets);
            }
        });

        textViewYear.setText(year + "");
    }

    private void initViewModels() {

        Calendar tempCalendar = Calendar.getInstance();
        int year = tempCalendar.get(Calendar.YEAR);

        textViewYear.setText(year + "");

        monthlyBudgetViewModel = ViewModelProviders.of(this).get(MonthlyBudgetViewModel.class);

        monthlyBudgetViewModel.getAllMonthlyBudgetsInAYear(year).observe(this, new Observer<List<MonthlyBudget>>() {
            @Override
            public void onChanged(@Nullable final List<MonthlyBudget> monthlyBudgets) {
                monthlyBudgetAdapter.submitList(monthlyBudgets);
            }
        });

        // when click on item in recycler view -> populate data and open up to edit
        monthlyBudgetAdapter.setOnItemClickListener(new MonthlyBudgetAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MonthlyBudget monthlyBudget) {
                Intent goToAddEditMonthlyBudgetActivity = new Intent(MonthlyBudgetActivity.this, AddEditMonthlyBudgetActivity.class);
                goToAddEditMonthlyBudgetActivity.putExtra(AddEditMonthlyBudgetActivity.EXTRA_ID, monthlyBudget.getMonthlyBudgetId());
                goToAddEditMonthlyBudgetActivity.putExtra(AddEditMonthlyBudgetActivity.EXTRA_BUDGET, monthlyBudget.getBudget());
                goToAddEditMonthlyBudgetActivity.putExtra(AddEditMonthlyBudgetActivity.EXTRA_YEAR, monthlyBudget.getYear());
                goToAddEditMonthlyBudgetActivity.putExtra(AddEditMonthlyBudgetActivity.EXTRA_MONTH, monthlyBudget.getMonth());
                startActivityForResult(goToAddEditMonthlyBudgetActivity, EDIT_MONTHLY_BUDGET_ACTIVITY_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == ADD_MONTHLY_BUDGET_ACTIVITY_REQUEST_CODE) {

                // create Monthly Budget
                if (data.getStringExtra(AddEditMonthlyBudgetActivity.EXTRA_OPERATION).equals("save")) {

                    MonthlyBudget monthlyBudget = extractDataToMonthlyBudget(data, 0L);
                    monthlyBudgetViewModel.insertMonthlyBudget(monthlyBudget);
                }
            } else {

                Long id = data.getLongExtra(AddEditMonthlyBudgetActivity.EXTRA_ID, -1);
                if (id == -1) {
                    showSnackbar("budget cannot be updated");
                }

                MonthlyBudget monthlyBudget = extractDataToMonthlyBudget(data, id);

                // update Monthly Budget
                if (data.getStringExtra(AddEditMonthlyBudgetActivity.EXTRA_OPERATION).equals("save")) {
                    monthlyBudgetViewModel.updateMonthlyBudget(monthlyBudget);

                    // update all future Monthly Budget
                }  else if (data.getStringExtra(AddEditMonthlyBudgetActivity.EXTRA_OPERATION).equals("save next")) {
                    monthlyBudgetViewModel.updateAllFutureMonthlyBudgets(monthlyBudget.getMonthlyBudgetId(), monthlyBudget.getBudget());

                    // update all
                } else if (data.getStringExtra(AddEditMonthlyBudgetActivity.EXTRA_OPERATION).equals("save all")) {
                    monthlyBudgetViewModel.updateAllMonthlyBudgets(monthlyBudget.getBudget());
                }
                // delete Monthly Budget
                else {
                    monthlyBudgetViewModel.deleteMonthlyBudget(monthlyBudget);
                    //showSnackbar(monthlyBudget);
                }
            }
        }
    }

    private void showSnackbar(String message) {

        Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG);

        snackbar.show();
    }

    private MonthlyBudget extractDataToMonthlyBudget(Intent data, Long id) {

        int budget = data.getIntExtra(AddEditMonthlyBudgetActivity.EXTRA_BUDGET, 0);
        int year = data.getIntExtra(AddEditMonthlyBudgetActivity.EXTRA_YEAR, 0);
        int month = data.getIntExtra(AddEditMonthlyBudgetActivity.EXTRA_MONTH, 0);

        MonthlyBudget monthlyBudget = new MonthlyBudget(budget, year, month);

        if (id != 0) {
            monthlyBudget.setMonthlyBudgetId(id);
        }
        return monthlyBudget;
    }

    private void showSnackbar(MonthlyBudget monthlyBudget) {

        Snackbar snackbar = Snackbar.make(coordinatorLayout, "Monthly Budget deleted", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // insert back
                        monthlyBudgetViewModel.insertMonthlyBudget(monthlyBudget);

                        Snackbar snackbar1 = Snackbar.make(coordinatorLayout, "Undo successful", Snackbar.LENGTH_SHORT);
                        snackbar1.show();
                    }
                });

        snackbar.show();
    }
}