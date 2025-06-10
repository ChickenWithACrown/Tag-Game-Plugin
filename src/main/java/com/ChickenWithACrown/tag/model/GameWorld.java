package com.ChickenWithACrown.tag.model;

import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.entity.Player;
import org.bukkit.block.Sign;
import com.ChickenWithACrown.tag.TagGame;
import java.util.Arrays;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class GameWorld {
    private final TagGame plugin;
    private World world;
    private Location itSpawnLocation;
    private Location playerSpawnLocation;
    private int borderSize = 100;
    private final Map<UUID, ItemStack[]> savedInventories = new HashMap<>();
    private final Map<UUID, ItemStack[]> savedArmor = new HashMap<>();
    private String mapName;
    private final Map<String, Location> mapSpawns = new HashMap<>();

    public GameWorld(TagGame plugin) {
        this.plugin = plugin;
    }

    public TagGame getPlugin() {
        return plugin;
    }

    public boolean isWorldReady() {
        return world != null && itSpawnLocation != null && playerSpawnLocation != null;
    }

    public Location getItSpawnLocation() {
        return itSpawnLocation;
    }

    public Location getPlayerSpawnLocation() {
        return playerSpawnLocation;
    }

    public boolean isWithinBorder(Location location) {
        if (world == null || !location.getWorld().equals(world)) return false;
        return Math.abs(location.getBlockX()) <= borderSize && 
               Math.abs(location.getBlockZ()) <= borderSize;
    }

    public ItemStack createGrapplingHook() {
        ItemStack hook = new ItemStack(Material.FISHING_ROD);
        ItemMeta meta = hook.getItemMeta();
        meta.setDisplayName("§6Grappling Hook");
        meta.setLore(Arrays.asList(
            "§7Right-click to swing to safety!",
            "§7Cooldown: 5 seconds"
        ));
        hook.setItemMeta(meta);
        return hook;
    }

    public ItemStack createSmokeBomb() {
        ItemStack bomb = new ItemStack(Material.ENDER_PEARL);
        ItemMeta meta = bomb.getItemMeta();
        meta.setDisplayName("§8Smoke Bomb");
        meta.setLore(Arrays.asList(
            "§7Creates a cloud of smoke",
            "§7to escape from IT!"
        ));
        bomb.setItemMeta(meta);
        return bomb;
    }

    public ItemStack createSpeedPotion() {
        ItemStack potion = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        meta.setDisplayName("§bSpeed Boost");
        meta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1), true);
        potion.setItemMeta(meta);
        return potion;
    }

    public ItemStack createJumpPotion() {
        ItemStack potion = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        meta.setDisplayName("§aJump Boost");
        meta.addCustomEffect(new PotionEffect(PotionEffectType.JUMP, 200, 1), true);
        potion.setItemMeta(meta);
        return potion;
    }

    public ItemStack createInvisibilityPotion() {
        ItemStack potion = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        meta.setDisplayName("§7Invisibility");
        meta.addCustomEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0), true);
        potion.setItemMeta(meta);
        return potion;
    }

    public ItemStack createStrengthPotion() {
        ItemStack potion = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        meta.setDisplayName("§cStrength Boost");
        meta.addCustomEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 200, 0), true);
        potion.setItemMeta(meta);
        return potion;
    }

    public ItemStack createTagger() {
        ItemStack tagger = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = tagger.getItemMeta();
        meta.setDisplayName("§cTagger");
        meta.setLore(Arrays.asList(
            "§7Right-click to tag players!",
            "§7You are IT!"
        ));
        tagger.setItemMeta(meta);
        return tagger;
    }

    public ItemStack createTeleporter() {
        ItemStack teleporter = new ItemStack(Material.ENDER_PEARL);
        ItemMeta meta = teleporter.getItemMeta();
        meta.setDisplayName("§5Teleporter");
        meta.setLore(Arrays.asList(
            "§7Right-click to teleport",
            "§7to a random location!"
        ));
        teleporter.setItemMeta(meta);
        return teleporter;
    }

    public ItemStack createShield() {
        ItemStack shield = new ItemStack(Material.SHIELD);
        ItemMeta meta = shield.getItemMeta();
        meta.setDisplayName("§eShield");
        meta.setLore(Arrays.asList(
            "§7Block IT's attacks!",
            "§7Limited uses"
        ));
        shield.setItemMeta(meta);
        return shield;
    }

    public void savePlayerInventory(Player player) {
        savedInventories.put(player.getUniqueId(), player.getInventory().getContents());
        savedArmor.put(player.getUniqueId(), player.getInventory().getArmorContents());
    }

    public void restorePlayerInventory(Player player) {
        // Clear current inventory
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        
        // Remove all potion effects
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        // Restore saved inventory
        ItemStack[] savedItems = savedInventories.remove(player.getUniqueId());
        if (savedItems != null) {
            player.getInventory().setContents(savedItems);
        }

        // Restore saved armor
        ItemStack[] savedArmorItems = savedArmor.remove(player.getUniqueId());
        if (savedArmorItems != null) {
            player.getInventory().setArmorContents(savedArmorItems);
        }
    }

    public void clearSavedInventory(UUID playerId) {
        savedInventories.remove(playerId);
        savedArmor.remove(playerId);
    }

    public boolean isValidTeamSign(Sign sign) {
        String[] lines = sign.getLines();
        if (lines.length < 4) return false;

        // Check first line
        if (!lines[0].equalsIgnoreCase("[TagGame]")) return false;

        // Check mode
        if (!lines[1].equalsIgnoreCase("team")) return false;

        // Check team size
        try {
            int teamSize = Integer.parseInt(lines[2]);
            if (teamSize < 2 || teamSize > 10) return false; // Limit team sizes
        } catch (NumberFormatException e) {
            return false;
        }

        // Check map name
        if (lines[3].trim().isEmpty()) return false;

        return true;
    }

    public void formatTeamSign(Sign sign) {
        sign.setLine(0, "§b[TagGame]");
        sign.setLine(1, "§aTeam Mode");
        sign.setLine(2, "§e5v5");
        sign.setLine(3, "§7" + sign.getLine(3).trim());
        sign.update();
    }

    public void setMapName(String name) {
        this.mapName = name;
    }

    public String getMapName() {
        return mapName;
    }

    public void addMapSpawn(String name, Location location) {
        mapSpawns.put(name, location);
        // Save to config
        plugin.getConfig().set("maps." + name + ".location", location);
        plugin.saveConfig();
    }

    public Location getMapSpawn(String name) {
        return mapSpawns.get(name);
    }

    public void loadMapSpawns() {
        if (plugin.getConfig().contains("maps")) {
            for (String mapName : plugin.getConfig().getConfigurationSection("maps").getKeys(false)) {
                Location loc = (Location) plugin.getConfig().get("maps." + mapName + ".location");
                if (loc != null) {
                    mapSpawns.put(mapName, loc);
                }
            }
        }
    }

    public void setMapSpawns(Location itSpawn, Location playerSpawn) {
        this.itSpawnLocation = itSpawn;
        this.playerSpawnLocation = playerSpawn;
        // Save to config
        plugin.getConfig().set("maps." + mapName + ".it_spawn", itSpawn);
        plugin.getConfig().set("maps." + mapName + ".player_spawn", playerSpawn);
        plugin.saveConfig();
    }

    public List<String> getAvailableMaps() {
        return new ArrayList<>(mapSpawns.keySet());
    }
} 