package com.example.wallet.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(version = 6, entities = {MonthlyBudget.class, Transaction.class, Type.class})
@TypeConverters({Converters.class})
public abstract class WalletDatabase extends RoomDatabase {

    abstract public MonthlyBudgetDao getMonthlyBudgetDao();

    abstract public TransactionDao getTransactionDao();

    abstract public TypeDao getTypeDao();

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
                            WalletDatabase.class, "WalletDatabase")
                            .addCallback(sRoomDatabaseCallback)
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
                // If you want to start with more words, just add them.
                TransactionDao transactionDao = INSTANCE.getTransactionDao();
                //transactionDao.deleteAllTransactions();
            });
        }
    };
}
