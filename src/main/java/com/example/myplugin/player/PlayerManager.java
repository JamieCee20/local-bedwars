package com.example.myplugin.player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {
    private final Map<UUID, PlayerData> players = new HashMap<>();

    public PlayerData addPlayer(UUID uuid) {
        PlayerData data = new PlayerData(uuid);
        players.put(uuid, data);
        return data;
    }

    public void removePlayer(UUID uuid) {
        players.remove(uuid);
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
}
