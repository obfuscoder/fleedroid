package de.obfusco.fleedroid.domain;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

@Entity(tableName="stock_items")
public class StockItem extends BaseItem {
    public int sold;

    @Override
    public String toString() {
        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        return code + " - " + description + " " + ": " + currency.format(price);
    }

    @Override
    public boolean isSellable() {
        return true;
    }

    @Override
    public void sell(Date date) {
        sold += 1;
    }

    @Override
    public boolean isUnique() {
        return false;
    }
}
