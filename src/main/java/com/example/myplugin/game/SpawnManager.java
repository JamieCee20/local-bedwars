package com.example.myplugin.game;

import org.bukkit.Location;
import org.bukkit.World;

public class SpawnManager {
    private final World world;

    public SpawnManager(World world) {
        this.world = world;
    }

    public Location getRedSpawn() {
        return new Location(world, 776, 69, 73);
    }

    public Location getBlueSpawn() {
        return new Location(world, 718, 69, 17);
    }
}
