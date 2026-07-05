package com.example.myplugin.player;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerManager {
    private final Map<UUID, PlayerData> players = new HashMap<>();
    private final Set<UUID> frozenPlayers = new HashSet<>();

    public PlayerData addPlayer(UUID uuid) {
        PlayerData data = new PlayerData(uuid);
        players.put(uuid, data);
        return data;
    }

    public void removePlayer(UUID uuid) {
        players.remove(uuid);
        frozenPlayers.remove(uuid);
    }

    public boolean isInGame(UUID uuid) {
        return players.containsKey(uuid);
    }

    public PlayerData getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    public Collection<PlayerData> getPlayers() {
        return players.values();
    }

    public int getCount() {
        return players.size();
    }

    public void clearPlayers() {
        players.clear();
        frozenPlayers.clear();
    }

    public void freezePlayer(UUID uuid) {
        frozenPlayers.add(uuid);
    }

    public void unfreezeAll() {
        frozenPlayers.clear();
    }

    public boolean isFrozen(UUID uuid) {
        return frozenPlayers.contains(uuid);
    }
}
