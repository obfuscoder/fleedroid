package de.obfusco.fleedroid.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import de.obfusco.fleedroid.domain.Transaction;

@Dao
public abstract class TransactionDao {
    @Insert
    public abstract void insert(Transaction transaction);

    @Query("select count(*) from transactions")
    public abstract int count();

    @Query("delete from transactions")
    public abstract void deleteAll();

    @Query("select * from transactions where id=:id")
    public abstract Transaction get(String id);

    @Query("select * from transactions order by date")
    public abstract List<Transaction> findAll();
}
