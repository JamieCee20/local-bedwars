package com.example.myplugin.ui;

import com.example.myplugin.data.ShopItem;
import com.example.myplugin.enums.ShopCategory;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class ShopInventory implements InventoryHolder {
    private Inventory inventory;
    private ShopCategory currentCategory;

    // Map of category -> its items, passed in from ShopManager
    private final Map<ShopCategory, List<ShopItem>> categoryItems;

    public ShopInventory(Map<ShopCategory, List<ShopItem>> categoryItems) {
        this.categoryItems = categoryItems;
        this.inventory = Bukkit.createInventory(this, 54, Component.text("Item Shop"));
        buildNavBar();
        buildBorder();
        showCategory(currentCategory);
    }

    public ShopCategory getCurrentCategory() { return currentCategory; }

    @Override
    public @NotNull Inventory getInventory() { return inventory; }

    private void buildNavBar() {
        // One icon per category, placed left to right in row 1 (slots 0-6)
        ShopCategory[] categories = ShopCategory.values();
        for (int i = 0; i < categories.length; i++) {
            inventory.setItem(i, makeNavIcon(categories[i]));
        }
    }

    private ItemStack makeNavIcon(ShopCategory category) {
        // Pick a representative material for each tab
        Material icon = switch (category) {
            case BLOCKS      -> Material.ORANGE_TERRACOTTA;
            case MELEE       -> Material.GOLDEN_SWORD;
            case ARMOUR      -> Material.IRON_BOOTS;
            case TOOLS       -> Material.STONE_PICKAXE;
            case RANGED      -> Material.BOW;
            case UTILITIES   -> Material.TNT;
            case POTIONS     -> Material.BREWING_STAND;
        };

        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(category.name()));
        item.setItemMeta(meta);
        return item;
    }

    private void buildBorder() {
        ItemStack pane = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.displayName(Component.empty()); // blank name — no tooltip clutter
        pane.setItemMeta(meta);

        for (int slot = 9; slot <= 17; slot++) {
            inventory.setItem(slot, pane);
        }
    }

    public void showCategory(ShopCategory category) {
        currentCategory = category;

        // Clear only the content area (rows 3-6, slots 18-53)
        for (int slot = 18; slot <= 53; slot++) {
            inventory.setItem(slot, null);
        }

        List<ShopItem> items = categoryItems.getOrDefault(category, List.of());
        for (int i = 0; i < items.size() && i < 36; i++) {
            inventory.setItem(18 + i, makeShopIcon(items.get(i)));
        }
    }

    private ItemStack makeShopIcon(ShopItem shopItem) {
        ItemStack item = new ItemStack(shopItem.getMaterial(), shopItem.getQuantity());
        ItemMeta meta = item.getItemMeta();

        String currencyName = shopItem.getCurrency() == Material.GOLD_INGOT ? "Gold" : "Iron";
        meta.lore(List.of(
            Component.text("Cost: " + shopItem.getPrice() + " " + currencyName)
        ));
        item.setItemMeta(meta);
        return item;
    }

    public ShopItem getItemAt(int slot) {
        int index = slot - 18; // content area starts at slot 18
        List<ShopItem> items = categoryItems.getOrDefault(currentCategory, List.of());
        if (index < 0 || index >= items.size()) return null;
        return items.get(index);
    }
}
