package com.venned.simplecrates.build;

import com.venned.simplecrates.gui.opening.CreateOpening;
import com.venned.simplecrates.interfaces.Opening;
import com.venned.simplecrates.utils.NameSpaceUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class Crate implements Opening {

    String name;
    String displayName;
    List<ItemReward> rewards;
    ItemStack item;
    int max_reward;
    List<String> loreS;
    List<String> hologramText;
    ItemStack itemKey;

    public Crate(String name, List<ItemReward> rewards, String displayName, int max_reward, List<String> lore, List<String> hologramText, ItemStack itemKeyG) {
        this.name = name;
        this.rewards = rewards;
        this.displayName = displayName;
        this.max_reward = max_reward;
        this.loreS = lore;
        this.hologramText = hologramText;

        ItemStack itemStack = new ItemStack(Material.CHEST);
        ItemMeta itemMeta = itemStack.getItemMeta();

        List<String> newLore = new ArrayList<>();
        for(String s : lore){
            s = ChatColor.translateAlternateColorCodes('&', s);
            newLore.add(s);
        }
        itemMeta.setLore(newLore);

        if(itemKeyG.getItemMeta() != null){
            if(!itemKeyG.getItemMeta().getPersistentDataContainer().has(NameSpaceUtils.key)){
                ItemMeta itemMeta2 = itemKeyG.getItemMeta();
                itemMeta2.getPersistentDataContainer().set(NameSpaceUtils.key, PersistentDataType.STRING, name);
                itemKeyG.setItemMeta(itemMeta2);
            }
        }
        itemKey = itemKeyG;


        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        itemMeta.getPersistentDataContainer().set(NameSpaceUtils.crate, PersistentDataType.STRING, name);
        itemStack.setItemMeta(itemMeta);
        this.item = itemStack;
    }

    public Crate(String name, List<ItemReward> rewards, String displayName) {
        this.name = name;
        this.rewards = rewards;
        this.displayName = displayName;
        this.max_reward = 3;

        ItemStack itemKey = new ItemStack(Material.PAPER);
        ItemMeta itemMetaKey = itemKey.getItemMeta();
        itemMetaKey.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName) + " Key");
        itemKey.setItemMeta(itemMetaKey);

        this.itemKey = itemKey;


        ItemStack itemStack = new ItemStack(Material.SHULKER_BOX);
        ItemMeta itemMeta = itemStack.getItemMeta();

        List<String> loreTest = Arrays.asList( "%name% Crate" , "&aSet the lore in your config and reload the Crates", "&aUse the reload command" , "/crate reload");
        List<String> loreA = new ArrayList<>();
        for(String lore : loreTest){
            lore = ChatColor.translateAlternateColorCodes('&', lore);
            loreA.add(lore);
        }
        itemMeta.setLore(loreA);

        this.loreS = loreTest;
        this.hologramText = loreTest;
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        itemMeta.getPersistentDataContainer().set(NameSpaceUtils.crate, PersistentDataType.STRING, name);
        itemStack.setItemMeta(itemMeta);
        this.item = itemStack;
    }

    public void openCrate(Player player){
        CreateOpening.open(player, this);
    }

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


    public boolean isCrate(ItemStack itemStack) {
        if(itemStack != null){
            if(itemStack.getItemMeta() != null){
                if(itemStack.getItemMeta().getPersistentDataContainer().has(NameSpaceUtils.crate, PersistentDataType.STRING)){
                    String lootBox = itemStack.getItemMeta().getPersistentDataContainer().get(NameSpaceUtils.crate, PersistentDataType.STRING);
                    return lootBox.equalsIgnoreCase(name);
                }
            }
        }
        return false;
    }

    public void setItemKey(ItemStack itemKey) {
        this.itemKey = itemKey;
    }

    public ItemStack getItemKey() {
        return itemKey;
    }

    public List<String> getHologramText() {
        return hologramText;
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
