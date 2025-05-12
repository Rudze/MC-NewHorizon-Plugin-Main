package fr.rudy.newhorizon.stats;

import fr.rudy.newhorizon.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerSessionListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Main.get().getSessionStatManager().loadStats(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Main.get().getSessionStatManager().unload(event.getPlayer());
    }
}
