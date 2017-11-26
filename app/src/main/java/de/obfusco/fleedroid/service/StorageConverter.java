package de.obfusco.fleedroid.service;

import java.util.HashMap;
import java.util.Map;

import de.obfusco.fleedroid.db.AppDatabase;
import de.obfusco.fleedroid.net.msg.dto.Category;
import de.obfusco.fleedroid.net.msg.dto.Data;
import de.obfusco.fleedroid.net.msg.dto.Item;
import de.obfusco.fleedroid.net.msg.dto.Reservation;
import de.obfusco.fleedroid.net.msg.dto.StockItem;

public class StorageConverter {
    private AppDatabase database;

    public StorageConverter(AppDatabase database) {
        this.database = database;
    }

    public void store(Data data) {
        Map<Integer, Category> categoryMap = new HashMap<>();
        Map<Integer, Reservation> reservationMap = new HashMap<>();

        de.obfusco.fleedroid.domain.Data dbData = new de.obfusco.fleedroid.domain.Data();

        for (Category category : data.categories) {
            categoryMap.put(category.id, category);
        }
        for (Reservation reservation : data.reservations) {
            reservationMap.put(reservation.id, reservation);
        }
        for (Item item : data.items) {
            dbData.addItem(item.code, reservationMap.get(item.reservationId).number, item.number,
                    categoryMap.get(item.categoryId).name, item.description,
                    item.size, item.price, item.sold);
        }
        for (StockItem stockItem : data.stockItems) {
            dbData.addStockItem(stockItem.code, stockItem.description, stockItem.price, stockItem.sold);
        }
        database.store(dbData);
    }

}
