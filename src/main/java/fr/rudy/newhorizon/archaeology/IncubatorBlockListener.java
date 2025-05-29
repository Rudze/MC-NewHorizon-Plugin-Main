package fr.rudy.newhorizon.archaeology;

import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class IncubatorBlockListener implements Listener {

    private final IncubatorManager incubatorManager;

    public IncubatorBlockListener(Plugin plugin, IncubatorManager manager) {
        this.incubatorManager = manager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onIncubatorClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack itemInHand = event.getPlayer().getInventory().getItemInMainHand();
        CustomStack heldCustomStack = CustomStack.byItemStack(itemInHand);
        if (heldCustomStack != null && heldCustomStack.getNamespacedID().equalsIgnoreCase("newhorizon:incubator")) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) return;

        CustomBlock customBlock = CustomBlock.byAlreadyPlaced(block);
        if (customBlock == null) return;

        String id = customBlock.getNamespacedID();
        if (id != null && id.startsWith("newhorizon:incubator")) {
            event.setCancelled(true);
            incubatorManager.openGUI(event.getPlayer(), block);
        }
    }

    @EventHandler
    public void onIncubatorBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        CustomBlock customBlock = CustomBlock.byAlreadyPlaced(block);
        if (customBlock == null) return;

        if (customBlock.getNamespacedID().startsWith("newhorizon:incubator")) {
            Location loc = block.getLocation();
            Inventory inv = incubatorManager.getInventory(loc);
            if (inv != null) {
                for (ItemStack item : inv.getContents()) {
                    if (item == null || item.getType() == Material.AIR) continue;

                    ItemMeta meta = item.getItemMeta();
                    if (item.getType() == Material.PAPER && meta != null && meta.hasCustomModelData()) {
                        int data = meta.getCustomModelData();
                        if ((data >= 10001801 && data <= 10001810) || (data >= 10001811 && data <= 10001820)) continue;
                    }

                    if (item.getType() == Material.PAPER && meta != null && "§7".equals(meta.getDisplayName())) {
                        continue;
                    }

                    block.getWorld().dropItemNaturally(loc, item);
                }
                inv.clear();
                incubatorManager.getInventories().remove(loc);
            }

            incubatorManager.getStorage().delete(loc);
            incubatorManager.getIncubators().remove(loc);
        }
    }
}
