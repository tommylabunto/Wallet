package com.example.wallet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
//public class SettingsActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TITLE_TAG = "settingsActivityTitle";

    public static final int REPEAT_ACTIVITY_REQUEST_CODE = 3;

    private TextView textViewRepeat;
    private TextView textViewExportImport;

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
