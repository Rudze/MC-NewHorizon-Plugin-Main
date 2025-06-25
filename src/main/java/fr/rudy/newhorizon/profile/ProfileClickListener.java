package fr.rudy.newhorizon.profile; // Assure-toi que c'est le bon package pour ta classe PlayerListener

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.event.block.Action; // <-- Ajoute cet import pour Action

public class ProfileClickListener implements Listener {

    // Événement pour le shift-clic droit sur un joueur (celui-ci est correct et utilise getRightClicked)
    @EventHandler
    public void onPlayerShiftRightClickPlayer(PlayerInteractAtEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player opener = event.getPlayer();
        if (event.getRightClicked() instanceof Player) {
            Player targetPlayer = (Player) event.getRightClicked();

            if (opener.isSneaking()) {
                event.setCancelled(true);
                ProfileManager.openProfileMenu(opener, targetPlayer);
            }
        }
    }

    // Événement pour le shift-clic droit sur l'air ou un bloc pour ouvrir son propre profil
    // CETTE MÉTHODE A ÉTÉ CORRIGÉE POUR NE PLUS UTILISER getRightClicked()
    @EventHandler
    public void onPlayerShiftRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return; // N'agir que pour le clic de la main principale
        }

        Player player = event.getPlayer();

        // Vérifier si c'est un clic droit (sur l'air ou un bloc) et que le joueur est en train de s'accroupir (shift)
        if ((event.getAction().equals(Action.RIGHT_CLICK_AIR) ||
                event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) &&
                player.isSneaking()) {

            // Annuler l'événement pour éviter d'autres actions par défaut (comme placer un bloc)
            event.setCancelled(true);

            // Ouvrir le profil du joueur qui a effectué le shift-clic droit
            ProfileManager.openProfileMenu(player, player);
        }
    }
}