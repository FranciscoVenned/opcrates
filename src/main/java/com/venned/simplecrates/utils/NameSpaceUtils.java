package com.venned.simplecrates.utils;

import com.venned.simplecrates.Main;
import org.bukkit.NamespacedKey;

public class NameSpaceUtils {

    public static NamespacedKey lootBox = new NamespacedKey(Main.getPlugin(Main.class), "lootbox");
    public static NamespacedKey crate = new NamespacedKey(Main.getPlugin(Main.class), "crate");
    public static NamespacedKey rewardName = new NamespacedKey(Main.getPlugin(Main.class), "rewardname");
    public static NamespacedKey key = new NamespacedKey(Main.getPlugin(Main.class), "key");
}
