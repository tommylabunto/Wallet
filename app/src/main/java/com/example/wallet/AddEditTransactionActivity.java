package com.example.wallet;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.wallet.db.entity.Type;
import com.example.wallet.db.viewmodel.TransactionViewModel;
import com.example.wallet.db.viewmodel.TypeViewModel;
import com.example.wallet.helper.DateFormatter;
import com.example.wallet.helper.DatePickerFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class AddEditTransactionActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    protected static final String EXTRA_ID =
            "com.example.wallet.EXTRA_ID";
    protected static final String EXTRA_NAME =
            "com.example.wallet.EXTRA_NAME";
    protected static final String EXTRA_TYPENAME =
            "com.example.wallet.EXTRA_TYPENAME";
    protected static final String EXTRA_VALUE =
            "com.example.wallet.EXTRA_VALUE";
    protected static final String EXTRA_DATE =
            "com.example.wallet.EXTRA_DATE";
    protected static final String EXTRA_IS_EXPENSE_TYPE =
            "com.example.wallet.EXTRA_IS_EXPENSE_TYPE";
    protected static final String EXTRA_OPERATION =
            "com.example.wallet.EXTRA_OPERATION";

    private AutoCompleteTextView editTextName;
    private TextInputEditText editTextValue;
    private TextInputEditText editTextDate;

    private TextInputLayout textInputLayoutValue;
    private TextInputLayout textInputLayoutName;

    // in the format of "02/12/2020"
    private static String formattedDate;

    private Spinner spinner;
    private ArrayAdapter<String> adapter;
    private static String type;

    private static List<String> expenseTypeList;
    private static List<String> incomeTypeList;

    private TypeViewModel typeViewModel;
    private TransactionViewModel transactionViewModel;

    private RadioButton radioButtonExpense;
    private RadioButton radioButtonIncome;
    private static boolean isExpenseType;

    public static String[] nameSuggestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_transaction);

        radioButtonExpense = findViewById(R.id.radio_expense);
        radioButtonIncome = findViewById(R.id.radio_income);

        editTextName = (AutoCompleteTextView) findViewById(R.id.edit_text_name);
        editTextName.setThreshold(1);
        editTextValue = findViewById(R.id.edit_text_value);
        editTextDate = findViewById(R.id.edit_text_dateTime);

        textInputLayoutValue = findViewById(R.id.edit_text_num_value_input_layout);
        textInputLayoutName = findViewById(R.id.edit_text_num_name_input_layout);

        spinner = findViewById(R.id.spinner_type);

        isExpenseType = true;
        expenseTypeList = new ArrayList<>();
        incomeTypeList = new ArrayList<>();
        initViewModel();

        // bring focus to edit text and show keybaord
        if (editTextValue.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        // submit form when clicked 'enter' on soft keyboard
        editTextName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    createOrSaveTransaction();
                    handled = true;
                }
                return handled;
            }
        });

        // submit form when clicked 'enter' on soft keyboard
        editTextValue.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    createOrSaveTransaction();
                    handled = true;
                }
                return handled;
            }
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar calendar = formatSelectedDate();
                DialogFragment datePicker = new DatePickerFragment(calendar);
                datePicker.show(getSupportFragmentManager(), "date picker");
            }
        });

        extractIntent();
    }

    private void extractIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_ID)) {
            editTextName.setText(intent.getStringExtra(EXTRA_NAME));

            formattedDate = intent.getStringExtra(EXTRA_DATE);
            editTextDate.setText(DateFormatter.beautifyDateString(formattedDate));

            editTextValue.setText(intent.getDoubleExtra(EXTRA_VALUE, 1) + "");

            type = intent.getStringExtra(EXTRA_TYPENAME);

            if (intent.getBooleanExtra(EXTRA_IS_EXPENSE_TYPE, true)) {
                radioButtonExpense.setChecked(true);
                isExpenseType = true;
            } else {
                radioButtonIncome.setChecked(true);
                isExpenseType = false;
            }

            if (adapter == null) {
                initAdapterType();
            }

            int selectionPosition = adapter.getPosition(type);
            Log.d("selectionPosition", selectionPosition + "");
            spinner.setSelection(selectionPosition);
        } else {
            setTodayDate(editTextDate);
            radioButtonExpense.setChecked(true);
            isExpenseType = true;
        }
    }

    private void initViewModel() {

        transactionViewModel = ViewModelProviders.of(this).get(TransactionViewModel.class);

        transactionViewModel.getAllTransactionNameString().observe(this, new Observer<List<String>>() {
            @Override
            public void onChanged(@Nullable final List<String> nameSuggestions) {

                deepCopySuggestions(nameSuggestions);
                Log.d("onchanged", "onchanged");
            }
        });

        typeViewModel = ViewModelProviders.of(this).get(TypeViewModel.class);

        typeViewModel.getAllTypes().observe(this, new Observer<List<Type>>() {
            @Override
            public void onChanged(@Nullable final List<Type> types) {

                deepCopyList(types);
                Log.d("onchanged", "onchanged");
            }
        });
    }

    private void deepCopySuggestions(List<String> tempNameSuggestions) {

        nameSuggestions = new String[tempNameSuggestions.size()];

        for (int i = 0; i < tempNameSuggestions.size(); i++) {
            nameSuggestions[i] = tempNameSuggestions.get(i);
        }

        ArrayAdapter<String> nameSuggestionsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, nameSuggestions);
        editTextName.setAdapter(nameSuggestionsAdapter);
    }

    private void deepCopyList(List<Type> types) {

        expenseTypeList = new ArrayList<>();
        incomeTypeList = new ArrayList<>();

        for (Type type : types) {

            if (type.isExpenseType()) {
                expenseTypeList.add(type.getName());
            } else {
                incomeTypeList.add(type.getName());
            }
        }

        showSpinner();
        //extractIntent();
    }

    private void showSpinner() {
        initAdapterType();
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                type = parent.getItemAtPosition(position).toString();
                Log.d("type", type);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // it takes a while to copy types, so the list might be null
    private void initAdapterType() {

        if (isExpenseType) {
            adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, expenseTypeList);
        } else {
            adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, incomeTypeList);
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    private Calendar formatSelectedDate() {

        Calendar calendar = Calendar.getInstance();
        Date date = DateFormatter.formatStringToDate(formattedDate);
        calendar.setTime(date);

        return calendar;
    }

    /*
    when save a recurring transaction -> it becomes a non-recurring transaction because the data is not passed into the transaction
     */
    private void createOrSaveTransaction() {

        String name = editTextName.getText().toString().trim();
        String valueString = editTextValue.getText().toString().trim();
        double value = 0;

        boolean violateInputValidation = false;

        if (!valueString.isEmpty()) {
            value = Double.parseDouble(valueString);
        }

        if (name.isEmpty()) {
            textInputLayoutName.setError("Please enter a name.");
            violateInputValidation = true;
        } else {
            textInputLayoutName.setError("");
        }

        if (value == 0) {
            textInputLayoutValue.setError("Please enter an amount.");
            violateInputValidation = true;
        } else if (value < 0) {
            textInputLayoutValue.setError("Please enter an amount greater than 0.");
            violateInputValidation = true;
        } else {
            textInputLayoutValue.setError("");
        }

        if (violateInputValidation) {
            return;
        }

        Intent newTransaction = createIntent(formattedDate, value, name, type, "save");

        setResult(RESULT_OK, newTransaction);
        finish();
    }

    private void deleteTransaction() {

        String name = editTextName.getText().toString().trim();
        String valueString = editTextValue.getText().toString().trim();
        double value = 0;

        if (!valueString.isEmpty()) {
            value = Double.parseDouble(valueString);
        }

        if (value < 0) {
            value = 0;
        }

        Intent oldTransaction = createIntent(formattedDate, value, name, type, "delete");

        setResult(RESULT_OK, oldTransaction);
        finish();
    }

    private Intent createIntent(String dateString, double value, String name, String typeName, String operation) {

        Intent transaction = new Intent();
        transaction.putExtra(EXTRA_NAME, name);
        transaction.putExtra(EXTRA_TYPENAME, typeName);
        transaction.putExtra(EXTRA_DATE, dateString);
        transaction.putExtra(EXTRA_VALUE, value);
        transaction.putExtra(EXTRA_OPERATION, operation);

        transaction.putExtra(EXTRA_IS_EXPENSE_TYPE, isExpenseType);

        Long id = getIntent().getLongExtra(EXTRA_ID, -1);
        if (id != -1) {
            transaction.putExtra(EXTRA_ID, id);
        }

        return transaction;
    }

    public void onRadioButtonClicked(View view) {

        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radio_expense:
                if (checked) {
                    isExpenseType = true;
                    showSpinner();
                    break;
                }
            case R.id.radio_income:
                if (checked) {
                    isExpenseType = false;
                    showSpinner();
                    break;
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_item_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_item:
                createOrSaveTransaction();
                return true;
            case R.id.delete_item:
                deleteTransaction();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        formattedDate = DateFormatter.formatDateToString(calendar.getTime());
        editTextDate.setText(DateFormatter.beautifyDateString(formattedDate));
    }

    private void setTodayDate(EditText editTextDate) {

        Calendar calendar = Calendar.getInstance();
        formattedDate = DateFormatter.formatDateToString(calendar.getTime());
        editTextDate.setText(DateFormatter.beautifyDateString(formattedDate));
    }
}
