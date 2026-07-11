package com.example.myplugin;

import com.example.myplugin.command.*;
import com.example.myplugin.game.InstanceManager;
import com.example.myplugin.listener.*;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class MyPlugin extends JavaPlugin {

    // The hub/lobby world. Set once at startup — never changes.
    private World lobbyWorld;

    // Manages all active game instances. This replaces the single-game managers
    // that used to live directly on the plugin.
    private InstanceManager instanceManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        String lobbyWorldName = getConfig().getString("game.world", "world");
        lobbyWorld = getServer().getWorld(lobbyWorldName);
        if (lobbyWorld == null) {
            getLogger().severe("Lobby world '" + lobbyWorldName + "' not found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // The instance manager is the new central hub. It creates GameInstances on demand.
        instanceManager = new InstanceManager(this);

        // Commands still take 'this' (the plugin) so they can reach instanceManager
        getCommand("join").setExecutor(new JoinCommand(this));
        getCommand("leave").setExecutor(new LeaveCommand(this));
        getCommand("endgame").setExecutor(new EndGameCommand(this));
        getCommand("bwsetup").setExecutor(new SetupWorldCommand(this));
        getCommand("bwreload").setExecutor(new ReloadCommand(this));

        // Listeners also take 'this' — they look up the right GameInstance via instanceManager
        getServer().getPluginManager().registerEvents(new BlockProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerFreezeListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);
        getServer().getPluginManager().registerEvents(new GhostListener(this), this);

        getLogger().info("Plugin Enabled");
    }

    public World getLobbyWorld()             { return lobbyWorld; }
    public InstanceManager getInstanceManager() { return instanceManager; }
}