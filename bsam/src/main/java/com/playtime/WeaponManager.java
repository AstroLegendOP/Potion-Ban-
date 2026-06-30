package com.playtime;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class WeaponManager {

    private final JavaPlugin plugin;
    private final NamespacedKey weaponKey;

    public static final String STEAL_SWORD = "time_steal_sword";
    public static final String CAGE_SWORD = "time_cage_sword";
    public static final String TIME_MACE = "time_mace";
    public static final String NUKE_TRIDENT = "nuke_trident";
    public static final String PAUSE_SHARD = "time_pause_shard";

    public WeaponManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.weaponKey = new NamespacedKey(plugin, "weapon_type");
    }

    public NamespacedKey getWeaponKey() {
        return weaponKey;
    }

    public ItemStack createTimeStealSword() {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Time Steal Sword");
        meta.addEnchant(Enchantment.SHARPNESS, 3, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.getPersistentDataContainer().set(weaponKey, PersistentDataType.STRING, STEAL_SWORD);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Steals " + ChatColor.GREEN + "10 seconds" + ChatColor.GRAY + " per hit");
        lore.add("");
        lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Forged from the essence of time itself");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createTimeCageSword() {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Time Cage Sword");
        meta.addEnchant(Enchantment.SHARPNESS, 2, true);
        meta.addEnchant(Enchantment.KNOCKBACK, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.getPersistentDataContainer().set(weaponKey, PersistentDataType.STRING, CAGE_SWORD);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Traps enemies in an " + ChatColor.DARK_RED + "obsidian cage" + ChatColor.GRAY + " for 5s");
        lore.add("");
        lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Time bends to your will");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createTimeMace() {
        ItemStack item = new ItemStack(Material.MACE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Time Mace");
        meta.addEnchant(Enchantment.SMITE, 4, true);
        meta.addEnchant(Enchantment.DENSITY, 3, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.getPersistentDataContainer().set(weaponKey, PersistentDataType.STRING, TIME_MACE);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Launch attack & " + ChatColor.RED + "anvil rain" + ChatColor.GRAY + " ability");
        lore.add(ChatColor.GRAY + "Right-click: " + ChatColor.YELLOW + "Anvil Rain");
        lore.add("");
        lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "The weight of time crushes all");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createNukeTrident() {
        ItemStack item = new ItemStack(Material.TRIDENT);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Nuke Trident");
        meta.addEnchant(Enchantment.LOYALTY, 3, true);
        meta.addEnchant(Enchantment.CHANNELING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.getPersistentDataContainer().set(weaponKey, PersistentDataType.STRING, NUKE_TRIDENT);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Creates a " + ChatColor.RED + "massive explosion" + ChatColor.GRAY + " on impact");
        lore.add(ChatColor.GRAY + "Deals " + ChatColor.RED + "30 damage" + ChatColor.GRAY + " in a large area");
        lore.add("");
        lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "The apocalypse in your hands");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createTimePauseShard() {
        ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Time Pause Shard");
        meta.addEnchant(Enchantment.UNBREAKING, 3, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.getPersistentDataContainer().set(weaponKey, PersistentDataType.STRING, PAUSE_SHARD);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Right-click to " + ChatColor.AQUA + "pause your time" + ChatColor.GRAY + " for 30s");
        lore.add(ChatColor.GRAY + "Cooldown: " + ChatColor.YELLOW + "3 minutes");
        lore.add("");
        lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "A fragment of frozen time");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public String getWeaponType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(weaponKey, PersistentDataType.STRING);
    }

    public boolean isCustomWeapon(ItemStack item) {
        return getWeaponType(item) != null;
    }
}
