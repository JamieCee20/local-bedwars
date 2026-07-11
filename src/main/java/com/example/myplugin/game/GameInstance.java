package com.example.myplugin.game;

import com.example.myplugin.MyPlugin;
import com.example.myplugin.enums.GameTeam;
import com.example.myplugin.player.PlayerManager;
import com.example.myplugin.world.WorldSetupManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashSet;
import java.util.Set;

public class GameInstance {

    // A reference back to the plugin for global things (server, config, lobby world).
    // Per-game things come from this class instead.
    private final MyPlugin plugin;

    // Each instance gets a unique world name so multiple games can run simultaneously
    // without their worlds colliding (e.g. "bedwars_game_1", "bedwars_game_2").
    private final String worldName;

    // --- Per-game managers ---
    // Every game has its own copy of each manager so state is completely isolated.
    private final PlayerManager playerManager;
    private final GameManager gameManager;
    private final LobbyManager lobbyManager;
    private final SpawnManager spawnManager;
    private final BedManager bedManager;
    private final GeneratorManager generatorManager;
    private final ShopManager shopManager;
    private final WorldSetupManager worldSetupManager;

    // Tracks blocks placed by players so only those can be broken mid-game.
    // Per-instance so each game has its own set of placed blocks.
    private final Set<Block> placedBlocks = new HashSet<>();

    // The live Bukkit world for this game. Null until the world has been set up.
    private World gameWorld;

    public GameInstance(MyPlugin plugin, String worldName) {
        this.plugin = plugin;
        this.worldName = worldName;

        // Build each manager. From this point on, managers receive 'this' (the
        // GameInstance) rather than the plugin, so they can only touch their own game's state.
        playerManager      = new PlayerManager();
        spawnManager       = new SpawnManager();
        bedManager         = new BedManager();
        generatorManager   = new GeneratorManager();
        gameManager        = new GameManager(this);
        lobbyManager       = new LobbyManager(this);
        shopManager        = new ShopManager(this);
        worldSetupManager  = new WorldSetupManager(this);
    }

    // Called by WorldSetupManager once the game world is ready.
    // Stores the world reference and loads all team locations into their managers.
    public void setGameWorld(World world) {
        this.gameWorld = world;
        reloadTeamLocations(world);
    }

    // Reads every team's spawn / bed / shop / generator offsets from config and
    // resolves them to real world coordinates by adding the schematic origin.
    // Moved here from MyPlugin so each instance can load into its own world.
    public void reloadTeamLocations(World world) {
        shopManager.despawnShopEntity();

        ConfigurationSection originSec = plugin.getConfig().getConfigurationSection("map.origin");
        double ox = originSec != null ? originSec.getDouble("x", 0) : 0;
        double oy = originSec != null ? originSec.getDouble("y", 64) : 64;
        double oz = originSec != null ? originSec.getDouble("z", 0) : 0;

        for (GameTeam team : GameTeam.values()) {
            String key = "teams." + team.name();
            ConfigurationSection sec = plugin.getConfig().getConfigurationSection(key);
            if (sec == null) {
                plugin.getLogger().warning("No config section for team: " + team.name());
                continue;
            }

            Location spawn = readOffsetLocation(sec.getConfigurationSection("spawn"), world, ox, oy, oz);
            if (spawn != null) spawnManager.setSpawn(team, spawn);

            Location bed = readOffsetLocation(sec.getConfigurationSection("bed"), world, ox, oy, oz);
            if (bed != null) bedManager.setBed(team, bed);

            Location shop = readOffsetLocation(sec.getConfigurationSection("shop"), world, ox, oy, oz);
            if (shop != null) shopManager.spawnShopEntity(shop);
        }

        generatorManager.reset();

        for (var entry : plugin.getConfig().getMapList("generators.diamonds")) {
            double x = ((Number) entry.get("x")).doubleValue();
            double y = ((Number) entry.get("y")).doubleValue();
            double z = ((Number) entry.get("z")).doubleValue();
            generatorManager.addDiamondSpawn(new Location(world, x + ox, y + oy, z + oz));
        }
        for (var entry : plugin.getConfig().getMapList("generators.emeralds")) {
            double x = ((Number) entry.get("x")).doubleValue();
            double y = ((Number) entry.get("y")).doubleValue();
            double z = ((Number) entry.get("z")).doubleValue();
            generatorManager.addEmeraldSpawns(new Location(world, x + ox, y + oy, z + oz));
        }
        for (GameTeam team : GameTeam.values()) {
            String path = "teams." + team.name();
            var ironSec = plugin.getConfig().getConfigurationSection(path + ".generators.iron");
            var goldSec = plugin.getConfig().getConfigurationSection(path + ".generators.gold");
            if (ironSec != null) {
                generatorManager.addIronSpawn(new Location(world,
                    ironSec.getDouble("x") + ox, ironSec.getDouble("y") + oy, ironSec.getDouble("z") + oz));
            }
            if (goldSec != null) {
                generatorManager.addGoldSpawn(new Location(world,
                    goldSec.getDouble("x") + ox, goldSec.getDouble("y") + oy, goldSec.getDouble("z") + oz));
            }
        }
    }

    private Location readOffsetLocation(ConfigurationSection sec, World world, double ox, double oy, double oz) {
        if (sec == null) return null;
        return new Location(world,
            sec.getDouble("x") + ox,
            sec.getDouble("y") + oy,
            sec.getDouble("z") + oz,
            (float) sec.getDouble("yaw", 0.0),
            0f);
    }

    // --- Getters ---

    public MyPlugin getPlugin()                  { return plugin; }
    public String getWorldName()                 { return worldName; }
    public World getGameWorld()                  { return gameWorld; }
    public PlayerManager getPlayerManager()      { return playerManager; }
    public GameManager getGameManager()          { return gameManager; }
    public LobbyManager getLobbyManager()        { return lobbyManager; }
    public SpawnManager getSpawnManager()        { return spawnManager; }
    public BedManager getBedManager()            { return bedManager; }
    public GeneratorManager getGeneratorManager(){ return generatorManager; }
    public ShopManager getShopManager()          { return shopManager; }
    public WorldSetupManager getWorldSetupManager() { return worldSetupManager; }
    public Set<Block> getPlacedBlocks()          { return placedBlocks; }
}