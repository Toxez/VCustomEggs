package org.txtox8729.vcustomeggs.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.txtox8729.vcustomeggs.VCustomEggs;
import org.txtox8729.vcustomeggs.utils.ConfigUtil;
import org.txtox8729.vcustomeggs.utils.EggUtils;
import org.txtox8729.vcustomeggs.utils.HexUtil;

public class VCustomEggsCommand implements CommandExecutor {

    private final VCustomEggs plugin;

    public VCustomEggsCommand(VCustomEggs plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("vcustomeggs.give") && !sender.hasPermission("vcustomeggs.reload")) {
            sender.sendMessage(HexUtil.translate(ConfigUtil.noPermissionMessage));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(HexUtil.translate(ConfigUtil.usageCommandsMessage));
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
            if (args.length < 3 || args.length > 4) {
                sender.sendMessage(HexUtil.translate(ConfigUtil.usageCommandsMessage));
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(HexUtil.translate(ConfigUtil.playerNotFoundMessage));
                return true;
            }

            String eggId = args[2].toLowerCase();
            ConfigUtil.EggConfig eggConfig = ConfigUtil.getEggConfig(eggId);

            if (eggConfig == null) {
                sender.sendMessage(HexUtil.translate(ConfigUtil.invalidItemMessage));
                return true;
            }

            int amount = 1;
            if (args.length == 4) {
                try {
                    amount = Integer.parseInt(args[3]);
                    if (amount <= 0) {
                        sender.sendMessage(HexUtil.translate(ConfigUtil.amountMustBePositiveMessage));
                        return true;
                    }
                    if (amount > 64) {
                        sender.sendMessage(HexUtil.translate(ConfigUtil.amountTooLargeMessage));
                        amount = 64;
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(HexUtil.translate(ConfigUtil.invalidAmountMessage));
                    return true;
                }
            }

            EggUtils.giveCustomEgg(target, eggId, amount);
            sender.sendMessage(HexUtil.translate(ConfigUtil.eggGivenMessage.replace("{player}", target.getName())));
        } else if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadPlugin();
            sender.sendMessage(HexUtil.translate(ConfigUtil.reloadSuccessMessage));
        } else {
            sender.sendMessage(HexUtil.translate(ConfigUtil.usageCommandsMessage));
        }

        return true;
    }
}