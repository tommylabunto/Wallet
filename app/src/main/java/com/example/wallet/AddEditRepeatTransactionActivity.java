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
import com.example.wallet.db.viewmodel.TypeViewModel;
import com.example.wallet.helper.DateFormatter;
import com.example.wallet.helper.DatePickerFragment;
import com.example.wallet.helper.FrequencyStringConverter;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AddEditRepeatTransactionActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

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
    protected static final String EXTRA_FREQUENCY =
            "com.example.wallet.EXTRA_FREQUENCY";
    protected static final String EXTRA_REPEAT =
            "com.example.wallet.EXTRA_REPEAT";
    protected static final String EXTRA_RECURRING_ID =
            "com.example.wallet.EXTRA_RECURRING_ID";
    protected static final String EXTRA_IS_EXPENSE_TYPE =
            "com.example.wallet.EXTRA_IS_EXPENSE_TYPE";
    protected static final String EXTRA_OPERATION =
            "com.example.wallet.EXTRA_OPERATION";

    private EditText editTextName;
    private EditText editTextValue;
    private EditText editTextDate;
    private EditText editTextRepeat;

    private TextInputLayout textInputLayoutValue;
    private TextInputLayout textInputLayoutName;
    private TextInputLayout textInputLayoutRepeat;

    // in the format of "02/12/2020"
    private static String formattedDate;

    private Spinner spinnerFrequency;
    private ArrayAdapter<CharSequence> adapterFrequency;

    private Spinner spinnerType;
    private ArrayAdapter<String> adapterType;

    private TypeViewModel typeViewModel;

    private RadioButton radioButtonExpense;
    private RadioButton radioButtonIncome;

    private static String type;

    private static int frequency;

    private static List<String> expenseTypeList;
    private static List<String> incomeTypeList;

    private static boolean isExpenseType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_repeat_transaction);

        radioButtonExpense = findViewById(R.id.radio_expense);
        radioButtonIncome = findViewById(R.id.radio_income);

        editTextName = findViewById(R.id.edit_text_name);
        editTextValue = findViewById(R.id.edit_text_value);
        editTextDate = findViewById(R.id.edit_text_dateTime);
        spinnerFrequency = findViewById(R.id.spinner);
        spinnerType = findViewById(R.id.spinner_repeat_type);
        editTextRepeat = findViewById(R.id.edit_text_num_repeat);

        textInputLayoutValue = findViewById(R.id.edit_text_num_value_input_layout);
        textInputLayoutName = findViewById(R.id.edit_text_num_name_input_layout);
        textInputLayoutRepeat = findViewById(R.id.edit_text_num_repeat_input_layout);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close);
        }

        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = formatSelectedDate();
                DialogFragment datePicker = new DatePickerFragment(calendar);
                datePicker.show(getSupportFragmentManager(), "date picker");
            }
        });

        adapterFrequency = ArrayAdapter.createFromResource(this,
                R.array.frequency_array, android.R.layout.simple_spinner_item);
        adapterFrequency.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(adapterFrequency);
        spinnerFrequency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String frequencyString = parent.getItemAtPosition(position).toString();
                frequency = FrequencyStringConverter.convertFrequencyStringToInt(frequencyString);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        isExpenseType = true;
        expenseTypeList = new ArrayList<>();
        incomeTypeList = new ArrayList<>();
        initViewModel();

        // bring focus to edit text and show keybaord
        if (editTextValue.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

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

        extractIntent();
    }

    private void initViewModel() {

        typeViewModel = ViewModelProviders.of(this).get(TypeViewModel.class);

        typeViewModel.getAllTypes().observe(this, new Observer<List<Type>>() {
            @Override
            public void onChanged(@Nullable final List<Type> types) {

                deepCopyList(types);
                Log.d("onchanged", "onchanged");
            }
        });
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

        showSpinnerType();
        //extractIntent();
    }

    private void showSpinnerType() {

        initAdapterType();
        spinnerType.setAdapter(adapterType);
        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

    private void extractIntent() {

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_ID)) {
            editTextName.setText(intent.getStringExtra(EXTRA_NAME));

            formattedDate = intent.getStringExtra(EXTRA_DATE);
            editTextDate.setText(DateFormatter.beautifyDateString(formattedDate));

            editTextValue.setText(intent.getDoubleExtra(EXTRA_VALUE, 1) + "");
            editTextRepeat.setText(intent.getIntExtra(EXTRA_REPEAT, 1) + "");

            frequency = intent.getIntExtra(EXTRA_FREQUENCY, 12);
            String frequencyString = FrequencyStringConverter.convertFrequencyIntToString(frequency);
            int selectionPosition = adapterFrequency.getPosition(frequencyString);
            spinnerFrequency.setSelection(selectionPosition);

            if (intent.getBooleanExtra(EXTRA_IS_EXPENSE_TYPE, true)) {
                radioButtonExpense.setChecked(true);
                isExpenseType = true;
            } else {
                radioButtonIncome.setChecked(true);
                isExpenseType = false;
            }

            if (adapterType == null) {
                initAdapterType();
            }

            type = intent.getStringExtra(EXTRA_TYPENAME);
            int selectionPositionType = adapterType.getPosition(type);
            Log.d("selectionPositionType", selectionPositionType + "");
            spinnerType.setSelection(selectionPositionType);
        } else {
            setTodayDate(editTextDate);
            radioButtonExpense.setChecked(true);
            isExpenseType = true;
        }
    }

    // it takes a while to copy types, so the list might be null
    private void initAdapterType() {

        if (isExpenseType) {
            adapterType = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, expenseTypeList);
        } else {
            adapterType = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, incomeTypeList);
        }
        adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    private Calendar formatSelectedDate() {

        Calendar calendar = Calendar.getInstance();
        Date date = DateFormatter.formatStringToDate(formattedDate);
        calendar.setTime(date);

        return calendar;
    }

    private void createOrSaveTransaction() {

        String name = editTextName.getText().toString().trim();
        String valueString = editTextValue.getText().toString().trim();
        double value = 0;

        String repeatString = editTextRepeat.getText().toString();
        int repeat = -1;

        boolean violateInputValidation = false;

        if (!valueString.isEmpty()) {
            value = Double.parseDouble(valueString);
        }

        if (!repeatString.isEmpty()) {
            repeat = Integer.parseInt(repeatString);
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

        if (repeat < 0 || repeat > 30) {
            textInputLayoutRepeat.setError("Please enter a value between 0 and 30.");
            violateInputValidation = true;
        } else {
            textInputLayoutRepeat.setError("");
        }

        if (violateInputValidation) {
            return;
        }

        Intent newTransaction = createIntent(formattedDate, value, name, type, frequency, repeat, "save");

        setResult(RESULT_OK, newTransaction);
        finish();

    }

    private void deleteTransaction() {

        String name = editTextName.getText().toString().trim();
        String valueString = editTextValue.getText().toString().trim();
        double value = 0;

        String repeatString = editTextRepeat.getText().toString().trim();

        int repeat = 0;

        if (!valueString.isEmpty()) {
            value = Double.parseDouble(valueString);
        }

        if (!repeatString.isEmpty()) {
            repeat = Integer.parseInt(repeatString);
        }

        // force value to be within acceptable range, since user is going to delete anyway
        if (repeat < 0 || repeat > 30) {
            repeat = 0;
        }

        if (value < 0) {
            value = 0;
        }

        Intent oldTransaction = createIntent(formattedDate, value, name, type, frequency, repeat, "delete");

        setResult(RESULT_OK, oldTransaction);
        finish();

    }

    private Intent createIntent(String dateString, double value, String name, String typeName, int frequency, int repeat, String operation) {

        Intent transaction = new Intent();
        transaction.putExtra(EXTRA_NAME, name);
        transaction.putExtra(EXTRA_TYPENAME, typeName);
        transaction.putExtra(EXTRA_DATE, dateString);
        transaction.putExtra(EXTRA_VALUE, value);
        transaction.putExtra(EXTRA_FREQUENCY, frequency);
        transaction.putExtra(EXTRA_REPEAT, repeat);
        transaction.putExtra(EXTRA_OPERATION, operation);

        transaction.putExtra(EXTRA_IS_EXPENSE_TYPE, isExpenseType);

        Long id = getIntent().getLongExtra(EXTRA_ID, -1);
        if (id != -1) {
            transaction.putExtra(EXTRA_ID, id);
        }

        String recurringId = getIntent().getStringExtra(EXTRA_RECURRING_ID);
        transaction.putExtra(EXTRA_RECURRING_ID, recurringId);

        return transaction;
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

    public void onRadioButtonClicked(View view) {

        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radio_expense:
                if (checked) {
                    isExpenseType = true;
                    showSpinnerType();
                    break;
                }
            case R.id.radio_income:
                if (checked) {
                    isExpenseType = false;
                    showSpinnerType();
                    break;
                }
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
