package de.obfusco.fleedroid.domain;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity(tableName="transactions")
public class Transaction {
    @PrimaryKey
    @NonNull
    public String id;
    @TypeConverters(Type.class)
    public Type type;
    public Date date;
    @TypeConverters(Transaction.class)
    public List<String> itemCodes;

    public Transaction() {}
    public Transaction(String id, Type type, Date date, List<String> itemCodes, String zipCode) {

        this.id = id;
        this.type = type;
        this.date = date;
        this.itemCodes = itemCodes;
    }

    public static Transaction create(Type type, List<BaseItem> items) {
        Transaction transaction = new Transaction();
        transaction.id = UUID.randomUUID().toString();
        transaction.type = type;
        transaction.date = Calendar.getInstance().getTime();
        transaction.itemCodes = new ArrayList<>();
        for(BaseItem item : items) {
            transaction.itemCodes.add(item.code);
        }
        return transaction;
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

    @TypeConverter
    public static List<String> toList(String string) {
        return Arrays.asList(string.split(";"));
    }

    @TypeConverter
    public static String fromList(List<String> list) {
        // we can't use String.join, so we need to iterate ourselves
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<list.size(); i++) {
            sb.append(list.get(i));
            if (i<list.size()-1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }
}
