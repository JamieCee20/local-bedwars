package com.example.myplugin.data;

import org.bukkit.Material;

public class ShopItem {
    private final Material material;
    private final int quantity;
    private final Material currency;
    private final int price;

    public ShopItem(Material material, int quantity, Material currency, int price) {
        this.material = material;
        this.quantity = quantity;
        this.currency = currency;
        this.price = price;
    }

    public int getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public Material getCurrency() {
        return currency;
    }

    public Material getMaterial() {
        return material;
    }
}
