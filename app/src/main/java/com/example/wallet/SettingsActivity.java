package com.example.wallet;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.example.wallet.db.entity.CarryOver;
import com.example.wallet.db.viewmodel.CarryOverViewModel;
import com.example.wallet.db.WalletDatabase;
import com.example.wallet.helper.SearchSuggestionProvider;

public class SettingsActivity extends AppCompatActivity {
//public class SettingsActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TITLE_TAG = "settingsActivityTitle";

    protected static final int REPEAT_ACTIVITY_REQUEST_CODE = 3;

    private TextView textViewRepeat;
    private TextView textViewExportImport;
    private TextView textViewReset;
    private TextView textViewType;

    private Switch switchCarryOver;

    private CarryOverViewModel carryOverViewModel;
    private static CarryOver carryOver;

    private TextView textViewMonthlyBudget;
    private TextView textViewClearSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

//        if (savedInstanceState == null) {
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.settings, new HeaderFragment())
//                    .commit();
//        } else {
//            setTitle(savedInstanceState.getCharSequence(TITLE_TAG));
//        }
//        getSupportFragmentManager().addOnBackStackChangedListener(
//                new FragmentManager.OnBackStackChangedListener() {
//                    @Override
//                    public void onBackStackChanged() {
//                        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
//                            setTitle(R.string.title_activity_settings);
//                        }
//                    }
//                });

        initCarryOver();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        textViewRepeat = findViewById(R.id.textView_repeat);
        textViewRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToRepeatTransactionActivity = new Intent(SettingsActivity.this, RepeatTransactionActivity.class);
                startActivityForResult(goToRepeatTransactionActivity, REPEAT_ACTIVITY_REQUEST_CODE);
            }
        });

        textViewExportImport = findViewById(R.id.textView_export_import);
        textViewExportImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToExportImportActivity= new Intent(SettingsActivity.this, ExportImportActivity.class);
                startActivity(goToExportImportActivity);
            }
        });

        textViewReset = findViewById(R.id.textView_reset);
        textViewReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle("Warning")
                        .setMessage("Are you sure you want to reset database?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                WalletDatabase.deleteAllData();

                                // go home page
                                Intent goToAddMainActivity = new Intent(SettingsActivity.this, MainActivity.class);
                                startActivity(goToAddMainActivity);
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create().show();
            }
        });

        textViewType = findViewById(R.id.textView_type);
        textViewType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToTypeActivity= new Intent(SettingsActivity.this, TypeActivity.class);
                startActivity(goToTypeActivity);
            }
        });

        /*
        On second thoughts, I disabled carry over. Because it's not so straightforward to manage money.
        If you make a few big purchases, it could break your budget for a few months.
        Even if you save a lot in a month, you will be tempted to spend that excess.
        Regardless, it presents a illusion on telling you how to manage your money.
        But its a wallet, not a bank.
        Also, don't let the money dictate how you live. Its supposed to be just a guide, a reference.
         */
        switchCarryOver = findViewById(R.id.switch_carryover);

        if (carryOver != null && carryOver.isCarryOver()) {
            switchCarryOver.setChecked(true);
        }

        switchCarryOver.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    carryOver.setCarryOver(true);
                    carryOverViewModel.updateCarryOver(carryOver);
                } else {
                    // The toggle is disabled
                    carryOver.setCarryOver(false);
                    carryOverViewModel.updateCarryOver(carryOver);
                }
            }
        });

        textViewMonthlyBudget = findViewById(R.id.textView_monthly_budget);
        textViewMonthlyBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToMonthlyBudgetActivity= new Intent(SettingsActivity.this, MonthlyBudgetActivity.class);
                startActivity(goToMonthlyBudgetActivity);
            }
        });

        textViewClearSearch = findViewById(R.id.textView_clear_search);
        textViewClearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(SettingsActivity.this,
                        SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
                suggestions.clearHistory();
            }
        });
    }

    private void initCarryOver() {

        carryOverViewModel = ViewModelProviders.of(this).get(CarryOverViewModel.class);

        WalletDatabase.databaseWriteExecutor.execute(() -> {
            carryOver = deepCopy(carryOverViewModel.getCarryOver());
        });
    }

    private CarryOver deepCopy(CarryOver carryOver) {

        CarryOver tempCarryOver = new CarryOver();
        tempCarryOver.setCarryOverId(carryOver.getCarryOverId());
        tempCarryOver.setCarryOver(carryOver.isCarryOver());

        return tempCarryOver;
    }

//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        // Save current activity title so we can set it again after a configuration change
//        outState.putCharSequence(TITLE_TAG, getTitle());
//    }
//
//    @Override
//    public boolean onSupportNavigateUp() {
//        if (getSupportFragmentManager().popBackStackImmediate()) {
//            return true;
//        }
//        return super.onSupportNavigateUp();
//    }
//
//    @Override
//    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
//        // Instantiate the new Fragment
//        final Bundle args = pref.getExtras();
//        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
//                getClassLoader(),
//                pref.getFragment());
//        fragment.setArguments(args);
//        fragment.setTargetFragment(caller, 0);
//        // Replace the existing Fragment with the new Fragment
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.settings, fragment)
//                .addToBackStack(null)
//                .commit();
//        setTitle(pref.getTitle());
//        return true;
//    }
//
//    public static class HeaderFragment extends PreferenceFragmentCompat {
//
//        @Override
//        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
//            //setPreferencesFromResource(R.xml.header_preferences, rootKey);
//        }
//    }
//
//    public static class MessagesFragment extends PreferenceFragmentCompat {
//
//        @Override
//        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
//            //setPreferencesFromResource(R.xml.messages_preferences, rootKey);
//        }
//    }
//
//    public static class SyncFragment extends PreferenceFragmentCompat {
//
//        @Override
//        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
//            //setPreferencesFromResource(R.xml.sync_preferences, rootKey);
//        }
//    }
}
