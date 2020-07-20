package com.xingtingkai.wallet.helper;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.time.ZonedDateTime;

public class DatePickerFragment extends DialogFragment {

    private ZonedDateTime zonedDateTime;

    public DatePickerFragment() {
        zonedDateTime = ZonedDateTime.now();
    }

    public DatePickerFragment(ZonedDateTime zonedDateTime) {
        this();
        this.zonedDateTime = zonedDateTime;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        int year = this.zonedDateTime.getYear();
        // for compatibility with Calendar month
        int month = this.zonedDateTime.getMonth().getValue() - 1;
        int day = this.zonedDateTime.getDayOfMonth();

        return new DatePickerDialog(getActivity(), (DatePickerDialog.OnDateSetListener) getActivity(), year, month, day);
    }
}
