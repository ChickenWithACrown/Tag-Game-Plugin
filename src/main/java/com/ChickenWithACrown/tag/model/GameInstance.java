package com.ChickenWithACrown.tag.model;

import org.bukkit.Location;

public class GameInstance {
    private final String id;
    private final String mode;
    private final int teamSize;
    private String mapName;
    private final GameState gameState;
    private final GameWorld gameWorld;
    private Location signLocation;
    private boolean autoStart;
    private String displayName;

    public GameInstance(String mode, int teamSize, String mapName, GameState gameState, GameWorld gameWorld) {
        this.mode = mode;
        this.teamSize = teamSize;
        this.mapName = mapName;
        this.gameState = gameState;
        this.gameWorld = gameWorld;
        this.displayName = mode + " " + mapName;
        this.id = mode.toLowerCase() + "_" + mapName.toLowerCase();
    }

    public String getId() {
        return id;
    }

    public String getMode() {
        return mode;
    }

    public int getTeamSize() {
        return teamSize;
    }

    public int getMaxPlayers() {
        return teamSize * 2;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
        this.displayName = mode + " " + mapName;
    }

    public GameState getGameState() {
        return gameState;
    }

    public GameWorld getGameWorld() {
        return gameWorld;
    }

    public Location getSignLocation() {
        return signLocation;
    }

    public void setSignLocation(Location signLocation) {
        this.signLocation = signLocation;
    }

    public boolean isAutoStart() {
        return autoStart;
    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    public String getDisplayName() {
        return displayName;
    }
} 