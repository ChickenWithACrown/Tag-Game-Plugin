package com.ChickenWithACrown.tag.controller;

import com.ChickenWithACrown.tag.TagGame;
import com.ChickenWithACrown.tag.model.GameState;
import com.ChickenWithACrown.tag.model.GameWorld;
import com.ChickenWithACrown.tag.model.GameInstance;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Sound;
import java.util.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import java.util.Arrays;
import org.bukkit.Location;
import org.bukkit.block.Sign;

public class GameController {
    private final TagGame plugin;
    private final Map<String, GameInstance> gameInstances = new HashMap<>();
    private static final int GAME_DURATION = 600; // 10 minutes in seconds
    private static final int COUNTDOWN_DURATION = 10; // 10 seconds countdown

    public GameController(TagGame plugin) {
        this.plugin = plugin;
        startGameTimer();
        loadGameInstances(); // Load saved games on startup
    }

    private void loadGameInstances() {
        if (plugin.getConfig().contains("games")) {
            for (String gameId : plugin.getConfig().getConfigurationSection("games").getKeys(false)) {
                String mode = plugin.getConfig().getString("games." + gameId + ".mode");
                int teamSize = plugin.getConfig().getInt("games." + gameId + ".teamSize");
                String mapName = plugin.getConfig().getString("games." + gameId + ".mapName");
                Location signLoc = (Location) plugin.getConfig().get("games." + gameId + ".signLocation");
                
                if (mode != null && mapName != null && signLoc != null) {
                    GameState gameState = new GameState(plugin);
                    GameWorld gameWorld = new GameWorld(plugin);
                    GameInstance instance = new GameInstance(mode, teamSize, mapName, gameState, gameWorld);
                    instance.setSignLocation(signLoc);
                    gameInstances.put(gameId, instance);
                }
            }
        }
    }

    public void saveGameInstance(GameInstance instance) {
        String id = instance.getId();
        plugin.getConfig().set("games." + id + ".mode", instance.getMode());
        plugin.getConfig().set("games." + id + ".teamSize", instance.getTeamSize());
        plugin.getConfig().set("games." + id + ".mapName", instance.getMapName());
        plugin.getConfig().set("games." + id + ".signLocation", instance.getSignLocation());
        plugin.saveConfig();
    }

    public GameInstance createGameInstance(String mode, int teamSize, String mapName, Location signLocation) {
        GameState gameState = new GameState(plugin);
        GameWorld gameWorld = new GameWorld(plugin);
        GameInstance instance = new GameInstance(mode, teamSize, mapName, gameState, gameWorld);
        instance.setSignLocation(signLocation);
        gameInstances.put(instance.getId(), instance);
        saveGameInstance(instance); // Save the new instance
        return instance;
    }

    public GameInstance getGameInstance(String id) {
        return gameInstances.get(id);
    }

    public GameInstance getGameInstanceBySign(Location signLocation) {
        return gameInstances.values().stream()
            .filter(instance -> instance.getSignLocation().equals(signLocation))
            .findFirst()
            .orElse(null);
    }

    public void removeGameInstance(String id) {
        GameInstance instance = gameInstances.remove(id);
        if (instance != null && instance.getGameState().isGameRunning()) {
            stopGame(instance);
        }
    }

    public void startGame(GameInstance instance) {
        GameState gameState = instance.getGameState();
        GameWorld gameWorld = instance.getGameWorld();

        if (gameState.getPlayersInGame().size() < instance.getTeamSize() * 2) {
            Bukkit.broadcastMessage(ChatColor.RED + "Not enough players to start " + instance.getDisplayName());
            return;
        }

        if (!gameWorld.isWorldReady()) {
            Bukkit.broadcastMessage(ChatColor.RED + "Error: Game world is not ready!");
            return;
        }

        gameState.setGameRunning(true);
        gameState.setGameTime(0);
        UUID randomPlayer = getRandomPlayer(gameState);
        gameState.setCurrentIt(randomPlayer);
        gameState.setLastIt(null);

        // Teleport players and give them tools
        for (UUID uuid : gameState.getPlayersInGame()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                if (uuid.equals(randomPlayer)) {
                    p.teleport(gameWorld.getItSpawnLocation());
                    p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 0));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 200, 255));
                    p.sendMessage(ChatColor.RED + "You're IT! Wait for the countdown to end!");
                    giveItTools(p);
                } else {
                    p.teleport(gameWorld.getPlayerSpawnLocation());
                    p.sendMessage(ChatColor.GREEN + "The game is starting! Run!");
                    giveRunnerTools(p);
                }
                showGameUI(p, instance);
                showCountdown(p, COUNTDOWN_DURATION);
            }
        }

        // Start game timer
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!gameState.isGameRunning()) {
                    this.cancel();
                    return;
                }

                int timeLeft = GAME_DURATION - gameState.getGameTime();
                if (timeLeft <= 0) {
                    stopGame(instance);
                    this.cancel();
                    return;
                }

                if (timeLeft % 60 == 0) {
                    Bukkit.broadcastMessage(ChatColor.YELLOW + instance.getDisplayName() + ": " + 
                        String.valueOf(timeLeft / 60) + " minutes remaining!");
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void stopGame(GameInstance instance) {
        GameState gameState = instance.getGameState();
        gameState.setGameRunning(false);
        gameState.setCurrentIt(null);
        gameState.setLastIt(null);
        Bukkit.broadcastMessage(ChatColor.GRAY + instance.getDisplayName() + " stopped.");

        // Teleport players back to spawn
        for (UUID uuid : gameState.getPlayersInGame()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawn " + p.getName());
                p.sendMessage(ChatColor.GREEN + "You've been teleported back to spawn!");
                p.getInventory().clear();
            }
        }

        // Update leaderboard
        plugin.getGameView().updateLeaderboardHologram();
        showFinalScores(gameState);
        gameState.reset();
    }

    private UUID getRandomPlayer(GameState gameState) {
        List<UUID> list = new ArrayList<>(gameState.getPlayersInGame());
        return list.get(new Random().nextInt(list.size()));
    }

    private void showGameUI(Player player, GameInstance instance) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!instance.getGameState().isGameRunning()) {
                    this.cancel();
                    return;
                }

                int timeLeft = GAME_DURATION - instance.getGameState().getGameTime();
                if (timeLeft <= 0) {
                    this.cancel();
                    return;
                }

                // Create scoreboard
                String title = ChatColor.GOLD + "=== " + instance.getDisplayName() + " ===";
                String time = ChatColor.YELLOW + "Time Left: " + ChatColor.WHITE + 
                    String.format("%02d:%02d", timeLeft / 60, timeLeft % 60);
                String mode = ChatColor.AQUA + "Mode: " + ChatColor.WHITE + instance.getMode();
                String it = ChatColor.RED + "IT: " + ChatColor.WHITE + 
                    (instance.getGameState().getCurrentIt() != null ? 
                    Bukkit.getPlayer(instance.getGameState().getCurrentIt()).getName() : "None");
                String players = ChatColor.GREEN + "Players: " + ChatColor.WHITE + 
                    instance.getGameState().getPlayersInGame().size();

                player.sendMessage("\n" + title + "\n" + time + "\n" + mode + "\n" + it + "\n" + players + "\n");
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void startGameTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (GameInstance instance : gameInstances.values()) {
                    GameState gameState = instance.getGameState();
                    if (gameState.isGameRunning()) {
                        gameState.setGameTime(gameState.getGameTime() + 1);
                        gameState.setPowerUpTimer(gameState.getPowerUpTimer() + 1);
                        
                        if (gameState.getPowerUpTimer() >= 2400) {
                            spawnPowerUp(instance);
                            gameState.setPowerUpTimer(0);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void spawnPowerUp(GameInstance instance) {
        GameState gameState = instance.getGameState();
        if (!gameState.isGameRunning() || gameState.getPlayersInGame().isEmpty()) return;
        
        List<UUID> players = new ArrayList<>(gameState.getPlayersInGame());
        UUID randomPlayer = players.get(new Random().nextInt(players.size()));
        Player p = Bukkit.getPlayer(randomPlayer);
        
        if (p != null) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
            p.sendMessage(ChatColor.GOLD + "You got a speed boost!");
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        }
    }

    private void showFinalScores(GameState gameState) {
        Bukkit.broadcastMessage(ChatColor.GOLD + "=== Final Scores ===");
        gameState.getPlayerScores().entrySet().stream()
            .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
            .forEach(entry -> {
                Player p = Bukkit.getPlayer(entry.getKey());
                if (p != null) {
                    Bukkit.broadcastMessage(ChatColor.YELLOW + p.getName() + ": " + entry.getValue() + " points");
                }
            });
    }

    public void checkPlayerLocation(Player player) {
        for (GameInstance instance : gameInstances.values()) {
            GameState gameState = instance.getGameState();
            if (!gameState.isGameRunning() || !gameState.getPlayersInGame().contains(player.getUniqueId())) {
                continue;
            }

            GameWorld gameWorld = instance.getGameWorld();
            if (!gameWorld.isWithinBorder(player.getLocation())) {
                player.teleport(player.getUniqueId().equals(gameState.getCurrentIt()) ? 
                    gameWorld.getItSpawnLocation() : gameWorld.getPlayerSpawnLocation());
                player.sendMessage(ChatColor.RED + "You cannot leave the game area!");
            }
        }
    }

    private void showCountdown(Player player, int seconds) {
        new BukkitRunnable() {
            int timeLeft = seconds;

            @Override
            public void run() {
                if (timeLeft > 0) {
                    // Create title
                    String title = ChatColor.GOLD + "=== Game Starting ===";
                    String subtitle = ChatColor.GREEN + "Get Ready! " + timeLeft;

                    // Send title
                    player.sendTitle(title, subtitle, 0, 20, 10);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                    timeLeft--;
                } else {
                    player.sendTitle(ChatColor.GREEN + "GO!", "", 0, 20, 10);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void giveItTools(Player player) {
        // Clear inventory first
        player.getInventory().clear();
        
        // Give IT player tools
        ItemStack tagger = new ItemStack(Material.BLAZE_ROD);
        ItemMeta taggerMeta = tagger.getItemMeta();
        taggerMeta.setDisplayName(ChatColor.RED + "Tagger");
        taggerMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Right-click to tag players!",
            ChatColor.GRAY + "You are IT!"
        ));
        tagger.setItemMeta(taggerMeta);
        player.getInventory().setItem(0, tagger);

        // Give speed potion
        ItemStack speedPotion = new ItemStack(Material.SPLASH_POTION);
        PotionMeta potionMeta = (PotionMeta) speedPotion.getItemMeta();
        potionMeta.setDisplayName(ChatColor.AQUA + "Speed Boost");
        potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1), true);
        speedPotion.setItemMeta(potionMeta);
        player.getInventory().setItem(1, speedPotion);

        // Give jump boost
        ItemStack jumpPotion = new ItemStack(Material.SPLASH_POTION);
        PotionMeta jumpMeta = (PotionMeta) jumpPotion.getItemMeta();
        jumpMeta.setDisplayName(ChatColor.GREEN + "Jump Boost");
        jumpMeta.addCustomEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 200, 1), true);
        jumpPotion.setItemMeta(jumpMeta);
        player.getInventory().setItem(2, jumpPotion);

        // Give strength potion
        ItemStack strengthPotion = new ItemStack(Material.SPLASH_POTION);
        PotionMeta strengthMeta = (PotionMeta) strengthPotion.getItemMeta();
        strengthMeta.setDisplayName(ChatColor.DARK_RED + "Strength Boost");
        strengthMeta.addCustomEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 0), true);
        strengthPotion.setItemMeta(strengthMeta);
        player.getInventory().setItem(3, strengthPotion);
    }

    private void giveRunnerTools(Player player) {
        // Clear inventory first
        player.getInventory().clear();
        
        // Give runner tools
        ItemStack smokeBomb = new ItemStack(Material.ENDER_PEARL);
        ItemMeta smokeMeta = smokeBomb.getItemMeta();
        smokeMeta.setDisplayName(ChatColor.GRAY + "Smoke Bomb");
        smokeMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Right-click to throw a smoke bomb!",
            ChatColor.GRAY + "Creates a cloud of smoke to escape!"
        ));
        smokeBomb.setItemMeta(smokeMeta);
        player.getInventory().setItem(0, smokeBomb);

        // Give speed potion
        ItemStack speedPotion = new ItemStack(Material.SPLASH_POTION);
        PotionMeta potionMeta = (PotionMeta) speedPotion.getItemMeta();
        potionMeta.setDisplayName(ChatColor.AQUA + "Speed Boost");
        potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1), true);
        speedPotion.setItemMeta(potionMeta);
        player.getInventory().setItem(1, speedPotion);

        // Give invisibility potion
        ItemStack invisPotion = new ItemStack(Material.SPLASH_POTION);
        PotionMeta invisMeta = (PotionMeta) invisPotion.getItemMeta();
        invisMeta.setDisplayName(ChatColor.GRAY + "Invisibility");
        invisMeta.addCustomEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0), true);
        invisPotion.setItemMeta(invisMeta);
        player.getInventory().setItem(2, invisPotion);

        // Give jump boost
        ItemStack jumpPotion = new ItemStack(Material.SPLASH_POTION);
        PotionMeta jumpMeta = (PotionMeta) jumpPotion.getItemMeta();
        jumpMeta.setDisplayName(ChatColor.GREEN + "Jump Boost");
        jumpMeta.addCustomEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 200, 1), true);
        jumpPotion.setItemMeta(jumpMeta);
        player.getInventory().setItem(3, jumpPotion);
    }

    public void addPlayerToGame(Player player, GameInstance instance) {
        GameState gameState = instance.getGameState();
        gameState.addPlayer(player);
        player.sendMessage(ChatColor.GREEN + "You joined " + instance.getDisplayName() + "!");
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);

        // Check if we should auto-start
        if (instance.isAutoStart() && gameState.getPlayersInGame().size() >= instance.getMaxPlayers()) {
            startGame(instance);
        }
    }

    public void removePlayerFromGame(Player player, GameInstance instance) {
        GameState gameState = instance.getGameState();
        gameState.removePlayer(player);
        player.sendMessage(ChatColor.RED + "You left " + instance.getDisplayName() + ".");
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);

        // Teleport player back to spawn
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawn " + player.getName());
        player.getInventory().clear();

        // Check if we should end the game
        if (gameState.isGameRunning()) {
            int remainingPlayers = gameState.getPlayersInGame().size();
            int maxPlayers = instance.getMaxPlayers();
            if (remainingPlayers < maxPlayers / 2) {
                Bukkit.broadcastMessage(ChatColor.RED + "Too many players left! Ending game...");
                stopGame(instance);
            }
        }

        // Update the game sign
        updateGameSign(instance);
    }

    private void updateGameSign(GameInstance instance) {
        if (instance.getSignLocation() != null && instance.getSignLocation().getBlock().getState() instanceof Sign sign) {
            GameState gameState = instance.getGameState();
            int playersInGame = gameState.getPlayersInGame().size();
            int maxPlayers = instance.getMaxPlayers();
            String status = gameState.isGameRunning() ? ChatColor.RED + "In Progress" : 
                          playersInGame >= maxPlayers ? ChatColor.YELLOW + "Full" :
                          ChatColor.GREEN + "Waiting";
            
            sign.setLine(0, ChatColor.AQUA + "[TagGame]");
            sign.setLine(1, ChatColor.GREEN + instance.getMode());
            sign.setLine(2, ChatColor.YELLOW + String.valueOf(playersInGame) + "/" + String.valueOf(maxPlayers));
            sign.setLine(3, status);
            sign.update();
        }
    }

    public void setMapForInstance(GameInstance instance, String mapName) {
        instance.setMapName(mapName);
        // Update sign if it exists
        if (instance.getSignLocation() != null && instance.getSignLocation().getBlock().getState() instanceof Sign sign) {
            sign.setLine(1, ChatColor.GREEN + instance.getMode());
            sign.setLine(2, new StringBuilder().append(ChatColor.YELLOW).append(instance.getTeamSize()).append("v").append(instance.getTeamSize()).toString());
            sign.setLine(3, ChatColor.GRAY + mapName);
            sign.update();
        }
    }

    public void setAutoStartForInstance(GameInstance instance, boolean autoStart) {
        instance.setAutoStart(autoStart);
    }

    public List<GameInstance> getInstancesByMap(String mapName) {
        return gameInstances.values().stream()
            .filter(instance -> instance.getMapName().equals(mapName))
            .toList();
    }

    public List<GameInstance> getInstancesByMode(String mode) {
        return gameInstances.values().stream()
            .filter(instance -> instance.getMode().equals(mode))
            .toList();
    }

    public List<GameInstance> getGameInstances() {
        return new ArrayList<>(gameInstances.values());
    }

    public void setItSpawn(GameInstance instance, Location location) {
        instance.getGameWorld().setItSpawnLocation(location);
        // Save to config
        plugin.getConfig().set("games." + instance.getId() + ".it_spawn", location);
        plugin.saveConfig();
    }

    public void setPlayerSpawn(GameInstance instance, Location location) {
        instance.getGameWorld().setPlayerSpawnLocation(location);
        // Save to config
        plugin.getConfig().set("games." + instance.getId() + ".player_spawn", location);
        plugin.saveConfig();
    }

    public void setMapCorner1(GameInstance instance, Location location) {
        instance.getGameWorld().setCorner1(location);
        saveGameInstance(instance);
    }

    public void setMapCorner2(GameInstance instance, Location location) {
        instance.getGameWorld().setCorner2(location);
        saveGameInstance(instance);
    }
} 