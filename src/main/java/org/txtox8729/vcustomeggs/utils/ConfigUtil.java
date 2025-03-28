package org.txtox8729.vcustomeggs.utils;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.txtox8729.vcustomeggs.VCustomEggs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfigUtil {
    public static VCustomEggs plugin;
    private static Map<String, EggConfig> eggs;

    public static String noPermissionMessage;
    public static String usageCommandsMessage;
    public static String playerNotFoundMessage;
    public static String eggGivenMessage;
    public static String invalidItemMessage;
    public static String reloadSuccessMessage;
    public static String amountMustBePositiveMessage;
    public static String amountTooLargeMessage;
    public static String invalidAmountMessage;

    public static void init(VCustomEggs pluginInstance) {
        plugin = pluginInstance;
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();

        noPermissionMessage = String.join("\n", config.getStringList("messages.no-permission-message"));
        usageCommandsMessage = String.join("\n", config.getStringList("messages.usage-commands-message"));
        playerNotFoundMessage = String.join("\n", config.getStringList("messages.player-not-found-message"));
        eggGivenMessage = String.join("\n", config.getStringList("messages.egg-given-message"));
        invalidItemMessage = String.join("\n", config.getStringList("messages.invalid-item-message"));
        reloadSuccessMessage = String.join("\n", config.getStringList("messages.reload-success-message"));
        amountMustBePositiveMessage = String.join("\n", config.getStringList("messages.amount-must-be-positive-message"));
        amountTooLargeMessage = String.join("\n", config.getStringList("messages.amount-too-large-message"));
        invalidAmountMessage = String.join("\n", config.getStringList("messages.invalid-amount-message"));

        eggs = new HashMap<>();

        ConfigurationSection eggsSection = config.getConfigurationSection("eggs");
        if (eggsSection == null) {
            plugin.getLogger().warning("Секция 'eggs' не найдена в конфиге! Плагин не сможет работать без настроек яиц.");
            return;
        }

        for (String eggId : eggsSection.getKeys(false)) {
            ConfigurationSection eggSection = eggsSection.getConfigurationSection(eggId);
            if (eggSection == null) continue;

            EggConfig eggConfig = new EggConfig();

            String materialName = eggSection.getString("material", "ZOMBIE_SPAWN_EGG");
            try {
                eggConfig.material = Material.valueOf(materialName.toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Ошибка конфига: материал '" + materialName + "' для яйца '" + eggId + "' не найден! Используется ZOMBIE_SPAWN_EGG.");
                eggConfig.material = Material.ZOMBIE_SPAWN_EGG;
            }

            eggConfig.name = eggSection.getString("name", "&#B481FFКастомное Яйцо");
            eggConfig.lore = eggSection.getStringList("lore");

            List<String> mobChances = eggSection.getStringList("mobs");
            eggConfig.mobs = new ArrayList<>();

            for (String mobChance : mobChances) {
                String[] parts = mobChance.split(";");
                if (parts.length != 2) continue;

                String entityName = parts[0].toUpperCase();
                double chance;
                try {
                    chance = Double.parseDouble(parts[1]);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Ошибка: шанс '" + parts[1] + "' для моба '" + entityName + "' в яйце '" + eggId + "' не является числом!");
                    continue;
                }

                if (chance <= 0) continue;

                EntityType entityType;
                try {
                    entityType = EntityType.valueOf(entityName);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Ошибка: моб '" + entityName + "' в яйце '" + eggId + "' не найден!");
                    continue;
                }

                if (chance > 100) {
                    plugin.getLogger().warning("Предупреждение: шанс '" + chance + "' для моба '" + entityName + "' в яйце '" + eggId + "' больше 100! Устанавливается 100.");
                    chance = 100;
                }

                eggConfig.mobs.add(new MobChance(entityType, chance));
            }

            if (eggConfig.mobs.isEmpty()) {
                plugin.getLogger().warning("Ошибка: в яйце '" + eggId + "' не найдено ни одного моба!");
                continue;
            }

            int hundredPercentCount = 0;
            for (MobChance mobChance : eggConfig.mobs) {
                if (mobChance.chance == 100) {
                    hundredPercentCount++;
                }
            }
            if (hundredPercentCount > 1) {
                plugin.getLogger().warning("Ошибка: в яйце '" + eggId + "' найдено " + hundredPercentCount + " мобов с шансом 100%! В одном яйце может быть только один моб с шансом 100%.");
                continue;
            }

            eggs.put(eggId.toLowerCase(), eggConfig);
        }

        if (eggs.isEmpty()) {
            plugin.getLogger().warning("Не найдено ни одного яйца в конфиге! Плагин не сможет работать без настроек яиц.");
        }
    }

    public static EggConfig getEggConfig(String eggId) {
        return eggs.get(eggId.toLowerCase());
    }

    public static Set<String> getEggIds() {
        return eggs.keySet();
    }

    public static class EggConfig {
        public Material material;
        public String name;
        public List<String> lore;
        public List<MobChance> mobs;
    }

    public static class MobChance {
        public EntityType entityType;
        public double chance;

        public MobChance(EntityType entityType, double chance) {
            this.entityType = entityType;
            this.chance = chance;
        }

        @Override
        public String toString() {
            return entityType + " (" + chance + "%)";
        }
    }
}