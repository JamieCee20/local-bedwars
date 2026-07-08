package com.example.myplugin.game;

import com.example.myplugin.MyPlugin;
import com.example.myplugin.data.ShopItem;
import com.example.myplugin.enums.ShopCategory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Piglin;
import org.bukkit.persistence.PersistentDataType;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ShopManager {
    private final NamespacedKey shopKey;
    private final Set<UUID> shopEntityIds = new HashSet<>();
    private final Map<ShopCategory, List<ShopItem>> categoryItems;

    public ShopManager(MyPlugin plugin) {
        this.shopKey = new NamespacedKey(plugin, "shop_piglin");
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
