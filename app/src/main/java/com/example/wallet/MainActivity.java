package com.example.wallet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wallet.db.MonthlyBudgetViewModel;
import com.example.wallet.db.Transaction;
import com.example.wallet.db.TransactionViewModel;
import com.example.wallet.db.TypeViewModel;
import com.example.wallet.helper.DateFormatter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.Date;
import java.util.List;


// TODO: optimise total amount (transaction adapter), refresh amount when transaction is deleted or saved (today)
// TODO: fix bug when try to change month (today)
// TODO: implement settings (reset, app theme)
// TODO: throw possible exceptions
// TODO: try to use functional approach (return, dont set)
// TODO: change string to string resource
// TODO: (end) comply with google's standards
// TODO:
/*
TODO: import database
- can get uri from clicking a file -> which gets copies file into files folder
- can createfromassets successfully
- but cannot create from file (maybe something to do with the path)
 */

public class MainActivity extends AppCompatActivity {

    public static final int ADD_TRANSACTION_ACTIVITY_REQUEST_CODE = 1;
    public static final int EDIT_TRANSACTION_ACTIVITY_REQUEST_CODE = 2;

    private TypeViewModel typeViewModel;
    private TransactionViewModel transactionViewModel;
    private MonthlyBudgetViewModel monthlyBudgetViewModel;

    private TransactionAdapter transactionAdapter;

    private CoordinatorLayout coordinatorLayout;

    private Calendar startMonthCal;
    private Calendar endMonthCal;

    private ImageButton prevMonth;
    private ImageButton nextMonth;

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

        //showHeader();
    }

    private void showHeader() {

        CardView constraintLayout = findViewById(R.id.recyclerview_card_view);
        TextView textView = new TextView(this);
        textView.setText("header");
        CardView.LayoutParams params = new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT,
                CardView.LayoutParams.MATCH_PARENT
        );
        textView.setLayoutParams(params);
        Log.d("textview", (String) textView.getText());
        constraintLayout.addView(textView);

//        ConstraintLayout constraintLayout = findViewById(R.id.content_main_layout);
//        TextView textView = new TextView(this);
//        textView.setText("header");
//        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
//                ConstraintLayout.LayoutParams.MATCH_PARENT,
//                ConstraintLayout.LayoutParams.MATCH_PARENT
//        );
//        textView.setLayoutParams(params);
//        constraintLayout.addView(textView);
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
                startActivityForResult(goToAddEditTransactionActivity, EDIT_TRANSACTION_ACTIVITY_REQUEST_CODE);
            }
        });

//        // Get a new or existing ViewModel from the ViewModelProvider.
//        typeViewModel = ViewModelProviders.of(this).get(TypeViewModel.class);
//
//        // Add an observer on the LiveData returned by getAllTypes.
//        // The onChanged() method fires when the observed data changes and the activity is
//        // in the foreground.
//        typeViewModel.getAllTypes().observe(this, new Observer<List<Type>>() {
//            @Override
//            public void onChanged(@Nullable final List<Type> types) {
//                // Update the cached copy of the words in the adapter.
//                //adapter.setWords(words);
//            }
//        });
//
//        monthlyBudgetViewModel = ViewModelProviders.of(this).get(MonthlyBudgetViewModel.class);
//
//        monthlyBudgetViewModel.getAllMonthlyBudgets().observe(this, new Observer<List<MonthlyBudget>>() {
//            @Override
//            public void onChanged(@Nullable final List<MonthlyBudget> monthlyBudgets) {
//                // Update the cached copy of the words in the adapter.
//                //adapter.setWords(words);
//            }
//        });
    }

    private void updateTransactionsInAMonth() {

        transactionViewModel.getAllTransactionsInAMonth(startMonthCal.getTimeInMillis(), endMonthCal.getTimeInMillis()).observe(this, new Observer<List<Transaction>>() {
            @Override
            public void onChanged(@Nullable final List<Transaction> transactions) {
                // Update the cached copy of the words in the transactionAdapter.
                transactionAdapter.submitList(transactions);
                transactionAdapter.showHeader(transactions);
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
                    Toast.makeText(this, "Transaction updated", Toast.LENGTH_SHORT).show();

                    // delete transaction
                } else {
                    transactionViewModel.deleteTransaction(transaction);
                    showSnackbar(transaction);
                }
            }
        }
    }

    private void showSnackbar(Transaction transaction) {

        Snackbar snackbar = Snackbar.make(coordinatorLayout, "Transaction deleted", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // insert back
                        transactionViewModel.insertTransaction(transaction);

                        Snackbar snackbar1 = Snackbar.make(coordinatorLayout, "Undo successful", Snackbar.LENGTH_SHORT);
                        snackbar1.show();
                    }
                });

        snackbar.show();
    }

    private Transaction extractDataToTransaction(Intent data, Long id) {

        String name = data.getStringExtra(AddEditTransactionActivity.EXTRA_NAME);
        String typeName = data.getStringExtra(AddEditTransactionActivity.EXTRA_TYPENAME);
        double value = data.getDoubleExtra(AddEditTransactionActivity.EXTRA_VALUE, 1);

        String dateString = data.getStringExtra(AddEditTransactionActivity.EXTRA_DATE);
        Date date = DateFormatter.formatStringToDate(dateString);

        Transaction transaction = new Transaction(date, value, name, typeName);

        if (id != 0) {
            transaction.setTransactionId(id);
        }
        return transaction;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
