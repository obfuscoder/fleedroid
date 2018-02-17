package de.obfusco.fleedroid.domain;

import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Date;

public abstract class BaseItem {
    @PrimaryKey
    @NonNull
    public String code;
    public String description;
    public double price;

    public abstract boolean isSellable();
    public abstract void sell(Date date);
    public abstract boolean isUnique();
}
