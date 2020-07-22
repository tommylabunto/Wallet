package com.xingtingkai.wallet;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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
    private TextInputEditText editTextBudget;

    private TextInputLayout textInputLayoutBudget;

    private static int month;

    // cannot add or delete, only can save
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_monthly_budget);

        textViewYear = findViewById(R.id.textView_year);
        textViewMonth = findViewById(R.id.textView_month);
        editTextBudget = findViewById(R.id.edit_text_budget);

        // bring focus to edit text and show keybaord
        if (editTextBudget.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        /*
         submit form when clicked 'enter' on soft keyboard
        */
        editTextBudget.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                if (!violateInputValidation()) {
                    createAlertDialog().show();
                }
                handled = true;
            }
            return handled;
        });

        textInputLayoutBudget = findViewById(R.id.edit_text_budget_input_layout);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close);
        }

        extractIntent();
    }

    private void extractIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_ID)) {
            String year = getString(R.string.single_string_param, intent.getIntExtra(EXTRA_YEAR, 0) + "");
            textViewYear.setText(year);

            month = intent.getIntExtra(EXTRA_MONTH, 1);
            Month tempMonth = Month.of(month);

            // originally is all caps
            String monthString = tempMonth.name().substring(0, 1) + tempMonth.name().substring(1, 3).toLowerCase();

            textViewMonth.setText(monthString);
            String budget = getString(R.string.single_string_param, intent.getIntExtra(EXTRA_BUDGET, 0) + "");
            editTextBudget.setText(budget);

            Editable budgetEditable = editTextBudget.getText();

            /*
             place cursor on the right side
             only for the first edit text
            */
            if (budgetEditable != null && budgetEditable.length() > 0) {
                editTextBudget.setSelection(budgetEditable.length());
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_item_menu, menu);

        // not allowed to delete monthly budget
        MenuItem delete = menu.findItem(R.id.delete_item);
        delete.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_item:
                if (!violateInputValidation()) {
                    createAlertDialog().show();
                }
                return true;
            case R.id.delete_item:
                // not allowed to delete monthly budget
//                Intent intent = extractInputToIntent("delete");
//
//                setResult(RESULT_OK, intent);
//                finish();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean violateInputValidation() {

        int budget = 0;
        boolean violate = false;

        Editable budgetEditable = editTextBudget.getText();
        String budgetString = "";

        if (budgetEditable != null) {
            budgetString = budgetEditable.toString().trim();
        }

        if (!budgetString.isEmpty()) {
            budget = Integer.parseInt(budgetString);
        }

        if (budget < 0) {
            textInputLayoutBudget.setError("Please enter an amount.");
            violate = true;
        } else {
            textInputLayoutBudget.setError("");
        }

        return violate;
    }

    private AlertDialog createAlertDialog() {

        String[] choices = {
                "Update this month only",
                "Update subsequent months",
                "Update all months"
        };

        return new AlertDialog.Builder(this)
                .setTitle("Update")
                // on click
                .setSingleChoiceItems(choices, 0, (DialogInterface dialog, int which) -> {
                })
                // on click
                .setPositiveButton("yes", (DialogInterface dialog, int which) -> {
                    int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();

                    Intent intent;

                    if (selectedPosition == 0) {
                        intent = extractInputToIntent("save");
                    } else if (selectedPosition == 1) {
                        intent = extractInputToIntent("save next");
                    } else {
                        intent = extractInputToIntent("save all");
                    }

                    setResult(RESULT_OK, intent);
                    finish();
                })
                // on click
                .setNegativeButton("no", (DialogInterface dialog, int which) -> {
                })
                .create();
    }

    private Intent extractInputToIntent(String operation) {

        int year = 0;
        int budget = 0;

        String yearString = textViewYear.getText().toString().trim();
        Editable budgetEditable = editTextBudget.getText();
        String budgetString = "";

        if (budgetEditable != null) {
            budgetString = budgetEditable.toString().trim();
        }

        if (!yearString.isEmpty()) {
            year = Integer.parseInt(yearString);
        }

        if (!budgetString.isEmpty()) {
            budget = Integer.parseInt(budgetString);
        }

        // bypass check for delete
        if (operation.equals("delete") && budget < 0) {
            budget = 0;
        }

        return createIntent(budget, year, month, operation);
    }

    private Intent createIntent(int budget, int year, int month, String operation) {

        Intent intent = new Intent();
        intent.putExtra(EXTRA_BUDGET, budget);
        intent.putExtra(EXTRA_YEAR, year);
        intent.putExtra(EXTRA_MONTH, month);
        intent.putExtra(EXTRA_OPERATION, operation);

        long id = getIntent().getLongExtra(EXTRA_ID, -1);
        if (id != -1) {
            intent.putExtra(EXTRA_ID, id);
        }
        return intent;
    }
}