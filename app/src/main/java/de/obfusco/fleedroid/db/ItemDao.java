package de.obfusco.fleedroid.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import de.obfusco.fleedroid.domain.Item;

@Dao
public abstract class ItemDao {
    @Insert
    public abstract void insert(Item... item);

    @Insert
    public abstract void insert(List<Item> items);

    @Query("select count(*) from items")
    public abstract int count();

    @Query("delete from items")
    public abstract void deleteAll();

    @Query("select * from items where code=:code")
    public abstract Item get(String code);

    @Update
    public abstract void update(Item item);

    @Query("select count(*) from items where sold is not null")
    public abstract int soldCount();

    @Query("select sum(price) from items where sold is not null")
    public abstract double soldSum();
}
