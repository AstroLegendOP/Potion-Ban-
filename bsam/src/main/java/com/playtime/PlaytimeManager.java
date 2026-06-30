package com.playtime;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlaytimeManager {

    private final JavaPlugin plugin;
    private final ConcurrentHashMap<UUID, Integer> playtimeSeconds = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Boolean> onlineTracking = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Boolean> timePaused = new ConcurrentHashMap<>();
    private File dataFile;
    private FileConfiguration dataConfig;

    public final int DAILY_SECONDS;
    public final int KILL_BONUS_SECONDS;
    public final int DEATH_PENALTY_SECONDS;
    public final int TICK_INTERVAL;

    private boolean enabled = false;

    public PlaytimeManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "playtime.yml");

        FileConfiguration cfg = plugin.getConfig();
        this.DAILY_SECONDS = cfg.getInt("daily-reset-hours", 4) * 60 * 60;
        this.KILL_BONUS_SECONDS = cfg.getInt("kill-bonus-seconds", 3600);
        this.DEATH_PENALTY_SECONDS = cfg.getInt("death-penalty-seconds", 3600);
        this.TICK_INTERVAL = cfg.getInt("tick-interval", 20);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isTimePaused(UUID uuid) {
        return timePaused.getOrDefault(uuid, false);
    }

    public void pauseTime(UUID uuid, int seconds) {
        timePaused.put(uuid, true);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            timePaused.put(uuid, false);
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.sendMessage(ChatColor.AQUA + "Your time pause has ended!");
            }
        }, seconds * 20L);
    }

    public void loadData() {
        plugin.getDataFolder().mkdirs();
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create playtime.yml!");
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        if (dataConfig.contains("players")) {
            for (String key : dataConfig.getConfigurationSection("players").getKeys(false)) {
                UUID uuid = UUID.fromString(key);
                int seconds = dataConfig.getInt("players." + key + ".time");
                playtimeSeconds.put(uuid, seconds);
            }
        }
        plugin.getLogger().info("Loaded playtime data for " + playtimeSeconds.size() + " players.");
    }

    public void saveData() {
        for (UUID uuid : playtimeSeconds.keySet()) {
            dataConfig.set("players." + uuid.toString() + ".time", playtimeSeconds.get(uuid));
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save playtime.yml!");
        }
    }

    public int getPlaytime(UUID uuid) {
        return playtimeSeconds.getOrDefault(uuid, DAILY_SECONDS);
    }

    public void setPlaytime(UUID uuid, int seconds) {
        playtimeSeconds.put(uuid, Math.max(0, seconds));
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            updatePlayerDisplay(player);
        }
    }

    public boolean canJoin(UUID uuid) {
        return getPlaytime(uuid) > 0;
    }

    public void addTime(UUID uuid, int seconds) {
        int current = getPlaytime(uuid);
        playtimeSeconds.put(uuid, current + seconds);
    }

    public void removeTime(UUID uuid, int seconds) {
        int current = getPlaytime(uuid);
        playtimeSeconds.put(uuid, Math.max(0, current - seconds));
    }

    public void resetDaily() {
        for (UUID uuid : playtimeSeconds.keySet()) {
            playtimeSeconds.put(uuid, DAILY_SECONDS);
            timePaused.put(uuid, false);
        }
        saveData();
        plugin.getLogger().info("Daily playtime reset complete!");
        Bukkit.broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Tick SMP Season 2 - Time has been reset! Everyone has " + (DAILY_SECONDS / 3600) + " hours again.");
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerDisplay(player);
        }
    }

    public void startDailyResetTask() {
        long ticksIn24Hours = 24L * 60 * 60 * 20;
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!enabled) return;
            resetDaily();
        }, ticksIn24Hours, ticksIn24Hours);
    }

    public void startPlaytimeTickTask() {
        final int decrementPerTick = Math.max(1, TICK_INTERVAL / 20);
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!enabled) return;
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                if (!onlineTracking.getOrDefault(uuid, false) || isTimePaused(uuid)) continue;

                int remaining = getPlaytime(uuid);
                if (remaining <= 0) {
                    player.setHealth(0);
                    player.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "You have been eliminated! Your time ran out!");
                    continue;
                }
                playtimeSeconds.put(uuid, remaining - decrementPerTick);
            }
        }, TICK_INTERVAL, TICK_INTERVAL);
    }

    public void updatePlayerDisplay(Player player) {
        UUID uuid = player.getUniqueId();
        int time = getPlaytime(uuid);
        String timeStr = formatTime(time);
        ChatColor color = getTimeColor(time);

        String pauseTag = isTimePaused(uuid) ? " \u00a7b\u00a7l[P] " : " ";
        String displayName = color + timeStr + pauseTag + "\u00a7r" + player.getName();
        String listName = color + timeStr + pauseTag + "\u00a7r" + player.getName();

        player.setCustomName(displayName);
        player.setCustomNameVisible(true);
        player.setPlayerListName(listName);
    }

    public void resetPlayerDisplay(Player player) {
        player.setCustomName(player.getName());
        player.setCustomNameVisible(false);
        player.setPlayerListName(null);
    }

    public ChatColor getTimeColor(int seconds) {
        if (seconds > 3600) return ChatColor.GREEN;
        if (seconds > 1800) return ChatColor.YELLOW;
        if (seconds > 600) return ChatColor.GOLD;
        if (seconds > 300) return ChatColor.RED;
        return ChatColor.DARK_RED;
    }

    public void startTracking(UUID uuid) {
        onlineTracking.put(uuid, true);
        if (!playtimeSeconds.containsKey(uuid)) {
            playtimeSeconds.put(uuid, DAILY_SECONDS);
        }
    }

    public void stopTracking(UUID uuid) {
        onlineTracking.put(uuid, false);
        timePaused.put(uuid, false);
        saveData();
    }

    public String formatTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        if (hours > 0) {
            return hours + "h " + minutes + "m " + seconds + "s";
        } else if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        } else {
            return seconds + "s";
        }
    }

    public String formatTimeShort(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public ConcurrentHashMap<UUID, Boolean> getTimePausedMap() {
        return timePaused;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }
}
