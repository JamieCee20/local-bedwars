package com.example.myplugin.player;

import java.util.UUID;

import com.example.myplugin.enums.GameTeam;

public class PlayerData {

    private final UUID uuid;
    private GameTeam team;

    private boolean alive = true;

    // True once their bed is gone and they died — they become a ghost observer.
    // Unlike 'alive', this never resets during a game; it's cleared when the game ends.
    private boolean eliminated = false;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public boolean isEliminated() {
        return eliminated;
    }

    public void setEliminated(boolean eliminated) {
        this.eliminated = eliminated;
    }

    public UUID getUuid() {
        return uuid;
    }

    public GameTeam getTeam() {
        return team;
    }

    public void setTeam(GameTeam team) {
        this.team = team;
    }
}