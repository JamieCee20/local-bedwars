package com.example.myplugin.game;

import com.example.myplugin.MyPlugin;
import com.example.myplugin.data.ShopItem;
import com.example.myplugin.enums.ShopCategory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Piglin;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ShopManager {
    private final MyPlugin plugin;
    private final NamespacedKey shopKey;
    private final Set<UUID> shopEntityIds = new HashSet<>();
    private Map<ShopCategory, List<ShopItem>> categoryItems;

    public ShopManager(MyPlugin plugin) {
        this.plugin = plugin;
        this.shopKey = new NamespacedKey(plugin, "shop_piglin");
        this.categoryItems = loadItems(plugin.getConfig());
    }

    public void reload(FileConfiguration config) {
        this.categoryItems = loadItems(config);
    }

    private Map<ShopCategory, List<ShopItem>> loadItems(FileConfiguration config) {
        Map<ShopCategory, List<ShopItem>> map = new EnumMap<>(ShopCategory.class);
        if (config.getConfigurationSection("shop") == null) {
            plugin.getLogger().warning("No 'shop' section in config.yml — shop will be empty.");
            return map;
        }
        for (ShopCategory category : ShopCategory.values()) {
            List<Map<?, ?>> entries = config.getMapList("shop." + category.name());
            List<ShopItem> items = new ArrayList<>();
            for (Map<?, ?> entry : entries) {
                try {
                    Material material = Material.valueOf(((String) entry.get("material")).toUpperCase());
                    int quantity = ((Number) entry.get("quantity")).intValue();
                    Material currency = parseCurrency((String) entry.get("currency"));
                    int price = ((Number) entry.get("price")).intValue();
                    int slot = ((Number) entry.get("slot")).intValue();
                    items.add(new ShopItem(material, quantity, currency, price, slot));
                } catch (Exception e) {
                    plugin.getLogger().warning("Skipping invalid shop entry in " + category.name() + ": " + e.getMessage());
                }
            }
            map.put(category, items);
        }
        return map;
    }

    private Material parseCurrency(String name) {
        return switch (name.toLowerCase()) {
            case "iron"    -> Material.IRON_INGOT;
            case "gold"    -> Material.GOLD_INGOT;
            case "diamond" -> Material.DIAMOND;
            case "emerald" -> Material.EMERALD;
            default -> throw new IllegalArgumentException("Unknown currency '" + name + "'. Use: iron, gold, diamond, emerald");
        };
    }

    public void spawnShopEntity(Location location) {
        Piglin piglin = (Piglin) location.getWorld()
            .spawnEntity(location, EntityType.PIGLIN);

        piglin.setCustomNameVisible(false);
        piglin.setAI(false);           // freeze in place — no wandering or bartering
        piglin.setInvulnerable(true);  // can't be killed by players or environment
        piglin.setSilent(true);        // no ambient sounds
        piglin.setRemoveWhenFarAway(false); // won't despawn when players move away
        piglin.setImmuneToZombification(true); // won't convert to zombified piglin in the overworld
        piglin.setAdult();             // prevent random baby spawns

        // Tag with a namespaced key so we can reliably identify and clean up this piglin
        // even if its UUID is lost (e.g. after a server restart or world reload)
        piglin.getPersistentDataContainer().set(shopKey, PersistentDataType.BOOLEAN, true);

        shopEntityIds.add(piglin.getUniqueId());
    }

    public void despawnShopEntity() {
        // Step 1: remove all currently tracked piglins by UUID.
        // Bukkit.getEntity searches across all loaded worlds, so this works even if
        // the active game world reference has already been swapped to a new one.
        for (UUID id : shopEntityIds) {
            Entity entity = Bukkit.getEntity(id);
            if (entity != null) entity.remove();
        }
        shopEntityIds.clear();

        // Step 2: sweep every world for orphaned shop piglins that slipped through
        // UUID tracking. Catches two cases:
        //   - PDC-tagged piglins (spawned after tagging was introduced)
        //   - Pre-PDC piglins (no tag, but identifiable by no-AI + invulnerable,
        //     which is unique to our shop piglins since the game world has mob spawning off)
        for (World world : Bukkit.getWorlds()) {
            for (Piglin piglin : world.getEntitiesByClass(Piglin.class)) {
                if (piglin.getPersistentDataContainer().has(shopKey, PersistentDataType.BOOLEAN)
                        || (!piglin.hasAI() && piglin.isInvulnerable())) {
                    piglin.remove();
                }
            }
        }
    }

    public boolean isShopEntity(UUID uuid) {
        return shopEntityIds.contains(uuid);
    }

    public Map<ShopCategory, List<ShopItem>> getCategoryItems() {
        return categoryItems;
    }
}
