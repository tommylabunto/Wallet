package com.example.wallet;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wallet.adapter.MonthlyTransactionAdapter;
import com.example.wallet.db.entity.MonthlyBudget;
import com.example.wallet.db.entity.Transaction;
import com.example.wallet.db.viewmodel.MonthlyBudgetViewModel;
import com.example.wallet.db.viewmodel.TransactionViewModel;
import com.example.wallet.helper.SearchSuggestionProvider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
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

    private TextView textViewMonth;
    private TextView textViewYear;
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

        textViewMonth = findViewById(R.id.textView_name);
        textViewYear = findViewById(R.id.textView_year);
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

        handleIntent(getIntent());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    private void initStartEndCal() {

        // first and last day of month
        startMonthCal = Calendar.getInstance();
        startMonthCal.set(Calendar.DAY_OF_MONTH, 1);
        startMonthCal.set(Calendar.HOUR_OF_DAY, 0);
        startMonthCal.set(Calendar.MINUTE, 0);

        endMonthCal = Calendar.getInstance();
        int lastDay = endMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH);
        endMonthCal.set(Calendar.DAY_OF_MONTH, lastDay);
        endMonthCal.set(Calendar.HOUR_OF_DAY, 23);
        endMonthCal.set(Calendar.MINUTE, 59);

        int yearInt = startMonthCal.get(Calendar.YEAR);
        int monthInt = startMonthCal.get(Calendar.MONTH);
        // month uses 1 (jan) to 12 (dec)
        Month month = Month.of(monthInt + 1);
        // originally is all caps
        String monthString = month.name().substring(0,1) + month.name().substring(1,3).toLowerCase();

        textViewYear.setText(yearInt + "");
        textViewMonth.setText(monthString);
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

        textViewMonthlyBudget.setText(totalBudgetBd.toBigInteger() + "");

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

        textViewRemaining.setText( (int) remaining + "");

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

        int yearInt = startMonthCal.get(Calendar.YEAR);
        int monthInt = startMonthCal.get(Calendar.MONTH);
        // month uses 1 (jan) to 12 (dec)
        Month month = Month.of(monthInt + 1);
        // originally is all caps
        String monthString = month.name().substring(0,1) + month.name().substring(1,3).toLowerCase();

        textViewYear.setText(yearInt + "");
        textViewMonth.setText(monthString);
    }

    private void analyzeTransactions(List<Transaction> transactions) {

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

        textViewTotalAmount.setText( (int) (totalAmountBd.doubleValue()) + "/");
    }

    private void handleIntent(Intent intent) {

        Log.d("handle intent"," here");

        // Get the intent, verify the action and get the query
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.d("query main", query);
            Intent goToSearchTransactionActivity = new Intent(MonthlyTransactionActivity.this, SearchTransactionActivity.class);
            goToSearchTransactionActivity.setAction(Intent.ACTION_SEARCH);
            goToSearchTransactionActivity.putExtra(SearchTransactionActivity.EXTRA_SEARCH, query);

            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);

            startActivity(goToSearchTransactionActivity);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        MenuItem monthView = menu.findItem(R.id.action_month_view);
        monthView.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent goToSettingsActivity = new Intent(MonthlyTransactionActivity.this, SettingsActivity.class);
                startActivity(goToSettingsActivity);
                return true;
            case R.id.action_normal_view:
                Intent goToMainActivity = new Intent(MonthlyTransactionActivity.this, MainActivity.class);
                startActivity(goToMainActivity);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}