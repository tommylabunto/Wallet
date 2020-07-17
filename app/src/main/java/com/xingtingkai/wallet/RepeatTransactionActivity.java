package com.xingtingkai.wallet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.xingtingkai.wallet.adapter.RepeatTransactionAdapter;
import com.xingtingkai.wallet.db.WalletDatabase;
import com.xingtingkai.wallet.db.entity.Transaction;
import com.xingtingkai.wallet.db.viewmodel.TransactionViewModel;
import com.xingtingkai.wallet.db.viewmodel.TypeViewModel;
import com.xingtingkai.wallet.helper.DateFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class RepeatTransactionActivity extends AppCompatActivity {

    protected static final int ADD_TRANSACTION_ACTIVITY_REQUEST_CODE = 1;
    protected static final int EDIT_TRANSACTION_ACTIVITY_REQUEST_CODE = 2;

    private TransactionViewModel transactionViewModel;
    private TypeViewModel typeViewModel;

    private RepeatTransactionAdapter repeatExpenseTransactionAdapter;
    private RepeatTransactionAdapter repeatIncomeTransactionAdapter;

    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repeat_transaction);

        // create transaction
        FloatingActionButton fab = findViewById(R.id.repeat_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToAddEditRepeatTransactionActivity = new Intent(RepeatTransactionActivity.this, AddEditRepeatTransactionActivity.class);
                startActivityForResult(goToAddEditRepeatTransactionActivity, ADD_TRANSACTION_ACTIVITY_REQUEST_CODE);
            }
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

        Calendar today = Calendar.getInstance();

        transactionViewModel = ViewModelProviders.of(this).get(TransactionViewModel.class);

        transactionViewModel.getExpenseRecurringTransactions(today.getTimeInMillis()).observe(this, new Observer<List<Transaction>>() {
            @Override
            public void onChanged(@Nullable final List<Transaction> transactions) {
                // Update the cached copy of the words in the transactionAdapter.
                // list received is not distinct by recurring id
                List<Transaction> distinctTransactions = deepCopyDistinctTransaction(transactions);
                repeatExpenseTransactionAdapter.submitList(distinctTransactions);
            }
        });

        // when click on item in recycler view -> populate data and open up to edit
        repeatExpenseTransactionAdapter.setOnItemClickListener(new RepeatTransactionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Transaction transaction) {
                Intent goToAddEditRepeatTransactionActivity = new Intent(
                        RepeatTransactionActivity.this, AddEditRepeatTransactionActivity.class);
                goToAddEditRepeatTransactionActivity.putExtra(AddEditRepeatTransactionActivity.EXTRA_ID,
                        transaction.getTransactionId());
                goToAddEditRepeatTransactionActivity.putExtra(AddEditRepeatTransactionActivity.EXTRA_NAME,
                        transaction.getName());
                goToAddEditRepeatTransactionActivity.putExtra(AddEditRepeatTransactionActivity.EXTRA_TYPENAME,
                        transaction.getTypeName());
                goToAddEditRepeatTransactionActivity.putExtra(AddEditRepeatTransactionActivity.EXTRA_DATE,
                        DateFormatter.formatDateToString(transaction.getDate()));
                goToAddEditRepeatTransactionActivity.putExtra(AddEditRepeatTransactionActivity.EXTRA_VALUE,
                        transaction.getValue());
                goToAddEditRepeatTransactionActivity.putExtra(AddEditRepeatTransactionActivity.EXTRA_FREQUENCY,
                        transaction.getFrequency());
                goToAddEditRepeatTransactionActivity.putExtra(AddEditRepeatTransactionActivity.EXTRA_REPEAT,
                        transaction.getNumOfRepeat());
                goToAddEditRepeatTransactionActivity.putExtra(AddEditRepeatTransactionActivity.EXTRA_IS_EXPENSE_TYPE,
                        transaction.isExpenseTransaction());
                goToAddEditRepeatTransactionActivity.putExtra(AddEditRepeatTransactionActivity.EXTRA_RECURRING_ID,
                        transaction.getTransactionRecurringId());
                startActivityForResult(goToAddEditRepeatTransactionActivity, EDIT_TRANSACTION_ACTIVITY_REQUEST_CODE);
            }
        });

        transactionViewModel.getIncomeRecurringTransactions(today.getTimeInMillis()).observe(this, new Observer<List<Transaction>>() {
            @Override
            public void onChanged(@Nullable final List<Transaction> transactions) {
                // Update the cached copy of the words in the transactionAdapter.
                // list received is not distinct by recurring id
                List<Transaction> distinctTransactions = deepCopyDistinctTransaction(transactions);
                repeatIncomeTransactionAdapter.submitList(distinctTransactions);
            }
        });

        // when click on item in recycler view -> populate data and open up to edit
        repeatIncomeTransactionAdapter.setOnItemClickListener(new RepeatTransactionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Transaction transaction) {
                Intent goToAddEditRepeatTransactionActivity = new Intent(
                        RepeatTransactionActivity.this, AddEditRepeatTransactionActivity.class);
                goToAddEditRepeatTransactionActivity.putExtra(AddEditRepeatTransactionActivity.EXTRA_ID,
                        transaction.getTransactionId());
                goToAddEditRepeatTransactionActivity.putExtra(AddEditRepeatTransactionActivity.EXTRA_NAME,
                        transaction.getName());
                goToAddEditRepeatTransactionActivity.putExtra(AddEditRepeatTransactionActivity.EXTRA_TYPENAME,
                        transaction.getTypeName());
                goToAddEditRepeatTransactionActivity.putExtra(AddEditRepeatTransactionActivity.EXTRA_DATE,
                        DateFormatter.formatDateToString(transaction.getDate()));
                goToAddEditRepeatTransactionActivity.putExtra(AddEditRepeatTransactionActivity.EXTRA_VALUE,
                        transaction.getValue());
                goToAddEditRepeatTransactionActivity.putExtra(AddEditRepeatTransactionActivity.EXTRA_FREQUENCY,
                        transaction.getFrequency());
                goToAddEditRepeatTransactionActivity.putExtra(AddEditRepeatTransactionActivity.EXTRA_REPEAT,
                        transaction.getNumOfRepeat());
                goToAddEditRepeatTransactionActivity.putExtra(AddEditRepeatTransactionActivity.EXTRA_IS_EXPENSE_TYPE,
                        transaction.isExpenseTransaction());
                goToAddEditRepeatTransactionActivity.putExtra(AddEditRepeatTransactionActivity.EXTRA_RECURRING_ID,
                        transaction.getTransactionRecurringId());
                startActivityForResult(goToAddEditRepeatTransactionActivity, EDIT_TRANSACTION_ACTIVITY_REQUEST_CODE);
            }
        });

        typeViewModel = ViewModelProviders.of(this).get(TypeViewModel.class);

        typeViewModel.getAllTypesString().observe(this, new Observer<List<String>>() {
            @Override
            public void onChanged(@Nullable final List<String> types) {

                if (types != null && types.size() == 0) {
                    WalletDatabase.addTypes();
                }
            }
        });
    }

    private List<Transaction> deepCopyDistinctTransaction(List<Transaction> transactions) {

        List<Transaction> distinctTransactions = new ArrayList<>();

        String recurringId = "";

        int arraySize = transactions.size();

        for (int i = 0; i < arraySize; i++) {

            Transaction transaction = transactions.get(i);

            if (recurringId.isEmpty()) {
                recurringId = transaction.getTransactionRecurringId();
                distinctTransactions.add(transaction);
            }

            if (!recurringId.equals(transaction.getTransactionRecurringId())) {
                recurringId = transaction.getTransactionRecurringId();
                distinctTransactions.add(transaction);
            }
        }

        return distinctTransactions;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == ADD_TRANSACTION_ACTIVITY_REQUEST_CODE) {

                // create transaction
                if (data.getStringExtra(AddEditRepeatTransactionActivity.EXTRA_OPERATION).equals("save")) {

                    Transaction transaction = extractDataToTransaction(data, 0L);
                    addRecurringTransactions(transaction);
                }
            } else {

                long id = data.getLongExtra(AddEditRepeatTransactionActivity.EXTRA_ID, -1);
                if (id == -1) {
                    showSnackbar("transaction cannot be updated");
                }

                Transaction transaction = extractDataToTransaction(data, id);

                // update transaction
                if (data.getStringExtra(AddEditRepeatTransactionActivity.EXTRA_OPERATION).equals("save")) {
                    updateRecurringTransactions(transaction);

                    // delete transaction
                    // cannot recover deleted recurring transactions
                } else {
                    deleteRecurringTransactions(transaction);
                }
            }
        }
    }

    private void showSnackbar(String message) {

        Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void addRecurringTransactions(Transaction transaction) {

        int numOfTransactions = transaction.getFrequency() * transaction.getNumOfRepeat();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(transaction.getDate());

        int count = 0;
        int repeat = transaction.getNumOfRepeat();
        int frequency = transaction.getFrequency();
        int remainingRepeat = repeat;

        final String recurringId = UUID.randomUUID().toString();
        //transaction.setTransactionRecurringId(recurringId);

        Transaction newTransaction;
        //Transaction newTransaction = new Transaction();

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
            newTransaction = deepCopyTransaction(transaction, recurringId, calendar.getTime(), remainingRepeat);
            transactionViewModel.insertTransaction(newTransaction);

            // change date for next transaction
            calendar.add(Calendar.MONTH, 12 / transaction.getFrequency());
            //transaction.setDate(calendar.getTime());

            // update repeat for current transaction
            count++;
            remainingRepeat = countRemainingRepeat(count, repeat, frequency);

            // ignore first count for all frequencies except annually
            // so that remainingRepeat won't reduce 1 after first occurrence (since anything % 1 == 0)
            if (count == 1 && frequency != 1) {
                remainingRepeat = repeat;
            }

            // after every 1 year, update number of repeat left
            if (remainingRepeat != -1) {
                //transaction.setNumOfRepeat(remainingRepeat);
                repeat = remainingRepeat;
            }
        }
    }

    // doesn't update the list of recurring transactions, but delete and create a new list
    private void updateRecurringTransactions(Transaction transaction) {

        deleteRecurringTransactions(transaction);

        addRecurringTransactions(transaction);
    }

    private void deleteRecurringTransactions(Transaction transaction) {

        transactionViewModel.deleteFutureRecurringTransactions(
                transaction.getTransactionRecurringId(),
                transaction.getDate().getTime());

        transactionViewModel.deleteTransaction(transaction);
    }

    private int countRemainingRepeat(int count, int repeat, int frequency) {

        // minus 1 year
        if (count % frequency == 0) {
            return repeat - 1;
        } else {
            return -1;
        }
    }

    private Transaction deepCopyTransaction(Transaction transaction, String transactionRecurringId, Date date, int numOfRepeat) {

//        Transaction newTransaction = new Transaction();
//        newTransaction.setDate(transaction.getDate());
//        newTransaction.setValue(transaction.getValue());
//        newTransaction.setName(transaction.getName());
//        newTransaction.setTypeName(transaction.getTypeName());
//        newTransaction.setRepeat(transaction.isRepeat());
//        newTransaction.setFrequency(transaction.getFrequency());
//        newTransaction.setNumOfRepeat(transaction.getNumOfRepeat());
//        newTransaction.setExpenseTransaction(transaction.isExpenseTransaction());
//        newTransaction.setTransactionRecurringId(transaction.getTransactionRecurringId());

        return Transaction.createRecurringTransaction(
                0L,
                transactionRecurringId,
                date,
                transaction.getValue(),
                transaction.getName(),
                transaction.getTypeName(),
                transaction.getFrequency(),
                numOfRepeat,
                transaction.isExpenseTransaction());
    }

    private Transaction extractDataToTransaction(Intent data, long id) {

        String name = data.getStringExtra(AddEditRepeatTransactionActivity.EXTRA_NAME);
        String typeName = data.getStringExtra(AddEditRepeatTransactionActivity.EXTRA_TYPENAME);
        double value = data.getDoubleExtra(AddEditRepeatTransactionActivity.EXTRA_VALUE, 1);

        String dateString = data.getStringExtra(AddEditRepeatTransactionActivity.EXTRA_DATE);
        Date date = DateFormatter.formatStringToDate(dateString);

        int frequency = data.getIntExtra(AddEditRepeatTransactionActivity.EXTRA_FREQUENCY, 12);
        int numOfRepeat = data.getIntExtra(AddEditRepeatTransactionActivity.EXTRA_REPEAT, 1);

        String recurringId = data.getStringExtra(AddEditRepeatTransactionActivity.EXTRA_RECURRING_ID);

        boolean isExpenseType = data.getBooleanExtra(AddEditTransactionActivity.EXTRA_IS_EXPENSE_TYPE, true);

//        if (id != 0) {
//            transaction.setTransactionId(id);
//        }

        //transaction.setTransactionRecurringId(recurringId);

        return Transaction.createRecurringTransaction(id, recurringId, date, value, name, typeName, frequency, numOfRepeat, isExpenseType);
    }
}
