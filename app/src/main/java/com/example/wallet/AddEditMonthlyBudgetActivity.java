package com.example.wallet;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.time.Month;

public class AddEditMonthlyBudgetActivity extends AppCompatActivity {

    protected static final String EXTRA_ID =
            "com.example.wallet.EXTRA_ID";
    protected static final String EXTRA_OPERATION =
            "com.example.wallet.EXTRA_OPERATION";
    protected static final String EXTRA_BUDGET =
            "com.example.wallet.EXTRA_BUDGET";
    protected static final String EXTRA_YEAR =
            "com.example.wallet.EXTRA_YEAR";
    protected static final String EXTRA_MONTH =
            "com.example.wallet.EXTRA_MONTH";

    private TextView textViewYear;
    private TextView textViewMonth;
    private EditText editTextBudget;

    private static int month;

    // cannot add or delete, only can save
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_monthly_budget);

        textViewYear = findViewById(R.id.textView_month);
        textViewMonth = findViewById(R.id.textView_month);
        editTextBudget = findViewById(R.id.edit_text_budget);

        // bring focus to edit text and show keybaord
        if (editTextBudget.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        // submit form when clicked 'enter' on soft keyboard
        editTextBudget.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    showAlertDialog();
                    handled = true;
                }
                return handled;
            }
        });

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

        extractIntent();
    }

    private void extractIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_ID)) {
            textViewYear.setText(intent.getIntExtra(EXTRA_YEAR, 0) + "");

            month = intent.getIntExtra(EXTRA_MONTH, 0);

            // month uses 1 (jan) to 12 (dec)
            Month tempMonth = Month.of(month + 1);

            // originally is all caps
            String monthString = tempMonth.name().substring(0,1) + tempMonth.name().substring(1,3).toLowerCase();

            textViewMonth.setText(monthString);
            editTextBudget.setText(intent.getIntExtra(EXTRA_BUDGET, 0) + "");
        }
    }

    private void createOrSaveMonthlyBudget(String operation) {

        int year = 0;
        int budget = 0;

        String yearString = textViewYear.getText().toString().trim();
        String budgetString = editTextBudget.getText().toString().trim();

        if (!yearString.isEmpty()) {
            year = Integer.parseInt(yearString);
        }

        if (!budgetString.isEmpty()) {
            budget = Integer.parseInt(budgetString);
        } else {
            Toast.makeText(this, "Please insert a value", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent newMonthlyBudget = createIntent(budget, year, month, operation);

        setResult(RESULT_OK, newMonthlyBudget);
        finish();
    }

    /*
    if delete a type that a transaction uses, it won't crash.
    the spinner will just set the type to the top option as default
     */
    private void deleteMonthlyBudget() {

        int year = 0;
        int budget = 0;

        String yearString = textViewYear.getText().toString().trim();
        String budgetString = editTextBudget.getText().toString().trim();

        if (!yearString.isEmpty()) {
            year = Integer.parseInt(yearString);
        }

        if (!budgetString.isEmpty()) {
            budget = Integer.parseInt(budgetString);
        } else {
            Toast.makeText(this, "Please insert a value", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent oldMonthlyBudget = createIntent(budget, year, month, "delete");

        setResult(RESULT_OK, oldMonthlyBudget);
        finish();
    }

    private Intent createIntent(int budget, int year, int month, String operation) {

        Intent monthlyBudget = new Intent();
        monthlyBudget.putExtra(EXTRA_BUDGET, budget);
        monthlyBudget.putExtra(EXTRA_YEAR, year);
        monthlyBudget.putExtra(EXTRA_MONTH, month);
        monthlyBudget.putExtra(EXTRA_OPERATION, operation);

        Long id = getIntent().getLongExtra(EXTRA_ID, -1);
        if (id != -1) {
            monthlyBudget.putExtra(EXTRA_ID, id);
        }

        return monthlyBudget;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_item_menu, menu);

        MenuItem delete = menu.findItem(R.id.delete_item);
        delete.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_item:
                showAlertDialog();
                return true;
            case R.id.delete_item:
                deleteMonthlyBudget();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showAlertDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Update")
                .setMessage("Do you want to set this amount as a default budget?")
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createOrSaveMonthlyBudget("save all");
                    }
                })
                .setNegativeButton("no", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createOrSaveMonthlyBudget("save");
                    }
                })
                .setNeutralButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create().show();
    }
}