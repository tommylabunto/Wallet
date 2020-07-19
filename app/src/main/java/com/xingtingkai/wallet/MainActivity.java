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
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.xingtingkai.wallet.adapter.TransactionAdapter;
import com.xingtingkai.wallet.db.WalletDatabase;
import com.xingtingkai.wallet.db.entity.Transaction;
import com.xingtingkai.wallet.db.viewmodel.TransactionViewModel;
import com.xingtingkai.wallet.db.viewmodel.TypeViewModel;
import com.xingtingkai.wallet.helper.DateFormatter;
import com.xingtingkai.wallet.helper.SearchSuggestionProvider;

import java.time.Month;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // add
    protected static final int ADD_TRANSACTION_ACTIVITY_REQUEST_CODE = 1;
    // save or delete
    protected static final int EDIT_TRANSACTION_ACTIVITY_REQUEST_CODE = 2;

    private TypeViewModel typeViewModel;
    private TransactionViewModel transactionViewModel;

    private TransactionAdapter transactionAdapter;

    private CoordinatorLayout coordinatorLayout;

    private static Calendar startMonthCal;
    private static Calendar endMonthCal;

    private ImageButton prevMonth;
    private ImageButton nextMonth;

    private TextView textViewYear;
    private TextView textViewMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // create transaction
        FloatingActionButton fab = findViewById(R.id.fab);
        // on click
        fab.setOnClickListener((View view) -> {
            Intent goToAddEditActivity = new Intent(MainActivity.this, AddEditTransactionActivity.class);
            startActivityForResult(goToAddEditActivity, ADD_TRANSACTION_ACTIVITY_REQUEST_CODE);
        });

        textViewYear = findViewById(R.id.textView_year);
        textViewMonth = findViewById(R.id.textView_month);

        prevMonth = findViewById(R.id.left_button);
        // on click
        prevMonth.setOnClickListener((View view) -> {
            startMonthCal.add(Calendar.MONTH, -1);
            endMonthCal.add(Calendar.MONTH, -1);
            updateTransactionsInAMonth();
            updateTextView();
        });

        nextMonth = findViewById(R.id.right_button);
        nextMonth.setOnClickListener((View view) -> {
            endMonthCal.add(Calendar.MONTH, 1);
            startMonthCal.add(Calendar.MONTH, 1);
            updateTransactionsInAMonth();
            updateTextView();
        });

        // show transaction in recycler view
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        transactionAdapter = new TransactionAdapter(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(transactionAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        initViewModels();

        coordinatorLayout = findViewById(R.id.mainActivity);

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

        String year = getString(R.string.single_string_param, yearInt + "");
        textViewYear.setText(year);
        textViewMonth.setText(monthString);
    }

    private void initViewModels() {

        initStartEndCal();

        transactionViewModel = ViewModelProviders.of(this).get(TransactionViewModel.class);

        updateTransactionsInAMonth();
        updateTextView();

        // when click on item in recycler view -> populate data and open up to edit
        // on item click
        transactionAdapter.setOnItemClickListener((Transaction transaction) -> {
            Intent goToAddEditTransaction = new Intent(MainActivity.this, AddEditTransactionActivity.class);
            goToAddEditTransaction.putExtra(AddEditTransactionActivity.EXTRA_ID, transaction.getTransactionId());
            goToAddEditTransaction.putExtra(AddEditTransactionActivity.EXTRA_NAME, transaction.getName());
            goToAddEditTransaction.putExtra(AddEditTransactionActivity.EXTRA_TYPENAME, transaction.getTypeName());
            goToAddEditTransaction.putExtra(AddEditTransactionActivity.EXTRA_DATE, DateFormatter.formatDateToString(transaction.getDate()));
            goToAddEditTransaction.putExtra(AddEditTransactionActivity.EXTRA_VALUE, transaction.getValue());
            goToAddEditTransaction.putExtra(AddEditTransactionActivity.EXTRA_IS_EXPENSE_TYPE, transaction.isExpenseTransaction());
            startActivityForResult(goToAddEditTransaction, EDIT_TRANSACTION_ACTIVITY_REQUEST_CODE);
        });

        typeViewModel = ViewModelProviders.of(this).get(TypeViewModel.class);

        // on changed
        typeViewModel.getAllTypesString().observe(this, (@Nullable final List<String> types) -> {
            if (types != null && types.size() == 0) {
                WalletDatabase.addTypes();
            }
        });
    }

    private void updateTransactionsInAMonth() {

        // on changed
        transactionViewModel.getAllTransactionsInAMonth(startMonthCal.getTimeInMillis(), endMonthCal.getTimeInMillis()).observe(this, transactions -> {
            // Update the cached copy of the words in the transactionAdapter.
            transactionAdapter.submitList(transactions);
            transactionAdapter.passTransactions(transactions);
        });
    }

    private void updateTextView() {

        int yearInt = startMonthCal.get(Calendar.YEAR);
        int monthInt = startMonthCal.get(Calendar.MONTH);
        // month uses 1 (jan) to 12 (dec)
        Month month = Month.of(monthInt + 1);
        // originally is all caps
        String monthString = month.name().substring(0,1) + month.name().substring(1,3).toLowerCase();

        String year = getString(R.string.single_string_param, yearInt + "");
        textViewYear.setText(year);
        textViewMonth.setText(monthString);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == ADD_TRANSACTION_ACTIVITY_REQUEST_CODE) {

                // create transaction
                if (data.getStringExtra(AddEditTransactionActivity.EXTRA_OPERATION).equals("save")) {
                    Transaction transaction = extractDataToTransaction(data, 0L);
                    transactionViewModel.insertTransaction(transaction);
                    reload();
                }
            } else {

                long id = data.getLongExtra(AddEditTransactionActivity.EXTRA_ID, -1);
                if (id == -1) {
                    showSnackbar("transaction cannot be updated");
                }

                Transaction transaction = extractDataToTransaction(data, id);

                // update transaction
                if (data.getStringExtra(AddEditTransactionActivity.EXTRA_OPERATION).equals("save")) {

                    transactionViewModel.updateTransaction(transaction);
                    reload();

                    // delete transaction
                } else {
                    transactionViewModel.deleteTransaction(transaction);
                    reload();
                }
            }
        }
    }

    private void reload() {
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }

    private void showSnackbar(String message) {

        Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private Transaction extractDataToTransaction(Intent data, long id) {

        String name = data.getStringExtra(AddEditTransactionActivity.EXTRA_NAME);
        String typeName = data.getStringExtra(AddEditTransactionActivity.EXTRA_TYPENAME);
        double value = data.getDoubleExtra(AddEditTransactionActivity.EXTRA_VALUE, 1);

        String dateString = data.getStringExtra(AddEditTransactionActivity.EXTRA_DATE);
        Date date = DateFormatter.formatStringToDate(dateString);

        boolean isExpenseType = data.getBooleanExtra(AddEditTransactionActivity.EXTRA_IS_EXPENSE_TYPE, true);

//        if (id != 0) {
//            transaction.setTransactionId(id);
//        }
        return Transaction.createNonRecurringTransaction(id, date, value, name, typeName, isExpenseType);
    }

    private void handleIntent(Intent intent) {

        // Get the intent, verify the action and get the query
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Intent goToSearchTransaction = new Intent(MainActivity.this, SearchTransactionActivity.class);
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

        MenuItem normalView = menu.findItem(R.id.action_normal_view);
        normalView.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent goToSettings = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(goToSettings);
                return true;

            case R.id.action_month_view:
                Intent goToMonthlyTransaction = new Intent(MainActivity.this, MonthlyTransactionActivity.class);

                ActivityOptions options = ActivityOptions
                        .makeSceneTransitionAnimation(MainActivity.this,
                                Pair.create(prevMonth, "changePrevMonth"),
                                Pair.create(nextMonth, "changeNextMonth"),
                                Pair.create(textViewMonth, "changeTextViewMonth"),
                                Pair.create(textViewYear, "changeTextViewYear")
                        );

                startActivity(goToMonthlyTransaction, options.toBundle());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
