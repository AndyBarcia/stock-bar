package com.example.stockbar.services.products;

import com.google.firebase.database.DataSnapshot;

public class Product implements Comparable<Product> {

    public String key;
    public String bar;
    public String section;
    public String name;
    public int stock;

    public Product(String key, String bar, String section, String name, int stock) {
        this.key = key;
        this.bar = bar;
        this.section = section;
        this.name = name;
        this.stock = stock;
    }

    public Product(DataSnapshot data, String bar, String section) {
        this.key = data.getKey();
        this.bar = bar;
        this.section = section;
        this.name = data.child("name").getValue(String.class);
        this.stock = data.child("stock").getValue(Integer.class);
    }

    @Override
    public int compareTo(Product o) {
        return this.name.compareToIgnoreCase(o.name);
    }
}
