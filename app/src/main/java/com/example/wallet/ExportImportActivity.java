package com.example.wallet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.example.wallet.db.TransactionViewModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ExportImportActivity extends AppCompatActivity {

    private TransactionViewModel transactionViewModel;

    private static final String DATABASE_NAME = "WalletDatabase";
    //    private static final String DirectoryName = "data/user/0/com.example.wallet/databases/WalletDatabase";
//    private static final String directoryName = "data/user/0/com.example.wallet/files/WalletDatabase";
    // TODO: dont hardcode path
    private static final String directoryName = "data/user/0/com.example.wallet/files";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_import);

        syncDB();
        copyToFiles();
        exportDB();
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
                    DATABASE_NAME + ".sqlite";

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
                new File(directoryName + File.separator + DATABASE_NAME + ".sqlite"));

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
}
