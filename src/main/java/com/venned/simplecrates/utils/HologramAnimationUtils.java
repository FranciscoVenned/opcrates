package com.venned.simplecrates.utils;

import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.ItemReward;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.*;

public class HologramAnimationUtils {


    public static void spawnSpinningItems(Location location, List<ItemReward> items, int duration) {
        if (items.isEmpty()) return;

        List<ItemDisplay> itemDisplays = new ArrayList<>();
        List<TextDisplay> textDisplays = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            ItemDisplay itemDisplay = (ItemDisplay) location.getWorld().spawnEntity(location, EntityType.ITEM_DISPLAY);
            itemDisplay.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    new AxisAngle4f(),
                    new Vector3f(0.5f, 0.5f, 0.5f),
                    new AxisAngle4f()
            ));
            itemDisplays.add(itemDisplay);

            TextDisplay textDisplay = (TextDisplay) location.getWorld().spawnEntity(
                    location.clone().add(0, 0.5, 0), EntityType.TEXT_DISPLAY);
            textDisplay.setBillboard(org.bukkit.entity.Display.Billboard.CENTER);
            textDisplay.setSeeThrough(true);
            textDisplay.setPersistent(false);
            textDisplay.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    new AxisAngle4f(),
                    new Vector3f(0.7f, 0.7f, 0.7f),
                    new AxisAngle4f()
            ));
            textDisplays.add(textDisplay);
        }

        new BukkitRunnable() {
            double angle = 0;
            final double radius = 2.5;
            int changeItem = 40;
            float rotationAngle = 0;
            int ticksElapsed = 0; // Contador para duración en ticks
            int maxTicks = duration * 20; // Convertir segundos a ticks

            List<ItemReward> selectedItems = new ArrayList<>(items.stream().toList());

            @Override
            public void run() {
                if (ticksElapsed >= maxTicks) {
                    // Cancelar la tarea y eliminar entidades
                    cancel();
                    itemDisplays.forEach(Entity::remove);
                    textDisplays.forEach(Entity::remove);
                    return;
                }

                if (changeItem == 40) {
                    Collections.shuffle(selectedItems);
                    int maxItems = Math.min(itemDisplays.size(), selectedItems.size());

                    selectedItems = selectedItems.subList(0, maxItems);

                    for (int i = 0; i < Math.min(itemDisplays.size(), selectedItems.size()); i++) {

                        ItemReward itemReward = selectedItems.get(i);
                        ItemStack itemStack = itemReward.getItemStack();
                        ItemMeta meta = itemStack.getItemMeta();

                        String displayName = (meta != null && meta.hasDisplayName())
                                ? itemReward.getName()
                                : "§e" + itemReward.getName().replace("&", "§");

                        itemDisplays.get(i).setItemStack(itemStack);
                        textDisplays.get(i).setText(displayName);
                    }

                    changeItem = 0;
                }

                for (int i = 0; i < itemDisplays.size(); i++) {
                    double currentAngle = angle + (Math.PI * 2 / itemDisplays.size()) * i;
                    double x = Math.cos(currentAngle) * radius;
                    double z = Math.sin(currentAngle) * radius;

                    Location newLocation = location.clone().add(x, 0.5, z);
                    itemDisplays.get(i).teleport(newLocation);
                    textDisplays.get(i).teleport(newLocation.clone().add(0, 0.4, 0));

                    float radians = (float) Math.toRadians(rotationAngle);
                    itemDisplays.get(i).setTransformation(new Transformation(
                            new Vector3f(0, 0, 0),
                            new AxisAngle4f(radians, 0, 1, 0),
                            new Vector3f(0.5f, 0.5f, 0.5f),
                            new AxisAngle4f()
                    ));
                }

                angle += Math.PI / 18;
                rotationAngle += 5;
                if (rotationAngle >= 360) rotationAngle = 0;

                changeItem++;
                ticksElapsed++; // Incrementar contador de duración
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 0, 1);
    }
}
