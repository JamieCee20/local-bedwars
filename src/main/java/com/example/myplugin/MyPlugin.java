package com.example.myplugin;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import com.example.myplugin.command.EndGameCommand;
import com.example.myplugin.command.JoinCommand;
import com.example.myplugin.command.LeaveCommand;
import com.example.myplugin.game.GameManager;
import com.example.myplugin.game.LobbyManager;
import com.example.myplugin.game.SpawnManager;
import com.example.myplugin.listener.BlockProtectionListener;
import com.example.myplugin.player.PlayerManager;

public class MyPlugin extends JavaPlugin {

    private PlayerManager playerManager;
    private GameManager gameManager;
    private LobbyManager lobbyManager;
    private SpawnManager spawnManager;
    private final Set<Block> placedBlocks = new HashSet<>();

    @Override
    public void onEnable() {
        playerManager = new PlayerManager();
        gameManager = new GameManager(this);
        lobbyManager = new LobbyManager(this);
        spawnManager = new SpawnManager(getServer().getWorld("world"));
        getCommand("join").setExecutor(new JoinCommand(this));

        getCommand("leave").setExecutor(new LeaveCommand(this));

        getCommand("endgame").setExecutor(new EndGameCommand(this));

        getServer().getPluginManager().registerEvents(
                new BlockProtectionListener(this),
                this);

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

    public Set<Block> getPlacedBlocks() {
        return placedBlocks;
    }

}
