package com.playtime;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final PlaytimeManager playtimeManager;

    public JoinListener(PlaytimeManager playtimeManager) {
        this.playtimeManager = playtimeManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (playtimeManager.isEnabled()) {
            int time = playtimeManager.getPlaytime(player.getUniqueId());

            if (time <= 0) {
                player.setGameMode(GameMode.SPECTATOR);
                player.teleport(player.getWorld().getSpawnLocation());
                player.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "You are out of time! You are now spectating.");
                return;
            }

            playtimeManager.startTracking(player.getUniqueId());
            playtimeManager.updatePlayerDisplay(player);

            player.sendMessage("");
            player.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "  TICK SMP SEASON 2");
            player.sendMessage(ChatColor.GRAY + "  Your time: " + ChatColor.GREEN + playtimeManager.formatTime(time));
            player.sendMessage(ChatColor.GRAY + "  Kill players: " + ChatColor.GREEN + "+1 hour");
            player.sendMessage(ChatColor.GRAY + "  Die to players: " + ChatColor.RED + "-1 hour");
            player.sendMessage(ChatColor.GRAY + "  Time runs out = eliminated!");
            player.sendMessage("");
        }
    }
}
