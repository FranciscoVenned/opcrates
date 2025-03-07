package com.venned.simplecrates.listeners;

import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.CrateBlock;
import com.venned.simplecrates.gui.preview.PreviewRewards;
import com.venned.simplecrates.manager.CrateBlockManager;
import com.venned.simplecrates.utils.NameSpaceUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlayerCrateListener implements Listener {


    CrateBlockManager crateBlockManager;
    PreviewRewards previewRewards;

    static public Set<Player> opening = new HashSet<Player>();

    public PlayerCrateListener(CrateBlockManager crateBlockManager, PreviewRewards previewRewards) {
        this.crateBlockManager = crateBlockManager;
        this.previewRewards = previewRewards;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if(event.getClickedBlock() == null) return;
        Location location = event.getClickedBlock().getLocation();
        if(crateBlockManager.getCrateBlocks().stream().noneMatch(crateBlock -> crateBlock.getLocation().equals(location))) {
            return;
        }
        CrateBlock crateBlock = crateBlockManager.getCrateBlocks().stream().filter(c->c.getLocation().equals(location))
                .findFirst().orElse(null);

        if(crateBlock != null) {
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                event.setCancelled(true);
                previewRewards.onPreview(event.getPlayer(), crateBlock.getCrate().getRewards());
            } else if (event.getHand() == EquipmentSlot.HAND) {
                if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    event.setCancelled(true);
                    ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    if(itemMeta != null) {
                        if (itemMeta.getPersistentDataContainer().has(NameSpaceUtils.key)) {
                            String key = itemMeta.getPersistentDataContainer().get(NameSpaceUtils.key, PersistentDataType.STRING);
                            if(key.equalsIgnoreCase(crateBlock.getCrate().getName())){
                                crateBlock.getCrate().openCrate(event.getPlayer());

                                itemStack.setAmount(itemStack.getAmount() - 1);

                                opening.add(event.getPlayer());
                                return;
                            } else {
                                event.getPlayer().sendMessage(Main.getMessage("no_key_crate", Map.of("crate", crateBlock.getCrate().getDisplayName())));
                                return;
                            }
                        }
                    }
                    event.getPlayer().sendMessage(Main.getMessage("no_key_crate", Map.of("crate", crateBlock.getCrate().getDisplayName())));
                }
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event){
        if(event.getClickedInventory() == null) return;
        if(opening.contains((Player) event.getWhoClicked())) event.setCancelled(true);
    }
}
