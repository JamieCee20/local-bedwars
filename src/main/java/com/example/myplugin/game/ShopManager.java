package com.example.myplugin.game;

import com.example.myplugin.MyPlugin;
import com.example.myplugin.data.ShopItem;
import com.example.myplugin.enums.ShopCategory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Piglin;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ShopManager {
    private final MyPlugin plugin;
    private UUID shopEntityId;
    private final Map<ShopCategory, List<ShopItem>> categoryItems;

    public ShopManager(MyPlugin plugin) {
        this.plugin = plugin;
        this.categoryItems = buildItems();
    }

    private Map<ShopCategory, List<ShopItem>> buildItems() {
        Map<ShopCategory, List<ShopItem>> map = new EnumMap<>(ShopCategory.class);

        map.put(ShopCategory.BLOCKS, List.of(
            new ShopItem(Material.WHITE_WOOL,    16, Material.IRON_INGOT, 4),
            new ShopItem(Material.TERRACOTTA,    16, Material.IRON_INGOT, 12),
            new ShopItem(Material.GLASS,          4, Material.IRON_INGOT, 12),
            new ShopItem(Material.OAK_PLANKS,    16, Material.GOLD_INGOT, 4),
            new ShopItem(Material.END_STONE,      4, Material.IRON_INGOT, 24),
            new ShopItem(Material.OBSIDIAN,       4, Material.GOLD_INGOT, 8)
        ));

        map.put(ShopCategory.MELEE, List.of(
            new ShopItem(Material.IRON_SWORD,  1, Material.IRON_INGOT, 10),
            new ShopItem(Material.DIAMOND_SWORD, 1, Material.GOLD_INGOT, 6)
        ));

        map.put(ShopCategory.ARMOUR, List.of(
            new ShopItem(Material.CHAINMAIL_BOOTS,     1, Material.IRON_INGOT, 40),
            new ShopItem(Material.IRON_BOOTS,          1, Material.GOLD_INGOT, 6),
            new ShopItem(Material.DIAMOND_BOOTS,       1, Material.GOLD_INGOT, 12)
        ));

        // Add TOOLS, RANGED, UTILITIES, POTIONS in the same way...

        return map;
    }

    public void spawnShopEntity(Location location) {
        // Clean up any old shop entity first (e.g. on game reset)
        despawnShopEntity();

        Piglin piglin = (Piglin) location.getWorld()
            .spawnEntity(location, EntityType.PIGLIN);

        piglin.customName(net.kyori.adventure.text.Component.text("Item Shop"));
        piglin.setCustomNameVisible(true);
        piglin.setAI(false);
        piglin.setInvulnerable(true);
        piglin.setSilent(true);
        piglin.setRemoveWhenFarAway(false);
        piglin.setImmuneToZombification(true);

        shopEntityId = piglin.getUniqueId();
    }

    public void despawnShopEntity() {
        if (shopEntityId == null) return;

        for (Entity entity : plugin.getGameWorld().getEntities()) {
            if (entity.getUniqueId().equals(shopEntityId)) {
                entity.remove();
                break;
            }
        }

        shopEntityId = null;
    }

    public UUID getShopEntityId() {
        return shopEntityId;
    }

    public Map<ShopCategory, List<ShopItem>> getCategoryItems() {
        return categoryItems;
    }
}
