/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerMoveEvent
 */
package fr.rudy.newhorizon.cinematic;

import fr.rudy.newhorizon.cinematic.CinematicManager;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class CinematicMoveListener
implements Listener {
    private final CinematicManager cinematicManager;
    private final Set<UUID> checkedPlayers = new HashSet<UUID>();

    public CinematicMoveListener(CinematicManager cinematicManager) {
        this.cinematicManager = cinematicManager;
        Bukkit.getPluginManager().registerEvents((Listener)this, cinematicManager.getPlugin());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (this.checkedPlayers.contains(uuid)) {
            return;
        }
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockY() == event.getTo().getBlockY() && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(this.cinematicManager.getPlugin(), () -> {
            if (!this.cinematicManager.hasPlayedCinematic(uuid, "first_join")) {
                this.cinematicManager.markCinematicAsPlayed(uuid, "first_join");
                Bukkit.getScheduler().runTask(this.cinematicManager.getPlugin(), () -> player.performCommand("cinema play first_join"));
            }
            this.checkedPlayers.add(uuid);
        });
    }
}

