package fr.rudy.newhorizon.archaeology;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class AnalyzerInventoryListener implements Listener {

    private final AnalyzerManager analyzerManager;
    private final Plugin plugin;

    public AnalyzerInventoryListener(Plugin plugin, AnalyzerManager manager) {
        this.plugin = plugin;
        this.analyzerManager = manager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Inventory inv = event.getInventory();
        if (inv == null || !analyzerManager.getInventories().containsValue(inv)) return;

        Location location = getAnalyzerLocation(inv);
        if (location == null) return;

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= inv.getSize()) return;

        // Bloquer les clics dans les slots décor
        if (!AnalyzerManager.INPUT_SLOTS.contains(slot) && !AnalyzerManager.OUTPUT_SLOTS.contains(slot)) {
            event.setCancelled(true);
            return;
        }

        // Gérer les clics sur les slots de sortie
        if (AnalyzerManager.OUTPUT_SLOTS.contains(slot)) {
            InventoryAction action = event.getAction();
            if (event.isShiftClick() && event.getClickedInventory() == inv) {
                // Autoriser le transfert vers l'inventaire du joueur
                return;
            }

            if (action != InventoryAction.PICKUP_ALL &&
                    action != InventoryAction.PICKUP_HALF &&
                    action != InventoryAction.PICKUP_ONE &&
                    action != InventoryAction.PICKUP_SOME) {
                event.setCancelled(true);
            }
            return;
        }

        // Si on place un item dans un slot valide, recheck l'inventaire
        Bukkit.getScheduler().runTask(plugin, () -> {
            analyzerManager.cancelAnalysisIfEmpty(location);
            if (!analyzerManager.isAnalyzing(location)) {
                Block block = location.getBlock();
                analyzerManager.analyze(block, inv.getContents());
            }
        });
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory inv = event.getInventory();
        if (inv == null || !analyzerManager.getInventories().containsValue(inv)) return;

        Location location = getAnalyzerLocation(inv);
        if (location == null) return;

        for (int slot : event.getRawSlots()) {
            if (!AnalyzerManager.INPUT_SLOTS.contains(slot)) {
                event.setCancelled(true);
                return;
            }
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            analyzerManager.cancelAnalysisIfEmpty(location);
            if (!analyzerManager.isAnalyzing(location)) {
                Block block = location.getBlock();
                analyzerManager.analyze(block, inv.getContents());
            }
        });
    }

    private boolean isItemAllowed(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (item.getType() == Material.BONE) return true;

        CustomStack customStack = CustomStack.byItemStack(item);
        return customStack != null && customStack.getNamespacedID().equalsIgnoreCase("newhorizon:fossil");
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
