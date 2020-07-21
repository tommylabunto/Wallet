package com.xingtingkai.wallet;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.google.android.material.snackbar.Snackbar;
import com.xingtingkai.wallet.db.WalletDatabase;
import com.xingtingkai.wallet.db.viewmodel.TransactionViewModel;
import com.xingtingkai.wallet.helper.SearchSuggestionProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class SettingsActivity extends AppCompatActivity {

    protected static final int REPEAT_ACTIVITY_REQUEST_CODE = 3;

//    private Switch switchCarryOver;
//    private CarryOverViewModel carryOverViewModel;
//    private static CarryOver carryOver;

    protected static final int REQUEST_SQLITE_GET = 1;

    private static final int STORAGE_PERMISSION_CODE = 2;

    private CoordinatorLayout coordinatorLayout;

    private static final String DATABASE_NAME = "WalletDatabase";
    private static String directoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        //initCarryOver();

        coordinatorLayout = findViewById(R.id.setting_coordinatorLayout);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        CardView cardViewRepeat = findViewById(R.id.cardview_transaction);
        // on click
        cardViewRepeat.setOnClickListener((View view) -> {
            Intent goToRepeatTransaction = new Intent(SettingsActivity.this, RepeatTransactionActivity.class);
            startActivityForResult(goToRepeatTransaction, REPEAT_ACTIVITY_REQUEST_CODE);
        });

        CardView cardViewType = findViewById(R.id.cardview_type);
        // on click
        cardViewType.setOnClickListener((View view) -> {
            Intent goToType = new Intent(SettingsActivity.this, TypeActivity.class);
            startActivity(goToType);
        });

        CardView cardViewMonthlyBudget = findViewById(R.id.cardview_monthly_budget);
        // on click
        cardViewMonthlyBudget.setOnClickListener((View view) -> {
            Intent goToMonthlyBudget = new Intent(SettingsActivity.this, MonthlyBudgetActivity.class);
            startActivity(goToMonthlyBudget);
        });

        CardView cardViewClearSearch = findViewById(R.id.cardview_clear_search);
        // on click
        cardViewClearSearch.setOnClickListener((View view) -> {
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(SettingsActivity.this,
                    SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);

            new AlertDialog.Builder(SettingsActivity.this)
                    .setTitle("Clear search history")
                    .setMessage("This action cannot be undone.")
                    // on click
                    .setPositiveButton("OK", (DialogInterface dialog, int which) -> {
                        suggestions.clearHistory();
                        showSnackbar("search history cleared");
                    })
                    // on click
                    .setNegativeButton("CANCEL", (DialogInterface dialog, int which)
                            -> dialog.dismiss())
                    .create().show();
        });

        CardView cardViewEraseAllData = findViewById(R.id.cardview_erase_all_data);
        // on click
        cardViewEraseAllData.setOnClickListener((View view) -> new AlertDialog.Builder(SettingsActivity.this)
                .setTitle("Erase all data")
                .setMessage("This action cannot be undone.")
                // on click
                .setPositiveButton("OK", (DialogInterface dialog, int which) -> {
                    WalletDatabase.deleteAllData();

                    SearchRecentSuggestions suggestions = new SearchRecentSuggestions(SettingsActivity.this,
                            SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
                    suggestions.clearHistory();
                    // go home page
//                  Intent goToAddMainActivity = new Intent(SettingsActivity.this, MainActivity.class);
//                  startActivity(goToAddMainActivity);

                    showSnackbar("database erased");
                })
                // on click
                .setNegativeButton("CANCEL", (DialogInterface dialog, int which) -> dialog.dismiss())
                .create().show());

        File files = new File(this.getApplicationInfo().dataDir + "/files");
        directoryName = files.getAbsolutePath();

        CardView cardViewExport = findViewById(R.id.cardview_export);
        // on click
        cardViewExport.setOnClickListener((View view) -> {
            syncDB();
            copyToFilesExport();
            exportDB();
        });

        CardView cardViewImport = findViewById(R.id.cardview_import);
        // on click
        cardViewImport.setOnClickListener((View view) -> checkPermissionToReadExternalFiles());

//        switchCarryOver = findViewById(R.id.switch_carryover);
//
//        if (carryOver != null && carryOver.isCarryOver()) {
//            switchCarryOver.setChecked(true);
//        }
//
//        switchCarryOver.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    // The toggle is enabled
//                    carryOver.setCarryOver(true);
//                    carryOverViewModel.updateCarryOver(carryOver);
//                } else {
//                    // The toggle is disabled
//                    carryOver.setCarryOver(false);
//                    carryOverViewModel.updateCarryOver(carryOver);
//                }
//            }
//        });
    }

//    private void initCarryOver() {
//
//        carryOverViewModel = ViewModelProviders.of(this).get(CarryOverViewModel.class);
//
//        WalletDatabase.databaseWriteExecutor.execute(() ->
//                carryOver = deepCopy(carryOverViewModel.getCarryOver()));
//    }
//
//    private CarryOver deepCopy(CarryOver carryOver) {
//
//        return CarryOver.create(carryOver.getCarryOverId(), carryOver.isCarryOver());
//    }

    private void showSnackbar(String message) {

        Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void checkPermissionToReadExternalFiles() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            syncDB();
            selectDBFile();
        } else {
            requestStoragePermission();
        }
    }

    private void requestStoragePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {

            // explain why need external storage permission
            // when permission is denied but still clicks on import
            Snackbar.make(coordinatorLayout, "external storage permission required to read database file in local storage",
                    Snackbar.LENGTH_INDEFINITE)
                    // on click
                    .setAction("ok", (View view) -> {
                        // Request the permission
                        ActivityCompat.requestPermissions(SettingsActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                    })
                    .setActionTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorYellow))
                    .show();

        } else {
            // Permission is missing and must be requested.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    // called after checkSelfPermission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                syncDB();
                selectDBFile();
            } else {
                showSnackbar("external storage permission denied");
            }
        }
    }

    private void syncDB() {

//        TransactionViewModel transactionViewModel = ViewModelProviders.of(this).get(TransactionViewModel.class);
        TransactionViewModel transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        SimpleSQLiteQuery checkPointQuery = new SimpleSQLiteQuery("pragma wal_checkpoint(full)");

        // sync wal files into database
//        transactionViewModel.checkpoint(checkPointQuery)
//                // on changed
//                .observe(this, (Integer integer) -> {
//        });

        Future<Integer> checkPoint = transactionViewModel.checkpoint(checkPointQuery);

        try {
            checkPoint.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    /*
- there's no api to export databases directly at the moment
    - also, cannot open files from <databases> folder since it exposes the file

-> so the workaround, is to copy a duplicate to <files> folder in com.example.wallet, then export it out
 */
    private void copyToFilesExport() {
        try {
            File dbFile = new File(this.getDatabasePath("WalletDatabase.db").getAbsolutePath());
            FileInputStream fis = new FileInputStream(dbFile);

            String outFileName = directoryName + File.separator +
                    DATABASE_NAME + "Export" + ".db";

            File outputFile = new File(outFileName);

            copyFile(fis, outputFile);
        } catch (IOException e) {
            Log.e("dbBackup:", Objects.requireNonNull(e.getMessage()));
        }
    }

    private File copyFile(InputStream fis, File outputFile) {
        try {
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
            Log.e("dbBackup:", Objects.requireNonNull(e.getMessage()));
        }

        return null;
    }

    private void exportDB() {

        Uri path = FileProvider.getUriForFile(
                this,
                getString(R.string.full_app_name) + "." + "fileprovider",
                // "com.xingtingkai.wallet.fileprovider",
                new File(directoryName + File.separator + DATABASE_NAME + "Export" + ".db"));

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
            File file = copyToFilesImport(path);

            deleteWalletDatabase();

//          prepopulate with db from files folder
            WalletDatabase.prepopulateDB(this, file);

            showSnackbar("database imported");
        }
    }

    private File copyToFilesImport(Uri path) {
        try {
            InputStream fis = getContentResolver().openInputStream(path);

            String outFileName = directoryName + File.separator +
                    DATABASE_NAME + "Import" + ".db";

            File outputFile = new File(outFileName);

            if (fis != null) {
                return copyFile(fis, outputFile);
            }
        } catch (IOException e) {
            Log.e("dbBackup:", Objects.requireNonNull(e.getMessage()));
        }

        return null;
    }

    private void deleteWalletDatabase() {

        WalletDatabase.INSTANCE.close();

        File databases = new File(this.getApplicationInfo().dataDir + "/databases");
        File db = new File(databases, "WalletDatabase.db");
        if (db.exists()) {
            db.delete();
        }

        File dbShm = new File(databases, "WalletDatabase.db-shm");
        if (dbShm.exists()) {
            dbShm.delete();
        }

        File dbWal = new File(databases, "WalletDatabase.db-wal");
        if (dbWal.exists()) {
            dbWal.delete();
        }
    }
}
