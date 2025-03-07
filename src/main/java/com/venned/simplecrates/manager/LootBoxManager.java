package com.venned.simplecrates.manager;

import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.ItemReward;
import com.venned.simplecrates.build.LootBox;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.util.*;

public class LootBoxManager {

    private final Set<LootBox> lootBoxes = new HashSet<>();
    private final File lootBoxFile;
    private FileConfiguration lootBoxConfig;

    public LootBoxManager() {
        lootBoxFile = new File(Main.getInstance().getDataFolder(), "lootbox.yml");

        if (!lootBoxFile.exists()) {
            try {
                lootBoxFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        lootBoxConfig = YamlConfiguration.loadConfiguration(lootBoxFile);
        loadLootBoxes();
    }

    public Set<LootBox> getLootBoxes() {
        return lootBoxes;
    }

    public void addLootBox(LootBox lootBox) {
        lootBoxes.add(lootBox);
        saveLootBoxes();
    }

    public void reloadAll(){
        lootBoxConfig = YamlConfiguration.loadConfiguration(lootBoxFile);

        loadLootBoxes();
    }

    public void refreshAll(){
        saveLootBoxes();
    }

    public void saveLootBoxes() {
        lootBoxConfig.set("lootboxes", null);

        for (LootBox lootBox : lootBoxes) {
            String path = "lootboxes." + lootBox.getName();
            lootBoxConfig.set(path + ".name", lootBox.getName());
            lootBoxConfig.set(path + ".display_name", lootBox.getDisplayName());
            lootBoxConfig.set(path + ".max_reward", lootBox.getMax_reward());
            lootBoxConfig.set(path + ".lore", lootBox.getLoreS());



            List<Map<String, Object>> rewardList = new ArrayList<>();
            for (ItemReward reward : lootBox.getRewards()) {
                Map<String, Object> rewardMap = new HashMap<>();
                rewardMap.put("name", reward.getName());
                rewardMap.put("chance", reward.getChance());
                rewardMap.put("item", serializeItemStack(reward.getItemStack()));
                rewardMap.put("commands", reward.getCommands());
                rewardMap.put("visible", reward.isVisible());

                rewardList.add(rewardMap);
            }

            lootBoxConfig.set(path + ".rewards", rewardList);
        }

        try {
            lootBoxConfig.save(lootBoxFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadLootBoxes() {
        lootBoxes.clear();

        if (!lootBoxConfig.contains("lootboxes")) return;

        ConfigurationSection section = lootBoxConfig.getConfigurationSection("lootboxes");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            String name = section.getString(key + ".name");

            String displayName = section.getString(key + ".display_name");
            if(displayName == null){
                displayName = name;
            }

            int max_reward = section.getInt(key + ".max_reward");

            List<String> lore = section.getStringList(key + ".lore");

            List<ItemReward> rewards = new ArrayList<>();
            List<Map<?, ?>> rewardList = section.getMapList(key + ".rewards");

            for (Map<?, ?> rewardMap : rewardList) {
                String rewardName = (String) rewardMap.get("name");
                double chance = (double) rewardMap.get("chance");

                List<String> commands = (List<String>) rewardMap.get("commands");
                ItemStack itemStack = (ItemStack) deserializeItemStack((String) rewardMap.get("item"));

                boolean visible = (boolean) rewardMap.get("visible");
                rewards.add(new ItemReward(rewardName, itemStack, chance, commands, visible, new ArrayList<>()));
            }

            lootBoxes.add(new LootBox(name, rewards, displayName, max_reward, lore));
        }
    }

    private String serializeItemStack(ItemStack item) {
        if (item == null) return null;
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream bukkitObjectOutputStream = new BukkitObjectOutputStream(byteArrayOutputStream);
            bukkitObjectOutputStream.writeObject(item);
            bukkitObjectOutputStream.close();
            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private ItemStack deserializeItemStack(String data) {
        if (data == null || data.isEmpty()) return null;
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(data);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decodedBytes);
            BukkitObjectInputStream bukkitObjectInputStream = new BukkitObjectInputStream(byteArrayInputStream);
            ItemStack item = (ItemStack) bukkitObjectInputStream.readObject();
            bukkitObjectInputStream.close();
            return item;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
