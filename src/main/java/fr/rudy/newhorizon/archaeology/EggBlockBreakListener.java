package fr.rudy.newhorizon.archaeology;

import dev.lone.itemsadder.api.CustomBlock;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.Plugin;

public class EggBlockBreakListener implements Listener {

    private final EggIncubationManager manager;

    public EggBlockBreakListener(Plugin plugin, EggIncubationManager manager) {
        this.manager = manager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onEggBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        CustomBlock customBlock = CustomBlock.byAlreadyPlaced(block);

        if (customBlock == null) return;

        String id = customBlock.getNamespacedID();
        if (id == null || !id.startsWith("newhorizon:egg_")) return;

        manager.cancelIncubation(block.getLocation());
    }
}
