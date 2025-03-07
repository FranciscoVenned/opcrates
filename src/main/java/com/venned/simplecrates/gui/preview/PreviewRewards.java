package com.venned.simplecrates.gui.preview;

import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.ItemReward;
import com.venned.simplecrates.build.player.PlayerPreview;
import com.venned.simplecrates.utils.NameSpaceUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class PreviewRewards implements Listener {

    Set<PlayerPreview> previewMenu;

    public PreviewRewards(Plugin plugin) {
        this.previewMenu = new HashSet<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void onPreview(Player player, List<ItemReward> rewards) {

        Inventory inventory = Bukkit.createInventory(null, 54, "Preview Rewards");

        FileConfiguration config = Main.getInstance().getConfig();

        List<ItemReward> visibleRewards = rewards.stream()
                .filter(ItemReward::isVisible)
                .toList();

        List<ItemReward> modified = new ArrayList<>();

        for(int i = 0; i < visibleRewards.size(); i++){
            ItemReward itemReward = visibleRewards.get(i);

            ItemStack itemStack = itemReward.getItemStack().clone();ItemMeta itemMeta = itemStack.getItemMeta();

            List<String> lore = new ArrayList<>(config.getStringList("lore-preview"));



            if (itemMeta.getLore() == null) {

                lore.add(" ");
                for (int z = 0; z < lore.size(); z++) {
                    lore.set(z, lore.get(z)
                            .replace("{chance}", String.valueOf(itemReward.getChance()))
                            .replace("{status}", itemReward.getDisabledPlayers().contains(player.getUniqueId()) ? "Disabled" : "Enabled")
                            .replace("&", "§")
                    );
                }

                itemMeta.getPersistentDataContainer().set(NameSpaceUtils.rewardName, PersistentDataType.STRING, itemReward.getName());

                itemMeta.setLore(lore);
                } else if (itemMeta.getLore() != null || !itemMeta.getLore().isEmpty()) {
                    List<String> loreGet = itemMeta.getLore();
                    lore.add(" ");
                for (int z = 0; z < lore.size(); z++) {
                    lore.set(z, lore.get(z)
                            .replace("{chance}", String.valueOf(itemReward.getChance()))
                            .replace("{status}", itemReward.getDisabledPlayers().contains(player.getUniqueId()) ? "Disabled" : "Enabled")
                            .replace("&", "§")
                    );
                }

                    itemMeta.getPersistentDataContainer().set(NameSpaceUtils.rewardName, PersistentDataType.STRING, itemReward.getName());
                    itemMeta.setLore(lore);
                }
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

            itemMeta.addAttributeModifier(Attribute.LUCK, new AttributeModifier(
                    "dummy",
                    0,
                    AttributeModifier.Operation.ADD_NUMBER
            ));

                itemStack.setItemMeta(itemMeta);
                inventory.setItem(i, itemStack);

                modified.add(itemReward);

        }
        player.openInventory(inventory);

        previewMenu.removeIf(p->p.getUUID().equals(player.getUniqueId()));
        previewMenu.add(new PlayerPreview(inventory, rewards, player.getUniqueId()));

    }

    @EventHandler
    public void onClick(InventoryClickEvent event){
        if(event.getClickedInventory() == null) return;

        PlayerPreview previewRewards = previewMenu.stream().filter(c->c.getUUID().equals(event.getWhoClicked().getUniqueId())).findFirst().orElse(null);
        if(previewRewards == null) return;
        if(previewRewards.getInventory().equals(event.getClickedInventory())){
            event.setCancelled(true);

            ItemStack item = event.getCurrentItem();
            if(item == null) return;
            if(item.getItemMeta() != null){
                if(item.getItemMeta().getPersistentDataContainer().has(NameSpaceUtils.rewardName)){
                    String rewardName = item.getItemMeta().getPersistentDataContainer().get(NameSpaceUtils.rewardName, PersistentDataType.STRING);
                    for(ItemReward itemReward : previewRewards.getRewardList()){
                        if(itemReward.getName().equalsIgnoreCase(rewardName)){
                            if(itemReward.getDisabledPlayers().contains(event.getWhoClicked().getUniqueId())){
                                itemReward.getDisabledPlayers().remove(event.getWhoClicked().getUniqueId());
                            } else {
                                itemReward.getDisabledPlayers().add(event.getWhoClicked().getUniqueId());
                            }
                            Player player = (Player) event.getWhoClicked();
                            onPreview(player, previewRewards.getRewardList());
                        }
                    }
                }
            }

        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event){
        previewMenu.remove((Player) event.getPlayer());
    }


}
