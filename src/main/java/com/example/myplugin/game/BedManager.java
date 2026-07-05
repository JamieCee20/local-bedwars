package com.example.myplugin.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;

import com.example.myplugin.enums.GameTeam;

public class BedManager {
    private final Map<GameTeam, Location> beds = new HashMap<>();
    private final Map<GameTeam, Boolean> bedAlive = new HashMap<>();

    public void setBed(GameTeam team, Location loc) {
        beds.put(team, loc);
        bedAlive.put(team, true);
    }

    public GameTeam getTeamFromLocation(Location loc) {
        for (Map.Entry<GameTeam, Location> entry : beds.entrySet()) {
            if (entry.getValue().getBlock().equals(loc.getBlock())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public boolean isBedAlive(GameTeam team) {
        return bedAlive.getOrDefault(team, false);
    }

    public void breakBed(GameTeam team) {
        bedAlive.put(team, false);
    }

    public Set<GameTeam> getAllTeams() {
        return beds.keySet();
    }
}
