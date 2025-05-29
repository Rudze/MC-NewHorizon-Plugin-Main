package fr.rudy.newhorizon.archaeology;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Map;

public class IncubatorInventoryListener implements Listener {

    private final Plugin plugin;
    private final IncubatorManager manager;

    public IncubatorInventoryListener(Plugin plugin, IncubatorManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Inventory inv = event.getInventory();
        if (!manager.getInventories().containsValue(inv)) return;

        int rawSlot = event.getRawSlot();
        if (rawSlot < 0 || rawSlot >= inv.getSize()) return;

        if (!IncubatorManager.INPUT_SLOT.contains(rawSlot)
                && !IncubatorManager.OUTPUT_SLOT.contains(rawSlot)
                && !IncubatorManager.MILK_SLOT.contains(rawSlot)) {
            event.setCancelled(true);
            return;
        }

        if (IncubatorManager.OUTPUT_SLOT.contains(rawSlot)) {
            InventoryAction action = event.getAction();
            if (!(action == InventoryAction.PICKUP_ALL ||
                    action == InventoryAction.PICKUP_HALF ||
                    action == InventoryAction.PICKUP_ONE ||
                    action == InventoryAction.PICKUP_SOME)) {
                event.setCancelled(true);
            }
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            Location loc = getLocation(inv);
            if (loc == null) return;

            manager.cancelIfInvalid(loc);
            if (!manager.isRunning(loc)) {
                manager.tryStart(loc, inv.getContents());
            }
        });
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        Inventory inv = event.getInventory();
        if (!manager.getInventories().containsValue(inv)) return;

        for (int slot : event.getRawSlots()) {
            if (!IncubatorManager.INPUT_SLOT.contains(slot)
                    && !IncubatorManager.MILK_SLOT.contains(slot)) {
                event.setCancelled(true);
                return;
            }
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            Location loc = getLocation(inv);
            if (loc == null) return;

            manager.cancelIfInvalid(loc);
            if (!manager.isRunning(loc)) {
                manager.tryStart(loc, inv.getContents());
            }
        });
    }

    private Location getLocation(Inventory inv) {
        for (Map.Entry<Location, Inventory> entry : manager.getInventories().entrySet()) {
            if (entry.getValue().equals(inv)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
