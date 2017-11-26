package de.obfusco.fleedroid.domain;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Date;

@Entity(tableName="stock_items")
public class StockItem {
    @PrimaryKey
    @NonNull
    public String code;
    public String description;
    public double price;
    public int sold;
}
