package com.playtime;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class CraftingManager {

    private final JavaPlugin plugin;
    private final WeaponManager weaponManager;

    public CraftingManager(JavaPlugin plugin, WeaponManager weaponManager) {
        this.plugin = plugin;
        this.weaponManager = weaponManager;
    }

    public void registerRecipes() {
        registerTimeStealSword();
        registerTimeCageSword();
        registerTimeMace();
        registerNukeTrident();
        registerTimePauseShard();
    }

    private void registerTimeStealSword() {
        NamespacedKey key = new NamespacedKey(plugin, "craft_time_steal_sword");
        ShapedRecipe recipe = new ShapedRecipe(key, weaponManager.createTimeStealSword());
        recipe.shape(" E ", " S ", " I ");
        recipe.setIngredient('E', Material.ENDER_PEARL);
        recipe.setIngredient('S', Material.DIAMOND_SWORD);
        recipe.setIngredient('I', Material.IRON_INGOT);
        plugin.getServer().addRecipe(recipe);
    }

    private void registerTimeCageSword() {
        NamespacedKey key = new NamespacedKey(plugin, "craft_time_cage_sword");
        ShapedRecipe recipe = new ShapedRecipe(key, weaponManager.createTimeCageSword());
        recipe.shape("O O", " S ", "O O");
        recipe.setIngredient('O', Material.OBSIDIAN);
        recipe.setIngredient('S', Material.DIAMOND_SWORD);
        plugin.getServer().addRecipe(recipe);
    }

    private void registerTimeMace() {
        NamespacedKey key = new NamespacedKey(plugin, "craft_time_mace");
        ShapedRecipe recipe = new ShapedRecipe(key, weaponManager.createTimeMace());
        recipe.shape(" N ", " B ", " I ");
        recipe.setIngredient('N', Material.NETHERITE_INGOT);
        recipe.setIngredient('B', Material.BLAZE_ROD);
        recipe.setIngredient('I', Material.IRON_BLOCK);
        plugin.getServer().addRecipe(recipe);
    }

    private void registerNukeTrident() {
        NamespacedKey key = new NamespacedKey(plugin, "craft_nuke_trident");
        ShapedRecipe recipe = new ShapedRecipe(key, weaponManager.createNukeTrident());
        recipe.shape("TGT", " G ", " G ");
        recipe.setIngredient('T', Material.TRIDENT);
        recipe.setIngredient('G', Material.GUNPOWDER);
        plugin.getServer().addRecipe(recipe);
    }

    private void registerTimePauseShard() {
        NamespacedKey key = new NamespacedKey(plugin, "craft_time_pause_shard");
        ShapedRecipe recipe = new ShapedRecipe(key, weaponManager.createTimePauseShard());
        recipe.shape("GAG", "ADA", "GAG");
        recipe.setIngredient('G', Material.GLOWSTONE_DUST);
        recipe.setIngredient('A', Material.AMETHYST_SHARD);
        recipe.setIngredient('D', Material.DIAMOND);
        plugin.getServer().addRecipe(recipe);
    }
}
