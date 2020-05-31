package com.example.wallet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

public class CustomAlertDialog extends DialogFragment {

    private String message;

    public CustomAlertDialog() {
        message = "";
    }

    public CustomAlertDialog(String message) {
        this();
        this.message = message;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Warning")
                .setMessage(message)
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // do nothing, just go back to previous screen
                    }
                });
        return builder.create();
    }
}
