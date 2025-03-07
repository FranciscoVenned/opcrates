package com.venned.simplecrates.commands.crates;

import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.Crate;
import com.venned.simplecrates.build.CrateBlock;
import com.venned.simplecrates.build.ItemReward;
import com.venned.simplecrates.build.LootBox;
import com.venned.simplecrates.gui.edit.EditChances;
import com.venned.simplecrates.manager.CrateBlockManager;
import com.venned.simplecrates.manager.CrateManager;
import com.venned.simplecrates.utils.NameSpaceUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CrateCommand implements CommandExecutor {


    CrateManager manager;
    CrateBlockManager crateBlockManager;
    EditChances editChances;

    public CrateCommand(CrateManager manager, EditChances editChances, CrateBlockManager crateBlockManager) {
        this.manager = manager;
        this.editChances = editChances;
        this.crateBlockManager = crateBlockManager;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(sender instanceof Player player) {
            if(args.length == 0) {
                List<String> info = Arrays.asList("&c&lInfo Crate",
                        " ",
                        "&d&lSub Commands: ",
                        " ",
                        "&c- &7create <name> <display_name> -> create a lootbox  ",
                        "&c- &7addreward <name> <name_reward> -> add a new reward ",
                        "&c- &7give <name> -> You will get a lootbox in your inventory",
                        "&c- &7givekey <name> -> You will get a key in your inventory",
                        "&c- &7setkey <name> -> You set key in your item main hand",
                        "&c- &7edit <name> -> You can edit the lootbox",
                        " ");

                for(String i : info){
                    i = ChatColor.translateAlternateColorCodes('&', i);
                    player.sendMessage(i);
                }

                return true;
            }

            switch (args[0]) {

                case "create" -> {
                    if(args.length < 3) {
                        player.sendMessage(Main.getMessage("create_crate_name", Map.of()));
                        return true;
                    }
                    String name = args[1];
                    String displayName = String.join(" ", Arrays.copyOfRange(args, 2, args.length)); // Unir todo el displayName

                    if(manager.getCrates().stream().anyMatch(m->m.getName().equalsIgnoreCase(name))){
                        player.sendMessage(Main.getMessage("already_exist_crate", Map.of()));
                        return true;
                    }
                    Crate crate = new Crate(name, new ArrayList<>(), displayName);
                    manager.addCrates(crate);
                    player.playSound(player, Sound.ENTITY_VILLAGER_YES, 1, 1);
                    player.sendMessage(Main.getMessage("crate_success", Map.of("name", name)));
                    break;
                }

                case "setkey" -> {
                    if (args.length < 2) {
                        player.sendMessage(Main.getMessage("no_crate_name_key", Map.of()));
                        return true;
                    }

                    String name = args[1];
                    Crate crate = manager.getCrates().stream()
                            .filter(n -> n.getName().equalsIgnoreCase(name))
                            .findFirst().orElse(null);

                    if (crate == null) {
                        player.sendMessage(Main.getMessage("crate_no_exist", Map.of()));
                        return true;
                    }

                    if(player.getInventory().getItemInMainHand().getType() == Material.AIR){
                        player.sendMessage(Main.getMessage("crate_reward_no_hand", Map.of()));
                        return true;
                    }

                    ItemStack itemStack = player.getInventory().getItemInMainHand().clone();
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.getPersistentDataContainer().set(NameSpaceUtils.key, PersistentDataType.STRING, crate.getName());
                    itemStack.setItemMeta(itemMeta);
                    itemStack.setAmount(1);
                    crate.setItemKey(itemStack);
                    player.sendMessage(Main.getMessage("crate_set_key", Map.of("crate", crate.getName())));
                    manager.refreshAll();

                    return true;
                }


                case "set" -> {
                    if (args.length < 2) {
                        player.sendMessage("§c§l(!) §cSpecify the name of the Crate");
                        return true;
                    }

                    String name = args[1];
                    Crate crate = manager.getCrates().stream()
                            .filter(n -> n.getName().equalsIgnoreCase(name))
                            .findFirst().orElse(null);

                    if (crate == null) {
                        player.sendMessage("§c§l(!) §cThere is no such Crate");
                        return true;
                    }

                    Block targetBlock = player.getTargetBlockExact(5);

                    if (targetBlock == null || targetBlock.getType().isAir()) {
                        player.sendMessage("§c§l(!) §cYou must be looking at a solid block!");
                        return true;
                    }


                    player.sendMessage(targetBlock.getType().toString());

                    Location location = targetBlock.getLocation();

                    if(crateBlockManager.getCrateBlocks().stream().anyMatch(c->c.getLocation().equals(location))){
                        player.sendMessage("§c§l(!) §cAlready exist hologram in Position");
                        return true;
                    }

                    // Crear y agregar el CrateBlock
                    CrateBlock crateBlock = new CrateBlock(location, crate);
                    crateBlockManager.getCrateBlocks().add(crateBlock);

                    // Mensaje de confirmación
                    player.sendMessage("§aCrate §e" + crate.getName() + " §ahas been set at " +
                            "§7X: " + location.getBlockX() +
                            " §7Y: " + location.getBlockY() +
                            " §7Z: " + location.getBlockZ());

                    return true;
                }

                case "reload" -> {
                    if(player.isOp()){
                        player.sendMessage("§c§l(!)  §cReload Success");
                        manager.reloadAll();
                        crateBlockManager.reloadAll();
                    }
                }




                case "addreward" -> {
                    if(args.length < 3) {
                        player.sendMessage("§c§l(!) §cSpecify the name and reward name");
                        return true;
                    }
                    String name = args[1];
                    Crate crate= manager.getCrates().stream()
                            .filter(n->n.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
                    if(crate == null) {
                        player.sendMessage("§c§l(!) §cThere is no such Crate");
                        return true;
                    }

                    String reward = String.join(" ", Arrays.copyOfRange(args, 2, args.length)); // Unir todo el displayName


                    if(crate.getRewards().stream().anyMatch(r->r.getName().equalsIgnoreCase(reward))) {
                        player.sendMessage("§c§l(!) §cAlready exist reward");
                        return true;
                    }

                    ItemReward itemReward = new ItemReward(reward, player.getInventory().getItemInMainHand().clone(), 10, new ArrayList<>(), true, new ArrayList<>());
                    crate.addReward(itemReward);
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BELL, 1, 1);
                    player.sendMessage("§c§l(!) §dCrate " + name + " item added successfully");
                    manager.refreshAll();
                    break;
                }

                case "edit" -> {
                    if(args.length < 2) {
                        player.sendMessage("§c§l(!) §cSpecifies the name of the Crate");
                        return true;
                    }
                    String name = args[1];
                    Crate crate = manager.getCrates().stream()
                            .filter(n->n.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
                    if(crate== null) {
                        player.sendMessage("§c§l(!) §cThere is no such Crate");
                        return true;
                    }
                    editChances.openInventory(player, crate);
                    break;
                }

                case "give" -> {
                    if(args.length < 2) {
                        player.sendMessage("§c§l(!) §cSpecify the name");
                        return true;
                    }
                    String name = args[1];
                    Crate lootBox = manager.getCrates().stream()
                            .filter(n->n.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
                    if(lootBox == null) {
                        player.sendMessage("§c§l(!) §cThere is no such Crate");
                        return true;
                    }
                    player.getInventory().addItem(lootBox.getItem());
                    player.sendMessage("§c§l(!) §dCrate " + name + " item was given to you");
                    break;
                }

                case "givekey" -> {
                    if(args.length < 2) {
                        player.sendMessage("§c§l(!) §cSpecify the name");
                        return true;
                    }
                    String name = args[1];
                    Crate lootBox = manager.getCrates().stream()
                            .filter(n->n.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
                    if(lootBox == null) {
                        player.sendMessage("§c§l(!) §cThere is no such Crate");
                        return true;
                    }
                    player.getInventory().addItem(lootBox.getItemKey());
                    player.sendMessage("§c§l(!) §dCrate Key " + name + " item was given to you");
                    break;
                }
            }

        }

        return false;
    }
}
