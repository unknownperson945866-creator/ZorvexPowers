package fun.zorvex.powers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class PowerMenu {
    private final ZorvexPowers plugin;

    public PowerMenu(ZorvexPowers plugin) { this.plugin = plugin; }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, "§5§lZorvex Powers");
        PowerType current = plugin.data().getPower(player.getUniqueId());
        inventory.setItem(4, item(Material.NETHER_STAR, "§dYour Power",
                List.of(current == null ? "§7No power assigned" :
                                current.color() + current.displayName(),
                        "§8Your assigned power is shown here.")));

        Material[] icons = {Material.BLAZE_POWDER, Material.PACKED_ICE,
                Material.HEART_OF_THE_SEA, Material.GLOWSTONE_DUST,
                Material.BLUE_ICE, Material.SLIME_BALL,
                Material.AMETHYST_SHARD, Material.IRON_BARS};
        PowerType[] powers = PowerType.values();
        for (int i = 0; i < powers.length; i++) {
            PowerType power = powers[i];
            List<String> lore = new ArrayList<>();
            lore.add("§7" + power.description());
            lore.add(current == power ? "§aCurrently assigned" : "§8Admin-assigned power");
            inventory.setItem(10 + i, item(icons[i], power.color() + power.displayName(), lore));
        }

        inventory.setItem(22, item(Material.NETHER_STAR, "§d§lCast Ability",
                List.of("§7Use your assigned power ability.",
                        "§eClick to cast", "§8Cooldown: " +
                                plugin.getConfig().getInt("settings.cooldown-seconds", 15) + " seconds")));
        player.openInventory(inventory);
    }

    private ItemStack item(Material material, String name, List<String> lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }
}
