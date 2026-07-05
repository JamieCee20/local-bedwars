package com.example.myplugin.player;

import java.util.UUID;

import com.example.myplugin.enums.GameTeam;

public class PlayerData {

    private final UUID uuid;
    private GameTeam team;

    private boolean alive = true;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
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