package fr.rudy.newhorizon.archaeology;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

public class AnalyzerInventoryCloseListener implements Listener {

    private final AnalyzerManager analyzerManager;

    public AnalyzerInventoryCloseListener(Plugin plugin, AnalyzerManager manager) {
        this.analyzerManager = manager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        if (inv == null || !analyzerManager.getInventories().containsValue(inv)) return;

        Location location = getAnalyzerLocation(inv);
        if (location == null) return;

        analyzerManager.getStorage().saveInventory(location, inv);
    }

    private Location getAnalyzerLocation(Inventory inv) {
        for (var entry : analyzerManager.getInventories().entrySet()) {
            if (entry.getValue().equals(inv)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
