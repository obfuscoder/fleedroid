package de.obfusco.fleedroid.domain;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Date;

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
}
