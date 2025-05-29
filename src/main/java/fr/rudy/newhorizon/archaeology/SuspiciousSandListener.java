package fr.rudy.newhorizon.archaeology;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BrushableBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SuspiciousSandListener implements Listener {

    private final JavaPlugin plugin;

    public SuspiciousSandListener(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onBrushUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null || (block.getType() != Material.SUSPICIOUS_SAND && block.getType() != Material.SUSPICIOUS_GRAVEL)) return;

        if (!(block.getState() instanceof BrushableBlock brushable)) return;

        ItemStack brush = event.getItem();
        if (brush == null || brush.getType() != Material.BRUSH) return;

        // Si le bloc contient déjà un item, on ne change rien (déjà fouillé)
        if (brushable.getItem() != null && brushable.getItem().getType() != Material.AIR) return;

        // Génération d'un seul item aléatoire à insérer dans le bloc
        ItemStack loot = generateLoot();
        if (loot != null) {
            brushable.setLootTable(null); // Supprime le loot vanilla
            brushable.setItem(loot);
            brushable.update();
        }
    }

    private ItemStack generateLoot() {
        Random rand = new Random();
        double roll = rand.nextDouble() * 100;

        // Très communs (40%)
        if (roll < 40) {
            Material[] commons = {Material.BONE, Material.STICK, Material.FLINT};
            return new ItemStack(commons[rand.nextInt(commons.length)]);
        }

        // Communs (30%)
        if (roll < 70) {
            Material[] semiCommons = {Material.RAW_GOLD, Material.EMERALD, Material.COAL};
            return new ItemStack(semiCommons[rand.nextInt(semiCommons.length)]);
        }

        // Peu communs (15%)
        if (roll < 85 && rand.nextDouble() < 0.5) {
            CustomStack fossil = CustomStack.getInstance("newhorizon:fossil");
            if (fossil != null) return fossil.getItemStack();
        }

        // Rares (10%)
        if (roll < 95 && rand.nextDouble() < 0.3) {
            CustomStack relic = CustomStack.getInstance("newhorizon:artisan_relic");
            if (relic != null) return relic.getItemStack();
        }

        // Très rares (5%)
        if (roll >= 95 && rand.nextDouble() < 0.2) {
            CustomStack sand = CustomStack.getInstance("newhorizon:spiritual_sand");
            if (sand != null) return sand.getItemStack();
        }

        return null;
    }
}
