package fr.rudy.newhorizon.archaeology;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ArchaeologistCloseListener implements Listener {

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        Inventory inv = event.getInventory();
        if (!event.getView().getTitle().equals(ArchaeologistGUI.GUI_TITLE)) return;

        ItemStack fossil = inv.getItem(4);
        if (fossil != null && fossil.getType().isItem()) {
            // Tenter de rendre au joueur
            player.getInventory().addItem(fossil).values().forEach(itemLeft -> {
                // Drop au sol s'il y a des restes
                player.getWorld().dropItemNaturally(player.getLocation(), itemLeft);
            });
        }
    }
}
