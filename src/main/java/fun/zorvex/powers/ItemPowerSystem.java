package fun.zorvex.powers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class ItemPowerSystem implements Listener {
    private static final String SPIN_TITLE = "§4§lZORVEX §cPOWER SPIN";
    private static final String INFO_TITLE = "§5§lZORVEX ITEM POWER";
    private final ZorvexPowers plugin;
    private final NamespacedKey powerKey, idKey, levelKey, progressKey, cooldownKey, activeKey;
    private final File spinFile;
    private final Set<UUID> spun = new HashSet<>();
    private final Random random = new Random();

    public ItemPowerSystem(ZorvexPowers plugin) {
        this.plugin = plugin;
        powerKey = new NamespacedKey(plugin, "power_type");
        idKey = new NamespacedKey(plugin, "power_item_id");
        levelKey = new NamespacedKey(plugin, "power_level");
        progressKey = new NamespacedKey(plugin, "power_progress");
        cooldownKey = new NamespacedKey(plugin, "power_cooldown_until");
        activeKey = new NamespacedKey(plugin, "power_active_until");
        spinFile = new File(plugin.getDataFolder(), "spin-data.yml");
        loadSpins();
        registerRecipes();
    }

    public ItemStack createItem(PowerType power) {
        ItemStack item = new ItemStack(icon(power));
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(powerKey, PersistentDataType.STRING, power.name());
        pdc.set(idKey, PersistentDataType.STRING, UUID.randomUUID().toString());
        pdc.set(levelKey, PersistentDataType.INTEGER, 1);
        pdc.set(progressKey, PersistentDataType.INTEGER, 0);
        pdc.set(cooldownKey, PersistentDataType.LONG, 0L);
        pdc.set(activeKey, PersistentDataType.LONG, 0L);
        meta.setDisplayName(power.color() + "§l" + power.displayName() + " Power");
        meta.setLore(List.of("§7A transferable power item.", "§7Level: §f1§7/3",
                "§7Progress: §f0", "§eHold in main hand and right-click.",
                "§8The power belongs to this item."));
        item.setItemMeta(meta);
        return item;
    }

    public PowerType getPower(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        String value = item.getItemMeta().getPersistentDataContainer().get(powerKey, PersistentDataType.STRING);
        return PowerType.fromString(value);
    }

    public void give(Player player, PowerType power) {
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(createItem(power));
        leftover.values().forEach(stack -> player.getWorld().dropItemNaturally(player.getLocation(), stack));
        player.sendMessage("§aYou received the §f" + power.displayName() + " §apower item. Hold it and right-click.");
    }

    public void showHeldInfo(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        PowerType power = getPower(item);
        if (power == null) {
            player.sendMessage("§cHold a Zorvex power item in your main hand.");
            return;
        }
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        int level = pdc.getOrDefault(levelKey, PersistentDataType.INTEGER, 1);
        int progress = pdc.getOrDefault(progressKey, PersistentDataType.INTEGER, 0);
        long ready = pdc.getOrDefault(cooldownKey, PersistentDataType.LONG, 0L);
        long active = pdc.getOrDefault(activeKey, PersistentDataType.LONG, 0L);
        long now = System.currentTimeMillis();
        player.sendMessage(power.color() + "§l" + power.displayName() + " §7| Level §f" + level + "/3 §7| Progress §f" + progress);
        if (active > now) player.sendMessage("§aActive for " + ((active-now+999)/1000) + "s");
        else if (ready > now) player.sendMessage("§cCooldown: " + ((ready-now+999)/1000) + "s");
        else player.sendMessage("§aReady to use!");
    }

    public boolean activate(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        PowerType power = getPower(item);
        if (power == null) return false;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        long now = System.currentTimeMillis();
        long activeUntil = pdc.getOrDefault(activeKey, PersistentDataType.LONG, 0L);
        long readyAt = pdc.getOrDefault(cooldownKey, PersistentDataType.LONG, 0L);
        if (activeUntil > now) {
            player.sendMessage("§e" + power.displayName() + " is already active for " + ((activeUntil-now+999)/1000) + "s.");
            return true;
        }
        if (readyAt > now) {
            player.sendMessage("§cPower cooldown: " + ((readyAt-now+999)/1000) + "s.");
            return true;
        }
        int level = pdc.getOrDefault(levelKey, PersistentDataType.INTEGER, 1);
        int duration = Math.max(1, plugin.getConfig().getInt("powers." + power.name() + ".duration-seconds", 60));
        int cooldown = Math.max(0, plugin.getConfig().getInt("powers." + power.name() + ".cooldown-seconds", 120));
        long ends = now + duration * 1000L;
        pdc.set(activeKey, PersistentDataType.LONG, ends);
        pdc.set(cooldownKey, PersistentDataType.LONG, ends + cooldown * 1000L);
        refreshLore(meta, power, level, pdc.getOrDefault(progressKey, PersistentDataType.INTEGER, 0));
        item.setItemMeta(meta);
        applyAbility(player, power, level, duration);
        player.sendMessage(power.color() + "✦ " + power.displayName() + " activated! §7Duration: §f" + duration + "s §7| Cooldown after: §f" + cooldown + "s");
        return true;
    }

    private void applyAbility(Player player, PowerType power, int level, int durationSeconds) {
        int ticks = durationSeconds * 20;
        int amp = Math.min(2, Math.max(0, level - 1));
        switch (power) {
            case AGNI -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, ticks, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, ticks, amp));
                player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0,1,0), 45, 1,1,1, .04);
                player.getWorld().playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1, 1);
            }
            case BOREAS -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, ticks, amp));
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, ticks, 0));
                player.getWorld().spawnParticle(Particle.SNOWFLAKE, player.getLocation().add(0,1,0), 55, 1,1,1, .04);
            }
            case OCEANIC -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, ticks, amp));
                player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, ticks, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, ticks, 0));
                player.getWorld().spawnParticle(Particle.BUBBLE, player.getLocation().add(0,1,0), 50, 1,1,1, .04);
            }
            case EOS -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, ticks, amp));
                player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, ticks, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, ticks, amp));
                player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0,1,0), 50, 1,1,1, .04);
            }
            case YMIR -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, ticks, amp));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, ticks, 0));
                player.getWorld().spawnParticle(Particle.SNOWFLAKE, player.getLocation().add(0,1,0), 60, 1,1,1, .04);
            }
            case CORROSIVE -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, ticks, amp));
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, ticks, 0));
                player.getWorld().spawnParticle(Particle.SPORE_BLOSSOM_AIR, player.getLocation().add(0,1,0), 50, 1,1,1, .02);
            }
            case ASTRAEUS -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, ticks, amp));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, ticks, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, ticks, 0));
                player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0,1,0), 60, 1,1,1, .2);
            }
            case CHAINED -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, ticks, amp));
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, ticks, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, ticks, 0));
                player.getWorld().spawnParticle(Particle.SOUL, player.getLocation().add(0,1,0), 55, 1,1,1, .03);
            }
        }
    }

    @EventHandler public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (activate(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (spun.contains(player.getUniqueId())) return;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline() || spun.contains(player.getUniqueId())) return;
            Inventory inv = Bukkit.createInventory(null, 27, SPIN_TITLE);
            ItemStack token = new ItemStack(Material.NETHER_STAR);
            ItemMeta meta = token.getItemMeta();
            meta.setDisplayName("§c§lSPIN FOR YOUR FIRST POWER");
            meta.setLore(List.of("§7Click to randomly receive one of 8 powers.", "§eThis spin can only be used once."));
            token.setItemMeta(meta);
            inv.setItem(13, token);
            player.openInventory(inv);
        }, 40L);
    }

    @EventHandler public void onSpinClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(SPIN_TITLE)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getClickedInventory() != event.getView().getTopInventory()) return;
        if (event.getSlot() != 13 || spun.contains(player.getUniqueId())) return;
        PowerType[] values = PowerType.values();
        PowerType selected = values[random.nextInt(values.length)];
        spun.add(player.getUniqueId());
        saveSpins();
        player.closeInventory();
        give(player, selected);
        player.sendTitle("§4" + selected.displayName(), "§eYour power has been chosen!", 10, 60, 15);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
    }

    @EventHandler public void onSpinDrag(InventoryDragEvent event) {
        if (event.getView().getTitle().equals(SPIN_TITLE)) event.setCancelled(true);
    }

    private void progress(Player player, PowerType required, int amount) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (getPower(item) != required || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        int level = pdc.getOrDefault(levelKey, PersistentDataType.INTEGER, 1);
        if (level >= 3) return;
        int progress = pdc.getOrDefault(progressKey, PersistentDataType.INTEGER, 0) + amount;
        int threshold = plugin.getConfig().getInt("progression." + required.name() + ".level-" + level, 10);
        if (progress >= threshold) {
            level++;
            progress = 0;
            player.sendMessage("§a✦ Your " + required.displayName() + " item reached level " + level + "!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        }
        pdc.set(levelKey, PersistentDataType.INTEGER, level);
        pdc.set(progressKey, PersistentDataType.INTEGER, progress);
        refreshLore(meta, required, level, progress);
        item.setItemMeta(meta);
    }

    @EventHandler public void onKill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) progress(killer, PowerType.AGNI, 1);
    }
    @EventHandler public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null || event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;
        if (event.getPlayer().isSprinting()) progress(event.getPlayer(), PowerType.BOREAS, 1);
        if (event.getPlayer().isSwimming()) progress(event.getPlayer(), PowerType.OCEANIC, 1);
    }
    @EventHandler public void onXp(PlayerExpChangeEvent event) { progress(event.getPlayer(), PowerType.EOS, Math.max(1, event.getAmount())); }
    @EventHandler public void onBlockBreak(BlockBreakEvent event) {
        Material type = event.getBlock().getType();
        if (type == Material.ICE || type == Material.PACKED_ICE || type == Material.BLUE_ICE || type == Material.SNOW_BLOCK)
            progress(event.getPlayer(), PowerType.YMIR, 1);
        if (type == Material.CACTUS || type == Material.MAGMA_BLOCK || type == Material.NETHER_WART_BLOCK)
            progress(event.getPlayer(), PowerType.CORROSIVE, 1);
    }
    @EventHandler public void onConsume(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() == Material.SPIDER_EYE || event.getItem().getType() == Material.FERMENTED_SPIDER_EYE)
            progress(event.getPlayer(), PowerType.CORROSIVE, 1);
    }
    @EventHandler public void onTeleport(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL)
            progress(event.getPlayer(), PowerType.ASTRAEUS, 1);
    }
    @EventHandler public void onCraft(CraftItemEvent event) {
        ItemStack crafted = event.getCurrentItem();
        if (crafted != null && getPower(crafted) != null) {
            ItemMeta craftedMeta = crafted.getItemMeta();
            craftedMeta.getPersistentDataContainer().set(idKey, PersistentDataType.STRING, UUID.randomUUID().toString());
            crafted.setItemMeta(craftedMeta);
            event.setCurrentItem(crafted);
        }
        if (event.getWhoClicked() instanceof Player player
                && event.getRecipe().getResult().getType() == Material.IRON_BARS
                && getPower(event.getRecipe().getResult()) == null) {
            progress(player, PowerType.CHAINED, 1);
        }
    }

    private void refreshLore(ItemMeta meta, PowerType power, int level, int progress) {
        meta.setDisplayName(power.color() + "§l" + power.displayName() + " Power §7[L" + level + "]");
        meta.setLore(List.of("§7A transferable power item.", "§7Level: §f" + level + "§7/3",
                "§7Progress: §f" + progress, "§eHold in main hand and right-click.",
                "§8The power, level and cooldown travel with this item."));
    }

    private Material icon(PowerType p) {
        return switch (p) {
            case AGNI -> Material.BLAZE_POWDER; case BOREAS -> Material.PACKED_ICE;
            case OCEANIC -> Material.HEART_OF_THE_SEA; case EOS -> Material.GLOWSTONE_DUST;
            case YMIR -> Material.BLUE_ICE; case CORROSIVE -> Material.SLIME_BALL;
            case ASTRAEUS -> Material.AMETHYST_SHARD; case CHAINED -> Material.IRON_BARS;
        };
    }

    private void registerRecipes() {
        if (!plugin.getConfig().getBoolean("recipes.enabled", true)) return;
        addRecipe(PowerType.AGNI, Material.BLAZE_POWDER, Material.FIRE_CHARGE, Material.MAGMA_CREAM, Material.NETHER_STAR);
        addRecipe(PowerType.BOREAS, Material.PACKED_ICE, Material.SNOWBALL, Material.BLUE_ICE, Material.PRISMARINE_SHARD);
        addRecipe(PowerType.OCEANIC, Material.HEART_OF_THE_SEA, Material.NAUTILUS_SHELL, Material.PRISMARINE_CRYSTALS, Material.WATER_BUCKET);
        addRecipe(PowerType.EOS, Material.GLOWSTONE_DUST, Material.GOLDEN_APPLE, Material.AMETHYST_SHARD, Material.END_ROD);
        addRecipe(PowerType.YMIR, Material.BLUE_ICE, Material.SNOW_BLOCK, Material.POWDER_SNOW_BUCKET, Material.DIAMOND);
        addRecipe(PowerType.CORROSIVE, Material.SPIDER_EYE, Material.FERMENTED_SPIDER_EYE, Material.SLIME_BALL, Material.CACTUS);
        addRecipe(PowerType.ASTRAEUS, Material.ENDER_PEARL, Material.AMETHYST_SHARD, Material.ECHO_SHARD, Material.NETHER_STAR);
        addRecipe(PowerType.CHAINED, Material.IRON_BARS, Material.IRON_INGOT, Material.PHANTOM_MEMBRANE, Material.LEAD);
    }

    private void addRecipe(PowerType power, Material a, Material b, Material c, Material d) {
        NamespacedKey key = new NamespacedKey(plugin, "craft_" + power.name().toLowerCase(Locale.ROOT));
        Bukkit.removeRecipe(key);
        ShapedRecipe recipe = new ShapedRecipe(key, createItem(power));
        recipe.shape("ABA", "CDC", " A ");
        recipe.setIngredient('A', a);
        recipe.setIngredient('B', b);
        recipe.setIngredient('C', c);
        recipe.setIngredient('D', d);
        Bukkit.addRecipe(recipe);
    }

    private void loadSpins() {
        if (!spinFile.exists()) return;
        for (String id : YamlConfiguration.loadConfiguration(spinFile).getStringList("completed")) {
            try { spun.add(UUID.fromString(id)); } catch (IllegalArgumentException ignored) { }
        }
    }
    private void saveSpins() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("completed", spun.stream().map(UUID::toString).toList());
        try { yaml.save(spinFile); } catch (IOException ex) { plugin.getLogger().severe("Could not save first-join spin data: " + ex.getMessage()); }
    }
}
