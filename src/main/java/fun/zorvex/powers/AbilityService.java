package fun.zorvex.powers;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class AbilityService {
    private static final long COOLDOWN_MS = 15_000L;
    private final ZorvexPowers plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public AbilityService(ZorvexPowers plugin) {
        this.plugin = plugin;
    }

    public boolean cast(Player player) {
        PowerType power = plugin.data().getPower(player.getUniqueId());
        if (power == null) {
            player.sendMessage(plugin.msg("no-power"));
            return false;
        }

        long now = System.currentTimeMillis();
        long readyAt = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        if (readyAt > now) {
            long seconds = (readyAt - now + 999) / 1000;
            player.sendMessage("§cAbility cooldown: §f" + seconds + "s");
            return false;
        }

        Location location = player.getLocation();
        switch (power) {
            case AGNI -> {
                burst(player, Particle.FLAME, 45);
                player.getWorld().playSound(location, Sound.ITEM_FIRECHARGE_USE, 1f, 1f);
                affectNearby(player, 4.5, target -> {
                    target.damage(4.0, player);
                    target.setFireTicks(Math.max(target.getFireTicks(), 40));
                });
                player.sendMessage("§c✦ Agni Burst!");
            }
            case BOREAS -> {
                burst(player, Particle.SNOWFLAKE, 55);
                player.getWorld().playSound(location, Sound.BLOCK_GLASS_BREAK, 1f, 1.5f);
                affectNearby(player, 5.0, target -> {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
                    target.damage(2.0, player);
                });
                player.sendMessage("§b✦ Boreas Chill!");
            }
            case OCEANIC -> {
                burst(player, Particle.BUBBLE, 50);
                player.getWorld().playSound(location, Sound.ITEM_TRIDENT_RIPTIDE_1, 1f, 1f);
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 200, 0));
                player.sendMessage("§3✦ Oceanic Renewal!");
            }
            case EOS -> {
                burst(player, Particle.END_ROD, 60);
                player.getWorld().playSound(location, Sound.BLOCK_BEACON_POWER_SELECT, 1f, 1.3f);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 120, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 0));
                player.sendMessage("§e✦ Eos Radiance!");
            }
            case YMIR -> {
                burst(player, Particle.SNOWFLAKE, 70);
                player.getWorld().playSound(location, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 0.7f);
                affectNearby(player, 5.0, target -> {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 2));
                    target.damage(3.0, player);
                });
                player.sendMessage("§9✦ Ymir's Grasp!");
            }
            case CORROSIVE -> {
                burst(player, Particle.SPORE_BLOSSOM_AIR, 55);
                player.getWorld().playSound(location, Sound.BLOCK_BREWING_STAND_BREW, 1f, 0.8f);
                affectNearby(player, 4.5, target ->
                        target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0)));
                player.sendMessage("§2✦ Corrosive Cloud!");
            }
            case ASTRAEUS -> {
                burst(player, Particle.PORTAL, 80);
                player.getWorld().playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.5f);
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 80, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 100, 1));
                player.sendMessage("§d✦ Astraeus Shift!");
            }
            case CHAINED -> {
                burst(player, Particle.SOUL, 55);
                player.getWorld().playSound(location, Sound.BLOCK_CHAIN_HIT, 1f, 0.7f);
                affectNearby(player, 5.0, target -> {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 1));
                    target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 0));
                });
                player.sendMessage("§8✦ Chained Bind!");
            }
        }

        cooldowns.put(player.getUniqueId(), now + COOLDOWN_MS);
        return true;
    }

    public void clear(UUID playerId) {
        cooldowns.remove(playerId);
    }

    private void burst(Player player, Particle particle, int count) {
        player.getWorld().spawnParticle(particle, player.getLocation().add(0, 1, 0),
                count, 1.5, 1.0, 1.5, 0.08);
    }

    private void affectNearby(Player caster, double radius, java.util.function.Consumer<LivingEntity> action) {
        for (var entity : caster.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity target && target != caster && !target.isDead()) {
                action.accept(target);
            }
        }
    }
}
