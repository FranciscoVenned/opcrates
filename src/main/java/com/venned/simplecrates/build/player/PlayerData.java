package com.venned.simplecrates.build.player;

import java.util.UUID;

public class PlayerData {

    UUID uuid;
    boolean notifiedReward;

    public PlayerData(UUID uuid, boolean notifiedReward) {
        this.uuid = uuid;
        this.notifiedReward = notifiedReward;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void setNotifiedReward(boolean notifiedReward) {
        this.notifiedReward = notifiedReward;
    }

    public boolean isNotifiedReward() {
        return notifiedReward;
    }
}
