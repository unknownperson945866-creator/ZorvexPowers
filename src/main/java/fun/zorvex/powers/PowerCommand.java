package fun.zorvex.powers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class PowerCommand implements CommandExecutor {
    private final ZorvexPowers plugin;

    public PowerCommand(ZorvexPowers plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("powers")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("This command can only be used by a player.");
                return true;
            }
            if (!player.hasPermission("zorvexpowers.use")) {
                player.sendMessage(plugin.msg("no-permission"));
                return true;
            }
            if (args.length > 0 && args[0].equalsIgnoreCase("cast")) {
                if (!plugin.itemPowers().activate(player)) player.sendMessage("§cHold a Zorvex power item in your main hand, then use /powers cast or right-click.");
            } else {
                plugin.itemPowers().showHeldInfo(player);
            }
            return true;
        }

        if (!sender.hasPermission("zorvexpowers.admin")) {
            sender.sendMessage(plugin.msg("no-permission"));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "ZorvexPowers admin: /zp <reload|give|remove|list>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reloadConfig();
                sender.sendMessage(plugin.msg("reloaded"));
            }
            case "list" -> {
                for (PowerType power : PowerType.values()) {
                    sender.sendMessage(power.color() + power.name() + ChatColor.GRAY + " - " + power.description());
                }
            }
            case "give" -> {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /zp give <player> <power>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                PowerType power = PowerType.fromString(args[2]);
                if (target == null || power == null) {
                    sender.sendMessage(ChatColor.RED + "Player not online or unknown power.");
                    return true;
                }
                plugin.itemPowers().give(target, power);
                sender.sendMessage(ChatColor.GREEN + "Gave a " + power.name() + " power item to " + target.getName());
            }
            case "remove" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /zp remove <player>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player is not online.");
                    return true;
                }
                if (plugin.itemPowers().getPower(target.getInventory().getItemInMainHand()) == null) {
                    sender.sendMessage(ChatColor.RED + "That player must hold the power item in their main hand.");
                    return true;
                }
                target.getInventory().setItemInMainHand(new org.bukkit.inventory.ItemStack(Material.AIR));
                target.sendMessage("§cYour held power item was removed by an admin.");
                sender.sendMessage(ChatColor.GREEN + "Removed the held power item from " + target.getName());
            }
            default -> sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /zp.");
        }
        return true;
    }
}
