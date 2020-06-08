package com.example.wallet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wallet.adapter.RepeatTransactionAdapter;
import com.example.wallet.db.Transaction;
import com.example.wallet.db.TransactionViewModel;
import com.example.wallet.db.TypeViewModel;
import com.example.wallet.db.WalletDatabase;
import com.example.wallet.helper.DateFormatter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class RepeatTransactionActivity extends AppCompatActivity {

    public static final int ADD_TRANSACTION_ACTIVITY_REQUEST_CODE = 1;
    public static final int EDIT_TRANSACTION_ACTIVITY_REQUEST_CODE = 2;

    private TransactionViewModel transactionViewModel;
    private TypeViewModel typeViewModel;

    private RepeatTransactionAdapter repeatTransactionAdapter;

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
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        repeatTransactionAdapter = new RepeatTransactionAdapter(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(repeatTransactionAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

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

        transactionViewModel.getAllRecurringTransactions(today.getTimeInMillis()).observe(this, new Observer<List<Transaction>>() {
            @Override
            public void onChanged(@Nullable final List<Transaction> transactions) {
                // Update the cached copy of the words in the transactionAdapter.
                // list received is not distinct by recurring id
                List<Transaction> distinctTransactions = deepCopyDistinctTransaction(transactions);
                repeatTransactionAdapter.submitList(distinctTransactions);
            }
        });

        // when click on item in recycler view -> populate data and open up to edit
        repeatTransactionAdapter.setOnItemClickListener(new RepeatTransactionAdapter.OnItemClickListener() {
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

                if (types.size() == 0) {
                    WalletDatabase.addTypes();
                }
            }
        });
    }

    private List<Transaction> deepCopyDistinctTransaction(List<Transaction> transactions) {

        List<Transaction> distinctTransactions = new ArrayList<>();

        String recurringId = "";

        for (int i = 0; i < transactions.size(); i++) {

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

                Long id = data.getLongExtra(AddEditRepeatTransactionActivity.EXTRA_ID, -1);
                if (id == -1) {
                    Toast.makeText(this, "Transaction can't be updated", Toast.LENGTH_SHORT).show();
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

    private void deleteRecurringTransactions(Transaction transaction) {

        transactionViewModel.deleteFutureRecurringTransactions(
                transaction.getTransactionRecurringId(),
                transaction.getDate().getTime());

        transactionViewModel.deleteTransaction(transaction);
    }

    // doesn't update the list of recurring transactions, but delete and create a new list
    private void updateRecurringTransactions(Transaction transaction) {

        deleteRecurringTransactions(transaction);

        addRecurringTransactions(transaction);

        Toast.makeText(this, "Transaction updated", Toast.LENGTH_SHORT).show();
    }

    private void addRecurringTransactions(Transaction transaction) {

        int numOfTransactions = transaction.getFrequency() * transaction.getNumOfRepeat();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(transaction.getDate());

        int count = 0;
        int repeat = transaction.getNumOfRepeat();
        int frequency = transaction.getFrequency();

        final String recurringId = UUID.randomUUID().toString();
        transaction.setTransactionRecurringId(recurringId);

        Transaction newTransaction = new Transaction();

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
            newTransaction = deepCopyTransaction(transaction);
            transactionViewModel.insertTransaction(newTransaction);

            // change date for next transaction
            calendar.add(Calendar.MONTH, 12 / transaction.getFrequency());
            transaction.setDate(calendar.getTime());

            // update repeat for current transaction
            count++;
            int remainingRepeat = countRemainingRepeat(count, repeat, frequency);

            // ignore first count for all frequencies except annually
            // so that remainingRepeat won't reduce 1 after first occurrence (since anything % 1 == 0)
            if (count == 1 && frequency != 1) {
                remainingRepeat = repeat;
            }

            // after every 1 year, update number of repeat left
            if (remainingRepeat != -1) {
                transaction.setNumOfRepeat(remainingRepeat);
                repeat = remainingRepeat;
            }
        }

        Toast.makeText(this, "Transaction saved", Toast.LENGTH_LONG).show();
    }

    private int countRemainingRepeat(int count, int repeat, int frequency) {

        // minus 1 year
        if (count % frequency == 0) {
            return repeat - 1;
        } else {
            return -1;
        }
    }

    private Transaction deepCopyTransaction(Transaction transaction) {

        Transaction newTransaction = new Transaction();
        newTransaction.setDate(transaction.getDate());
        newTransaction.setValue(transaction.getValue());
        newTransaction.setName(transaction.getName());
        newTransaction.setTypeName(transaction.getTypeName());
        newTransaction.setRepeat(transaction.isRepeat());
        newTransaction.setFrequency(transaction.getFrequency());
        newTransaction.setNumOfRepeat(transaction.getNumOfRepeat());
        newTransaction.setExpenseTransaction(transaction.isExpenseTransaction());
        newTransaction.setTransactionRecurringId(transaction.getTransactionRecurringId());

        return newTransaction;
    }

    private Transaction extractDataToTransaction(Intent data, Long id) {

        String name = data.getStringExtra(AddEditRepeatTransactionActivity.EXTRA_NAME);
        String typeName = data.getStringExtra(AddEditRepeatTransactionActivity.EXTRA_TYPENAME);
        double value = data.getDoubleExtra(AddEditRepeatTransactionActivity.EXTRA_VALUE, 1);

        String dateString = data.getStringExtra(AddEditRepeatTransactionActivity.EXTRA_DATE);
        Date date = DateFormatter.formatStringToDate(dateString);

        int frequency = data.getIntExtra(AddEditRepeatTransactionActivity.EXTRA_FREQUENCY, 12);
        int repeat = data.getIntExtra(AddEditRepeatTransactionActivity.EXTRA_REPEAT, 1);

        String recurringId = data.getStringExtra(AddEditRepeatTransactionActivity.EXTRA_RECURRING_ID);

        boolean isExpenseType = data.getBooleanExtra(AddEditTransactionActivity.EXTRA_IS_EXPENSE_TYPE, true);

        Transaction transaction = new Transaction(date, value, name, typeName, frequency, repeat, isExpenseType);

        if (id != 0) {
            transaction.setTransactionId(id);
        }

        transaction.setTransactionRecurringId(recurringId);

        return transaction;
    }
}
