package fr.rudy.newhorizon.archaeology;

import dev.lone.itemsadder.api.CustomBlock;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.Plugin;

public class EggBlockListener implements Listener {

    private final EggIncubationManager manager;
    private final Plugin plugin;

    public EggBlockListener(Plugin plugin, EggIncubationManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onEggPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();

        // Attendre quelques ticks avant de détecter le CustomBlock
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            CustomBlock customBlock = CustomBlock.byAlreadyPlaced(block);

            if (customBlock == null) {
                plugin.getLogger().info("[DEBUG] Aucun CustomBlock détecté même après délai.");
                return;
            }

            String id = customBlock.getNamespacedID();
            plugin.getLogger().info("[DEBUG] Bloc IA détecté après délai : " + id);

            if (!id.startsWith("newhorizon:egg_")) return;

            if (!manager.isIncubating(block.getLocation())) {
                plugin.getLogger().info("[DEBUG] Lancement incubation après délai pour : " + id);
                manager.startIncubation(block);
            } else {
                plugin.getLogger().info("[DEBUG] Incubation déjà en cours pour : " + id);
            }
        }, 3L); // 2-3 ticks suffisent pour laisser IA finir
    }

}
