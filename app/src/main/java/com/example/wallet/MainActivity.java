package com.example.wallet;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wallet.adapter.TransactionAdapter;
import com.example.wallet.db.WalletDatabase;
import com.example.wallet.db.entity.Transaction;
import com.example.wallet.db.viewmodel.TransactionViewModel;
import com.example.wallet.db.viewmodel.TypeViewModel;
import com.example.wallet.helper.DateFormatter;
import com.example.wallet.helper.SearchSuggestionProvider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/*
TODO: (note):  use sp for text and container for text -> it scales with phone size and font size preference on phone
TODO: (note): try to use flat view hierachy, instead of nested
TODO: (note): change all input types to material design

 */
// TODO: improve UI for settings (change to cardview) (today)
// TODO: improve UI for type (today)
// TODO: improve UI for monthly budget (today)
// TODO: improve UI for main, month view, add


/* (end)
// TODO: use error text for repeat transaction
// TODO: change string to string resource
// TODO: set up cards for user to see when first download
// TODO: comply with google's standards
// TODO: learn to handle room migration
 */
/*
TODO: import database (try update INSTANCE in db, refresh viewmodel) (second)
TODO: use sp or dp
- can createfromassets successfully
- works if change the name in wallet database (prepopulate db), it takes a while,
 then need to change the name in getInstance to load it
 */

/*
FUTURE updates:
// TODO: try using fts3 to search, instead of using LIKE %keyword%, which takes up more time when there are more transactions
// TODO: implement widgets
// TODO: send crash reports
// TODO: drag and hold in recyclerview to update and delete
 */

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

    private Button buttonMonthlyTransaction;
    private Button buttonSearchTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // create transaction
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToAddEditActivity = new Intent(MainActivity.this, AddEditTransactionActivity.class);
                startActivityForResult(goToAddEditActivity, ADD_TRANSACTION_ACTIVITY_REQUEST_CODE);
            }
        });

        prevMonth = findViewById(R.id.left_button);
        prevMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMonthCal.add(Calendar.MONTH, -1);
                endMonthCal.add(Calendar.MONTH, -1);
                updateTransactionsInAMonth();
            }
        });

        nextMonth = findViewById(R.id.right_button);
        nextMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endMonthCal.add(Calendar.MONTH, 1);
                startMonthCal.add(Calendar.MONTH, 1);
                updateTransactionsInAMonth();
            }
        });

        // show transaction in recycler view
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        transactionAdapter = new TransactionAdapter(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(transactionAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        initViewModels();

        coordinatorLayout = findViewById(R.id.mainActivity);

        buttonMonthlyTransaction = findViewById(R.id.button_monthly_transaction);
        buttonMonthlyTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToMonthlyTransactionActivity = new Intent(MainActivity.this, MonthlyTransactionActivity.class);
                startActivity(goToMonthlyTransactionActivity);
            }
        });

        buttonSearchTransaction = findViewById(R.id.button_search_transaction);
        buttonSearchTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToSearchTransactionActivity = new Intent(MainActivity.this, SearchTransactionActivity.class);
                startActivity(goToSearchTransactionActivity);
            }
        });

        handleIntent(getIntent());
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

        // when click on item in recycler view -> populate data and open up to edit
        transactionAdapter.setOnItemClickListener(new TransactionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Transaction transaction) {
                Intent goToAddEditTransactionActivity = new Intent(MainActivity.this, AddEditTransactionActivity.class);
                goToAddEditTransactionActivity.putExtra(AddEditTransactionActivity.EXTRA_ID, transaction.getTransactionId());
                goToAddEditTransactionActivity.putExtra(AddEditTransactionActivity.EXTRA_NAME, transaction.getName());
                goToAddEditTransactionActivity.putExtra(AddEditTransactionActivity.EXTRA_TYPENAME, transaction.getTypeName());
                goToAddEditTransactionActivity.putExtra(AddEditTransactionActivity.EXTRA_DATE, DateFormatter.formatDateToString(transaction.getDate()));
                goToAddEditTransactionActivity.putExtra(AddEditTransactionActivity.EXTRA_VALUE, transaction.getValue());
                goToAddEditTransactionActivity.putExtra(AddEditTransactionActivity.EXTRA_IS_EXPENSE_TYPE, transaction.isExpenseTransaction());
                startActivityForResult(goToAddEditTransactionActivity, EDIT_TRANSACTION_ACTIVITY_REQUEST_CODE);
            }
        });

        typeViewModel = ViewModelProviders.of(this).get(TypeViewModel.class);

        typeViewModel.getAllTypesString().observe(this, new Observer<List<String>>() {
            @Override
            public void onChanged(@Nullable final List<String> types) {

                if (types.size() == 0) {
                    WalletDatabase.addTypes();
                }
            }
        });
    }

    private void updateTransactionsInAMonth() {

        transactionViewModel.getAllTransactionsInAMonth(startMonthCal.getTimeInMillis(), endMonthCal.getTimeInMillis()).observe(this, new Observer<List<Transaction>>() {
            @Override
            public void onChanged(@Nullable final List<Transaction> transactions) {
                // Update the cached copy of the words in the transactionAdapter.
                transactionAdapter.submitList(transactions);
                transactionAdapter.passTransactions(transactions);
            }
        });
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
                    Toast.makeText(this, "Transaction saved", Toast.LENGTH_LONG).show();
                }
            } else {

                Long id = data.getLongExtra(AddEditTransactionActivity.EXTRA_ID, -1);
                if (id == -1) {
                    Toast.makeText(this, "Transaction can't be updated", Toast.LENGTH_SHORT).show();
                }

                Transaction transaction = extractDataToTransaction(data, id);

                // update transaction
                if (data.getStringExtra(AddEditTransactionActivity.EXTRA_OPERATION).equals("save")) {

                    transactionViewModel.updateTransaction(transaction);
                    reload();
                    Toast.makeText(this, "Transaction updated", Toast.LENGTH_SHORT).show();

                    // delete transaction
                } else {
                    transactionViewModel.deleteTransaction(transaction);
                    //showSnackbar(transaction);
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

    private void showSnackbar(Transaction transaction) {

        final int isClicked = 0;

        Snackbar snackbar = Snackbar.make(coordinatorLayout, "Transaction deleted", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // insert back
                        transactionViewModel.insertTransaction(transaction);
                        reload();

                        Snackbar snackbar1 = Snackbar.make(coordinatorLayout, "Undo successful", Snackbar.LENGTH_SHORT);
                        snackbar1.show();
                    }
                })
                .addCallback(new Snackbar.Callback() {

                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        //see Snackbar.Callback docs for event details
                        showToast();
                        reload();
                    }
                });

        snackbar.show();
    }

    private void showToast() {
        Toast.makeText(this, "Reloading", Toast.LENGTH_SHORT).show();
    }

    private Transaction extractDataToTransaction(Intent data, Long id) {

        String name = data.getStringExtra(AddEditTransactionActivity.EXTRA_NAME);
        String typeName = data.getStringExtra(AddEditTransactionActivity.EXTRA_TYPENAME);
        double value = data.getDoubleExtra(AddEditTransactionActivity.EXTRA_VALUE, 1);

        String dateString = data.getStringExtra(AddEditTransactionActivity.EXTRA_DATE);
        Date date = DateFormatter.formatStringToDate(dateString);

        boolean isExpenseType = data.getBooleanExtra(AddEditTransactionActivity.EXTRA_IS_EXPENSE_TYPE, true);

        Transaction transaction = new Transaction(date, value, name, typeName, isExpenseType);

        if (id != 0) {
            transaction.setTransactionId(id);
        }
        return transaction;
    }

    private void handleIntent(Intent intent) {

        // Get the intent, verify the action and get the query
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.d("query main", query);
            Intent goToSearchTransactionActivity = new Intent(MainActivity.this, SearchTransactionActivity.class);
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

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent goToSettingsActivity = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(goToSettingsActivity);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
