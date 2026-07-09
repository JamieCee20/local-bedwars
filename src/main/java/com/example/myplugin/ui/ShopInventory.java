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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopInventory implements InventoryHolder {
    private Inventory inventory;
    private ShopCategory currentCategory;

    // Map of category -> its items, passed in from ShopManager
    private final Map<ShopCategory, List<ShopItem>> categoryItems;
    // Maps inventory slot -> ShopItem for the currently displayed category
    private final Map<Integer, ShopItem> slotItemMap = new HashMap<>();

    // Left and right column slots in the content area (rows 3-6) — kept as glass
    private static final int[] BORDER_COLUMNS = {18, 26, 27, 35, 36, 44, 45, 53};

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
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.displayName(Component.empty());
        pane.setItemMeta(meta);

        // Row 2 separator
        for (int slot = 9; slot <= 17; slot++) {
            inventory.setItem(slot, pane);
        }
        // Left and right columns of the content area (rows 3-6)
        for (int slot : BORDER_COLUMNS) {
            inventory.setItem(slot, pane);
        }
    }

    public void showCategory(ShopCategory category) {
        currentCategory = category;
        slotItemMap.clear();

        // Clear content slots that aren't part of the glass border
        for (int slot = 18; slot <= 53; slot++) {
            if (!isBorderSlot(slot)) {
                inventory.setItem(slot, null);
            }
        }

        List<ShopItem> items = categoryItems.getOrDefault(category, List.of());
        for (ShopItem item : items) {
            int slot = item.getSlot();
            if (isBorderSlot(slot)) {
                org.bukkit.Bukkit.getLogger().warning(
                    "[Shop] Item " + item.getMaterial() + " in " + category + " uses border slot " + slot + " — it will not be displayed. Valid slots: 19-25, 28-34, 37-43, 46-52.");
            } else if (slot >= 18 && slot <= 53) {
                inventory.setItem(slot, makeShopIcon(item));
                slotItemMap.put(slot, item);
            } else {
                org.bukkit.Bukkit.getLogger().warning(
                    "[Shop] Item " + item.getMaterial() + " in " + category + " has out-of-range slot " + slot + " — it will not be displayed.");
            }
        }
    }

    private boolean isBorderSlot(int slot) {
        for (int borderSlot : BORDER_COLUMNS) {
            if (slot == borderSlot) return true;
        }
        return false;
    }

    private ItemStack makeShopIcon(ShopItem shopItem) {
        ItemStack item = new ItemStack(shopItem.getMaterial(), shopItem.getQuantity());
        ItemMeta meta = item.getItemMeta();

        String currencyName = switch (shopItem.getCurrency()) {
            case IRON_INGOT -> "Iron";
            case GOLD_INGOT -> "Gold";
            case DIAMOND    -> "Diamond";
            case EMERALD    -> "Emerald";
            default -> shopItem.getCurrency().name();
        };
        meta.lore(List.of(
            Component.text("Cost: " + shopItem.getPrice() + " " + currencyName)
        ));
        item.setItemMeta(meta);
        return item;
    }

    public ShopItem getItemAt(int slot) {
        return slotItemMap.get(slot);
    }
}
