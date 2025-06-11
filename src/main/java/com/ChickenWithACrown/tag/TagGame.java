package com.ChickenWithACrown.tag;

import com.ChickenWithACrown.tag.controller.GameController;
import com.ChickenWithACrown.tag.model.GameState;
import com.ChickenWithACrown.tag.model.GameInstance;
import com.ChickenWithACrown.tag.view.GameView;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.*;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.Particle;

public class TagGame extends JavaPlugin implements Listener {
    private GameController gameController;
    private GameView gameView;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        try {
            saveDefaultConfig();
            config = getConfig();
            
            gameController = new GameController(this);
            gameView = new GameView(new GameState(this));
            
            getServer().getPluginManager().registerEvents(this, this);
            
            if (getServer().getPluginManager().getPlugin("DecentHolograms") == null) {
                getLogger().warning("DecentHolograms not found! Leaderboard features will be disabled.");
            } else {
                getLogger().info("DecentHolograms found! Leaderboard features enabled.");
            }

            if (getServer().getPluginManager().getPlugin("WorldGuard") == null) {
                getLogger().warning("WorldGuard not found! Please install WorldGuard for proper game area protection.");
            } else {
                getLogger().info("WorldGuard found! Game area protection enabled.");
            }
            
            getLogger().info("TagGame enabled successfully!");
        } catch (Exception e) {
            getLogger().severe("Failed to enable TagGame: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            if (gameController != null) {
                for (GameInstance instance : gameController.getGameInstances()) {
                    if (instance.getGameState().isGameRunning()) {
                        gameController.stopGame(instance);
                    }
                }
            }
            getLogger().info("TagGame disabled!");
        } catch (Exception e) {
            getLogger().severe("Error while disabling TagGame: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            gameView.openTagGui(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help" -> gameView.showHelp(player);
            case "join" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Please specify a game ID!");
                    return true;
                }
                GameInstance instance = gameController.getGameInstance(args[1]);
                if (instance != null) {
                    gameController.addPlayerToGame(player, instance);
                } else {
                    player.sendMessage(ChatColor.RED + "Game not found!");
                }
            }
            case "leave" -> {
                GameInstance instance = gameController.getGameInstanceBySign(player.getLocation());
                if (instance != null) {
                    gameController.removePlayerFromGame(player, instance);
                } else {
                    player.sendMessage(ChatColor.RED + "You're not in a game!");
                }
            }
            case "start" -> {
                if (!player.hasPermission("tag.admin.start")) return true;
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Please specify a game ID!");
                    return true;
                }
                GameInstance instance = gameController.getGameInstance(args[1]);
                if (instance != null) {
                    gameController.startGame(instance);
                } else {
                    player.sendMessage(ChatColor.RED + "Game not found!");
                }
            }
            case "stop" -> {
                if (!player.hasPermission("tag.admin.stop")) return true;
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Please specify a game ID!");
                    return true;
                }
                GameInstance instance = gameController.getGameInstance(args[1]);
                if (instance != null) {
                    gameController.stopGame(instance);
                } else {
                    player.sendMessage(ChatColor.RED + "Game not found!");
                }
            }
            case "it" -> {
                GameInstance instance = gameController.getGameInstanceBySign(player.getLocation());
                if (instance != null) {
                    UUID currentIt = instance.getGameState().getCurrentIt();
                    if (currentIt == null) {
                        player.sendMessage(ChatColor.GRAY + "Nobody is IT right now.");
                    } else {
                        player.sendMessage(ChatColor.GOLD + "IT: " + Bukkit.getPlayer(currentIt).getName());
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You're not in a game!");
                }
            }
            case "score" -> gameView.showScoreboard(player);
            case "setleaderboard" -> {
                if (!player.hasPermission("tag.admin.setleaderboard")) return true;
                gameView.createLeaderboardHologram(player.getLocation());
                player.sendMessage(ChatColor.GREEN + "Leaderboard created at your location!");
            }
            case "mode" -> {
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Please specify a game ID and mode!");
                    return true;
                }
                if (!player.hasPermission("tag.admin.mode")) return true;
                GameInstance instance = gameController.getGameInstance(args[1]);
                if (instance != null) {
                    gameController.setMapForInstance(instance, args[2]);
                    player.sendMessage(ChatColor.GREEN + "Game mode set to " + args[2] + "!");
                } else {
                    player.sendMessage(ChatColor.RED + "Game not found!");
                }
            }
            case "setmap" -> {
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Please specify a game ID and map name!");
                    return true;
                }
                if (!player.hasPermission("tag.admin.setmap")) return true;
                GameInstance instance = gameController.getGameInstance(args[1]);
                if (instance != null) {
                    gameController.setMapForInstance(instance, args[2]);
                    player.sendMessage(ChatColor.GREEN + "Map set to " + args[2] + "!");
                } else {
                    player.sendMessage(ChatColor.RED + "Game not found!");
                }
            }
            case "setitspawn" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Please specify a game ID!");
                    return true;
                }
                if (!player.hasPermission("tag.admin.setmap")) return true;
                GameInstance instance = gameController.getGameInstance(args[1]);
                if (instance != null) {
                    gameController.setItSpawn(instance, player.getLocation());
                    player.sendMessage(ChatColor.GREEN + "IT spawn set for " + instance.getDisplayName() + "!");
                } else {
                    player.sendMessage(ChatColor.RED + "Game not found!");
                }
            }
            case "setplayerspawn" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Please specify a game ID!");
                    return true;
                }
                if (!player.hasPermission("tag.admin.setmap")) return true;
                GameInstance instance = gameController.getGameInstance(args[1]);
                if (instance != null) {
                    gameController.setPlayerSpawn(instance, player.getLocation());
                    player.sendMessage(ChatColor.GREEN + "Player spawn set for " + instance.getDisplayName() + "!");
                } else {
                    player.sendMessage(ChatColor.RED + "Game not found!");
                }
            }
            case "autostart" -> {
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Please specify a game ID and true/false!");
                    return true;
                }
                if (!player.hasPermission("tag.admin.autostart")) return true;
                GameInstance instance = gameController.getGameInstance(args[1]);
                if (instance != null) {
                    boolean autoStart = Boolean.parseBoolean(args[2]);
                    gameController.setAutoStartForInstance(instance, autoStart);
                    player.sendMessage(ChatColor.GREEN + "Auto-start " + (autoStart ? "enabled" : "disabled") + "!");
                } else {
                    player.sendMessage(ChatColor.RED + "Game not found!");
                }
            }
            case "list" -> {
                player.sendMessage(ChatColor.GOLD + "=== Active Games ===");
                for (GameInstance instance : gameController.getGameInstances()) {
                    player.sendMessage(ChatColor.YELLOW + instance.getDisplayName() + 
                        " (" + instance.getGameState().getPlayersInGame().size() + "/" + 
                        instance.getMaxPlayers() + " players)");
                    player.sendMessage(ChatColor.GRAY + "ID: " + ChatColor.WHITE + instance.getId());
                }
            }
            case "setcorner1" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Please specify a game ID!");
                    return true;
                }
                if (!player.hasPermission("tag.admin.setmap")) return true;
                GameInstance instance = gameController.getGameInstance(args[1]);
                if (instance != null) {
                    gameController.setMapCorner1(instance, player.getLocation());
                    player.sendMessage(ChatColor.GREEN + "First corner set for " + instance.getDisplayName() + "!");
                    
                    // Create WorldGuard region
                    String regionName = "tag_" + instance.getMapName().toLowerCase();
                    player.performCommand("rg define " + regionName + " " + 
                        player.getLocation().getBlockX() + "," + 
                        player.getLocation().getBlockY() + "," + 
                        player.getLocation().getBlockZ());
                } else {
                    player.sendMessage(ChatColor.RED + "Game not found!");
                }
            }
            case "setcorner2" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Please specify a game ID!");
                    return true;
                }
                if (!player.hasPermission("tag.admin.setmap")) return true;
                GameInstance instance = gameController.getGameInstance(args[1]);
                if (instance != null) {
                    gameController.setMapCorner2(instance, player.getLocation());
                    player.sendMessage(ChatColor.GREEN + "Second corner set for " + instance.getDisplayName() + "!");
                    
                    // Set WorldGuard region flags
                    String regionName = "tag_" + instance.getMapName().toLowerCase();
                    player.performCommand("rg flag " + regionName + " mob-spawning deny");
                    player.performCommand("rg flag " + regionName + " block-break deny");
                    player.performCommand("rg flag " + regionName + " block-place deny");
                    player.performCommand("rg flag " + regionName + " pvp allow");
                } else {
                    player.sendMessage(ChatColor.RED + "Game not found!");
                }
            }
            case "map" -> {
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Please specify a game ID and 'flags'!");
                    return true;
                }
                if (!player.hasPermission("tag.admin.map")) return true;
                GameInstance instance = gameController.getGameInstance(args[1]);
                if (instance != null) {
                    if (args[2].equalsIgnoreCase("flags")) {
                        gameView.openMapFlagsGui(player, instance);
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Game not found!");
                }
            }
            default -> player.sendMessage(ChatColor.RED + "Unknown subcommand. Use /tag help for commands.");
        }
        return true;
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

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && event.getClickedBlock().getState() instanceof Sign sign) {
            String firstLine = ChatColor.stripColor(sign.getLine(0));
            if (firstLine.equals("[TagGame]")) {
                GameInstance instance = gameController.getGameInstanceBySign(sign.getLocation());
                if (instance != null) {
                    gameController.addPlayerToGame(event.getPlayer(), instance);
                }
            }
        }

        if (!event.getPlayer().hasPermission("tag.admin")) return;

        ItemStack item = event.getItem();
        if (item == null) return;

        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String name = item.getItemMeta().getDisplayName();
            
            if (name.equals(ChatColor.RED + "Tagger")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.YELLOW + "You are IT! Tag players by hitting them!");
            } else if (name.equals(ChatColor.GRAY + "Smoke Bomb")) {
                event.setCancelled(true);
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    event.getPlayer().getInventory().setItemInMainHand(null);
                }
                
                Location loc = event.getPlayer().getLocation();
                for (int i = 0; i < 10; i++) {
                    loc.getWorld().spawnParticle(Particle.SMOKE, loc, 20, 0.5, 0.5, 0.5, 0.1);
                }
                event.getPlayer().playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.0f);
            }
        }
    }

    @EventHandler
    public void onTag(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager) || !(event.getEntity() instanceof Player victim)) return;

        GameInstance instance = gameController.getGameInstanceBySign(damager.getLocation());
        if (instance == null || !instance.getGameState().isGameRunning()) return;

        GameState gameState = instance.getGameState();
        if (!gameState.getPlayersInGame().contains(damager.getUniqueId()) || 
            !gameState.getPlayersInGame().contains(victim.getUniqueId())) return;

        if (!damager.getUniqueId().equals(gameState.getCurrentIt())) return;

        if (config.getBoolean("settings.preventTagBack") && victim.getUniqueId().equals(gameState.getLastIt())) {
            damager.sendMessage(ChatColor.GRAY + "You can't tag back the last player immediately!");
            return;
        }

        // Handle different game modes
        switch (gameState.getCurrentGameMode()) {
            case "freeze" -> {
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, -1));
                victim.sendMessage(ChatColor.RED + "You've been frozen! Wait for someone to unfreeze you!");
            }
            case "team" -> {
                // Team mode logic would go here
            }
        }

        // Give tools to the new IT player and the previous IT player
        giveItTools(victim);
        giveRunnerTools(damager);

        gameState.incrementScore(damager.getUniqueId());
        gameState.setCurrentIt(victim.getUniqueId());
        gameState.setLastIt(damager.getUniqueId());

        // Update the hologram when scores change
        gameView.updateLeaderboardHologram();

        if (config.getBoolean("settings.broadcastTags"))
            Bukkit.broadcastMessage(ChatColor.AQUA + victim.getName() + " is now IT!");
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        String firstLine = event.getLine(0);
        if (firstLine == null) return;

        if (firstLine.equalsIgnoreCase("[TagGame]")) {
            String mode = event.getLine(1);
            String teamSize = event.getLine(2);
            String mapName = event.getLine(3);
            
            if (mode == null || teamSize == null || mapName == null) {
                event.getPlayer().sendMessage(ChatColor.RED + "Invalid sign format! Use: [TagGame] <mode> <teamSize> <mapName>");
                return;
            }

            try {
                int size = Integer.parseInt(teamSize);
                GameInstance instance = gameController.createGameInstance(mode, size, mapName, event.getBlock().getLocation());
                
                event.setLine(0, ChatColor.AQUA + "[TagGame]");
                event.setLine(1, ChatColor.GREEN + mode);
                event.setLine(2, ChatColor.YELLOW + String.valueOf(size) + "v" + String.valueOf(size));
                event.setLine(3, ChatColor.GRAY + mapName);
                
                // Update sign with initial status
                updateGameSign(instance);
            } catch (NumberFormatException e) {
                event.getPlayer().sendMessage(ChatColor.RED + "Invalid team size! Use a number.");
            }
        } else if (firstLine.equalsIgnoreCase("[TagLeader]")) {
            event.setLine(0, ChatColor.GOLD + "[TagLeader]");
            event.setLine(1, ChatColor.YELLOW + "Top Players:");
            if (event.getBlock().getState() instanceof Sign sign) {
                gameView.updateLeaderboardSign(sign);
            }
        }
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

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.AQUA + "Tag Game")) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;
            String name = clicked.getItemMeta().getDisplayName();

            if (name.equals(ChatColor.GREEN + "Join Game")) {
                GameInstance instance = gameController.getGameInstanceBySign(player.getLocation());
                if (instance != null) {
                    gameController.addPlayerToGame(player, instance);
                }
            } else if (name.equals(ChatColor.RED + "Leave Game")) {
                GameInstance instance = gameController.getGameInstanceBySign(player.getLocation());
                if (instance != null) {
                    gameController.removePlayerFromGame(player, instance);
                }
            } else if (name.equals(ChatColor.YELLOW + "Who is It?")) {
                GameInstance instance = gameController.getGameInstanceBySign(player.getLocation());
                if (instance != null) {
                    UUID currentIt = instance.getGameState().getCurrentIt();
                    if (currentIt == null) {
                        player.sendMessage(ChatColor.GRAY + "Nobody is IT right now.");
                    } else {
                        player.sendMessage(ChatColor.GOLD + "IT: " + Bukkit.getPlayer(currentIt).getName());
                    }
                }
                player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1);
            } else if (name.equals(ChatColor.GOLD + "View Scores")) {
                gameView.showScoreboard(player);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            }
            player.closeInventory();
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        GameInstance instance = gameController.getGameInstanceBySign(event.getPlayer().getLocation());
        if (instance != null && instance.getGameState().isGameRunning()) {
            gameController.checkPlayerLocation(event.getPlayer());
        }
    }

    public void startGame(GameInstance instance) {
        if (instance.getGameState().getPlayersInGame().size() < 2) {
            Bukkit.broadcastMessage(ChatColor.RED + "Not enough players to start.");
            return;
        }

        if (!instance.getGameWorld().isWorldReady()) {
            Bukkit.broadcastMessage(ChatColor.RED + "Error: Game world is not ready!");
            return;
        }

        gameController.startGame(instance);
    }

    public void stopGame(GameInstance instance) {
        gameController.stopGame(instance);
    }

    public GameView getGameView() {
        return gameView;
    }
}
