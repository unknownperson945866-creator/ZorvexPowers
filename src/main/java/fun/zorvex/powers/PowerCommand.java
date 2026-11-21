package fun.zorvex.powers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
            plugin.menu().open(player);
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
                plugin.data().setPower(target.getUniqueId(), power);
                target.sendMessage(plugin.msg("power-assigned").replace("%power%", power.color() + power.displayName()));
                sender.sendMessage(ChatColor.GREEN + "Assigned " + power.name() + " to " + target.getName());
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
                plugin.data().setPower(target.getUniqueId(), null);
                target.sendMessage(plugin.msg("power-removed"));
                sender.sendMessage(ChatColor.GREEN + "Removed power from " + target.getName());
            }
            default -> sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /zp.");
        }
        return true;
    }
}
