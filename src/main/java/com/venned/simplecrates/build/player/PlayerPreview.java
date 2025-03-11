package com.venned.simplecrates.build.player;

import com.venned.simplecrates.build.ItemReward;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.UUID;

public class PlayerPreview {

    Inventory inventory;
    List<ItemReward> rewardList;
    UUID uuid;
    String title;

    public PlayerPreview(Inventory inventory, List<ItemReward> rewardList, UUID uuid, String title) {
        this.inventory = inventory;
        this.rewardList = rewardList;
        this.uuid = uuid;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public UUID getUUID() {
        return uuid;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public List<ItemReward> getRewardList() {
        return rewardList;
    }
}
