package fr.rudy.newhorizon.itemscustom;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class RocketBootsListener implements Listener {

    private final JavaPlugin plugin;
    private final String ROCKET_BOOTS_ID = "newhorizon:rocket_boots";
    private final Set<UUID> flyingPlayers = new HashSet<>();

    public RocketBootsListener(JavaPlugin plugin) {
        this.plugin = plugin;
        startFlyingMonitor();
        startFlyingCheck();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> handleBootsFly(player), 1L);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> handleBootsFly(player), 1L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> handleBootsFly(player), 20L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        flyingPlayers.remove(player.getUniqueId());
        player.setAllowFlight(false);
        player.setFlying(false);
    }

    private void handleBootsFly(Player player) {
        if (player.getGameMode() == GameMode.CREATIVE) return;

        boolean hasRocketBoots = isWearingRocketBoots(player);
        if (hasRocketBoots) {
            player.setAllowFlight(true);
        } else {
            player.setAllowFlight(false);
            if (player.isFlying()) {
                player.setFlying(false);
            }
            flyingPlayers.remove(player.getUniqueId());
        }
    }

    private void startFlyingCheck() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (isWearingRocketBoots(player) && player.isFlying()) {
                        flyingPlayers.add(player.getUniqueId());
                    } else {
                        flyingPlayers.remove(player.getUniqueId());
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    private void startFlyingMonitor() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : flyingPlayers) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline() && player.isFlying()) {
                        player.getWorld().spawnParticle(Particle.FIREWORK,
                                player.getLocation().clone().add(0, 0.2, 0),
                                10, 0.2, 0.1, 0.2, 0.01);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : new HashSet<>(flyingPlayers)) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null || !player.isOnline() || !player.isFlying()) {
                        flyingPlayers.remove(uuid);
                        continue;
                    }

                    ItemStack boots = player.getInventory().getBoots();
                    if (boots == null || !boots.hasItemMeta()) {
                        player.setFlying(false);
                        player.setAllowFlight(false);
                        flyingPlayers.remove(uuid);
                        continue;
                    }

                    CustomStack custom = CustomStack.byItemStack(boots);
                    if (custom == null || !custom.getNamespacedID().equals(ROCKET_BOOTS_ID)) {
                        player.setFlying(false);
                        player.setAllowFlight(false);
                        flyingPlayers.remove(uuid);
                        continue;
                    }

                    ItemMeta meta = boots.getItemMeta();
                    if (!(meta instanceof Damageable damageable)) {
                        player.setFlying(false);
                        player.setAllowFlight(false);
                        flyingPlayers.remove(uuid);
                        continue;
                    }

                    int maxDurability = boots.getType().getMaxDurability();
                    int currentDamage = damageable.getDamage();

                    if (currentDamage >= maxDurability) {
                        player.setFlying(false);
                        player.setAllowFlight(false);
                        flyingPlayers.remove(uuid);
                        //player.sendMessage(ChatColor.RED + "⚠ Vos Rocket Boots sont cassées !");
                        continue;
                    }

                    try {
                        damageable.setDamage(currentDamage + 1);
                        boots.setItemMeta(meta);
                        player.getInventory().setBoots(boots);
                        player.updateInventory();
                    } catch (IllegalArgumentException e) {
                        player.setFlying(false);
                        player.setAllowFlight(false);
                        flyingPlayers.remove(uuid);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 100L);
    }

    private boolean isWearingRocketBoots(Player player) {
        ItemStack boots = player.getInventory().getBoots();
        if (boots == null || !boots.hasItemMeta()) return false;

        CustomStack custom = CustomStack.byItemStack(boots);
        if (custom == null || !ROCKET_BOOTS_ID.equals(custom.getNamespacedID())) return false;

        ItemMeta meta = boots.getItemMeta();
        if (meta instanceof Damageable damageable) {
            return damageable.getDamage() < boots.getType().getMaxDurability();
        }

        return true;
    }
}
