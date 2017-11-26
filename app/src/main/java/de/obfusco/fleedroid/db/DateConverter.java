package de.obfusco.fleedroid.db;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

class DateConverter {
    @TypeConverter
    public static String fromDate(Date date) {
        return date == null ? null : Long.toString(date.getTime());
    }

    @TypeConverter
    public static Date toDate(String date) {
        return date == null ? null : new Date(Long.parseLong(date));
    }
}
