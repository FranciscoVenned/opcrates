package com.venned.simplecrates.build;

import com.venned.simplecrates.Main;
import com.venned.simplecrates.interfaces.Opening;
import com.venned.simplecrates.utils.HologramAnimationUtils;
import com.venned.simplecrates.utils.HologramTextUtils;
import com.venned.simplecrates.utils.NameSpaceUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class LootBox implements Opening {

    String name;
    String displayName;
    List<ItemReward> rewards;
    ItemStack item;
    int max_reward;
    List<String> loreS;

    public LootBox(String name, List<ItemReward> rewards, String displayName, int max_reward, List<String> lore) {
        this.name = name;
        this.rewards = rewards;
        this.displayName = displayName;
        this.max_reward = max_reward;
        this.loreS = lore;

        ItemStack itemStack = new ItemStack(Material.SHULKER_BOX);
        ItemMeta itemMeta = itemStack.getItemMeta();

        List<String> newLore = new ArrayList<>();
        for(String s : lore){
            s = ChatColor.translateAlternateColorCodes('&', s);
            newLore.add(s);
        }
        itemMeta.setLore(newLore);

        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        itemMeta.getPersistentDataContainer().set(NameSpaceUtils.lootBox, PersistentDataType.STRING, name);
        itemStack.setItemMeta(itemMeta);
        this.item = itemStack;
    }

    public LootBox(String name, List<ItemReward> rewards, String displayName) {
        this.name = name;
        this.rewards = rewards;
        this.displayName = displayName;
        this.max_reward = 3;

        ItemStack itemStack = new ItemStack(Material.SHULKER_BOX);
        ItemMeta itemMeta = itemStack.getItemMeta();

        List<String> loreTest = Arrays.asList("&aSet the lore in your config and reload the lootboxes", "&aUse the reload command" , "/lootbox reload");
        List<String> loreA = new ArrayList<>();
        for(String lore : loreTest){
            lore = ChatColor.translateAlternateColorCodes('&', lore);
            loreA.add(lore);
        }
        itemMeta.setLore(loreA);

        this.loreS = loreTest;

        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        itemMeta.getPersistentDataContainer().set(NameSpaceUtils.lootBox, PersistentDataType.STRING, name);
        itemStack.setItemMeta(itemMeta);
        this.item = itemStack;
    }

    public void open(Player player) {
        Location open = player.getLocation();

        int duration_lootbox = Main.getInstance().getConfig().getInt("duration-lootbox-opening");

        HologramAnimationUtils.spawnSpinningItems(open, this.rewards, duration_lootbox);
        List<ItemReward> rewardsCopy = new ArrayList<>(rewards);
        List<ItemReward> selectedRewards = getRandomRewards(rewardsCopy, max_reward);

        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            HologramTextUtils.textReward(player, open, selectedRewards, ChatColor.translateAlternateColorCodes('&', displayName));

            for (ItemReward itemReward : selectedRewards) {
                player.getInventory().addItem(itemReward.getItemStack().clone());
                if (!itemReward.getCommands().isEmpty()) {
                    for (String command : itemReward.getCommands()) {
                        command = command.replace("{player}", player.getName());
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    }
                }
            }
        }, duration_lootbox * 20L);
    }

    /**
     * Método para seleccionar ítems aleatoriamente basado en la probabilidad de cada uno.
     */
    private List<ItemReward> getRandomRewards(List<ItemReward> rewards, int maxRewards) {
        List<ItemReward> selected = new ArrayList<>();
        Random random = new Random();

        while (selected.size() < maxRewards && !rewards.isEmpty()) {
            double totalWeight = rewards.stream().mapToDouble(ItemReward::getChance).sum();
            double r = random.nextDouble() * totalWeight;
            double cumulative = 0.0;

            for (Iterator<ItemReward> iterator = rewards.iterator(); iterator.hasNext(); ) {
                ItemReward reward = iterator.next();
                cumulative += reward.getChance();

                if (r <= cumulative) {
                    selected.add(reward);
                    iterator.remove(); // Evita seleccionar el mismo ítem más de una vez
                    break;
                }
            }
        }

        return selected;
    }

    public boolean isLootBox(ItemStack itemStack) {
        if(itemStack != null){
            if(itemStack.getItemMeta() != null){
                if(itemStack.getItemMeta().getPersistentDataContainer().has(NameSpaceUtils.lootBox, PersistentDataType.STRING)){
                    String lootBox = itemStack.getItemMeta().getPersistentDataContainer().get(NameSpaceUtils.lootBox, PersistentDataType.STRING);
                    return lootBox.equalsIgnoreCase(name);
                }
            }
        }
        return false;
    }

    public List<String> getLoreS() {
        return loreS;
    }

    public int getMax_reward() {
        return max_reward;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ItemStack getItem() {
        return item;
    }

    public String getName() {
        return name;
    }

    public List<ItemReward> getRewards() {
        return rewards;
    }

    public void addReward(ItemReward item) {
        rewards.add(item);
    }

}
