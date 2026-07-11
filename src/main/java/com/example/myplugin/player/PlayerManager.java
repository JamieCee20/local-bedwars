package com.example.myplugin.player;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;

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

    // Shows a title screen to every registered player in this game.
// Used instead of getServer().showTitle() so only in-game players see it.
    public void showTitle(Title title) {
        for (PlayerData data : players.values()) {
            var player = Bukkit.getPlayer(data.getUuid());
            if (player != null) player.showTitle(title);
        }
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

    public void broadcast(Component message) {
        for (PlayerData data : players.values()) {
            var player = Bukkit.getPlayer(data.getUuid());
            if (player != null) player.sendMessage(message);
        }
    }
}
