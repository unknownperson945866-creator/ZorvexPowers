package fun.zorvex.powers;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class ZorvexPowers extends JavaPlugin {
    private PlayerDataStore data;
    private PowerMenu menu;
    private AbilityService abilities;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        data = new PlayerDataStore(this);
        menu = new PowerMenu(this);
        abilities = new AbilityService(this);

        PowerCommand command = new PowerCommand(this);
        getCommand("powers").setExecutor(command);
        getCommand("zorvexpowers").setExecutor(command);

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new PowerListener(), this);
        getLogger().info("ZorvexPowers enabled. Eight active abilities registered.");
    }

    @Override
    public void onDisable() {
        if (data != null) data.saveAll();
        getLogger().info("ZorvexPowers disabled.");
    }

    public PlayerDataStore data() { return data; }
    public PowerMenu menu() { return menu; }
    public AbilityService abilities() { return abilities; }

    public String msg(String key) {
        String prefix = getConfig().getString("messages.prefix", "&8[&5Zorvex Powers&8] &r");
        String message = getConfig().getString("messages." + key, "&cMissing message: " + key);
        return ChatColor.translateAlternateColorCodes('&', prefix + message);
    }
}
