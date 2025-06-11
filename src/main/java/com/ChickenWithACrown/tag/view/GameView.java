package com.ChickenWithACrown.tag.view;

import com.ChickenWithACrown.tag.model.GameState;
import com.ChickenWithACrown.tag.model.GameInstance;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class GameView {
    private final GameState gameState;
    private Hologram leaderboardHologram;
    private static final String HOLOGRAM_NAME = "tag_leaderboard";

    public GameView(GameState gameState) {
        this.gameState = gameState;
        loadLeaderboard();
    }

    private void loadLeaderboard() {
        if (gameState.getPlugin().getConfig().contains("leaderboard.location")) {
            Location loc = (Location) gameState.getPlugin().getConfig().get("leaderboard.location");
            if (loc != null) {
                createLeaderboardHologram(loc);
            }
        }
    }

    public void openMapFlagsGui(Player player, GameInstance instance) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.AQUA + "Map Flags - " + instance.getDisplayName());

        // Mob Spawning Toggle
        ItemStack mobSpawning = new ItemStack(Material.ZOMBIE_SPAWN_EGG);
        ItemMeta mobMeta = mobSpawning.getItemMeta();
        mobMeta.setDisplayName(ChatColor.YELLOW + "Mob Spawning");
        mobMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Current: " + (instance.getGameWorld().isMobSpawningEnabled() ? 
                ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"),
            ChatColor.GRAY + "Click to toggle"
        ));
        mobSpawning.setItemMeta(mobMeta);
        gui.setItem(10, mobSpawning);

        // Block Breaking Toggle
        ItemStack blockBreak = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta breakMeta = blockBreak.getItemMeta();
        breakMeta.setDisplayName(ChatColor.YELLOW + "Block Breaking");
        breakMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Current: " + (instance.getGameWorld().isBlockBreakingEnabled() ? 
                ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"),
            ChatColor.GRAY + "Click to toggle"
        ));
        blockBreak.setItemMeta(breakMeta);
        gui.setItem(11, blockBreak);

        // Block Placing Toggle
        ItemStack blockPlace = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta placeMeta = blockPlace.getItemMeta();
        placeMeta.setDisplayName(ChatColor.YELLOW + "Block Placing");
        placeMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Current: " + (instance.getGameWorld().isBlockPlacingEnabled() ? 
                ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"),
            ChatColor.GRAY + "Click to toggle"
        ));
        blockPlace.setItemMeta(placeMeta);
        gui.setItem(12, blockPlace);

        // PvP Toggle
        ItemStack pvp = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta pvpMeta = pvp.getItemMeta();
        pvpMeta.setDisplayName(ChatColor.YELLOW + "PvP");
        pvpMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Current: " + (instance.getGameWorld().isPvPEnabled() ? 
                ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"),
            ChatColor.GRAY + "Click to toggle"
        ));
        pvp.setItemMeta(pvpMeta);
        gui.setItem(13, pvp);

        // Decay Toggle
        ItemStack decay = new ItemStack(Material.ROTTEN_FLESH);
        ItemMeta decayMeta = decay.getItemMeta();
        decayMeta.setDisplayName(ChatColor.YELLOW + "Decay");
        decayMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Current: " + (instance.getGameWorld().isDecayEnabled() ? 
                ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"),
            ChatColor.GRAY + "Click to toggle"
        ));
        decay.setItemMeta(decayMeta);
        gui.setItem(14, decay);

        // Close Button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName(ChatColor.RED + "Close");
        close.setItemMeta(closeMeta);
        gui.setItem(26, close);

        player.openInventory(gui);
    }

    public void openTagGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.AQUA + "Tag Game");

        // Join Game
        ItemStack join = new ItemStack(Material.SLIME_BALL);
        ItemMeta joinMeta = join.getItemMeta();
        joinMeta.setDisplayName(ChatColor.GREEN + "Join Game");
        join.setItemMeta(joinMeta);
        gui.setItem(11, join);

        // Leave Game
        ItemStack leave = new ItemStack(Material.BARRIER);
        ItemMeta leaveMeta = leave.getItemMeta();
        leaveMeta.setDisplayName(ChatColor.RED + "Leave Game");
        leave.setItemMeta(leaveMeta);
        gui.setItem(13, leave);

        // Who is It?
        ItemStack it = new ItemStack(Material.NAME_TAG);
        ItemMeta itMeta = it.getItemMeta();
        itMeta.setDisplayName(ChatColor.YELLOW + "Who is It?");
        it.setItemMeta(itMeta);
        gui.setItem(15, it);

        // Scoreboard
        ItemStack score = new ItemStack(Material.PAPER);
        ItemMeta scoreMeta = score.getItemMeta();
        scoreMeta.setDisplayName(ChatColor.GOLD + "View Scores");
        score.setItemMeta(scoreMeta);
        gui.setItem(21, score);

        // Game Mode Info
        ItemStack mode = new ItemStack(Material.COMPASS);
        ItemMeta modeMeta = mode.getItemMeta();
        modeMeta.setDisplayName(ChatColor.BLUE + "Current Mode: " + gameState.getCurrentGameMode());
        mode.setItemMeta(modeMeta);
        gui.setItem(23, mode);

        player.openInventory(gui);
    }

    public void showScoreboard(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Current Scores ===");
        player.sendMessage(ChatColor.YELLOW + "Game Time: " + gameState.getGameTime() + " seconds");
        gameState.getPlayerScores().entrySet().stream()
            .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
            .forEach(entry -> {
                Player p = Bukkit.getPlayer(entry.getKey());
                if (p != null) {
                    player.sendMessage(ChatColor.YELLOW + p.getName() + ": " + entry.getValue() + " points");
                }
            });
    }

    public void updateLeaderboardSign(Sign sign) {
        sign.setLine(0, ChatColor.GOLD + "[TagLeaderboard]");
        sign.setLine(1, ChatColor.YELLOW + "Top Players:");
        
        gameState.getPlayerScores().entrySet().stream()
            .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
            .limit(3)
            .forEach(entry -> {
                Player p = Bukkit.getPlayer(entry.getKey());
                if (p != null) {
                    sign.setLine(2, ChatColor.WHITE + "1. " + p.getName() + ": " + entry.getValue());
                }
            });
        sign.update();
    }

    public void showHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Tag Game Commands ===");
        player.sendMessage(ChatColor.YELLOW + "/tag help" + ChatColor.WHITE + " - Shows this help menu");
        player.sendMessage(ChatColor.YELLOW + "/tag" + ChatColor.WHITE + " - Opens the Tag Game menu");
        player.sendMessage(ChatColor.YELLOW + "/tag join <gameID>" + ChatColor.WHITE + " - Join the Tag game");
        player.sendMessage(ChatColor.YELLOW + "/tag leave" + ChatColor.WHITE + " - Leave the Tag game");
        player.sendMessage(ChatColor.YELLOW + "/tag start <gameID>" + ChatColor.WHITE + " - Start the Tag game (Admin only)");
        player.sendMessage(ChatColor.YELLOW + "/tag stop <gameID>" + ChatColor.WHITE + " - Stop the Tag game (Admin only)");
        player.sendMessage(ChatColor.YELLOW + "/tag it" + ChatColor.WHITE + " - Check who is currently IT");
        player.sendMessage(ChatColor.YELLOW + "/tag score" + ChatColor.WHITE + " - View current scores");
        player.sendMessage(ChatColor.YELLOW + "/tag mode <gameID> <mode>" + ChatColor.WHITE + " - Change game mode (Admin only)");
        player.sendMessage(ChatColor.YELLOW + "/tag setmap <gameID> <mapname>" + ChatColor.WHITE + " - Set map name (Admin only)");
        player.sendMessage(ChatColor.YELLOW + "/tag setitspawn <gameID>" + ChatColor.WHITE + " - Set IT spawn point (Admin only)");
        player.sendMessage(ChatColor.YELLOW + "/tag setplayerspawn <gameID>" + ChatColor.WHITE + " - Set player spawn point (Admin only)");
        player.sendMessage(ChatColor.GRAY + "Available modes: classic, freeze, team");
        player.sendMessage(ChatColor.GOLD + "=== Examples ===");
        player.sendMessage(ChatColor.WHITE + "• /tag setmap team_desert Desert" + ChatColor.GRAY + " - Sets map name to Desert");
        player.sendMessage(ChatColor.WHITE + "• /tag setitspawn team_desert" + ChatColor.GRAY + " - Sets IT spawn at your location");
        player.sendMessage(ChatColor.WHITE + "• /tag setplayerspawn team_desert" + ChatColor.GRAY + " - Sets player spawn at your location");
    }

    public void createLeaderboardHologram(Location location) {
        if (leaderboardHologram != null) {
            DHAPI.removeHologram(HOLOGRAM_NAME);
        }
        
        // Save location to config
        gameState.getPlugin().getConfig().set("leaderboard.location", location);
        gameState.getPlugin().saveConfig();
        
        List<String> lines = new ArrayList<>();
        lines.add(ChatColor.GOLD + "=== Tag Game Leaderboard ===");
        lines.add(ChatColor.YELLOW + "Top Players:");
        
        leaderboardHologram = DHAPI.createHologram(HOLOGRAM_NAME, location, lines);
    }

    public void updateLeaderboardHologram() {
        if (leaderboardHologram == null) return;

        List<String> lines = new ArrayList<>();
        lines.add(ChatColor.GOLD + "=== Tag Game Leaderboard ===");
        lines.add(ChatColor.YELLOW + "Top Players:");

        gameState.getPlayerScores().entrySet().stream()
            .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
            .limit(10)
            .forEach(entry -> {
                Player p = Bukkit.getPlayer(entry.getKey());
                if (p != null) {
                    lines.add(ChatColor.WHITE + p.getName() + ": " + entry.getValue() + " points");
                }
            });

        DHAPI.setHologramLines(leaderboardHologram, lines);
    }

    public void showFinalScores() {
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
} 