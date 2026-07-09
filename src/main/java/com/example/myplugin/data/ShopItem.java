package com.example.myplugin.data;

import org.bukkit.Material;

public class ShopItem {
    private final Material material;
    private final int quantity;
    private final Material currency;
    private final int price;
    private final int slot;

    public ShopItem(Material material, int quantity, Material currency, int price, int slot) {
        this.material = material;
        this.quantity = quantity;
        this.currency = currency;
        this.price = price;
        this.slot = slot;
    }

    public int getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public Material getCurrency() { return currency; }
    public Material getMaterial() { return material; }
    public int getSlot() { return slot; }
}
