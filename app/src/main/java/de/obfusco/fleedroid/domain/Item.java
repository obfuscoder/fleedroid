package de.obfusco.fleedroid.domain;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

@Entity(tableName="items")
public class Item {
    @PrimaryKey
    @NonNull
    public String code;
    public int number;
    public int reservationNumber;
    public String category;
    public String description;
    public String size;
    public double price;
    public Date sold;

    @Override
    public String toString() {
        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        return "" + reservationNumber + "-" + number +
                " - " + category + ", " + description + " " + size +
                ": " + currency.format(price);
    }
}
