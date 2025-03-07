package com.venned.simplecrates;

import com.venned.simplecrates.commands.LootBoxCommand;
import com.venned.simplecrates.commands.LootBoxTabCompleter;
import com.venned.simplecrates.commands.crates.CrateCommand;
import com.venned.simplecrates.gui.edit.EditChances;
import com.venned.simplecrates.gui.edit.EditOptions;
import com.venned.simplecrates.gui.listener.EditListener;
import com.venned.simplecrates.gui.preview.PreviewRewards;
import com.venned.simplecrates.listeners.PlayerCrateListener;
import com.venned.simplecrates.listeners.PlayerLootBoxListener;
import com.venned.simplecrates.manager.CrateBlockManager;
import com.venned.simplecrates.manager.CrateManager;
import com.venned.simplecrates.manager.LootBoxManager;
import com.venned.simplecrates.task.HologramChestTask;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public final class Main extends JavaPlugin implements Listener {

    public static Main instance;

    LootBoxManager lootBoxManager;
    EditChances editChances;
    EditOptions editOptions;
    PreviewRewards previewRewards;
    CrateManager crateManager;
    CrateBlockManager crateBlockManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        instance = this;

        lootBoxManager = new LootBoxManager();
        crateManager = new CrateManager();
        crateBlockManager = new CrateBlockManager(this, crateManager);
        editChances = new EditChances();
        editOptions = new EditOptions();
        previewRewards = new PreviewRewards(this);

        loadCommands();
        loadListeners();

        new HologramChestTask().runTaskTimer(this, 20, 120);

    }

    @Override
    public void onDisable() {
        lootBoxManager.saveLootBoxes();
        crateManager.saveCrates();
        crateBlockManager.saveCrates();

    }

    void loadListeners(){
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new EditListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerLootBoxListener(lootBoxManager, previewRewards), this);
        getServer().getPluginManager().registerEvents(new PlayerCrateListener(crateBlockManager, previewRewards), this);
    }

    void loadCommands(){
        getCommand("lootbox").setExecutor(new LootBoxCommand(lootBoxManager, editChances));
        getCommand("lootbox").setTabCompleter(new LootBoxTabCompleter(lootBoxManager));
        getCommand("crate").setExecutor(new CrateCommand(crateManager, editChances, crateBlockManager));
    }


    public static String getMessage(String key, Map<String, String> placeholders) {
        String message = Main.getInstance().getConfig().getString("messages." + key, "&cMessage not found: " + key);

        // Reemplazar los placeholders
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }


    public CrateBlockManager getCrateBlockManager() {
        return crateBlockManager;
    }

    public static Main getInstance() {
        return instance;
    }

    public EditOptions getEditOptions() {
        return editOptions;
    }

    public EditChances getEditChances() {
        return editChances;
    }
}
