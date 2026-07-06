package com.example.myplugin.game;

import com.example.myplugin.MyPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class GeneratorManager {
    private final List<Location> diamondSpawns = new ArrayList<>();
    private final List<Location> emeraldSpawns = new ArrayList<>();
    private BukkitRunnable diamondTask;
    private BukkitRunnable emeraldTask;
    private BukkitRunnable cleanupTask;

    public void addDiamondSpawn(Location loc) {
        diamondSpawns.add(loc);
    }

    public void addEmeraldSpawns(Location loc) {
        emeraldSpawns.add(loc);
    }

    public void start(MyPlugin plugin) {
        // 🟡 DIAMONDS (15s)
        diamondTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Location loc : diamondSpawns) {
                    spawnItem(loc, Material.DIAMOND);
                }
            }
        };
        diamondTask.runTaskTimer(plugin, 20L * 15, 20L * 20);

        // 🟢 EMERALDS (25s)
        emeraldTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Location loc : emeraldSpawns) {
                    spawnItem(loc, Material.EMERALD);
                }
            }
        };
        emeraldTask.runTaskTimer(plugin, 20L * 25, 20L * 60);
    }

    private void spawnItem(Location loc, Material material) {
        Item item = loc.getWorld().dropItem(loc.clone().add(0.5, 1.2, 0.5), new ItemStack(material));
        item.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
        item.setPickupDelay(10);
        item.setCanMobPickup(false);
    }

    public void startCleanupTask(MyPlugin plugin) {
        cleanupTask = new BukkitRunnable() {
            @Override
            public void run() {

                for (Entity entity : plugin.getGameWorld().getEntities()) {

                    if (entity instanceof Item item) {

                        if (isNearGenerator(item.getLocation())) {
                            item.setTicksLived(1);
                        }
                    }
                }
            }
        };
        cleanupTask.runTaskTimer(plugin, 0L, 20L * 30);
    }

    public void stop() {
        if (diamondTask != null) { diamondTask.cancel(); diamondTask = null; }
        if (emeraldTask != null) { emeraldTask.cancel(); emeraldTask = null; }
        if (cleanupTask != null) { cleanupTask.cancel(); cleanupTask = null; }
    }

    public void reset() {
        stop();
        diamondSpawns.clear();
        emeraldSpawns.clear();
    }

    private boolean isNearGenerator(Location loc) {

        for (Location d : diamondSpawns) {
            if (d.getWorld().equals(loc.getWorld()) && d.distance(loc) < 3) return true;
        }

        for (Location e : emeraldSpawns) {
            if (e.getWorld().equals(loc.getWorld()) && e.distance(loc) < 3) return true;
        }

        return false;
    }

}
