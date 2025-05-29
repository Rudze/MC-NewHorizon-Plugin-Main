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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

public class AnalyzerTask {

    private final Location location;
    private final ItemStack inputItem;
    private final double successChance;
    private final Consumer<ItemStack> resultConsumer;
    private final Plugin plugin;

    private BukkitRunnable task;

    public AnalyzerTask(Plugin plugin, Location location, ItemStack inputItem, double successChance, Consumer<ItemStack> resultConsumer) {
        this.plugin = plugin;
        this.location = location;
        this.inputItem = inputItem;
        this.successChance = successChance;
        this.resultConsumer = resultConsumer;
    }

    public void runTask() {
        World world = location.getWorld();
        if (world == null) return;

        task = new BukkitRunnable() {
            int seconds = 0;
            final int maxSeconds = 60;

            @Override
            public void run() {
                if (seconds >= maxSeconds) {
                    finishAnalysis();
                    cancel();
                    return;
                }
                updateProgress(seconds, maxSeconds);
                seconds++;
            }
        };

        task.runTaskTimer(plugin, 0L, 20L);
    }

    private void updateProgress(int currentSecond, int totalSeconds) {
        int percentage = (currentSecond * 100) / totalSeconds;
        int modelData = 10001801 + ((percentage - 1) / 10); // 10001801 à 10001810

        ItemStack progressItem = new ItemStack(Material.PAPER);
        ItemMeta meta = progressItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§7Analyse en cours: " + percentage + "%");
            meta.setCustomModelData(modelData);
            progressItem.setItemMeta(meta);
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            Inventory inv = AnalyzerManager.getInventoryStatic(location);
            if (inv != null) {
                inv.setItem(13, progressItem);
            }
        });
    }

    private void finishAnalysis() {
        double roll = new Random().nextDouble() * 100.0;

        if (roll <= successChance) {
            double resultRoll = new Random().nextDouble() * 100.0;
            ItemStack result = getRandomDNA(resultRoll);
            if (result != null) {
                resultConsumer.accept(result);
                return;
            }
        }

        resultConsumer.accept(new ItemStack(Material.SAND));
    }

    private ItemStack getRandomDNA(double roll) {
        double cumulative = 0.0;

        Map<String, Double> dnaChances = new LinkedHashMap<>();
        dnaChances.put("newhorizon:tyrannosaurus_dna", 5.0);
        dnaChances.put("newhorizon:ankylosaurus_dna", 15.0);
        dnaChances.put("newhorizon:brachiosaurus_dna", 10.0);
        dnaChances.put("newhorizon:dilophosaurus_dna", 12.0);
        dnaChances.put("newhorizon:parasaurolophus_dna", 14.0);
        dnaChances.put("newhorizon:triceratops_dna", 8.0);

        for (Map.Entry<String, Double> entry : dnaChances.entrySet()) {
            cumulative += entry.getValue();
            if (roll <= cumulative) {
                CustomStack stack = CustomStack.getInstance(entry.getKey());
                if (stack != null) return stack.getItemStack();
            }
        }

        return null;
    }

    public void cancel() {
        if (task != null) {
            task.cancel();
        }
    }
}
