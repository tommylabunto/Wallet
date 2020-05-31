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

import com.example.wallet.db.DateFormatter;
import com.example.wallet.db.Transaction;
import com.example.wallet.db.TransactionViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Date;
import java.util.List;

// TODO: change get all transactions to get recurring ones
public class RepeatTransactionActivity extends AppCompatActivity {

    public static final int ADD_TRANSACTION_ACTIVITY_REQUEST_CODE = 1;
    public static final int EDIT_TRANSACTION_ACTIVITY_REQUEST_CODE = 2;

    private TransactionViewModel transactionViewModel;

    private TransactionAdapter transactionAdapter;

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
                Intent intent = new Intent(RepeatTransactionActivity.this, AddEditRepeatTransactionActivity.class);
                startActivityForResult(intent, ADD_TRANSACTION_ACTIVITY_REQUEST_CODE);
            }
        });

        // show transaction in recycler view
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        transactionAdapter = new TransactionAdapter(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(transactionAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        initViewModels();

        coordinatorLayout = findViewById(R.id.repeatTransactionActivity);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initViewModels() {

        transactionViewModel = ViewModelProviders.of(this).get(TransactionViewModel.class);

        transactionViewModel.getAllTransactions().observe(this, new Observer<List<Transaction>>() {
            @Override
            public void onChanged(@Nullable final List<Transaction> transactions) {
                // Update the cached copy of the words in the transactionAdapter.
                transactionAdapter.submitList(transactions);
            }
        });

        // when click on item in recycler view -> populate data and open up to edit
        transactionAdapter.setOnItemClickListener(new TransactionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Transaction transaction) {
                Intent intent = new Intent(RepeatTransactionActivity.this, AddEditRepeatTransactionActivity.class);
                intent.putExtra(AddEditRepeatTransactionActivity.EXTRA_ID, transaction.getTransactionId());
                intent.putExtra(AddEditRepeatTransactionActivity.EXTRA_NAME, transaction.getName());
                intent.putExtra(AddEditRepeatTransactionActivity.EXTRA_TYPENAME, transaction.getTypeName());
                intent.putExtra(AddEditRepeatTransactionActivity.EXTRA_DATE, DateFormatter.formatDateToString(transaction.getDate()));
                intent.putExtra(AddEditRepeatTransactionActivity.EXTRA_VALUE, transaction.getValue());
                startActivityForResult(intent, EDIT_TRANSACTION_ACTIVITY_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == ADD_TRANSACTION_ACTIVITY_REQUEST_CODE) {

                // create transaction
                if (data.getStringExtra(AddEditRepeatTransactionActivity.EXTRA_OPERATION).equals("save")) {
                    Transaction transaction = extractDataToTransaction(data, 0L);
                    transactionViewModel.insertTransaction(transaction);
                    Toast.makeText(this, "Transaction saved", Toast.LENGTH_LONG).show();
                }
            } else {

                Long id = data.getLongExtra(AddEditRepeatTransactionActivity.EXTRA_ID, -1);
                if (id == -1) {
                    Toast.makeText(this, "Transaction can't be updated", Toast.LENGTH_SHORT).show();
                }

                Transaction transaction = extractDataToTransaction(data, id);

                // update transaction
                if (data.getStringExtra(AddEditRepeatTransactionActivity.EXTRA_OPERATION).equals("save")) {

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

        String name = data.getStringExtra(AddEditRepeatTransactionActivity.EXTRA_NAME);
        String typeName = data.getStringExtra(AddEditRepeatTransactionActivity.EXTRA_TYPENAME);
        double value = data.getDoubleExtra(AddEditRepeatTransactionActivity.EXTRA_VALUE, 1);

        String dateString = data.getStringExtra(AddEditRepeatTransactionActivity.EXTRA_DATE);
        Date date = DateFormatter.formatStringToDate(dateString);

        Transaction transaction = new Transaction(date, value, name, typeName);

        if (id != 0) {
            transaction.setTransactionId(id);
        }
        return transaction;
    }
}
