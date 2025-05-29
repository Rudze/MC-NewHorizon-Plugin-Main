package fr.rudy.newhorizon.archaeology;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Consumer;

public class IncubatorTask {

    private final Plugin plugin;
    private final Location location;
    private final Consumer<ItemStack> resultConsumer;
    private BukkitRunnable task;

    public IncubatorTask(Plugin plugin, Location location, Consumer<ItemStack> resultConsumer) {
        this.plugin = plugin;
        this.location = location;
        this.resultConsumer = resultConsumer;
    }

    public void start() {
        World world = location.getWorld();
        if (world == null) return;

        task = new BukkitRunnable() {
            int seconds = 0;
            final int maxSeconds = 600;

            @Override
            public void run() {
                if (seconds >= maxSeconds) {
                    finish();
                    cancel();
                    return;
                }
                updateProgress(seconds, maxSeconds);
                seconds++;
            }
        };

        task.runTaskTimer(plugin, 0L, 20L);
    }

    private void updateProgress(int current, int total) {
        int percent = (current * 100) / total;
        int modelData = 10001811 + ((percent - 1) / 10); // Barre animée

        ItemStack progress = new ItemStack(Material.PAPER);
        ItemMeta meta = progress.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§7Incubation: " + percent + "%");
            meta.setCustomModelData(modelData);
            progress.setItemMeta(meta);
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            Inventory inv = IncubatorManager.getInventoryStatic(location);
            if (inv != null) inv.setItem(13, progress);
        });
    }

    private void finish() {
        Bukkit.getScheduler().runTask(plugin, () -> {
            CustomStack egg = CustomStack.getInstance("newhorizon:tyrannosaurus_egg");
            ItemStack result = egg != null ? egg.getItemStack() : new ItemStack(Material.EGG);
            resultConsumer.accept(result);
        });
    }

    public void cancel() {
        if (task != null) task.cancel();
    }
}
