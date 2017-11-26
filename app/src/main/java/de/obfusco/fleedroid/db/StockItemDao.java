package de.obfusco.fleedroid.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import de.obfusco.fleedroid.domain.Item;
import de.obfusco.fleedroid.domain.StockItem;

@Dao
public abstract class StockItemDao {
    @Insert
    public abstract void insert(StockItem... stockItem);

    @Insert
    public abstract void insert(List<StockItem> stockItems);

    @Query("select count(*) from stock_items")
    public abstract int count();

    @Query("delete from stock_items")
    public abstract void deleteAll();

    @Query("select * from stock_items where code=:code")
    public abstract StockItem get(String code);

    @Update
    public abstract void update(StockItem stockItem);
}
