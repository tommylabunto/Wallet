package com.example.wallet.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.wallet.db.dao.CarryOverDao;
import com.example.wallet.db.dao.MonthlyBudgetDao;
import com.example.wallet.db.dao.TransactionDao;
import com.example.wallet.db.dao.TypeDao;
import com.example.wallet.db.entity.CarryOver;
import com.example.wallet.db.entity.MonthlyBudget;
import com.example.wallet.db.entity.Transaction;
import com.example.wallet.db.entity.Type;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(version = 1, entities = {MonthlyBudget.class, Transaction.class, Type.class, CarryOver.class})
@TypeConverters({Converters.class})
public abstract class WalletDatabase extends RoomDatabase {

    abstract public MonthlyBudgetDao getMonthlyBudgetDao();

    abstract public TransactionDao getTransactionDao();

    abstract public TypeDao getTypeDao();

    abstract public CarryOverDao getCarryOverDao();

    // marking the instance as volatile to ensure atomic access to the variable
    public static volatile WalletDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // to create FTS3 table

    private static DatabaseOpenHelper databaseOpenHelper;

    //The columns we'll include in the dictionary table
    public static final String COL_ID = "ID";
    public static final String COL_NAME = "NAME";
    public static final String COL_VALUE = "VALUE";
    public static final String COL_TYPE_NAME = "TYPENAME";
    public static final String COL_DATE = "DATE";
    public static final String COL_IS_REPEAT = "ISREPEAT";
    public static final String COL_IS_EXPENSE_TRANSACTION = "ISEXPENSETRANSACTION";

    private static final String DATABASE_NAME = "WalletDatabase";
    private static final String FTS_VIRTUAL_TABLE = "FTS";
    private static final int DATABASE_VERSION = 1;

    public static WalletDatabase getDatabase(final Context context) {

        if (INSTANCE == null) {
            synchronized (WalletDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            WalletDatabase.class, "WalletDatabase")
                            .addCallback(sRoomDatabaseCallback)
                            //.createFromFile(new File("data/data/com.example.wallet/files/WalletDatabase.db"))
                            //.createFromAsset("WalletDatabase.db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }

        databaseOpenHelper = new DatabaseOpenHelper(context);

        Log.d("getDatabase", "getDatabase");

        return INSTANCE;
    }

    /**
     * Override the onOpen method to populate the database.
     * For this sample, we clear the database every time it is created or opened.
     * <p>
     * If you want to populate the database only when the database is created for the 1st time,
     * override RoomDatabase.Callback()#onCreate
     */
    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);

            // If you want to keep data through app restarts,
            // comment out the following block
            databaseWriteExecutor.execute(() -> {
                // Populate the database in the background.
                TransactionDao transactionDao = INSTANCE.getTransactionDao();

                // one db should only have one instance of a carry over
                CarryOverDao carryOverDao = INSTANCE.getCarryOverDao();

                if (carryOverDao.getAllCarryOverList().size() == 0) {
                    carryOverDao.insertCarryOver(new CarryOver());
                }

                // db creates 10 years worth of monthly budgets if there are no more upcoming budgets
                // takes at least 2 minutes to populate whole list
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);

                MonthlyBudgetDao monthlyBudgetDao = INSTANCE.getMonthlyBudgetDao();

                if (monthlyBudgetDao.getAllFutureMonthlyBudgetsList(year, month).size() == 0) {

                    double budget = 0;
                    int numOfYearsToCreate = 10;
                    int totalMonthlyBudgets = numOfYearsToCreate * 12;

                    for (int i = 0; i < totalMonthlyBudgets; i++) {

                        year = calendar.get(Calendar.YEAR);
                        month = calendar.get(Calendar.MONTH);

                        MonthlyBudget monthlyBudget = new MonthlyBudget(budget, year, month);
                        monthlyBudgetDao.insertMonthlyBudget(monthlyBudget);

                        calendar.add(Calendar.MONTH, 1);
                    }
                }
            });
        }
    };

    public static void addTypes() {

        databaseWriteExecutor.execute(() -> {
            // Populate the database in the background.
            // If you want to start with more words, just add them.
            TypeDao typeDao = INSTANCE.getTypeDao();

            typeDao.insertType(new Type("Food", true));
            typeDao.insertType(new Type("Transport", true));
            typeDao.insertType(new Type("Others", true));
            typeDao.insertType(new Type("Allowance", false));
        });
    }

    public static WalletDatabase prepopulateDB(final Context context, File file) {

        INSTANCE = Room.databaseBuilder(context.getApplicationContext(), WalletDatabase.class, "WalletDatabase")
                .createFromFile(file)
                .fallbackToDestructiveMigration()
                .build();
//        INSTANCE.close();

        return INSTANCE;
    }

    // run using another thread so main thread wont lock up
    // but room doesn't let us know when it finishes
    public static void deleteAllData() {

        databaseWriteExecutor.execute(() -> {

            INSTANCE.clearAllTables();
        });
    }

    private static class DatabaseOpenHelper extends SQLiteOpenHelper {

        private final Context helperContext;
        private static SQLiteDatabase mDatabase;

        private static final String FTS_TABLE_CREATE =
                "CREATE VIRTUAL TABLE IF NOT EXISTS "
                        + FTS_VIRTUAL_TABLE
                        + " USING fts3 ("
                        + COL_ID + ", "
                        + COL_NAME + ", "
                        + COL_VALUE + ", "
                        + COL_TYPE_NAME + ", "
                        + COL_DATE + ", "
                        + COL_IS_REPEAT + ", "
                        + COL_IS_EXPENSE_TRANSACTION
                        + ")";

        public DatabaseOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            helperContext = context;
            Log.d("constructor DatabaseOpenHelper", "constructor DatabaseOpenHelper");
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            mDatabase = db;
            mDatabase.execSQL(FTS_TABLE_CREATE);
            //loadAllTransaction();
            fakeLoad();
            Log.d("onCreate sqlite","onCreate");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
//                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
            onCreate(db);
        }

        private void fakeLoad(){

            List<Transaction> tempTransactions = new ArrayList<>();

            for (int i = 1; i <= 5; i++) {

                ContentValues initialValues = new ContentValues();
                initialValues.put(COL_ID, new Long(i));
                initialValues.put(COL_NAME, "food");
                initialValues.put(COL_VALUE, new Double(i));
                initialValues.put(COL_TYPE_NAME, "type");
                initialValues.put(COL_DATE, Calendar.getInstance().getTimeInMillis());
                initialValues.put(COL_IS_REPEAT, false);
                initialValues.put(COL_IS_EXPENSE_TRANSACTION, true);

                mDatabase.insert(FTS_VIRTUAL_TABLE, null, initialValues);
            }

            Log.d("fakeLoad sqlite","fakeLoad");
        }
    }

    private void loadAllTransaction() {

        TransactionDao transactionDao = INSTANCE.getTransactionDao();

//        transactionDao.getAllTransactions().observe(this, new Observer<List<Transaction>>() {
//            @Override
//            public void onChanged(@Nullable final List<Transaction> transactions) {
//                // Update the cached copy of the words in the transactionAdapter.
//                deepCopyTransactions(transactions);
//            }
//        });
    }

    private void deepCopyTransactions(List<Transaction> transactions){

        List<Transaction> tempTransactions = new ArrayList<>();

        for (int i = 0; i < transactions.size(); i++) {

            Transaction transaction = transactions.get(i);

            ContentValues initialValues = new ContentValues();
            initialValues.put(COL_ID, transaction.getTransactionId());
            initialValues.put(COL_NAME, transaction.getName());
            initialValues.put(COL_VALUE, transaction.getValue());
            initialValues.put(COL_TYPE_NAME, transaction.getTypeName());
            //initialValues.put(COL_DATE, transaction.getDate());
            initialValues.put(COL_IS_REPEAT, transaction.isRepeat());
            initialValues.put(COL_IS_EXPENSE_TRANSACTION, transaction.isExpenseTransaction());

            DatabaseOpenHelper.mDatabase.insert(FTS_VIRTUAL_TABLE, null, initialValues);
        }
    }

    public Cursor getNameMatches(String query, String[] columns) {
        String selection = COL_NAME + " MATCH ?";
        String[] selectionArgs = new String[] {query+"*"};

        return query(selection, selectionArgs, columns);
    }

    private Cursor query(String selection, String[] selectionArgs, String[] columns) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(FTS_VIRTUAL_TABLE);

        Cursor cursor = builder.query(databaseOpenHelper.getReadableDatabase(),
                columns, selection, selectionArgs, null, null, null);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

}
