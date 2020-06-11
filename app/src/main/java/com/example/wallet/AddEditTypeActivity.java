package com.example.wallet;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddEditTypeActivity extends AppCompatActivity {

    protected static final String EXTRA_ID =
            "com.example.wallet.EXTRA_ID";
    protected static final String EXTRA_NAME =
            "com.example.wallet.EXTRA_NAME";
    protected static final String EXTRA_IS_EXPENSE_TYPE =
            "com.example.wallet.EXTRA_IS_EXPENSE_TYPE";
    protected static final String EXTRA_OPERATION =
            "com.example.wallet.EXTRA_OPERATION";

    private EditText editTextType;
    private RadioButton radioButtonExpense;
    private RadioButton radioButtonIncome;

    private boolean isExpenseType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_type);

        editTextType = findViewById(R.id.edit_text_type);
        radioButtonExpense = findViewById(R.id.radio_expense);
        radioButtonIncome = findViewById(R.id.radio_income);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

        extractIntent();
    }

    private void extractIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_ID)) {
            editTextType.setText(intent.getStringExtra(EXTRA_NAME));

            if (intent.getBooleanExtra(EXTRA_IS_EXPENSE_TYPE, true)) {
                radioButtonExpense.setChecked(true);
                isExpenseType = true;
            } else {
                radioButtonIncome.setChecked(true);
                isExpenseType = false;
            }
            // set as default option
        } else {
            radioButtonExpense.setChecked(true);
            isExpenseType = true;
        }
    }

    private void createOrSaveType() {

        String name = editTextType.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please insert a name", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent newType = createIntent(name, "save");

        setResult(RESULT_OK, newType);
        finish();
    }

    /*
    if delete a type that a transaction uses, it won't crash.
    the spinner will just set the type to the top option as default
     */
    private void deleteType() {

        String name = editTextType.getText().toString().trim();

        Intent oldType = createIntent(name, "delete");

        setResult(RESULT_OK, oldType);
        finish();
    }

    private Intent createIntent(String name, String operation) {

        Intent type = new Intent();
        type.putExtra(EXTRA_NAME, name);
        type.putExtra(EXTRA_OPERATION, operation);

        type.putExtra(EXTRA_IS_EXPENSE_TYPE, isExpenseType);

        Long id = getIntent().getLongExtra(EXTRA_ID, -1);
        if (id != -1) {
            type.putExtra(EXTRA_ID, id);
        }

        return type;
    }

    public void onRadioButtonClicked(View view) {

        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_expense:
                if (checked)
                    isExpenseType = true;
                    break;
            case R.id.radio_income:
                if (checked)
                    isExpenseType = false;
                    break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_transaction_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_transaction:
                createOrSaveType();
                return true;
            case R.id.delete_transaction:
                deleteType();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}