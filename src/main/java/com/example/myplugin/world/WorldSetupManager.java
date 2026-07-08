package com.example.myplugin.world;

import java.io.File;
import java.io.FileInputStream;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.example.myplugin.MyPlugin;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;

public class WorldSetupManager {

    private static final String GAME_WORLD_NAME = "bedwars_game";

    private final MyPlugin plugin;

    public WorldSetupManager(MyPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Creates a fresh void world and pastes the configured schematic into it.
     * Must be called on the main thread.
     *
     * @return true on success, false if world creation or schematic paste failed
     */
    public boolean setupGameWorld() {
        teardownGameWorld(); // clean up any leftover world from a previous run

        World gameWorld = createVoidWorld();
        if (gameWorld == null) {
            plugin.getLogger().severe("Failed to create game world '" + GAME_WORLD_NAME + "'.");
            return false;
        }

        gameWorld.setSpawnFlags(false, false); // no hostile, no passive mobs

        configureArenaWorld(gameWorld);

        if (!pasteSchematic(gameWorld)) {
            // Cleanup the empty world so it doesn't linger
            Bukkit.unloadWorld(gameWorld, false);
            return false;
        }

        // Update the plugin's active game world and re-register all team locations
        plugin.setGameWorld(gameWorld);
        return true;
    }

    /**
     * Setup time and weather
     *
     * @param world
     */
    private void configureArenaWorld(World world) {
        world.setStorm(false);
        world.setThundering(false);
        world.setClearWeatherDuration(Integer.MAX_VALUE);

        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setGameRule(GameRule.NATURAL_REGENERATION, false);

        world.setTime(6000);
    }

    /**
     * Unloads and permanently deletes the game world from disk.
     * Any players still inside are moved to the lobby world first.
     */
    public void teardownGameWorld() {
        World existing = Bukkit.getWorld(GAME_WORLD_NAME);
        if (existing == null) return;

        World lobbyWorld = plugin.getLobbyWorld();
        for (Player player : existing.getPlayers()) {
            player.teleport(lobbyWorld.getSpawnLocation());
        }

        Bukkit.unloadWorld(existing, false); // false = don't save

        File worldFolder = new File(Bukkit.getWorldContainer(), GAME_WORLD_NAME);
        deleteFolder(worldFolder);
    }

    // -------------------------------------------------------------------------

    private World createVoidWorld() {
        WorldCreator creator = new WorldCreator(GAME_WORLD_NAME)
                .generator(new VoidGenerator())
                .environment(World.Environment.NORMAL)
                .generateStructures(false);
        return Bukkit.createWorld(creator);
    }

    private boolean pasteSchematic(World world) {
        File schematicFile = resolveSchematicFile();

        if (schematicFile == null) {
            return false;
        }

        ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
        if (format == null) {
            plugin.getLogger().severe("Unknown schematic format for file: " + schematicFile.getName());
            return false;
        }

        ConfigurationSection originSec = plugin.getConfig().getConfigurationSection("map.origin");
        int ox = originSec != null ? originSec.getInt("x", 0) : 0;
        int oy = originSec != null ? originSec.getInt("y", 64) : 64;
        int oz = originSec != null ? originSec.getInt("z", 0) : 0;

        try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
            Clipboard clipboard = reader.read();

            com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(world);
            try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder()
                    .world(weWorld)
                    .build()) {

                Operation paste = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BlockVector3.at(ox, oy, oz))
                        .ignoreAirBlocks(false)
                        .build();
                Operations.complete(paste);
            }

            plugin.getLogger().info("Schematic pasted at (" + ox + ", " + oy + ", " + oz + ").");
            return true;

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to paste schematic: " + e.getMessage());
            return false;
        }
    }

    private void deleteFolder(File folder) {
        if (!folder.exists()) return;
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolder(file);
                } else {
                    file.delete();
                }
            }
        }
        folder.delete();
    }

    /**
     * Resolves the schematic file by checking, in order:
     *   1. plugins/MyPlugin/<name>          (explicit placement next to config)
     *   2. plugins/WorldEdit/schematics/<name>  (WorldEdit's default folder)
     *
     * Returns null and logs an error if not found in either location.
     */
    public File resolveSchematicFile() {
        String schematicName = plugin.getConfig().getString("map.schematic", "map.schem");

        File pluginFolder = new File(plugin.getDataFolder(), schematicName);
        if (pluginFolder.exists()) return pluginFolder;

        File weFolder = new File(plugin.getDataFolder().getParentFile(),
                "WorldEdit/schematics/" + schematicName);
        if (weFolder.exists()) return weFolder;

        plugin.getLogger().severe(
                "Schematic '" + schematicName + "' not found. Checked:"
                + "\n  " + pluginFolder.getPath()
                + "\n  " + weFolder.getPath());
        return null;
    }
}
