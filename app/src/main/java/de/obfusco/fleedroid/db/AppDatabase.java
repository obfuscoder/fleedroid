package de.obfusco.fleedroid.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import de.obfusco.fleedroid.domain.Transaction;

@Database(entities = { Transaction.class }, version = 1)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TransactionDao transactionDao();
}
