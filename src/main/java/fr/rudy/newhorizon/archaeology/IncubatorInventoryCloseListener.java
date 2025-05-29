package fr.rudy.newhorizon.archaeology;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

public class IncubatorInventoryCloseListener implements Listener {

    private final IncubatorManager incubatorManager;

    public IncubatorInventoryCloseListener(Plugin plugin, IncubatorManager manager) {
        this.incubatorManager = manager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        if (inv == null || !incubatorManager.getInventories().containsValue(inv)) return;

        Location location = getIncubatorLocation(inv);
        if (location == null) return;

        incubatorManager.getStorage().saveInventory(location, inv);
    }

    private Location getIncubatorLocation(Inventory inv) {
        for (var entry : incubatorManager.getInventories().entrySet()) {
            if (entry.getValue().equals(inv)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
