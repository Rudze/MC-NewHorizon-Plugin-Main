package fr.rudy.newhorizon.itemscustom;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.utils.MessageUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class FlightPotionListener implements Listener {

    private final JavaPlugin plugin;
    private final Set<UUID> noFallDamagePlayers = new HashSet<>();
    private final Map<UUID, BukkitTask> fallTimerTasks = new HashMap<>();

    public FlightPotionListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDrink(PlayerItemConsumeEvent event) {
        if (!(event.getItem().getItemMeta() != null &&
                event.getItem().getItemMeta().getPersistentDataContainer()
                        .has(new NamespacedKey(plugin, "flight_potion"), PersistentDataType.INTEGER))) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        player.setAllowFlight(true);
        MessageUtil.sendMessage(player, Main.get().getPrefixInfo(), "Tu peux voler pendant §d10 minutes§b !");
        player.playSound(player.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 1.2f, 1.0f);

        new BukkitRunnable() {
            int ticks = 0;
            boolean warned = false;

            @Override
            public void run() {
                if (!player.isOnline() || !player.getAllowFlight()) {
                    disableFlight(player);
                    cancel();
                    return;
                }

                if (ticks >= 20 * 60 * 10) { // 10 minutes
                    disableFlight(player);
                    cancel();
                    return;
                }

                if (!warned && ticks >= 20 * 60 * 10 - 200) { // 10 sec avant fin
                    warned = true;
                    MessageUtil.sendMessage(player, Main.get().getPrefixInfo(), "Vol désactivé dans §d10 secondes§b !");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1.5f);
                }

                Location loc = player.getLocation().clone().add(0, 0.1, 0);
                player.getWorld().spawnParticle(
                        Particle.CLOUD,
                        loc,
                        8,
                        0.2, 0.05, 0.2,
                        0.01
                );

                ticks += 10;
            }
        }.runTaskTimer(plugin, 0, 10);
    }

    private void disableFlight(Player player) {
        UUID uuid = player.getUniqueId();

        player.setAllowFlight(false);
        player.setFlying(false);

        noFallDamagePlayers.add(uuid);
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                noFallDamagePlayers.remove(uuid);
                fallTimerTasks.remove(uuid);
            }
        }.runTaskLater(plugin, 20 * 10); // 10 secondes de protection

        fallTimerTasks.put(uuid, task);

        MessageUtil.sendMessage(player, Main.get().getPrefixError(), "Tu ne peux plus voler !");
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.2f, 0.8f);
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            if (noFallDamagePlayers.contains(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
}
