package fr.rudy.newhorizon.chat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Collection;

/**
 * Inspired by hyperdefined/TabCompleter:
 * filtre les commandes visibles selon les permissions du joueur.
 */
public class CommandVisibilityListener implements Listener {

    private static final Set<String> PUBLIC_CMDS;
    static {
        Set<String> tmp = new HashSet<>();
        tmp.add("wiki");
        tmp.add("discord");
        PUBLIC_CMDS = Collections.unmodifiableSet(tmp);
    }

    @EventHandler
    public void onPlayerCommandSend(PlayerCommandSendEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("newhorizon.admin")) {
            return;
        }

        // CORRECTION : utiliser Collection ici
        Collection<String> visible = event.getCommands();
        visible.retainAll(PUBLIC_CMDS);
    }
}
