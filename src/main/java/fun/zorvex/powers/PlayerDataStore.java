package fun.zorvex.powers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public final class PlayerDataStore {
    private final ZorvexPowers plugin;
    private final File folder;
    private final Map<UUID, PowerType> cache = new HashMap<>();

    public PlayerDataStore(ZorvexPowers plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "players");
        if (!folder.exists() && !folder.mkdirs()) {
            plugin.getLogger().warning("Could not create player data directory.");
        }
    }

    public void load(Player player) {
        File file = fileFor(player.getUniqueId());
        if (!file.exists()) {
            cache.remove(player.getUniqueId());
            return;
        }
        String raw = YamlConfiguration.loadConfiguration(file).getString("power");
        PowerType power = PowerType.fromString(raw);
        if (power == null) cache.remove(player.getUniqueId());
        else cache.put(player.getUniqueId(), power);
    }

    public PowerType getPower(UUID uuid) { return cache.get(uuid); }

    public void setPower(UUID uuid, PowerType power) {
        if (power == null) cache.remove(uuid);
        else cache.put(uuid, power);
        save(uuid);
        plugin.syncPower(uuid);
    }

    public void save(UUID uuid) {
        File file = fileFor(uuid);
        YamlConfiguration yaml = new YamlConfiguration();
        PowerType power = cache.get(uuid);
        if (power != null) yaml.set("power", power.name());
        try {
            yaml.save(file);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save data for " + uuid, ex);
        }
    }

    public void saveAll() {
        for (UUID uuid : cache.keySet().toArray(UUID[]::new)) save(uuid);
    }

    private File fileFor(UUID uuid) { return new File(folder, uuid + ".yml"); }
}
