package de.obfusco.fleedroid.net.msg.dto;

import java.util.List;

public class Data {
    public int id;
    public String name;
    public List<Category> categories;
    public List<Reservation> reservations;
    public List<Item> items;
    public List<StockItem> stockItems;
}
