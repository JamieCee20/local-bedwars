package com.example.myplugin.game;

import com.example.myplugin.MyPlugin;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InstanceManager {
    private final MyPlugin plugin;

    // All currently active game instances - regardless of game state
    private final List<GameInstance> instances = new ArrayList<>();

    // Simple counter used to give each game world a unique game
    //TODO: Change to a short uuid?
    private int nextId = 1;

    public InstanceManager(MyPlugin plugin) {
        this.plugin = plugin;
    }

    // Called by /join. Returns a LOBBY instance with room, or creates a fresh one.
    // This is where the "matchmaking" logic lives — for now it's just first-available.
    public GameInstance getOrCreateAvailableInstance() {
        for (GameInstance instance : instances) {
            // Only let players join a game that is still in the lobby/waiting phase
            if (instance.getGameManager().isLobby()) {
                return instance;
            }
        }
        // No available instance — spin up a brand new one
        return createInstance();
    }

    public GameInstance createInstance() {
        // Each game world needs a unique name so they don't collide
        String worldName = "bedwars_game_" + nextId++;
        GameInstance instance = new GameInstance(plugin,worldName);
        instances.add(instance);
        return instance;
    }

    // Looks up which game a player currently belongs to.
    // Returns null if the player isn't in any game (i.e. they're in the hub).
    public GameInstance getInstanceForPlayer(UUID uuid) {
        for (GameInstance instance : instances) {
            if (instance.getPlayerManager().isInGame(uuid)) {
                return instance;
            }
        }
        return null;
    }

    // Looks up which game owns a particular world.
    // Used by block-event listeners so they know which game's state to check.
    public GameInstance getInstanceForWorld(World world) {
        for (GameInstance instance : instances) {
            if (world.equals(instance.getGameWorld())) {
                return instance;
            }
        }
        return null;
    }

    // Called at the end of GameManager.endGame() once the world is torn down.
    // Removes the completed game from the list so it can be garbage-collected.
    public void removeInstance(GameInstance instance) {
        instances.remove(instance);
    }

    public List<GameInstance> getInstances() {
        return instances;
    }
}
