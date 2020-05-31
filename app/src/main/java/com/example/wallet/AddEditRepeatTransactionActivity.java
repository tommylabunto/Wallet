package com.example.wallet;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.example.wallet.db.DateFormatter;

import java.util.Calendar;
import java.util.Date;

public class AddEditRepeatTransactionActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_transaction);

        editTextName = findViewById(R.id.edit_text_name);
        editTextTypeName = findViewById(R.id.edit_text_type_name);
        editTextValue = findViewById(R.id.edit_text_value);
        editTextDate = findViewById(R.id.edit_text_dateTime);

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
            editTextTypeName.setText(intent.getStringExtra(EXTRA_TYPENAME));
            editTextDate.setText(intent.getStringExtra(EXTRA_DATE));
            editTextValue.setText(intent.getDoubleExtra(EXTRA_VALUE, 1) + "");
        } else {
            setTodayDate(editTextDate);
        }
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
        String typeName = editTextTypeName.getText().toString().trim();
        String dateString = editTextDate.getText().toString();
        String valueString = editTextValue.getText().toString();
        double value = 0;

        if (!valueString.isEmpty()) {
            value = Double.parseDouble(valueString);
        }

        if (name.isEmpty() || typeName.isEmpty() || value == 0) {
            Toast.makeText(this, "Please insert a name or typeName or value", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent newTransaction = createIntent(dateString, value, name, typeName, "save");

        setResult(RESULT_OK, newTransaction);
        finish();
    }

    private void deleteTransaction(){

        String name = editTextName.getText().toString().trim();
        String typeName = editTextTypeName.getText().toString().trim();
        String dateString = editTextDate.getText().toString();
        String valueString = editTextValue.getText().toString();
        double value = 0;

        if (!valueString.isEmpty()) {
            value = Double.parseDouble(valueString);
        }

        Intent oldTransaction = createIntent(dateString, value, name, typeName, "delete");

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
