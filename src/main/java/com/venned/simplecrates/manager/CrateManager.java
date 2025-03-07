package com.venned.simplecrates.manager;

import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.Crate;
import com.venned.simplecrates.build.ItemReward;
import com.venned.simplecrates.build.LootBox;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class CrateManager {


    private final Set<Crate> crates = new HashSet<>();
    private final File cratesFile;
    private FileConfiguration cratesConfig;

    public CrateManager() {
        cratesFile = new File(Main.getInstance().getDataFolder(), "crates.yml");

        if (!cratesFile.exists()) {
            try {
                cratesFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        cratesConfig = YamlConfiguration.loadConfiguration(cratesFile);
        loadCrates();
    }

    public void loadCrates() {
        crates.clear();

        if (!cratesConfig.contains("crates")) return;

        ConfigurationSection section = cratesConfig.getConfigurationSection("crates");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            String name = section.getString(key + ".name");

            String displayName = section.getString(key + ".display_name");
            if(displayName == null){
                displayName = name;
            }

            int max_reward = section.getInt(key + ".max_reward");

            List<String> lore = section.getStringList(key + ".lore");

            List<String> hologramText = section.getStringList(key + ".textHologram");

            String materialName = section.getString(key + ".key.material");
            Material material = Material.matchMaterial(materialName);
            String keyName = section.getString(key + ".key.name");
            List<String> keyLore = section.getStringList(key + ".key.lore");
            ItemStack itemKey = new ItemStack(material);
            ItemMeta meta = itemKey.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(keyName);
                meta.setLore(keyLore);
                itemKey.setItemMeta(meta);
            }

            List<ItemReward> rewards = new ArrayList<>();
            List<Map<?, ?>> rewardList = section.getMapList(key + ".rewards");

            for (Map<?, ?> rewardMap : rewardList) {
                String rewardName = (String) rewardMap.get("name");
                double chance = (double) rewardMap.get("chance");

                List<String> commands = (List<String>) rewardMap.get("commands");
                ItemStack itemStack = (ItemStack) deserializeItemStack((String) rewardMap.get("item"));

                boolean visible = (boolean) rewardMap.get("visible");


                List<String> UUIDs = (List<String>) rewardMap.get("disabled_players");

                List<UUID> playerDisabled = new ArrayList<>();
                for(String uuid : UUIDs){
                    playerDisabled.add(UUID.fromString(uuid));
                }

                rewards.add(new ItemReward(rewardName, itemStack, chance, commands, visible, playerDisabled));
            }

            crates.add(new Crate(name, rewards, displayName, max_reward, lore, hologramText, itemKey));
        }
    }

    public void saveCrates() {
        cratesConfig.set("crates", null);

        for (Crate crates : crates) {
            String path = "crates." + crates.getName();
            cratesConfig.set(path + ".name", crates.getName());
            cratesConfig.set(path + ".display_name", crates.getDisplayName());
            cratesConfig.set(path + ".max_reward", crates.getMax_reward());
            cratesConfig.set(path + ".lore", crates.getLoreS());
            cratesConfig.set(path + ".textHologram", crates.getHologramText());
            cratesConfig.set(path + ".key.material", crates.getItemKey().getType().name());
            cratesConfig.set(path + ".key.name", crates.getItemKey().getItemMeta().getDisplayName());
            cratesConfig.set(path + ".key.lore", crates.getItemKey().getItemMeta().hasLore() ? crates.getItemKey().getItemMeta().getLore() : new ArrayList<>());



            List<Map<String, Object>> rewardList = new ArrayList<>();
            for (ItemReward reward : crates.getRewards()) {
                Map<String, Object> rewardMap = new HashMap<>();
                rewardMap.put("name", reward.getName());
                rewardMap.put("chance", reward.getChance());
                rewardMap.put("item", serializeItemStack(reward.getItemStack()));
                rewardMap.put("commands", reward.getCommands());
                rewardMap.put("visible", reward.isVisible());

                List<String> UUIDs = new ArrayList<>();
                for(UUID uuid : reward.getDisabledPlayers()){
                    UUIDs.add(uuid.toString());
                }
                rewardMap.put("disabled_players", UUIDs);


                rewardList.add(rewardMap);
            }
            cratesConfig.set(path + ".rewards", rewardList);
        }

        try {
            cratesConfig.save(cratesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<Crate> getCrates() {
        return crates;
    }

    public void addCrates(Crate crate) {
        crates.add(crate);
        saveCrates();
    }


    public void refreshAll(){
        saveCrates();
    }

    public void reloadAll(){

        cratesConfig = YamlConfiguration.loadConfiguration(cratesFile);
        loadCrates();
    }

    public Crate getCrateByName(String name){
        return crates.stream().filter(c->c.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
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
