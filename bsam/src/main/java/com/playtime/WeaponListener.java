package com.playtime;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class WeaponListener implements Listener {

    private final JavaPlugin plugin;
    private final PlaytimeManager playtimeManager;
    private final WeaponManager weaponManager;
    private final HashMap<UUID, Long> pauseCooldown = new HashMap<>();
    private final HashMap<UUID, Long> maceCooldown = new HashMap<>();

    private final int maceHitDamage;
    private final long maceCooldownMs;
    private final int maceAnvilWaves;
    private final int maceAnvilsPerWave;
    private final int pausePauseDuration;
    private final long pauseCooldownMs;

    public WeaponListener(JavaPlugin plugin, PlaytimeManager playtimeManager, WeaponManager weaponManager) {
        this.plugin = plugin;
        this.playtimeManager = playtimeManager;
        this.weaponManager = weaponManager;

        var cfg = plugin.getConfig();
        this.maceHitDamage = cfg.getInt("time-mace.hit-damage", 12);
        this.maceCooldownMs = cfg.getLong("time-mace.cooldown-ms", 10000);
        this.maceAnvilWaves = cfg.getInt("time-mace.anvil-rain.waves", 3);
        this.maceAnvilsPerWave = cfg.getInt("time-mace.anvil-rain.anvils-per-wave", 4);
        this.pausePauseDuration = cfg.getInt("time-pause-shard.pause-duration-seconds", 30);
        this.pauseCooldownMs = cfg.getLong("time-pause-shard.cooldown-ms", 180000);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) return;
        if (!playtimeManager.isEnabled()) return;

        Player attacker = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();
        String type = weaponManager.getWeaponType(attacker.getInventory().getItemInMainHand());
        if (type == null) return;

        switch (type) {
            case WeaponManager.STEAL_SWORD -> {
                playtimeManager.removeTime(victim.getUniqueId(), 10);
                playtimeManager.addTime(attacker.getUniqueId(), 10);
                attacker.sendMessage(ChatColor.GREEN + "Stole 10s from " + victim.getName());
                victim.sendMessage(ChatColor.RED + attacker.getName() + " stole 10s from you!");
            }
            case WeaponManager.CAGE_SWORD -> {
                event.setCancelled(true);
                Location cageLoc = victim.getLocation().clone();
                cagePlayer(victim, cageLoc);
                attacker.sendMessage(ChatColor.DARK_PURPLE + "Caging " + victim.getName());
                victim.sendMessage(ChatColor.RED + "You are trapped in a cage for 5s!");
                Bukkit.getScheduler().runTaskLater(plugin, () -> uncagePlayer(cageLoc), 100L);
            }
            case WeaponManager.TIME_MACE -> {
                event.setDamage(event.getDamage() + maceHitDamage);
                Vector kb = victim.getLocation().toVector().subtract(attacker.getLocation().toVector()).normalize().multiply(1.5).setY(0.5);
                victim.setVelocity(kb);
                victim.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 0, false, false));
                attacker.sendMessage(ChatColor.GOLD + "Mace hit for " + maceHitDamage + "!");
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player shooter)) return;
        if (!playtimeManager.isEnabled()) return;

        ItemStack weapon = shooter.getInventory().getItemInMainHand();
        String type = weaponManager.getWeaponType(weapon);
        if (!WeaponManager.NUKE_TRIDENT.equals(type)) return;

        Location loc = event.getEntity().getLocation();
        World world = loc.getWorld();

        for (Entity entity : world.getNearbyEntities(loc, 8, 8, 8)) {
            if (entity instanceof Player target && !target.equals(shooter)) {
                target.damage(25.0, shooter);
                playtimeManager.removeTime(target.getUniqueId(), 15);
                target.sendMessage(ChatColor.RED + "Nuke hit! -15s!");
            }
        }
        shooter.sendMessage(ChatColor.DARK_RED + "Nuke detonated!");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!playtimeManager.isEnabled()) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        String type = weaponManager.getWeaponType(player.getInventory().getItemInMainHand());
        if (type == null) return;

        if (WeaponManager.PAUSE_SHARD.equals(type)) {
            UUID uuid = player.getUniqueId();
            if (pauseCooldown.containsKey(uuid) && System.currentTimeMillis() - pauseCooldown.get(uuid) < pauseCooldownMs) {
                long rem = (pauseCooldownMs - (System.currentTimeMillis() - pauseCooldown.get(uuid))) / 1000;
                player.sendMessage(ChatColor.RED + "Cooldown: " + rem + "s");
                return;
            }
            pauseCooldown.put(uuid, System.currentTimeMillis());
            playtimeManager.pauseTime(uuid, pausePauseDuration);
            player.sendMessage(ChatColor.AQUA + "Time paused for " + pausePauseDuration + "s!");

        } else if (WeaponManager.TIME_MACE.equals(type)) {
            UUID uuid = player.getUniqueId();
            if (maceCooldown.containsKey(uuid) && System.currentTimeMillis() - maceCooldown.get(uuid) < maceCooldownMs) {
                long rem = (maceCooldownMs - (System.currentTimeMillis() - maceCooldown.get(uuid))) / 1000;
                player.sendMessage(ChatColor.RED + "Anvil Rain cooldown: " + rem + "s");
                return;
            }
            maceCooldown.put(uuid, System.currentTimeMillis());
            player.sendMessage(ChatColor.GOLD + "Anvil Rain!");
            launchAnvilRain(player);
        }
    }

    private void cagePlayer(Player player, Location loc) {
        int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
        World world = player.getWorld();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 0; dy <= 2; dy++) {
                    if (dy != 0 && dy != 2 && Math.abs(dx) != 1 && Math.abs(dz) != 1) continue;
                    world.getBlockAt(x + dx, y + dy, z + dz).setType(Material.OBSIDIAN);
                }
            }
        }
        player.teleport(new Location(world, x + 0.5, y + 0.1, z + 0.5));
    }

    private void uncagePlayer(Location loc) {
        int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
        World world = loc.getWorld();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 0; dy <= 2; dy++) {
                    if (dy != 0 && dy != 2 && Math.abs(dx) != 1 && Math.abs(dz) != 1) continue;
                    world.getBlockAt(x + dx, y + dy, z + dz).setType(Material.AIR);
                }
            }
        }
    }

    private void launchAnvilRain(Player player) {
        Location center = player.getLocation();
        World world = player.getWorld();

        new BukkitRunnable() {
            int wave = 0;
            @Override
            public void run() {
                if (wave >= maceAnvilWaves) { cancel(); return; }
                double radius = 2.0 + (wave * 2.0);
                for (int i = 0; i < maceAnvilsPerWave; i++) {
                    double angle = 2 * Math.PI * i / maceAnvilsPerWave + (wave * 0.5);
                    double x = center.getX() + Math.cos(angle) * radius;
                    double z = center.getZ() + Math.sin(angle) * radius;
                    Location spawnLoc = new Location(world, x, center.getY() + 20, z);
                    world.spawnFallingBlock(spawnLoc, Material.ANVIL.createBlockData());
                }
                wave++;
            }
        }.runTaskTimer(plugin, 10L, 10L);
    }

    public void cleanupAnvils() {}
}
