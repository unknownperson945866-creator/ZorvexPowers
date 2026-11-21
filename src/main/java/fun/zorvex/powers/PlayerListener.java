package fun.zorvex.powers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerListener implements Listener {
    private final ZorvexPowers plugin;

    public PlayerListener(ZorvexPowers plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.data().load(event.getPlayer());
        plugin.getServer().getScheduler().runTaskLater(plugin,
                () -> plugin.syncPower(event.getPlayer()), 20L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.data().save(event.getPlayer().getUniqueId());
        plugin.abilities().clear(event.getPlayer().getUniqueId());
    }
}
