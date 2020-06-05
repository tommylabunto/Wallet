package com.example.wallet;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.wallet.db.TypeViewModel;
import com.example.wallet.helper.DateFormatter;
import com.example.wallet.helper.DatePickerFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class AddEditTransactionActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    public static final String EXTRA_ID =
            "com.example.wallet.EXTRA_ID";
    public static final String EXTRA_NAME =
            "com.example.wallet.EXTRA_NAME";
    public static final String EXTRA_TYPENAME =
            "com.example.wallet.EXTRA_TYPENAME";
    public static final String EXTRA_VALUE =
            "com.example.wallet.EXTRA_VALUE";
    public static final String EXTRA_DATE =
            "com.example.wallet.EXTRA_DATE";
    public static final String EXTRA_OPERATION =
            "com.example.wallet.EXTRA_OPERATION";

    private EditText editTextName;
    private EditText editTextTypeName;
    private EditText editTextValue;
    private EditText editTextDate;

    private Spinner spinner;
    private String type;

    private List<String> typeList;

    private TypeViewModel typeViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_transaction);

        editTextName = findViewById(R.id.edit_text_name);
        //editTextTypeName = findViewById(R.id.edit_text_type_name);
        editTextValue = findViewById(R.id.edit_text_value);
        editTextDate = findViewById(R.id.edit_text_dateTime);

        spinner = findViewById(R.id.spinner_type);

        //initViewModel();

        List<String> typeList1 = new ArrayList<>();
        typeList1.add("Food");
        typeList1.add("Transport");

        //Log.d("type size", typeList.size() + "");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, typeList1);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar calendar = convertEditTextToCalendar(editTextDate);
                DialogFragment datePicker = new DatePickerFragment(calendar);
                datePicker.show(getSupportFragmentManager(), "date picker");
            }
        });

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_ID)) {
            editTextName.setText(intent.getStringExtra(EXTRA_NAME));
//            editTextTypeName.setText(intent.getStringExtra(EXTRA_TYPENAME));
            editTextDate.setText(intent.getStringExtra(EXTRA_DATE));
            editTextValue.setText(intent.getDoubleExtra(EXTRA_VALUE, 1) + "");

            type = intent.getStringExtra(EXTRA_TYPENAME);
            int selectionPosition= adapter.getPosition(type);
            spinner.setSelection(selectionPosition);
        } else {
            setTodayDate(editTextDate);
        }
    }

    private void deepCopyList(List<String> types) {

        typeList = new ArrayList<>();

        for (String type : types) {

            typeList.add(type);
            Log.d("type", type);
        }
    }

    private void initViewModel() {

        typeViewModel = ViewModelProviders.of(this).get(TypeViewModel.class);

        typeViewModel.getAllTypes().observe(this, new Observer<List<String>>() {
            @Override
            public void onChanged(@Nullable final List<String> types) {

                deepCopyList(types);
            }
        });
    }

    private Calendar convertEditTextToCalendar(EditText editTextDate) {

        Calendar calendar = Calendar.getInstance();
        String dateString = editTextDate.getText().toString();
        Date date = DateFormatter.formatStringToDate(dateString);
        calendar.setTime(date);

        return calendar;
    }

    private void createOrSaveTransaction() {

        String name = editTextName.getText().toString().trim();
        //String typeName = editTextTypeName.getText().toString().trim();
        String dateString = editTextDate.getText().toString();
        String valueString = editTextValue.getText().toString();
        double value = 0;

        if (!valueString.isEmpty()) {
            value = Double.parseDouble(valueString);
        }

        if (name.isEmpty() || value == 0) {
            Toast.makeText(this, "Please insert a name or typeName or value", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent newTransaction = createIntent(dateString, value, name, type, "save");

        setResult(RESULT_OK, newTransaction);
        finish();
    }

    private void deleteTransaction() {

        String name = editTextName.getText().toString().trim();
        //String typeName = editTextTypeName.getText().toString().trim();
        String dateString = editTextDate.getText().toString();
        String valueString = editTextValue.getText().toString();
        double value = 0;

        if (!valueString.isEmpty()) {
            value = Double.parseDouble(valueString);
        }

        Intent oldTransaction = createIntent(dateString, value, name, type, "delete");

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

        Long id = getIntent().getLongExtra(EXTRA_ID, -1);
        if (id != -1) {
            transaction.putExtra(EXTRA_ID, id);
        }

        return transaction;
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
                createOrSaveTransaction();
                return true;
            case R.id.delete_transaction:
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

        String dateString = DateFormatter.formatDateToString(calendar.getTime());
        editTextDate.setText(dateString);
    }

    private void setTodayDate(EditText editTextDate) {

        Calendar calendar = Calendar.getInstance();
        String dateString = DateFormatter.formatDateToString(calendar.getTime());
        editTextDate.setText(dateString);
    }
}
