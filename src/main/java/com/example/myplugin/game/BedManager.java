package com.example.myplugin.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.Location;
import org.bukkit.block.data.type.Bed;

import com.example.myplugin.enums.GameTeam;

public class BedManager {
    private final Map<GameTeam, Location> beds = new HashMap<>();
    private final Map<GameTeam, Boolean> bedAlive = new HashMap<>();

    public void setBed(GameTeam team, Location loc) {
        beds.put(team, loc);
        bedAlive.put(team, true);
    }

    public GameTeam getTeamFromLocation(Location loc) {
        Block broken = loc.getBlock();

        for (Map.Entry<GameTeam, Location> entry : beds.entrySet()) {
            Block stored = entry.getValue().getBlock();

            // Direct match — player broke the exact stored block
            if (stored.equals(broken)) {
                return entry.getKey();
            }

            // Beds are two blocks (head + foot). Check if the broken block is
            // the other half of a stored bed location.
            if (broken.getBlockData() instanceof Bed bedData) {
                Block otherHalf = getOtherBedHalf(broken, bedData);
                if (otherHalf != null && stored.equals(otherHalf)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    private Block getOtherBedHalf(Block block, Bed bedData) {
        BlockFace facing = bedData.getFacing();
        return bedData.getPart() == Bed.Part.FOOT
                ? block.getRelative(facing)                  // foot → head is in facing direction
                : block.getRelative(facing.getOppositeFace()); // head → foot is behind
    }

    public boolean isBedAlive(GameTeam team) {
        return bedAlive.getOrDefault(team, false);
    }

    public void breakBed(GameTeam team) {
        bedAlive.put(team, false);
    }

    public void resetBeds() {
        for (GameTeam team : beds.keySet()) {
            bedAlive.put(team, true);
        }
    }

    public Set<GameTeam> getAllTeams() {
        return beds.keySet();
    }
}
