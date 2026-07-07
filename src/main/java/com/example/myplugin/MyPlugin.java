package com.example.myplugin;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import com.example.myplugin.command.EndGameCommand;
import com.example.myplugin.command.JoinCommand;
import com.example.myplugin.command.LeaveCommand;
import com.example.myplugin.command.ReloadCommand;
import com.example.myplugin.command.SetupWorldCommand;
import com.example.myplugin.enums.GameTeam;
import com.example.myplugin.game.BedManager;
import com.example.myplugin.game.GameManager;
import com.example.myplugin.game.GeneratorManager;
import com.example.myplugin.game.LobbyManager;
import com.example.myplugin.game.SpawnManager;
import com.example.myplugin.listener.BlockProtectionListener;
import com.example.myplugin.listener.PlayerDeathListener;
import com.example.myplugin.listener.PlayerFreezeListener;
import com.example.myplugin.listener.PlayerJoinListener;
import com.example.myplugin.listener.PlayerQuitListener;
import com.example.myplugin.player.PlayerManager;
import com.example.myplugin.world.WorldSetupManager;

public class MyPlugin extends JavaPlugin {

    private PlayerManager playerManager;
    private GameManager gameManager;
    private LobbyManager lobbyManager;
    private SpawnManager spawnManager;
    private BedManager bedManager;
    private GeneratorManager generatorManager;
    private WorldSetupManager worldSetupManager;

    /** The static lobby world — set once at startup, never changes. */
    private World lobbyWorld;

    /** The active game world — replaced each time a new game is prepared. */
    private World gameWorld;

    private final Set<Block> placedBlocks = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        String lobbyWorldName = getConfig().getString("game.world", "world");
        lobbyWorld = getServer().getWorld(lobbyWorldName);
        if (lobbyWorld == null) {
            getLogger().severe("Lobby world '" + lobbyWorldName + "' not found! Check 'game.world' in config.yml.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        playerManager = new PlayerManager();
        gameManager = new GameManager(this);
        lobbyManager = new LobbyManager(this);
        spawnManager = new SpawnManager();
        bedManager = new BedManager();
        generatorManager = new GeneratorManager();
        worldSetupManager = new WorldSetupManager(this);

        getCommand("join").setExecutor(new JoinCommand(this));
        getCommand("leave").setExecutor(new LeaveCommand(this));
        getCommand("endgame").setExecutor(new EndGameCommand(this));
        getCommand("bwsetup").setExecutor(new SetupWorldCommand(this));
        getCommand("bwreload").setExecutor(new ReloadCommand(this));

        getServer().getPluginManager().registerEvents(new BlockProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerFreezeListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);

        getLogger().info("Plugin Enabled");
    }

    /**
     * Called by WorldSetupManager once a new game world is ready.
     * Updates the active game world reference and reloads all team locations into
     * SpawnManager and BedManager so they point at the correct world.
     */
    public void setGameWorld(World world) {
        this.gameWorld = world;
        reloadTeamLocations(world);
    }

    public void reloadTeamLocations(World world) {
        // Resolve the paste origin once — all team offsets are relative to this point
        ConfigurationSection originSec = getConfig().getConfigurationSection("map.origin");
        double ox = originSec != null ? originSec.getDouble("x", 0) : 0;
        double oy = originSec != null ? originSec.getDouble("y", 64) : 64;
        double oz = originSec != null ? originSec.getDouble("z", 0) : 0;

        for (GameTeam team : GameTeam.values()) {
            String key = "teams." + team.name();
            ConfigurationSection sec = getConfig().getConfigurationSection(key);
            if (sec == null) {
                getLogger().warning("No config section for team: " + team.name());
                continue;
            }

            Location spawn = readOffsetLocation(sec.getConfigurationSection("spawn"), world, ox, oy, oz);
            if (spawn != null) {
                spawnManager.setSpawn(team, spawn);
            } else {
                getLogger().warning("Missing spawn for team: " + team.name());
            }

            Location bed = readOffsetLocation(sec.getConfigurationSection("bed"), world, ox, oy, oz);
            if (bed != null) {
                bedManager.setBed(team, bed);
            } else {
                getLogger().warning("Missing bed for team: " + team.name());
            }
        }

        // Load generator locations (also offsets from origin)
        generatorManager.reset();
        var diamondList = getConfig().getMapList("generators.diamonds");
        for (var entry : diamondList) {
            double x = ((Number) entry.get("x")).doubleValue();
            double y = ((Number) entry.get("y")).doubleValue();
            double z = ((Number) entry.get("z")).doubleValue();
            generatorManager.addDiamondSpawn(new Location(world, x + ox, y + oy, z + oz));
        }
        var emeraldList = getConfig().getMapList("generators.emeralds");
        for (var entry : emeraldList) {
            double x = ((Number) entry.get("x")).doubleValue();
            double y = ((Number) entry.get("y")).doubleValue();
            double z = ((Number) entry.get("z")).doubleValue();
            generatorManager.addEmeraldSpawns(new Location(world, x + ox, y + oy, z + oz));
        }

        for (GameTeam team : GameTeam.values()) {
            String path = "teams." + team.name();
            var ironSec = getConfig().getConfigurationSection(path + ".generators.iron");
            var goldSec = getConfig().getConfigurationSection(path + ".generators.gold");

            if (ironSec != null) {
                double x = ironSec.getDouble("x"), y = ironSec.getDouble("y"), z = ironSec.getDouble("z");
                generatorManager.addIronSpawn(new Location(world, x + ox, y + oy, z + oz));
            }
            if (goldSec != null) {
                double x = goldSec.getDouble("x"), y = goldSec.getDouble("y"), z = goldSec.getDouble("z");
                generatorManager.addGoldSpawn(new Location(world, x + ox, y + oy, z + oz));
            }
        }
    }

    /**
     * Builds a Location from a config section whose x/y/z values are offsets
     * relative to the schematic paste origin (ox, oy, oz).
     */
    private Location readOffsetLocation(ConfigurationSection sec, World world, double ox, double oy, double oz) {
        if (sec == null) return null;
        return new Location(
                world,
                sec.getDouble("x") + ox,
                sec.getDouble("y") + oy,
                sec.getDouble("z") + oz
        );
    }

    // -------------------------------------------------------------------------

    public World getLobbyWorld() {
        return lobbyWorld;
    }

    public World getGameWorld() {
        return gameWorld;
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

    public BedManager getBedManager() {
        return bedManager;
    }

    public GeneratorManager getGeneratorManager() {
        return generatorManager;
    }

    public WorldSetupManager getWorldSetupManager() {
        return worldSetupManager;
    }

    public Set<Block> getPlacedBlocks() {
        return placedBlocks;
    }
}
