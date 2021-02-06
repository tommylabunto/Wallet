package com.xingtingkai.wallet;

import android.app.ActivityOptions;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.transition.Fade;
import android.util.Pair;
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
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xingtingkai.wallet.adapter.MonthlyTransactionAdapter;
import com.xingtingkai.wallet.db.entity.MonthlyBudget;
import com.xingtingkai.wallet.db.entity.Transaction;
import com.xingtingkai.wallet.db.viewmodel.MonthlyBudgetViewModel;
import com.xingtingkai.wallet.db.viewmodel.TransactionViewModel;
import com.xingtingkai.wallet.helper.SearchSuggestionProvider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MonthlyTransactionActivity extends AppCompatActivity {

    private TransactionViewModel transactionViewModel;
    private MonthlyBudgetViewModel monthlyBudgetViewModel;

    private MonthlyTransactionAdapter monthlyTransactionAdapter;

    private ImageButton prevMonth;
    private ImageButton nextMonth;

    private static ZonedDateTime startMonthDate;
    private static ZonedDateTime endMonthDate;

    private TextView textViewMonth;
    private TextView textViewYear;
    private TextView textViewTotalExpenses;
    private TextView textViewMonthlyBudget;
    private TextView textViewRemaining;
    private TextView textViewRemainingLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_transaction);

        textViewMonth = findViewById(R.id.textView_month);
        textViewYear = findViewById(R.id.textView_year);
        textViewTotalExpenses = findViewById(R.id.textView_totalAmount);
        textViewMonthlyBudget = findViewById(R.id.textView_monthly_budget);
        textViewRemaining = findViewById(R.id.textView_remaining);
        textViewRemainingLabel = findViewById(R.id.textView_remaining_label);

        // show transaction in recycler view
        RecyclerView recyclerView = findViewById(R.id.recyclerview_monthly_transaction);
        monthlyTransactionAdapter = new MonthlyTransactionAdapter(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(monthlyTransactionAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        initViewModels();

        prevMonth = findViewById(R.id.left_button);
        // on click
        prevMonth.setOnClickListener((View view) -> {
            startMonthDate = startMonthDate.minusMonths(1);
            endMonthDate = endMonthDate.minusMonths(1);
            updateTransactionsInAMonth();
            updateMonthlyBudget();
            calculateRemaining();
        });

        nextMonth = findViewById(R.id.right_button);
        // on click
        nextMonth.setOnClickListener((View view) -> {
            startMonthDate = startMonthDate.plusMonths(1);
            endMonthDate = endMonthDate.plusMonths(1);
            updateTransactionsInAMonth();
            updateMonthlyBudget();
            calculateRemaining();
        });

        handleIntent(getIntent());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

        removeBlinking();
    }

    // remove blinking effect from animating shared elements
    private void removeBlinking() {
        Fade fade = new Fade();
        View decor = getWindow().getDecorView();
        fade.excludeTarget(decor.findViewById(R.id.action_bar_container), true);
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);
        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);
    }

    private void initStartEndCal() {

        startMonthDate = ZonedDateTime
                .now()
                .withMinute(0)
                .withHour(0)
                .withDayOfMonth(1);

        boolean isLeapYear = startMonthDate.toLocalDate().isLeapYear();

        endMonthDate = ZonedDateTime
                .now()
                .withMinute(59)
                .withHour(23)
                .withDayOfMonth(startMonthDate.getMonth().length(isLeapYear));

        int yearInt = startMonthDate.getYear();
        Month month = startMonthDate.getMonth();
        // originally is all caps
        String monthString = month.name().substring(0,1) + month.name().substring(1,3).toLowerCase();

        String year = getString(R.string.single_string_param, yearInt + "");
        textViewYear.setText(year);
        textViewMonth.setText(monthString);
    }

    private void initViewModels() {

        initStartEndCal();

        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        updateTransactionsInAMonth();

        monthlyBudgetViewModel = new ViewModelProvider(this).get(MonthlyBudgetViewModel.class);
        updateMonthlyBudget();
    }

    private void updateMonthlyBudget() {

        int year = startMonthDate.getYear();
        int month = startMonthDate.getMonth().getValue();

        Future<MonthlyBudget> monthlyBudgetFuture = monthlyBudgetViewModel.getMonthlyBudget(year, month);

        try {
            MonthlyBudget monthlyBudget = monthlyBudgetFuture.get();

            updateTextViewBudget(monthlyBudget);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void updateTextViewBudget(MonthlyBudget tempMonthlyBudget) {

        double budget = 0;

        if (tempMonthlyBudget != null) {
            budget = tempMonthlyBudget.getBudget();
        }

        BigDecimal totalBudgetBd = new BigDecimal(budget).setScale(2, RoundingMode.HALF_UP);
        String totalBudget = getString(R.string.single_string_param, totalBudgetBd.intValue() + "");
        textViewMonthlyBudget.setText(totalBudget);

        calculateRemaining();
    }

    private void calculateRemaining() {

        String budgetString = textViewMonthlyBudget.getText().toString().trim();
        int budget = 0;

        String totalExpensesString = textViewTotalExpenses.getText().toString().trim();
        int totalExpenses = 0;

        if (!budgetString.isEmpty()) {
            budget = Integer.parseInt(budgetString);
        }

        if (!totalExpensesString.isEmpty()) {
            totalExpenses = Integer.parseInt(totalExpensesString);
        }

        int remaining = budget - totalExpenses;
        BigDecimal totalRemainingBd = new BigDecimal(remaining).setScale(2, RoundingMode.HALF_UP);

        if (remaining < 0) {
            textViewRemaining.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorDeficit));
            textViewRemainingLabel.setText(getString(R.string.deficit));
        } else if (remaining > 0) {
            textViewRemaining.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorSurplus));
            textViewRemainingLabel.setText(getString(R.string.surplus));
        } else {
            textViewRemaining.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorNotSoBlack));
            textViewRemainingLabel.setText(getString(R.string.balance));
        }

        remaining = Math.abs(totalRemainingBd.intValue());

        String remainingString = getString(R.string.single_string_param, remaining + "");
        textViewRemaining.setText(remainingString);
    }

    private void updateTransactionsInAMonth() {

        // take some time to calculate, store the variables
        long startMonthDateEpoch = startMonthDate.toEpochSecond();
        long endMonthDateEpoch = endMonthDate.toEpochSecond();

        // on changed
        transactionViewModel.getAllTransactionsInAMonthView(startMonthDateEpoch, endMonthDateEpoch)
                .observe(this, (@Nullable final List<Transaction> transactions) -> {
            // Update the cached copy of the words in the transactionAdapter.
            monthlyTransactionAdapter.submitList(transactions);
        });

        Future<Double> totalExpensesFuture = transactionViewModel.calculateExpensesInAMonth(startMonthDateEpoch, endMonthDateEpoch);

        try {
            double totalExpenses = totalExpensesFuture.get();

            updateTextViewTotalExpenses(totalExpenses);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }


        int yearInt = startMonthDate.getYear();
        Month month = startMonthDate.getMonth();
        // originally is all caps
        String monthString = month.name().substring(0,1) + month.name().substring(1,3).toLowerCase();

        String year = getString(R.string.single_string_param, yearInt + "");
        textViewYear.setText(year);
        textViewMonth.setText(monthString);
    }

    private void updateTextViewTotalExpenses(double totalExpenses) {

        // round up to 2.d.p
        BigDecimal totalAmountBd = new BigDecimal(totalExpenses).setScale(2, RoundingMode.HALF_UP);
        String totalAmount = getString(R.string.single_string_param, totalAmountBd.intValue() + "");
        textViewTotalExpenses.setText(totalAmount);
    }

    private void handleIntent(Intent intent) {

        // Get the intent, verify the action and get the query
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Intent goToSearchTransaction = new Intent(MonthlyTransactionActivity.this, SearchTransactionActivity.class);
            goToSearchTransaction.setAction(Intent.ACTION_SEARCH);
            goToSearchTransaction.putExtra(SearchTransactionActivity.EXTRA_SEARCH, query);

            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);

            startActivity(goToSearchTransaction);
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

        if (searchManager != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        }

        MenuItem monthView = menu.findItem(R.id.action_month_view);
        monthView.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
         Handle action bar item clicks here. The action bar will
         automatically handle clicks on the Home/Up button, so long
         as you specify a parent activity in AndroidManifest.xml.
        */
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent goToSettings = new Intent(MonthlyTransactionActivity.this, SettingsActivity.class);
                startActivity(goToSettings);
                return true;
            case R.id.action_normal_view:
                Intent goToMainActivity = new Intent(MonthlyTransactionActivity.this, MainActivity.class);

                ActivityOptions options = ActivityOptions
                        .makeSceneTransitionAnimation(MonthlyTransactionActivity.this,
                                Pair.create(prevMonth, "changePrevMonth"),
                                Pair.create(nextMonth, "changeNextMonth"),
                                Pair.create(textViewMonth, "changeTextViewMonth"),
                                Pair.create(textViewYear, "changeTextViewYear")
                        );

                startActivity(goToMainActivity, options.toBundle());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}