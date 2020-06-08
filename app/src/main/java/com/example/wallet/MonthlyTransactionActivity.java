package com.example.wallet;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wallet.adapter.MonthlyTransactionAdapter;
import com.example.wallet.db.entity.MonthlyBudget;
import com.example.wallet.db.entity.Transaction;
import com.example.wallet.db.viewmodel.MonthlyBudgetViewModel;
import com.example.wallet.db.viewmodel.TransactionViewModel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.List;

public class MonthlyTransactionActivity extends AppCompatActivity {

    private TransactionViewModel transactionViewModel;
    private MonthlyBudgetViewModel monthlyBudgetViewModel;

    private MonthlyTransactionAdapter monthlyTransactionAdapter;

    private ImageButton prevMonth;
    private ImageButton nextMonth;

    private Calendar startMonthCal;
    private Calendar endMonthCal;

    private static Calendar calendar = Calendar.getInstance();

    private Button buttonMainActivity;

    private TextView textViewMonth;
    private TextView textViewTotalAmount;
    private TextView textViewMonthlyBudget;
    private TextView textViewRemaining;

    private static double totalExpenses;
    private static double totalIncome;

    private static MonthlyBudget monthlyBudget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_transaction);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        textViewMonth = findViewById(R.id.textView_Month);
        textViewTotalAmount = findViewById(R.id.textView_totalAmount);
        textViewMonthlyBudget = findViewById(R.id.textView_monthly_budget);
        textViewRemaining = findViewById(R.id.textView_remaining);

        // show transaction in recycler view
        RecyclerView recyclerView = findViewById(R.id.recyclerview_monthly_transaction);
        monthlyTransactionAdapter = new MonthlyTransactionAdapter(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(monthlyTransactionAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        initViewModels();

        prevMonth = findViewById(R.id.left_button);
        prevMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMonthCal.add(Calendar.MONTH, -1);
                endMonthCal.add(Calendar.MONTH, -1);
                updateTransactionsInAMonth();
                updateMonthlyBudget();
                calculateRemaining();
            }
        });

        nextMonth = findViewById(R.id.right_button);
        nextMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endMonthCal.add(Calendar.MONTH, 1);
                startMonthCal.add(Calendar.MONTH, 1);
                updateTransactionsInAMonth();
                updateMonthlyBudget();
                calculateRemaining();
            }
        });

        buttonMainActivity = findViewById(R.id.button_main_activity);
        buttonMainActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToMainActivity = new Intent(MonthlyTransactionActivity.this, MainActivity.class);
                startActivity(goToMainActivity);
            }
        });
    }

    private void initStartEndCal() {

        // first and last day of month
        startMonthCal = Calendar.getInstance();
        startMonthCal.set(Calendar.DAY_OF_MONTH, 1);

        endMonthCal = Calendar.getInstance();
        int lastDay = endMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH);
        endMonthCal.set(Calendar.DAY_OF_MONTH, lastDay);
    }

    private void initViewModels() {

        initStartEndCal();

        transactionViewModel = ViewModelProviders.of(this).get(TransactionViewModel.class);

        updateTransactionsInAMonth();

        monthlyBudgetViewModel = ViewModelProviders.of(this).get(MonthlyBudgetViewModel.class);

        updateMonthlyBudget();
    }

    private void updateMonthlyBudget() {

        int year = startMonthCal.get(Calendar.YEAR);
        int month = startMonthCal.get(Calendar.MONTH);

        monthlyBudgetViewModel.getMonthlyBudget(year, month).observe(this, new Observer<MonthlyBudget>() {
            @Override
            public void onChanged(@Nullable final MonthlyBudget monthlyBudget) {

                deepCopyMonthlyBudget(monthlyBudget);
            }
        });
    }

    private void deepCopyMonthlyBudget(MonthlyBudget tempMonthlyBudget) {

        monthlyBudget = new MonthlyBudget();

        if (tempMonthlyBudget != null) {
            monthlyBudget.setMonthlyBudgetId(tempMonthlyBudget.getMonthlyBudgetId());
            monthlyBudget.setBudget(tempMonthlyBudget.getBudget());
            monthlyBudget.setYear(tempMonthlyBudget.getYear());
            monthlyBudget.setMonth(tempMonthlyBudget.getMonth());
        } else {
            monthlyBudget.setBudget(0);
        }

        totalIncome = monthlyBudget.getBudget() + totalIncome;
        BigDecimal totalBudgetBd = new BigDecimal(totalIncome).setScale(2, RoundingMode.HALF_UP);

        textViewMonthlyBudget.setText(totalBudgetBd.toString());

        calculateRemaining();
    }

    private void calculateRemaining() {

        double remaining = 0;

        remaining = totalIncome - totalExpenses;
        BigDecimal totalRemainingBd = new BigDecimal(remaining).setScale(2, RoundingMode.HALF_UP);

        if (remaining < 0) {
            textViewRemaining.setTextColor(Color.parseColor("#FF0000"));
        } else {
            textViewRemaining.setTextColor(Color.parseColor("#00ff9b"));
        }

        remaining = Math.abs(totalRemainingBd.doubleValue());

        textViewRemaining.setText(remaining + "");

    }

    private void updateTransactionsInAMonth() {

        transactionViewModel.getAllTransactionsInAMonthView(startMonthCal.getTimeInMillis(), endMonthCal.getTimeInMillis()).observe(this, new Observer<List<Transaction>>() {
            @Override
            public void onChanged(@Nullable final List<Transaction> transactions) {
                // Update the cached copy of the words in the transactionAdapter.
                monthlyTransactionAdapter.submitList(transactions);
                analyzeTransactions(transactions);
            }
        });
    }

    private void analyzeTransactions(List<Transaction> transactions) {

        int year = startMonthCal.get(Calendar.YEAR);
        int month = startMonthCal.get(Calendar.MONTH);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append((month+1));
        stringBuilder.append("/");
        stringBuilder.append(year);

        textViewMonth.setText(stringBuilder.toString());

        double tempTotalExpenses = 0;
        double tempTotalIncome = 0;

        for (int i = 0; i < transactions.size(); i++) {

            Transaction transaction = transactions.get(i);

            if (transaction.isExpenseTransaction()) {
                tempTotalExpenses += transaction.getValue();
            } else {
                tempTotalIncome += transaction.getValue();
            }
        }

        totalExpenses = tempTotalExpenses;
        totalIncome = tempTotalIncome;

        // round up to 2.d.p
        BigDecimal totalAmountBd = new BigDecimal(totalExpenses).setScale(2, RoundingMode.HALF_UP);

        textViewTotalAmount.setText(totalAmountBd.doubleValue() + "/");
    }
}