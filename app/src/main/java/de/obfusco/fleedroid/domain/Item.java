package de.obfusco.fleedroid.domain;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

@Entity(tableName="items")
public class Item extends BaseItem {
    public int number;
    public int reservationNumber;
    public String category;
    public String size;
    public Date sold;

    @Override
    public String toString() {
        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        return "" + reservationNumber + "-" + number +
                " - " + category + ", " + description + " " + size +
                ": " + currency.format(price);
    }

    @Override
    public boolean isSellable() {
        return sold == null;
    }

    @Override
    public void sell(Date date) {
        sold = date;
    }

    @Override
    public boolean isUnique() {
        return true;
    }
}
