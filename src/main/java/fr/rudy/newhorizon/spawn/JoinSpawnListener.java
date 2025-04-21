package fr.rudy.newhorizon.spawn;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JoinSpawnListener implements Listener {

    private final CoreSpawnManager spawnManager;
    private final Map<UUID, Instant> lastJoinCache = new HashMap<>();

    public JoinSpawnListener(CoreSpawnManager spawnManager) {
        this.spawnManager = spawnManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Get last join from server API
        long lastPlayed = player.getLastPlayed();
        long now = System.currentTimeMillis();

        // Ignore if the last login is recent (< 10min)
        if (now - lastPlayed < 600_000L) return;

        Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("NewHorizon"), () -> {
            Location spawn = spawnManager.getSpawn();
            if (spawn != null) {
                player.teleport(spawn);
                //player.sendMessage("§a✔ Téléporté au spawn !");
            }
        }, 20L); // 1 sec après la connexion
    }
}
