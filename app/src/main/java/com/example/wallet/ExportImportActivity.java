package com.example.wallet;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.example.wallet.db.Transaction;
import com.example.wallet.db.TransactionViewModel;
import com.example.wallet.db.WalletDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class ExportImportActivity extends AppCompatActivity {

    static final int REQUEST_SQLITE_GET = 1;

    private static final int STORAGE_PERMISSION_CODE = 2;

    private TransactionViewModel transactionViewModel;

    private TextView textViewExport;
    private TextView textViewImport;

    private static final String DATABASE_NAME = "WalletDatabase";
    //    private static final String DirectoryName = "data/user/0/com.example.wallet/databases/WalletDatabase";
//    private static final String directoryName = "data/user/0/com.example.wallet/files/WalletDatabase";
    // TODO: dont hardcode path
    private static final String directoryName = "data/user/0/com.example.wallet/files";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_import);

        textViewExport = findViewById(R.id.textView_export);
        textViewExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                syncDB();
                copyToFiles();
                exportDB();
            }
        });

        textViewImport = findViewById(R.id.textView_import);
        textViewImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissionToReadExternalFiles();

                selectDBFile();
            }
        });
    }

    private void checkPermissionToReadExternalFiles() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "You have already granted this permission!",
                    Toast.LENGTH_SHORT).show();
        } else {
            requestStoragePermission();
        }
    }

    private void requestStoragePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed because of this and that")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(ExportImportActivity.this,
                                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void syncDB() {

        transactionViewModel = ViewModelProviders.of(this).get(TransactionViewModel.class);

        // sync wal files into database
        transactionViewModel.checkpoint(new SimpleSQLiteQuery("pragma wal_checkpoint(full)")).observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                Log.d("checkpoint", integer + "");
            }
        });
    }

    /*
    - there's no api to export databases directly at the moment
        - also, cannot open files from <databases> folder

    -> so the workaround, is to copy a duplicate to <files> folder in com.example.wallet, then export it out
     */
    private void copyToFiles() {
        try {
            File dbFile = new File(this.getDatabasePath("WalletDatabase").getAbsolutePath());
            FileInputStream fis = new FileInputStream(dbFile);

            String outFileName = directoryName + File.separator +
                    DATABASE_NAME + ".db";

            Log.d("fis", "here");

            // Open the empty db as the output stream
            OutputStream output = new FileOutputStream(outFileName);

            Log.d("output", "here");

            // Transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                output.write(buffer, 0, length);
                Log.d("output write", "here");
            }
            // Close the streams
            output.flush();
            Log.d("output flush", "here");
            output.close();
            Log.d("output close", "here");
            fis.close();
            Log.d("fis close", "here");
        } catch (IOException e) {
            Log.e("dbBackup:", e.getMessage());
        }
    }

    private void exportDB() {

        // TODO: use string builder
        Uri path = FileProvider.getUriForFile(
                this,
                "com.example.wallet.fileprovider",
                new File(directoryName + File.separator + DATABASE_NAME + ".db"));

        composeEmail("Wallet Database", path);
    }

    private void composeEmail(String subject, Uri attachment) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_STREAM, attachment);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void selectDBFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_SQLITE_GET);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SQLITE_GET && resultCode == RESULT_OK) {

            Uri path = data.getData();
            // copy to files folder
            File file = copyFiles2(path);

            Log.d("file absolute path", file.getAbsolutePath());
            try {
                Log.d("file canonical path", file.getCanonicalPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("file path", file.getPath());
            Log.d("file name", file.getName());
            // prepopulate with db from files folder
            WalletDatabase instance = WalletDatabase.prepopulateDB(this, file);

            instance.getTransactionDao().getAllNonRecurringTransactions().observe(this, new Observer<List<Transaction>>() {
                @Override
                public void onChanged(@Nullable final List<Transaction> transactions) {
                    // Update the cached copy of the words in the transactionAdapter.
                    Log.d("size after populate",transactions.size() + "");
                }
            });
        }
    }

    // save to files directory
    private File copyFiles2(Uri path) {
        try {
            //File dbFile = new File(this.getDatabasePath("WalletDatabase").getAbsolutePath());
            InputStream fis = getContentResolver().openInputStream(path);;

            String outFileName = directoryName + File.separator +
                    DATABASE_NAME + ".db";

            File outputFile = new File(outFileName);

            // Open the empty db as the output stream
            OutputStream output = new FileOutputStream(outputFile);

            // Transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            // Close the streams
            output.flush();
            output.close();
            fis.close();

            return outputFile;
        } catch (IOException e) {
            Log.e("dbBackup:", e.getMessage());
        }

        return null;
    }
}
