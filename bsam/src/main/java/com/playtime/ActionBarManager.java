package com.playtime;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ActionBarManager {

    private final JavaPlugin plugin;
    private final PlaytimeManager playtimeManager;
    private final int updateTicks;

    public ActionBarManager(JavaPlugin plugin, PlaytimeManager playtimeManager) {
        this.plugin = plugin;
        this.playtimeManager = playtimeManager;
        this.updateTicks = plugin.getConfig().getInt("actionbar-update-ticks", 80);
    }

    public void startUpdateTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!playtimeManager.isEnabled()) return;
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                int time = playtimeManager.getPlaytime(player.getUniqueId());
                ChatColor color = playtimeManager.getTimeColor(time);
                String pause = playtimeManager.isTimePaused(player.getUniqueId()) ? ChatColor.AQUA + " [P] " : "";
                String msg = color + playtimeManager.formatTimeShort(time) + pause + ChatColor.DARK_GRAY + " | " + ChatColor.GRAY + "K: " + ChatColor.GREEN + player.getStatistic(org.bukkit.Statistic.PLAYER_KILLS);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg));
            }
        }, 0L, updateTicks);
    }
}
