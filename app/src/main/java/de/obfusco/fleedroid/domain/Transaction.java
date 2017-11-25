package de.obfusco.fleedroid.domain;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import java.util.Date;
import java.util.List;

@Entity(tableName="transactions")
public class Transaction {
    @PrimaryKey
    @NonNull
    public String id;

    @TypeConverters(Type.class)
    public Type type;
    public Date date;
    public String zipCode;

    @Ignore
    public List<String> itemCodes;

    public Transaction() {}
    public Transaction(String id, Type type, Date date, List<String> itemCodes, String zipCode) {

        this.id = id;
        this.type = type;
        this.date = date;
        this.itemCodes = itemCodes;
        this.zipCode = zipCode;
    }

    public enum Type {
        PURCHASE {
            @Override
            public String toString() {
                return "Verkauf";
            }
        },
        REFUND {
            @Override
            public String toString() {
                return "Storno";
            }
        };

        @TypeConverter
        public static Type toType(String string) {
            return Type.valueOf(string);
        }

        @TypeConverter
        public static String fromType(Type type) {
            return type.name();
        }
    }
}
