package de.obfusco.fleedroid.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Data {
    public List<Item> items = new ArrayList<>();
    public List<StockItem> stockItems = new ArrayList<>();

    public void addItem(String code, int reservationNumber, int number, String category,
                        String description, String size, double price, Date sold) {
        Item item = new Item();
        item.code = code;
        item.reservationNumber = reservationNumber;
        item.number = number;
        item.category = category;
        item.description = description;
        item.price = price;
        item.size = size;
        item.sold = sold;
        items.add(item);
    }

    public void addStockItem(String code, String description, double price, int sold) {
        StockItem stockItem = new StockItem();
        stockItem.code = code;
        stockItem.description = description;
        stockItem.price = price;
        stockItem.sold = sold;
        stockItems.add(stockItem);
    }
}
