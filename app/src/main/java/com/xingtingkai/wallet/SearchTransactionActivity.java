package com.xingtingkai.wallet;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xingtingkai.wallet.adapter.SearchTransactionAdapter;
import com.xingtingkai.wallet.db.entity.Transaction;
import com.xingtingkai.wallet.db.viewmodel.TransactionViewModel;
import com.xingtingkai.wallet.helper.SearchSuggestionProvider;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

public class SearchTransactionActivity extends AppCompatActivity {

    public static final String EXTRA_SEARCH =
            "com.example.wallet.EXTRA_SEARCH";
    private SearchTransactionAdapter searchTransactionAdapter;
    private TransactionViewModel transactionViewModel;

    private CoordinatorLayout coordinatorLayout;

    // save or delete
    protected static final int EDIT_TRANSACTION_ACTIVITY_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_transaction);

        coordinatorLayout = findViewById(R.id.searchTransactionActivity);

        // show transaction in recycler view
        RecyclerView recyclerView = findViewById(R.id.recyclerview_search_transaction);
        searchTransactionAdapter = new SearchTransactionAdapter(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(searchTransactionAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        initViewModel();
        handleIntent(getIntent());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    private void initViewModel() {

        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        /*
         when click on item in recycler view -> populate data and open up to edit
         on item click
        */
        searchTransactionAdapter.setOnItemClickListener((Transaction transaction) -> {
            Intent goToAddEditTransaction = new Intent(SearchTransactionActivity.this, AddEditTransactionActivity.class);
            goToAddEditTransaction.putExtra(AddEditTransactionActivity.EXTRA_ID, transaction.getTransactionId());
            goToAddEditTransaction.putExtra(AddEditTransactionActivity.EXTRA_NAME, transaction.getName());
            goToAddEditTransaction.putExtra(AddEditTransactionActivity.EXTRA_TYPENAME, transaction.getTypeName());
            goToAddEditTransaction.putExtra(AddEditTransactionActivity.EXTRA_INSTANT, transaction.getInstant().getEpochSecond());
            goToAddEditTransaction.putExtra(AddEditTransactionActivity.EXTRA_ZONE_ID, transaction.getZoneId().getId());
            goToAddEditTransaction.putExtra(AddEditTransactionActivity.EXTRA_VALUE, transaction.getValue());
            goToAddEditTransaction.putExtra(AddEditTransactionActivity.EXTRA_IS_EXPENSE_TYPE, transaction.isExpenseTransaction());
            startActivityForResult(goToAddEditTransaction, EDIT_TRANSACTION_ACTIVITY_REQUEST_CODE);
        });
    }

    private void searchTransactions(String query) {

        StringBuilder searchNameSb = new StringBuilder();
        searchNameSb.append("%");
        searchNameSb.append(query);
        searchNameSb.append("%");

        String searchName = searchNameSb.toString();

        // on changed
        transactionViewModel.searchAllTransactions(searchName).observe(this,
                (@Nullable final List<Transaction> transactions) -> {
            // Update the cached copy of the words in the transactionAdapter.
            searchTransactionAdapter.submitList(transactions);
        });
    }

    private void handleIntent(Intent intent) {

        // Get the intent, verify the action and get the query
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);

            if (intent.hasExtra(EXTRA_SEARCH)) {
                query = intent.getStringExtra(EXTRA_SEARCH);
            }

            searchTransactions(query);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {

        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == EDIT_TRANSACTION_ACTIVITY_REQUEST_CODE) {

                long id = data.getLongExtra(AddEditTransactionActivity.EXTRA_ID, -1);
                if (id == -1) {
                    Toast.makeText(this, "Transaction can't be updated", Toast.LENGTH_SHORT).show();
                }

                Transaction transaction = extractDataToTransaction(data, id);

                String operation = data.getStringExtra(AddEditTransactionActivity.EXTRA_OPERATION);

                // update transaction
                if (operation != null) {
                    if (operation.equals("save")) {
                        transactionViewModel.updateTransaction(transaction);
                        reload();
                    }
                    else {
                        // delete transaction
                        transactionViewModel.deleteTransaction(transaction);
                        //showSnackbar(transaction);
                    }
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

//    private void showSnackbar(Transaction transaction) {
//
//        final int isClicked = 0;
//
//        Snackbar snackbar = Snackbar.make(coordinatorLayout, "Transaction deleted", Snackbar.LENGTH_LONG)
//                // on click
//                .setAction("UNDO", (View v) -> {
//                    // insert back
//                    transactionViewModel.insertTransaction(transaction);
//                    reload();
//
//                    Snackbar snackbar1 = Snackbar.make(coordinatorLayout, "Undo successful", Snackbar.LENGTH_SHORT);
//                    snackbar1.show();
//                })
//                .addCallback(new Snackbar.Callback() {
//
//                    @Override
//                    public void onDismissed(Snackbar snackbar, int event) {
//                        //see Snackbar.Callback docs for event details
//                        showToast();
//                        reload();
//                    }
//                });
//
//        snackbar.show();
//    }
//
//    private void showToast() {
//        Toast.makeText(this, "Reloading", Toast.LENGTH_SHORT).show();
//    }

    private Transaction extractDataToTransaction(Intent data, long id) {

        String name = data.getStringExtra(AddEditTransactionActivity.EXTRA_NAME);

        if (name == null) {
            name = "";
        }

        String typeName = data.getStringExtra(AddEditTransactionActivity.EXTRA_TYPENAME);

        if (typeName == null) {
            typeName = "";
        }

        double value = data.getDoubleExtra(AddEditTransactionActivity.EXTRA_VALUE, 1);

        String zoneIdString = data.getStringExtra(AddEditRepeatTransactionActivity.EXTRA_ZONE_ID);
        ZoneId zoneId = ZoneId.of(zoneIdString);

        long instantLong = data.getLongExtra(AddEditTransactionActivity.EXTRA_INSTANT, 0L);
        Instant instant = Instant.ofEpochSecond(instantLong);

        boolean isExpenseType = data.getBooleanExtra(AddEditTransactionActivity.EXTRA_IS_EXPENSE_TYPE, true);

        return Transaction.createNonRecurringTransaction(id, instant, zoneId, value, name, typeName, isExpenseType);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
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
                Intent goToSettings = new Intent(SearchTransactionActivity.this, SettingsActivity.class);
                startActivity(goToSettings);
                return true;
            case R.id.action_normal_view:
                Intent goToMainActivity = new Intent(SearchTransactionActivity.this, MainActivity.class);
                startActivity(goToMainActivity);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}