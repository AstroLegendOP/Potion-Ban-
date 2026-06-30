package com.playtime;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class KillListener implements Listener {

    private final PlaytimeManager playtimeManager;

    public KillListener(PlaytimeManager playtimeManager) {
        this.playtimeManager = playtimeManager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!playtimeManager.isEnabled()) return;

        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null || killer.equals(victim)) return;
        if (victim.getGameMode() == GameMode.CREATIVE || victim.getGameMode() == GameMode.SPECTATOR) return;

        playtimeManager.addTime(killer.getUniqueId(), playtimeManager.KILL_BONUS_SECONDS);
        playtimeManager.removeTime(victim.getUniqueId(), playtimeManager.DEATH_PENALTY_SECONDS);

        int killerTime = playtimeManager.getPlaytime(killer.getUniqueId());
        int victimTime = playtimeManager.getPlaytime(victim.getUniqueId());

        killer.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "+" + (playtimeManager.KILL_BONUS_SECONDS / 60) + " MIN! " + ChatColor.GRAY + "Time: " +
                ChatColor.GREEN + playtimeManager.formatTime(killerTime));
        victim.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "-" + (playtimeManager.DEATH_PENALTY_SECONDS / 60) + " MIN! " + ChatColor.GRAY + "Time: " +
                ChatColor.YELLOW + playtimeManager.formatTime(victimTime));

        if (victimTime <= 0) {
            victim.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "YOU HAVE BEEN ELIMINATED!");
            victim.setGameMode(GameMode.SPECTATOR);
            victim.teleport(victim.getWorld().getSpawnLocation());
        }
    }
}
