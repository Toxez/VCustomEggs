package org.txtox8729.vcustomeggs.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.txtox8729.vcustomeggs.utils.ConfigUtil;
import org.txtox8729.vcustomeggs.utils.EggUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EggUseListener implements Listener {

    @EventHandler
    public void onEggUse(PlayerInteractEvent event) {
        if (!event.hasItem() || event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) return;

        ItemStack item = event.getItem();
        if (!EggUtils.isCustomEgg(item)) return;

        String eggId = EggUtils.getEggId(item);
        ConfigUtil.EggConfig eggConfig = ConfigUtil.getEggConfig(eggId);
        if (eggConfig == null) return;

        Random random = new Random();
        EntityType selectedEntityType;

        List<ConfigUtil.MobChance> hundredPercentMobs = new ArrayList<>();
        for (ConfigUtil.MobChance mobChance : eggConfig.mobs) {
            if (mobChance.chance == 100) {
                hundredPercentMobs.add(mobChance);
            }
        }

        if (!hundredPercentMobs.isEmpty()) {
            selectedEntityType = hundredPercentMobs.get(0).entityType;
        } else {
            double totalWeight = 0;
            for (ConfigUtil.MobChance mobChance : eggConfig.mobs) {
                totalWeight += mobChance.chance;
            }

            double roll = random.nextDouble() * totalWeight;
            double cumulativeWeight = 0;
            selectedEntityType = null;
            for (ConfigUtil.MobChance mobChance : eggConfig.mobs) {
                cumulativeWeight += mobChance.chance;
                if (roll <= cumulativeWeight) {
                    selectedEntityType = mobChance.entityType;
                    break;
                }
            }

            if (selectedEntityType == null) {
                selectedEntityType = eggConfig.mobs.get(eggConfig.mobs.size() - 1).entityType;
            }
        }

        Location spawnLocation = event.getClickedBlock().getLocation().add(0.5, 0, 0.5);
        BlockFace face = event.getBlockFace();

        if (face == BlockFace.UP) {
            spawnLocation.add(0, 1, 0);
        } else if (face == BlockFace.DOWN) {
            spawnLocation.add(0, -1, 0);
        } else {
            Vector direction = face.getDirection();
            spawnLocation.add(direction.getX(), 0, direction.getZ());
        }

        if (event.getClickedBlock().getType() == Material.SPAWNER) {
            event.setCancelled(true);

            if (selectedEntityType != null) {
                CreatureSpawner spawner = (CreatureSpawner) event.getClickedBlock().getState();
                spawner.setSpawnedType(selectedEntityType);
                spawner.update();
            }

            item.setAmount(item.getAmount() - 1);
            return;
        }

        event.setCancelled(true);

        if (selectedEntityType != null) {
            Entity entity = event.getPlayer().getWorld().spawnEntity(spawnLocation, selectedEntityType);

            if (entity instanceof Zombie) {
                ((Zombie) entity).setBaby(false);
            } else if (entity instanceof Ageable) {
                ((Ageable) entity).setAdult();
            }

            entity.setCustomName(null);
            entity.setCustomNameVisible(false);

            item.setAmount(item.getAmount() - 1);
        }
    }
}