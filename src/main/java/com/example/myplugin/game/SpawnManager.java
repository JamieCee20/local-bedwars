package com.example.myplugin.game;

import java.util.EnumMap;
import java.util.Map;

import org.bukkit.Location;

import com.example.myplugin.enums.GameTeam;

public class SpawnManager {
    private final Map<GameTeam, Location> spawns = new EnumMap<>(GameTeam.class);

    public void setSpawn(GameTeam team, Location location) {
        spawns.put(team, location);
    }

    public Location getSpawn(GameTeam team) {
        return spawns.get(team);
    }
}
