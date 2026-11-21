package fun.zorvex.powers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class ZorvexPowers extends JavaPlugin {
    public static final String POWER_CHANNEL = "zorvexpowers:power";
    private PlayerDataStore data;
    private PowerMenu menu;
    private AbilityService abilities;\n    private ItemPowerSystem itemPowers;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        data = new PlayerDataStore(this);
        menu = new PowerMenu(this);
        abilities = new AbilityService(this);\n        itemPowers = new ItemPowerSystem(this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, POWER_CHANNEL);

        PowerCommand command = new PowerCommand(this);
        if (getCommand("powers") == null || getCommand("zorvexpowers") == null) {
            getLogger().severe("Commands are missing from plugin.yml; disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getCommand("powers").setExecutor(command);
        getCommand("zorvexpowers").setExecutor(command);

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new PowerListener(this), this);\n        getServer().getPluginManager().registerEvents(itemPowers, this);
        getLogger().info("ZorvexPowers enabled. Eight active abilities registered.");
    }

    @Override
    public void onDisable() {
        if (data != null) data.saveAll();
        getServer().getMessenger().unregisterOutgoingPluginChannel(this, POWER_CHANNEL);
        getLogger().info("ZorvexPowers disabled.");
    }

    public void syncPower(Player player) {
        PowerType power = data.getPower(player.getUniqueId());
        String value = power == null ? "NONE" : power.name();
        player.sendPluginMessage(this, POWER_CHANNEL, value.getBytes(StandardCharsets.UTF_8));
    }

    public void syncPower(UUID playerId) {
        Player player = getServer().getPlayer(playerId);
        if (player != null) syncPower(player);
    }

    public PlayerDataStore data() { return data; }
    public PowerMenu menu() { return menu; }
    public AbilityService abilities() { return abilities; }\n    public ItemPowerSystem itemPowers() { return itemPowers; }

    public String msg(String key) {
        String prefix = getConfig().getString("messages.prefix", "&8[&5Zorvex Powers&8] &r");
        String message = getConfig().getString("messages." + key, "&cMissing message: " + key);
        return ChatColor.translateAlternateColorCodes('&', prefix + message);
    }
}
