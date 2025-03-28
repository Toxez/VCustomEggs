package org.txtox8729.vcustomeggs;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.txtox8729.vcustomeggs.commands.VCustomEggsCommand;
import org.txtox8729.vcustomeggs.listeners.EggUseListener;
import org.txtox8729.vcustomeggs.utils.ConfigUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class VCustomEggs extends JavaPlugin implements TabCompleter {

    @Override
    public void onEnable() {
        ConfigUtil.init(this);
        VCustomEggsCommand command = new VCustomEggsCommand(this);
        getCommand("vcustomeggs").setExecutor(command);
        getCommand("vcustomeggs").setTabCompleter(this);
        getServer().getPluginManager().registerEvents(new EggUseListener(), this);
        getLogger().info("Плагин VCustomEggs успешно запущен!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Плагин VCustomEggs отключен!");
    }

    public void reloadPlugin() {
        reloadConfig();
        ConfigUtil.init(this);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("vcustomeggs.give")) {
                completions.add("give");
            }
            if (sender.hasPermission("vcustomeggs.reload")) {
                completions.add("reload");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give") && sender.hasPermission("vcustomeggs.give")) {
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList()));
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give") && sender.hasPermission("vcustomeggs.give")) {
            completions.addAll(ConfigUtil.getEggIds());
        } else if (args.length == 4 && args[0].equalsIgnoreCase("give") && sender.hasPermission("vcustomeggs.give")) {
            completions.addAll(Arrays.asList("1", "16", "32", "64"));
        }

        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}