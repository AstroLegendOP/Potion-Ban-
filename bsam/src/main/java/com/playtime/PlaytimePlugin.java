package com.playtime;

import org.bukkit.plugin.java.JavaPlugin;

public class PlaytimePlugin extends JavaPlugin {

    private PlaytimeManager playtimeManager;
    private WeaponManager weaponManager;
    private WeaponListener weaponListener;
    private ActionBarManager actionBarManager;
    private AdminGUI adminGUI;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        playtimeManager = new PlaytimeManager(this);
        playtimeManager.loadData();

        weaponManager = new WeaponManager(this);
        actionBarManager = new ActionBarManager(this, playtimeManager);

        adminGUI = new AdminGUI(playtimeManager, weaponManager);
        CraftingManager craftingManager = new CraftingManager(this, weaponManager);

        weaponListener = new WeaponListener(this, playtimeManager, weaponManager);
        getServer().getPluginManager().registerEvents(new KillListener(playtimeManager), this);
        getServer().getPluginManager().registerEvents(new JoinListener(playtimeManager), this);
        getServer().getPluginManager().registerEvents(new PlayerSessionListener(playtimeManager), this);
        getServer().getPluginManager().registerEvents(weaponListener, this);
        getServer().getPluginManager().registerEvents(adminGUI, this);

        PlaytimeCommand command = new PlaytimeCommand(playtimeManager, weaponManager, adminGUI);
        getCommand("playtime").setExecutor(command);
        getCommand("playtime").setTabCompleter(command);
        getCommand("setplaytime").setExecutor(command);
        getCommand("setplaytime").setTabCompleter(command);

        craftingManager.registerRecipes();

        playtimeManager.startDailyResetTask();
        playtimeManager.startPlaytimeTickTask();
        actionBarManager.startUpdateTask();

        getLogger().info("TickSMP enabled! Particles: " + getConfig().getDouble("particle-multiplier"));
    }

    @Override
    public void onDisable() {
        if (weaponListener != null) weaponListener.cleanupAnvils();
        if (playtimeManager != null) playtimeManager.saveData();
    }
}
