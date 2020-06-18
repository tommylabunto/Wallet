package com.example.wallet.db;

import android.content.Context;

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
import java.util.Calendar;
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

    public static WalletDatabase getDatabase(final Context context) {

        if (INSTANCE == null) {
            synchronized (WalletDatabase.class) {
                if (INSTANCE == null) {

                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            WalletDatabase.class, "WalletDatabase.db")
                            .addCallback(sRoomDatabaseCallback)
                            //.createFromFile(new File("data/data/com.example.wallet/files/WalletDatabase.db"))
                            //.createFromAsset("WalletDatabase.db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }

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

                    int budget = 0;
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

    // only prepopulates when there is no existing database
    public static WalletDatabase prepopulateDB(final Context context, File file) {

        // takes some time to copy the table
        if (INSTANCE != null) {
            synchronized (WalletDatabase.class) {
                if (INSTANCE != null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            WalletDatabase.class, "WalletDatabase.db")
                            .createFromFile(file)
                            .addCallback(sRoomDatabaseCallback)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
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
}
