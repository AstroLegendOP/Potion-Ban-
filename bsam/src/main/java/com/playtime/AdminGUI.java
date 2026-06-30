package com.playtime;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class AdminGUI implements Listener {

    private final PlaytimeManager playtimeManager;
    private final WeaponManager weaponManager;
    private static final String GUI_TITLE = ChatColor.DARK_RED + "" + ChatColor.BOLD + "Tick SMP Season 2 - Admin";

    public AdminGUI(PlaytimeManager playtimeManager, WeaponManager weaponManager) {
        this.playtimeManager = playtimeManager;
        this.weaponManager = weaponManager;
    }

    public void openMainMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, GUI_TITLE);

        int slot = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (slot >= 45) break;
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwningPlayer(online);
            meta.setDisplayName(ChatColor.GREEN + online.getName());

            int time = playtimeManager.getPlaytime(online.getUniqueId());
            ChatColor color = playtimeManager.getTimeColor(time);
            String pause = playtimeManager.isTimePaused(online.getUniqueId()) ? ChatColor.AQUA + " [PAUSED]" : "";

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Time: " + color + playtimeManager.formatTime(time) + pause);
            lore.add("");
            lore.add(ChatColor.YELLOW + "Left-click: Add/Subtract time");
            lore.add(ChatColor.YELLOW + "Right-click: Manage weapons");
            meta.setLore(lore);
            skull.setItemMeta(meta);
            gui.setItem(slot, skull);
            slot++;
        }

        ItemStack addTime = createGuiItem(Material.LIME_DYE, ChatColor.GREEN + "+30 Minutes", ChatColor.GRAY + "Click to select player");
        ItemStack removeTime = createGuiItem(Material.RED_DYE, ChatColor.RED + "-30 Minutes", ChatColor.GRAY + "Click to select player");
        ItemStack setTime = createGuiItem(Material.CLOCK, ChatColor.YELLOW + "Set Time", ChatColor.GRAY + "Click to select player");
        ItemStack pauseAll = createGuiItem(Material.BARRIER, ChatColor.AQUA + "Pause All", ChatColor.GRAY + "Pause all players' time");
        ItemStack unpauseAll = createGuiItem(Material.LIME_STAINED_GLASS, ChatColor.GREEN + "Unpause All", ChatColor.GRAY + "Unpause all players' time");
        ItemStack giveWeapons = createGuiItem(Material.NETHERITE_SWORD, ChatColor.GOLD + "Give Weapons", ChatColor.GRAY + "Click to open weapon menu");

        gui.setItem(46, addTime);
        gui.setItem(47, removeTime);
        gui.setItem(49, setTime);
        gui.setItem(50, pauseAll);
        gui.setItem(51, unpauseAll);
        gui.setItem(52, giveWeapons);

        player.openInventory(gui);
    }

    public void openTimeMenu(Player admin, Player target) {
        Inventory gui = Bukkit.createInventory(null, 27,
                ChatColor.DARK_RED + "Manage: " + target.getName());

        int time = playtimeManager.getPlaytime(target.getUniqueId());
        ChatColor color = playtimeManager.getTimeColor(time);

        ItemStack info = createGuiItem(Material.PAPER, ChatColor.WHITE + target.getName(),
                ChatColor.GRAY + "Current: " + color + playtimeManager.formatTime(time));
        gui.setItem(4, info);

        gui.setItem(10, createGuiItem(Material.LIME_DYE, ChatColor.GREEN + "+1 Hour"));
        gui.setItem(11, createGuiItem(Material.LIME_DYE, ChatColor.GREEN + "+30 Minutes"));
        gui.setItem(12, createGuiItem(Material.LIME_DYE, ChatColor.GREEN + "+5 Minutes"));
        gui.setItem(14, createGuiItem(Material.RED_DYE, ChatColor.RED + "-5 Minutes"));
        gui.setItem(15, createGuiItem(Material.RED_DYE, ChatColor.RED + "-30 Minutes"));
        gui.setItem(16, createGuiItem(Material.RED_DYE, ChatColor.RED + "-1 Hour"));

        gui.setItem(22, createGuiItem(Material.BARRIER, ChatColor.RED + "Set to 0 (Eliminate)"));

        playerTargetMap.put(admin.getUniqueId(), target);
        admin.openInventory(gui);
    }

    public void openWeaponMenu(Player admin, Player target) {
        Inventory gui = Bukkit.createInventory(null, 27,
                ChatColor.DARK_RED + "Weapons for: " + target.getName());

        gui.setItem(10, weaponManager.createTimeStealSword());
        gui.setItem(11, weaponManager.createTimeCageSword());
        gui.setItem(12, weaponManager.createTimeMace());
        gui.setItem(14, weaponManager.createNukeTrident());
        gui.setItem(15, weaponManager.createTimePauseShard());

        gui.setItem(26, createGuiItem(Material.ARROW, ChatColor.WHITE + "Back"));

        weaponTargetMap.put(admin.getUniqueId(), target);
        admin.openInventory(gui);
    }

    private final HashMap<UUID, Player> playerTargetMap = new HashMap<>();
    private final HashMap<UUID, Player> weaponTargetMap = new HashMap<>();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player admin = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        String title = event.getView().getTitle();

        if (title.equals(GUI_TITLE)) {
            event.setCancelled(true);
            if (event.getRawSlot() < 45 && clicked.getType() == Material.PLAYER_HEAD) {
                String targetName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                Player target = Bukkit.getPlayer(targetName);
                if (target != null) {
                    if (event.isLeftClick()) {
                        openTimeMenu(admin, target);
                    } else if (event.isRightClick()) {
                        openWeaponMenu(admin, target);
                    }
                }
            } else if (event.getRawSlot() == 46) {
                admin.sendMessage(ChatColor.YELLOW + "Click a player to add 30 minutes");
            } else if (event.getRawSlot() == 47) {
                admin.sendMessage(ChatColor.YELLOW + "Click a player to subtract 30 minutes");
            } else if (event.getRawSlot() == 50) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    playtimeManager.pauseTime(p.getUniqueId(), 60);
                }
                admin.sendMessage(ChatColor.AQUA + "All players' time paused for 60 seconds!");
            } else if (event.getRawSlot() == 51) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    playtimeManager.getTimePausedMap().put(p.getUniqueId(), false);
                }
                admin.sendMessage(ChatColor.GREEN + "All players' time unpaused!");
            } else if (event.getRawSlot() == 52) {
                openWeaponGiveMenu(admin);
            }
            return;
        }

        if (title.startsWith("Manage:")) {
            event.setCancelled(true);
            Player target = playerTargetMap.get(admin.getUniqueId());
            if (target == null) return;

            switch (event.getRawSlot()) {
                case 10: playtimeManager.addTime(target.getUniqueId(), 3600); admin.sendMessage(ChatColor.GREEN + "+1 hour to " + target.getName()); break;
                case 11: playtimeManager.addTime(target.getUniqueId(), 1800); admin.sendMessage(ChatColor.GREEN + "+30 min to " + target.getName()); break;
                case 12: playtimeManager.addTime(target.getUniqueId(), 300); admin.sendMessage(ChatColor.GREEN + "+5 min to " + target.getName()); break;
                case 14: playtimeManager.removeTime(target.getUniqueId(), 300); admin.sendMessage(ChatColor.RED + "-5 min from " + target.getName()); break;
                case 15: playtimeManager.removeTime(target.getUniqueId(), 1800); admin.sendMessage(ChatColor.RED + "-30 min from " + target.getName()); break;
                case 16: playtimeManager.removeTime(target.getUniqueId(), 3600); admin.sendMessage(ChatColor.RED + "-1 hour from " + target.getName()); break;
                case 22:
                    playtimeManager.setPlaytime(target.getUniqueId(), 0);
                    admin.sendMessage(ChatColor.DARK_RED + "Set " + target.getName() + " to 0!");
                    target.sendMessage(ChatColor.DARK_RED + "Your time has been set to 0 by an admin!");
                    target.setHealth(0);
                    break;
            }
            openTimeMenu(admin, target);
            return;
        }

        if (title.startsWith("Weapons for:")) {
            event.setCancelled(true);
            Player target = weaponTargetMap.get(admin.getUniqueId());
            if (target == null) return;

            if (event.getRawSlot() == 26) {
                openMainMenu(admin);
                return;
            }

            if (clicked.getType() == Material.DIAMOND_SWORD || clicked.getType() == Material.MACE ||
                    clicked.getType() == Material.TRIDENT || clicked.getType() == Material.AMETHYST_SHARD) {
                target.getInventory().addItem(clicked.clone());
                admin.sendMessage(ChatColor.GREEN + "Gave " + ChatColor.stripColor(clicked.getItemMeta().getDisplayName()) + " to " + target.getName());
                target.sendMessage(ChatColor.GREEN + "You received a " + ChatColor.stripColor(clicked.getItemMeta().getDisplayName()) + "!");
            }
            return;
        }

        if (title.equals(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Weapon Select")) {
            event.setCancelled(true);
            if (event.getRawSlot() == 10) admin.getInventory().addItem(weaponManager.createTimeStealSword());
            if (event.getRawSlot() == 11) admin.getInventory().addItem(weaponManager.createTimeCageSword());
            if (event.getRawSlot() == 12) admin.getInventory().addItem(weaponManager.createTimeMace());
            if (event.getRawSlot() == 14) admin.getInventory().addItem(weaponManager.createNukeTrident());
            if (event.getRawSlot() == 15) admin.getInventory().addItem(weaponManager.createTimePauseShard());
            admin.sendMessage(ChatColor.GREEN + "Weapon added to your inventory!");
        }
    }

    public void openWeaponGiveMenu(Player admin) {
        Inventory gui = Bukkit.createInventory(null, 27,
                ChatColor.DARK_RED + "" + ChatColor.BOLD + "Weapon Select");

        gui.setItem(10, weaponManager.createTimeStealSword());
        gui.setItem(11, weaponManager.createTimeCageSword());
        gui.setItem(12, weaponManager.createTimeMace());
        gui.setItem(14, weaponManager.createNukeTrident());
        gui.setItem(15, weaponManager.createTimePauseShard());

        admin.openInventory(gui);
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) {
            List<String> loreList = new ArrayList<>();
            for (String l : lore) loreList.add(l);
            meta.setLore(loreList);
        }
        item.setItemMeta(meta);
        return item;
    }
}
