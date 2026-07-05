package com.example.myplugin;

import org.bukkit.plugin.java.JavaPlugin;

import com.example.myplugin.command.JoinCommand;
import com.example.myplugin.command.LeaveCommand;
import com.example.myplugin.game.GameManager;
import com.example.myplugin.game.LobbyManager;
import com.example.myplugin.game.SpawnManager;
import com.example.myplugin.player.PlayerManager;

public class MyPlugin extends JavaPlugin {

    private PlayerManager playerManager;
    private GameManager gameManager;
    private LobbyManager lobbyManager;
    private SpawnManager spawnManager;

    @Override
    public void onEnable() {
        playerManager = new PlayerManager();
        gameManager = new GameManager(this);
        lobbyManager = new LobbyManager(this);
        spawnManager = new SpawnManager(getServer().getWorld("world"));
        getCommand("join").setExecutor(new JoinCommand(this));

        getCommand("leave").setExecutor(new LeaveCommand(this));

        getLogger().info("Plugin Enabled");

    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public LobbyManager getLobbyManager() {
        return lobbyManager;
    }

    public SpawnManager getSpawnManager() {
        return spawnManager;
    }

}
