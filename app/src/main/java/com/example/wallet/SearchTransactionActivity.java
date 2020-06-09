package com.example.wallet;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wallet.adapter.SearchTransactionAdapter;
import com.example.wallet.db.WalletDatabase;
import com.example.wallet.db.entity.Transaction;
import com.example.wallet.db.viewmodel.TransactionViewModel;
import com.example.wallet.helper.DateFormatter;
import com.example.wallet.helper.SearchSuggestionProvider;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SearchTransactionActivity extends AppCompatActivity {

    public static final String EXTRA_SEARCH =
            "com.example.wallet.EXTRA_SEARCH";
    private SearchTransactionAdapter searchTransactionAdapter;
    private TransactionViewModel transactionViewModel;

    private CoordinatorLayout coordinatorLayout;

    // save or delete
    public static final int EDIT_TRANSACTION_ACTIVITY_REQUEST_CODE = 2;

    private static WalletDatabase walletDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_transaction);

        walletDatabase = WalletDatabase.getDatabase(this);

        coordinatorLayout = findViewById(R.id.searchTransactionActivity);

        // show transaction in recycler view
        RecyclerView recyclerView = findViewById(R.id.recyclerview_search_transaction);
        searchTransactionAdapter = new SearchTransactionAdapter(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(searchTransactionAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        initViewModel();
        handleIntent(getIntent());
    }

    private void initViewModel() {

        transactionViewModel = ViewModelProviders.of(this).get(TransactionViewModel.class);

        // when click on item in recycler view -> populate data and open up to edit
        searchTransactionAdapter.setOnItemClickListener(new SearchTransactionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Transaction transaction) {
                Intent goToAddEditTransactionActivity = new Intent(SearchTransactionActivity.this, AddEditTransactionActivity.class);
                goToAddEditTransactionActivity.putExtra(AddEditTransactionActivity.EXTRA_ID, transaction.getTransactionId());
                goToAddEditTransactionActivity.putExtra(AddEditTransactionActivity.EXTRA_NAME, transaction.getName());
                goToAddEditTransactionActivity.putExtra(AddEditTransactionActivity.EXTRA_TYPENAME, transaction.getTypeName());
                goToAddEditTransactionActivity.putExtra(AddEditTransactionActivity.EXTRA_DATE, DateFormatter.formatDateToString(transaction.getDate()));
                goToAddEditTransactionActivity.putExtra(AddEditTransactionActivity.EXTRA_VALUE, transaction.getValue());
                goToAddEditTransactionActivity.putExtra(AddEditTransactionActivity.EXTRA_IS_EXPENSE_TYPE, transaction.isExpenseTransaction());
                startActivityForResult(goToAddEditTransactionActivity, EDIT_TRANSACTION_ACTIVITY_REQUEST_CODE);
            }
        });
    }

    private void searchTransactions(String query) {

//        StringBuilder searchNameSb = new StringBuilder();
//        searchNameSb.append("%");
//        searchNameSb.append(query);
//        searchNameSb.append("%");
//
//        transactionViewModel.searchAllTransactions(searchNameSb.toString()).observe(this, new Observer<List<Transaction>>() {
//            @Override
//            public void onChanged(@Nullable final List<Transaction> transactions) {
//                // Update the cached copy of the words in the transactionAdapter.
//                searchTransactionAdapter.submitList(transactions);
//            }
//        });

        List<Transaction> transactions = new ArrayList<>();

        Cursor cursor = walletDatabase.getNameMatches(query, null);

        final int idIndex = cursor.getColumnIndex(WalletDatabase.COL_ID);
        final int nameIndex = cursor.getColumnIndex(WalletDatabase.COL_NAME);
        final int valueIndex = cursor.getColumnIndex(WalletDatabase.COL_VALUE);
        final int typeNameIndex = cursor.getColumnIndex(WalletDatabase.COL_TYPE_NAME);
        final int dateIndex = cursor.getColumnIndex(WalletDatabase.COL_DATE);
        final int isRepeatIndex = cursor.getColumnIndex(WalletDatabase.COL_IS_REPEAT);
        final int isExpenseTransactionIndex = cursor.getColumnIndex(WalletDatabase.COL_IS_EXPENSE_TRANSACTION);

        try {

            // If moveToFirst() returns false then cursor is empty
            if (!cursor.moveToFirst()) {
            }
            do {
                // Read the values of a row in the table using the indexes acquired above
                final long id = cursor.getLong(idIndex);
                final String name = cursor.getString(nameIndex);
                final double value = cursor.getDouble(valueIndex);
                final String typeName = cursor.getString(typeNameIndex);

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(cursor.getLong(dateIndex));
                final Date date = calendar.getTime();

                final boolean isRepeat = cursor.getInt(isRepeatIndex) > 0;
                final boolean isExpenseTransaction = cursor.getInt(isExpenseTransactionIndex) > 0;

                Transaction transaction = new Transaction(date, value, name, typeName, isExpenseTransaction);
                transaction.setTransactionId(id);

                transactions.add(transaction);

            } while (cursor.moveToNext());

            searchTransactionAdapter.submitList(transactions);
        } finally {
            // Don't forget to close the Cursor once you are done to avoid memory leaks.
            // Using a try/finally like in this example is usually the best way to handle this
            cursor.close();
            // close the database
            //database.close();
        }
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

            Log.d("query search", query);
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
                    showSnackbar(transaction);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
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
}