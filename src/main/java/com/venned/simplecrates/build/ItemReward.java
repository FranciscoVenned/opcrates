package com.venned.simplecrates.build;

import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class ItemReward {

    ItemStack itemStack;
    double chance;
    List<String> commands;
    String name;
    boolean visible;
    List<UUID> disabledPlayers;

    public ItemReward(String name, ItemStack itemStack, double chance, List<String> commands, boolean visible, List<UUID> disabledPlayers) {
        this.name = name;
        this.itemStack = itemStack;
        this.chance = chance;
        this.commands = commands;
        this.visible = visible;
        this.disabledPlayers = disabledPlayers;
    }

    public List<UUID> getDisabledPlayers() {
        return disabledPlayers;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getName() {
        return name;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public List<String> getCommands() {
        return commands;
    }

    public double getChance() {
        return chance;
    }
}
