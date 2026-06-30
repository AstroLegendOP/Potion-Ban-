package com.playtime;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PlaytimeCommand implements CommandExecutor, TabCompleter {

    private final PlaytimeManager playtimeManager;
    private final WeaponManager weaponManager;
    private final AdminGUI adminGUI;

    public PlaytimeCommand(PlaytimeManager playtimeManager, WeaponManager weaponManager, AdminGUI adminGUI) {
        this.playtimeManager = playtimeManager;
        this.weaponManager = weaponManager;
        this.adminGUI = adminGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("playtime")) {
            if (args.length == 0) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Use: /playtime start|stop|status|gui|weapons");
                    return true;
                }
                Player player = (Player) sender;
                int time = playtimeManager.getPlaytime(player.getUniqueId());
                ChatColor color = playtimeManager.getTimeColor(time);
                String pause = playtimeManager.isTimePaused(player.getUniqueId()) ? ChatColor.AQUA + " [PAUSED]" : "";

                player.sendMessage("");
                player.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "  TICK SMP SEASON 2");
                player.sendMessage(ChatColor.GRAY + "  Status: " + (playtimeManager.isEnabled() ? ChatColor.GREEN + "ACTIVE" : ChatColor.RED + "INACTIVE"));
                player.sendMessage(ChatColor.GRAY + "  Time: " + color + playtimeManager.formatTime(time) + pause);
                player.sendMessage(ChatColor.GRAY + "  Health: " + ChatColor.RED + String.format("%.1f", player.getHealth()) + "/20.0");
                player.sendMessage(ChatColor.GRAY + "  Kills: " + ChatColor.GREEN + player.getStatistic(org.bukkit.Statistic.PLAYER_KILLS));
                player.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "  ================");
                player.sendMessage("");
                return true;
            }

            String sub = args[0].toLowerCase();

            if (sub.equals("start")) {
                if (!sender.hasPermission("playtime.admin")) {
                    sender.sendMessage(ChatColor.RED + "No permission!");
                    return true;
                }
                if (playtimeManager.isEnabled()) {
                    sender.sendMessage(ChatColor.YELLOW + "Already active!");
                    return true;
                }
                playtimeManager.setEnabled(true);
                Bukkit.broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Tick SMP Season 2 ACTIVATED! Kill to survive!");
                return true;
            }

            if (sub.equals("stop")) {
                if (!sender.hasPermission("playtime.admin")) {
                    sender.sendMessage(ChatColor.RED + "No permission!");
                    return true;
                }
                if (!playtimeManager.isEnabled()) {
                    sender.sendMessage(ChatColor.YELLOW + "Already inactive!");
                    return true;
                }
                playtimeManager.setEnabled(false);
                Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Tick SMP Season 2 DEACTIVATED!");
                return true;
            }

            if (sub.equals("status")) {
                if (!sender.hasPermission("playtime.admin")) {
                    sender.sendMessage(ChatColor.RED + "No permission!");
                    return true;
                }
                sender.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "=== SYSTEM STATUS ===");
                sender.sendMessage(ChatColor.GRAY + "Active: " + (playtimeManager.isEnabled() ? ChatColor.GREEN + "YES" : ChatColor.RED + "NO"));
                sender.sendMessage(ChatColor.GRAY + "Online: " + ChatColor.WHITE + Bukkit.getOnlinePlayers().size());
                sender.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "====================");
                return true;
            }

            if (sub.equals("gui") || sub.equals("admin")) {
                if (!sender.hasPermission("playtime.admin")) {
                    sender.sendMessage(ChatColor.RED + "No permission!");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Players only!");
                    return true;
                }
                adminGUI.openMainMenu((Player) sender);
                return true;
            }

            if (sub.equals("weapons") || sub.equals("weapon")) {
                if (!sender.hasPermission("playtime.admin")) {
                    sender.sendMessage(ChatColor.RED + "No permission!");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Players only!");
                    return true;
                }
                adminGUI.openWeaponGiveMenu((Player) sender);
                return true;
            }

            if (sub.equals("give")) {
                if (!sender.hasPermission("playtime.admin")) {
                    sender.sendMessage(ChatColor.RED + "No permission!");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Players only!");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /playtime give <weapon>");
                    sender.sendMessage(ChatColor.GRAY + "Weapons: steal, cage, mace, nuke, pause");
                    return true;
                }
                Player player = (Player) sender;
                switch (args[1].toLowerCase()) {
                    case "steal": player.getInventory().addItem(weaponManager.createTimeStealSword()); break;
                    case "cage": player.getInventory().addItem(weaponManager.createTimeCageSword()); break;
                    case "mace": player.getInventory().addItem(weaponManager.createTimeMace()); break;
                    case "nuke": player.getInventory().addItem(weaponManager.createNukeTrident()); break;
                    case "pause": player.getInventory().addItem(weaponManager.createTimePauseShard()); break;
                    default:
                        sender.sendMessage(ChatColor.RED + "Unknown weapon! Options: steal, cage, mace, nuke, pause");
                        return true;
                }
                sender.sendMessage(ChatColor.GREEN + "Weapon given!");
                return true;
            }

            Player target = Bukkit.getPlayer(sub);
            if (target != null) {
                int time = playtimeManager.getPlaytime(target.getUniqueId());
                ChatColor color = playtimeManager.getTimeColor(time);
                sender.sendMessage(ChatColor.GREEN + target.getName() + ": " + color + playtimeManager.formatTime(time));
                return true;
            }

            sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use: start|stop|status|gui|weapons|give|<player>");
            return true;
        }

        if (command.getName().equalsIgnoreCase("setplaytime")) {
            if (!sender.hasPermission("playtime.admin")) {
                sender.sendMessage(ChatColor.RED + "No permission!");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /setplaytime <player> <minutes>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found!");
                return true;
            }
            try {
                int minutes = Integer.parseInt(args[1]);
                int seconds = minutes * 60;
                playtimeManager.setPlaytime(target.getUniqueId(), seconds);
                sender.sendMessage(ChatColor.GREEN + "Set " + target.getName() + " to " + playtimeManager.formatTime(seconds));
                target.sendMessage(ChatColor.GREEN + "Your time has been set to " + playtimeManager.formatTime(seconds));
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid number!");
            }
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("playtime")) {
            if (args.length == 1) {
                List<String> subs = new ArrayList<>(Arrays.asList("start", "stop", "status", "gui", "weapons", "give"));
                subs.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
                return subs.stream().filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
                return Arrays.asList("steal", "cage", "mace", "nuke", "pause").stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
            }
        }

        if (command.getName().equalsIgnoreCase("setplaytime") && args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }

        return completions;
    }
}
