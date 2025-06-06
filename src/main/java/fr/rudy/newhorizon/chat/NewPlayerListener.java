package fr.rudy.newhorizon.chat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class NewPlayerListener implements Listener {

    private final WelcomeManager welcomeManager;

    public NewPlayerListener(WelcomeManager welcomeManager) {
        this.welcomeManager = welcomeManager;
    }

    @EventHandler
    public void onFirstJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            welcomeManager.setNewPlayer(player.getUniqueId());

            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.getUniqueId().equals(player.getUniqueId())) {
                    online.sendMessage("§f\uE01F§b Un nouveau joueur vient d’arriver ! Faites §d/bvn §bpour lui souhaiter la bienvenue et gagner 50 pièces !");
                }
            }
        }
    }
}