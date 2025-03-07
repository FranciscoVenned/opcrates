package com.venned.simplecrates.build.player;

import com.venned.simplecrates.build.ItemReward;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.UUID;

public class PlayerPreview {

    Inventory inventory;
    List<ItemReward> rewardList;
    UUID uuid;

    public PlayerPreview(Inventory inventory, List<ItemReward> rewardList, UUID uuid) {
        this.inventory = inventory;
        this.rewardList = rewardList;
        this.uuid = uuid;
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
