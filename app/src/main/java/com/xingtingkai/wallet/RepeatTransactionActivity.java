package com.xingtingkai.wallet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.collect.ImmutableList;
import com.xingtingkai.wallet.adapter.RepeatTransactionAdapter;
import com.xingtingkai.wallet.db.WalletDatabase;
import com.xingtingkai.wallet.db.entity.Transaction;
import com.xingtingkai.wallet.db.viewmodel.TransactionViewModel;
import com.xingtingkai.wallet.db.viewmodel.TypeViewModel;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class RepeatTransactionActivity extends AppCompatActivity {

    protected static final int ADD_TRANSACTION_ACTIVITY_REQUEST_CODE = 1;
    protected static final int EDIT_TRANSACTION_ACTIVITY_REQUEST_CODE = 2;

    private TransactionViewModel transactionViewModel;

    private RepeatTransactionAdapter repeatExpenseTransactionAdapter;
    private RepeatTransactionAdapter repeatIncomeTransactionAdapter;

    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repeat_transaction);

        // create transaction
        FloatingActionButton fab = findViewById(R.id.repeat_fab);
        // on click
        fab.setOnClickListener((View view) -> {
            Intent goToAddEditRepeatTransaction = new Intent(RepeatTransactionActivity.this, AddEditRepeatTransactionActivity.class);
            startActivityForResult(goToAddEditRepeatTransaction, ADD_TRANSACTION_ACTIVITY_REQUEST_CODE);
        });

        // show transaction in recycler view
        RecyclerView recyclerViewExpense = findViewById(R.id.recyclerview_expense_type);
        repeatExpenseTransactionAdapter = new RepeatTransactionAdapter(this);
        recyclerViewExpense.setHasFixedSize(true);
        recyclerViewExpense.setAdapter(repeatExpenseTransactionAdapter);
        recyclerViewExpense.setLayoutManager(new LinearLayoutManager(this));

        // show transaction in recycler view
        RecyclerView recyclerViewIncome = findViewById(R.id.recyclerview_income_type);
        repeatIncomeTransactionAdapter = new RepeatTransactionAdapter(this);
        recyclerViewIncome.setHasFixedSize(true);
        recyclerViewIncome.setAdapter(repeatIncomeTransactionAdapter);
        recyclerViewIncome.setLayoutManager(new LinearLayoutManager(this));

        initViewModels();

        coordinatorLayout = findViewById(R.id.repeatTransactionActivity);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initViewModels() {

        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
//        transactionViewModel = ViewModelProviders.of(this).get(TransactionViewModel.class);

        long todayEpochSeconds = ZonedDateTime.now().toEpochSecond();

        // on changed
        transactionViewModel.getExpenseRecurringTransactions(todayEpochSeconds).observe(this,
                (@Nullable final List<Transaction> transactions) -> {
            // Update the cached copy of the words in the transactionAdapter.
            // list received is not distinct by recurring id
            List<Transaction> distinctTransactions = deepCopyDistinctTransaction(transactions);
            repeatExpenseTransactionAdapter.submitList(distinctTransactions);
        });

        // when click on item in recycler view -> populate data and open up to edit
        // on item click
        repeatExpenseTransactionAdapter.setOnItemClickListener((Transaction transaction) -> {
            Intent goToAddEditRepeatTransaction = new Intent(
                    RepeatTransactionActivity.this, AddEditRepeatTransactionActivity.class);
            goToAddEditRepeatTransaction.putExtra(AddEditRepeatTransactionActivity.EXTRA_ID,
                    transaction.getTransactionId());
            goToAddEditRepeatTransaction.putExtra(AddEditRepeatTransactionActivity.EXTRA_NAME,
                    transaction.getName());
            goToAddEditRepeatTransaction.putExtra(AddEditRepeatTransactionActivity.EXTRA_TYPENAME,
                    transaction.getTypeName());
//            goToAddEditRepeatTransaction.putExtra(AddEditRepeatTransactionActivity.EXTRA_INSTANT,
//                    DateFormatter.formatInstantToString(transaction.getInstant(), transaction.getZoneId()));
            goToAddEditRepeatTransaction.putExtra(AddEditRepeatTransactionActivity.EXTRA_INSTANT,
                    transaction.getInstant().getEpochSecond());

            goToAddEditRepeatTransaction.putExtra(AddEditRepeatTransactionActivity.EXTRA_ZONE_ID,
                    transaction.getZoneId().getId());
            goToAddEditRepeatTransaction.putExtra(AddEditRepeatTransactionActivity.EXTRA_VALUE,
                    transaction.getValue());
            goToAddEditRepeatTransaction.putExtra(AddEditRepeatTransactionActivity.EXTRA_FREQUENCY,
                    transaction.getFrequency());
            goToAddEditRepeatTransaction.putExtra(AddEditRepeatTransactionActivity.EXTRA_REPEAT,
                    transaction.getNumOfRepeat());
            goToAddEditRepeatTransaction.putExtra(AddEditRepeatTransactionActivity.EXTRA_IS_EXPENSE_TYPE,
                    transaction.isExpenseTransaction());
            goToAddEditRepeatTransaction.putExtra(AddEditRepeatTransactionActivity.EXTRA_RECURRING_ID,
                    transaction.getTransactionRecurringId());
            startActivityForResult(goToAddEditRepeatTransaction, EDIT_TRANSACTION_ACTIVITY_REQUEST_CODE);
        });

        // on changed
        transactionViewModel.getIncomeRecurringTransactions(todayEpochSeconds).observe(this,
                (@Nullable final List<Transaction> transactions) -> {
            // Update the cached copy of the words in the transactionAdapter.
            // list received is not distinct by recurring id
            ImmutableList<Transaction> distinctTransactions = deepCopyDistinctTransaction(transactions);
            repeatIncomeTransactionAdapter.submitList(distinctTransactions);
        });

        // when click on item in recycler view -> populate data and open up to edit
        // on item click
        repeatIncomeTransactionAdapter.setOnItemClickListener((Transaction transaction) -> {
            Intent goToAddEditRepeatTransaction = new Intent(
                    RepeatTransactionActivity.this, AddEditRepeatTransactionActivity.class);
            goToAddEditRepeatTransaction.putExtra(AddEditRepeatTransactionActivity.EXTRA_ID,
                    transaction.getTransactionId());
            goToAddEditRepeatTransaction.putExtra(AddEditRepeatTransactionActivity.EXTRA_NAME,
                    transaction.getName());
            goToAddEditRepeatTransaction.putExtra(AddEditRepeatTransactionActivity.EXTRA_TYPENAME,
                    transaction.getTypeName());
            goToAddEditRepeatTransaction.putExtra(AddEditRepeatTransactionActivity.EXTRA_INSTANT,
                    transaction.getInstant().getEpochSecond());
            goToAddEditRepeatTransaction.putExtra(AddEditRepeatTransactionActivity.EXTRA_ZONE_ID,
                    transaction.getZoneId().getId());
            goToAddEditRepeatTransaction.putExtra(AddEditRepeatTransactionActivity.EXTRA_VALUE,
                    transaction.getValue());
            goToAddEditRepeatTransaction.putExtra(AddEditRepeatTransactionActivity.EXTRA_FREQUENCY,
                    transaction.getFrequency());
            goToAddEditRepeatTransaction.putExtra(AddEditRepeatTransactionActivity.EXTRA_REPEAT,
                    transaction.getNumOfRepeat());
            goToAddEditRepeatTransaction.putExtra(AddEditRepeatTransactionActivity.EXTRA_IS_EXPENSE_TYPE,
                    transaction.isExpenseTransaction());
            goToAddEditRepeatTransaction.putExtra(AddEditRepeatTransactionActivity.EXTRA_RECURRING_ID,
                    transaction.getTransactionRecurringId());
            startActivityForResult(goToAddEditRepeatTransaction, EDIT_TRANSACTION_ACTIVITY_REQUEST_CODE);
        });

        TypeViewModel typeViewModel = new ViewModelProvider(this).get(TypeViewModel.class);

//        TypeViewModel typeViewModel = ViewModelProviders.of(this).get(TypeViewModel.class);

        // on changed
//        typeViewModel.getAllTypesString().observe(this, (@Nullable final List<String> types) -> {
//            if (types != null && types.size() == 0) {
//                WalletDatabase.addTypes();
//            }
//        });

        Future<List<String>> typesFuture =typeViewModel.getAllTypesStringTemp();

        try {
            List<String> types = typesFuture.get();

            if (types != null && types.size() == 0) {
                WalletDatabase.addTypes();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private ImmutableList<Transaction> deepCopyDistinctTransaction(@Nullable List<Transaction> transactions) {

        ImmutableList.Builder<Transaction> tempDistinctTransactions = new ImmutableList.Builder<>();

        String recurringId = "";

        int arraySize = 0;

        if (transactions != null) {
            arraySize = transactions.size();
        }

        for (int i = 0; i < arraySize; i++) {

            Transaction transaction = transactions.get(i);

            if (recurringId.isEmpty()) {
                recurringId = transaction.getTransactionRecurringId();
                tempDistinctTransactions.add(transaction);
            }

            if (!recurringId.equals(transaction.getTransactionRecurringId())) {
                recurringId = transaction.getTransactionRecurringId();
                tempDistinctTransactions.add(transaction);
            }
        }

        return tempDistinctTransactions.build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == ADD_TRANSACTION_ACTIVITY_REQUEST_CODE) {

                String operation = data.getStringExtra(AddEditRepeatTransactionActivity.EXTRA_OPERATION);

                // create transaction
                if (operation != null && operation.equals("save")) {
                    Transaction transaction = extractDataToTransaction(data, 0L);
                    addRecurringTransactions(transaction);
                }
            } else {

                long id = data.getLongExtra(AddEditRepeatTransactionActivity.EXTRA_ID, -1);
                if (id == -1) {
                    showSnackbar("transaction cannot be updated");
                }

                Transaction transaction = extractDataToTransaction(data, id);

                String operation = data.getStringExtra(AddEditRepeatTransactionActivity.EXTRA_OPERATION);

                if (operation != null) {
                    if (operation.equals("save")) {
                        updateRecurringTransactions(transaction);
                    } else {
                        deleteRecurringTransactions(transaction);
                    }
                }
            }
        }
    }

    private void showSnackbar(String message) {

        Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    // doesn't update the list of recurring transactions, but delete and create a new list
    private void updateRecurringTransactions(Transaction transaction) {

        deleteRecurringTransactions(transaction);

        addRecurringTransactions(transaction);
    }

    private void deleteRecurringTransactions(Transaction transaction) {

        long transactionEpochSecond = transaction.getInstant().getEpochSecond();

        transactionViewModel.deleteFutureRecurringTransactions(
                transaction.getTransactionRecurringId(), transactionEpochSecond);

        transactionViewModel.deleteTransaction(transaction);
    }

    private void addRecurringTransactions(Transaction transaction) {

        int numOfTransactions = transaction.getFrequency() * transaction.getNumOfRepeat();

        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(transaction.getInstant(), transaction.getZoneId());

        int count = 0;
        int repeat = transaction.getNumOfRepeat();
        int frequency = transaction.getFrequency();

        final String recurringId = UUID.randomUUID().toString();

        Transaction newTransaction;

        /*
        e.g. frequency = 2 (biannually), numOfRepeat = 2;
        1 Jun 2020 , numOfRepeat (2)
        1 Dec 2020 , numOfRepeat (2)
        1 Jun 2021 , numOfRepeat (1)
        1 Dec 2021 , numOfRepeat (1)
        1 Jun 2022 , numOfRepeat (0)

        e.g. frequency = 1 (annually), numOfRepeat = 1;
        1 Jun 2020 , numOfRepeat (1)
        1 Jun 2021 , numOfRepeat (0)
         */
        for (int i = 0; i <= numOfTransactions; i++) {

            // deep copy so that it wont reference the same object and change date for all transactions
            // newTransaction = deepCopyTransaction(transaction, recurringId, calendar.getTime(), repeat);
            newTransaction = Transaction.createRecurringTransaction(
                    0L,
                    recurringId,
                    zonedDateTime.toInstant(),
                    transaction.getZoneId(),
                    transaction.getValue(),
                    transaction.getName(),
                    transaction.getTypeName(),
                    transaction.getFrequency(),
                    repeat,
                    transaction.isExpenseTransaction());

            transactionViewModel.insertTransaction(newTransaction);

            // change date for next transaction
            zonedDateTime = zonedDateTime.plusMonths(12 / transaction.getFrequency());

            // update repeat for current transaction
            count++;
            repeat = countRemainingRepeat(count, repeat, frequency);

        }
    }

    private int countRemainingRepeat(int count, int repeat, int frequency) {

        // minus 1 year
        if (count % frequency == 0) {
            return repeat - 1;
        } else {
            return repeat;
        }
    }

    private Transaction extractDataToTransaction(Intent data, long id) {

        String name = data.getStringExtra(AddEditRepeatTransactionActivity.EXTRA_NAME);

        if (name == null) {
            name = "";
        }

        String typeName = data.getStringExtra(AddEditRepeatTransactionActivity.EXTRA_TYPENAME);

        if (typeName == null) {
            typeName = "";
        }

        double value = data.getDoubleExtra(AddEditRepeatTransactionActivity.EXTRA_VALUE, 1);

        String zoneIdString = data.getStringExtra(AddEditRepeatTransactionActivity.EXTRA_ZONE_ID);
        ZoneId zoneId = ZoneId.of(zoneIdString);

        long instantLong = data.getLongExtra(AddEditRepeatTransactionActivity.EXTRA_INSTANT, 0L);
        Instant instant = Instant.ofEpochSecond(instantLong);

        int frequency = data.getIntExtra(AddEditRepeatTransactionActivity.EXTRA_FREQUENCY, 12);
        int numOfRepeat = data.getIntExtra(AddEditRepeatTransactionActivity.EXTRA_REPEAT, 1);

        String recurringId = data.getStringExtra(AddEditRepeatTransactionActivity.EXTRA_RECURRING_ID);

        if (recurringId == null) {
            recurringId = "";
        }

        boolean isExpenseType = data.getBooleanExtra(AddEditTransactionActivity.EXTRA_IS_EXPENSE_TYPE, true);

        return Transaction.createRecurringTransaction(id, recurringId, instant, zoneId, value, name, typeName, frequency, numOfRepeat, isExpenseType);
    }
}
