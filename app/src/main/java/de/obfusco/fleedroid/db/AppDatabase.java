package de.obfusco.fleedroid.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import java.util.Calendar;

import de.obfusco.fleedroid.domain.Data;
import de.obfusco.fleedroid.domain.Item;
import de.obfusco.fleedroid.domain.StockItem;
import de.obfusco.fleedroid.domain.Transaction;

@Database(entities = { Transaction.class, Item.class, StockItem.class }, version = 3)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TransactionDao transactionDao();
    public abstract ItemDao itemDao();
    public abstract StockItemDao stockItemDao();

    @android.arch.persistence.room.Transaction
    public void store(Transaction transaction) {
        TransactionDao transactions = transactionDao();
        if (transactions.get(transaction.id) == null) {
            transactions.insert(transaction);
            for (String itemCode : transaction.itemCodes) {
                Item item = itemDao().get(itemCode);
                if (item != null) {
                    item.sold = transaction.type == Transaction.Type.PURCHASE ? transaction.date : null;
                    itemDao().update(item);
                } else {
                    StockItem stockItem = stockItemDao().get(itemCode);
                    if (stockItem != null) {
                        stockItem.sold = transaction.type == Transaction.Type.PURCHASE ? stockItem.sold + 1 : stockItem.sold - 1;
                        stockItemDao().update(stockItem);
                    }
                }
            }
        }
    }

    @android.arch.persistence.room.Transaction
    public void store(Data data) {
        transactionDao().deleteAll();
        itemDao().deleteAll();
        stockItemDao().deleteAll();

        itemDao().insert(data.items);
        stockItemDao().insert(data.stockItems);
    }
}
