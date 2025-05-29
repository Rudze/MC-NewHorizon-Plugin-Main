package fr.rudy.newhorizon.archaeology;

import fr.rudy.newhorizon.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class AnalyzerManager {

    private final Plugin plugin;
    private final AnalyzerStorage storage;
    private final Map<Location, List<AnalyzerTask>> runningTasks = new HashMap<>();
    private final Map<Location, Inventory> analyzerInventories = new HashMap<>();
    private final Map<Location, Integer> activeSlots = new HashMap<>();
    private final Set<Location> analyzerBlocks = new HashSet<>();

    public static final List<Integer> INPUT_SLOTS = Arrays.asList(11);
    public static final List<Integer> OUTPUT_SLOTS = Arrays.asList(6, 7, 8, 15, 16, 17, 24, 25, 26);

    public AnalyzerManager(Plugin plugin) {
        this.plugin = plugin;
        this.storage = new AnalyzerStorage(plugin);
        this.analyzerBlocks.addAll(storage.loadAnalyzers());

        for (Location loc : analyzerBlocks) {
            Inventory inv = storage.loadInventory(loc);
            if (inv != null) {
                analyzerInventories.put(loc, inv);
            }
        }
    }

    public void openAnalyzerGUI(Player player, Block block) {
        Location loc = block.getLocation();

        if (!analyzerBlocks.contains(loc)) {
            analyzerBlocks.add(loc);
            storage.saveAnalyzer(loc);
            Bukkit.getLogger().info("[Analyzer Debug] New analyzer registered at: " + loc);
        }

        Inventory inv = analyzerInventories.get(loc);
        if (inv == null) {
            AnalyzerGUI gui = new AnalyzerGUI(this, block);
            inv = gui.getInventory();
            analyzerInventories.put(loc, inv);
        } else {
            ItemStack filler = new ItemStack(Material.PAPER);
            ItemMeta meta = filler.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§7");
                meta.setCustomModelData(10077);
                filler.setItemMeta(meta);
            }
            for (int i = 0; i < inv.getSize(); i++) {
                if (!INPUT_SLOTS.contains(i) && !OUTPUT_SLOTS.contains(i)) {
                    ItemStack current = inv.getItem(i);
                    if (current == null || current.getType() == Material.AIR) {
                        inv.setItem(i, filler);
                    }
                }
            }
        }

        player.openInventory(inv);

        cancelAnalysisIfEmpty(loc);
        if (!isAnalyzing(loc)) {
            for (int slot : INPUT_SLOTS) {
                ItemStack item = inv.getItem(slot);
                if (item != null && item.getType() != Material.AIR) {
                    analyze(block, inv.getContents());
                    break;
                }
            }
        }
    }

    public void analyze(Block block, ItemStack[] contents) {
        Location location = block.getLocation();
        for (int slot : INPUT_SLOTS) {
            ItemStack item = contents[slot];
            if (item == null || item.getType() == Material.AIR) continue;

            double chance = getSuccessChance(item);
            if (chance <= 0) continue;

            AnalyzerTask task = new AnalyzerTask(plugin, location, item.clone(), chance, result -> {
                storeResult(location, result);
            });
            task.runTask();
            runningTasks.put(location, Collections.singletonList(task));
            activeSlots.put(location, slot);
            break;
        }
    }

    public double getSuccessChance(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return 0.0;

        dev.lone.itemsadder.api.CustomStack customStack = dev.lone.itemsadder.api.CustomStack.byItemStack(item);
        if (customStack != null && customStack.getNamespacedID().equalsIgnoreCase("newhorizon:fossil")) {
            return 15.0;
        }
        if (item.getType() == Material.BONE) return 1.0;

        return 0.0;
    }

    private void storeResult(Location location, ItemStack result) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Inventory inv = analyzerInventories.get(location);
            if (inv == null) return;

            Integer slotToClear = activeSlots.remove(location);
            if (slotToClear != null) {
                ItemStack item = inv.getItem(slotToClear);
                if (item != null && item.getType() != Material.AIR) {
                    item.setAmount(item.getAmount() - 1);
                    if (item.getAmount() <= 0) inv.setItem(slotToClear, null);
                }
            }

            boolean restarted = false;

            if (result != null) {
                for (int slot : OUTPUT_SLOTS) {
                    ItemStack current = inv.getItem(slot);
                    if (current != null && current.isSimilar(result) && current.getAmount() < current.getMaxStackSize()) {
                        current.setAmount(current.getAmount() + 1);
                        runningTasks.remove(location);
                        restartAnalysisIfNeeded(location);
                        restarted = isAnalyzing(location);
                        break;
                    }
                }
                if (!restarted) {
                    for (int slot : OUTPUT_SLOTS) {
                        ItemStack current = inv.getItem(slot);
                        if (current == null || current.getType() == Material.AIR) {
                            inv.setItem(slot, result);
                            runningTasks.remove(location);
                            restartAnalysisIfNeeded(location);
                            restarted = isAnalyzing(location);
                            break;
                        }
                    }
                }
            } else {
                ItemStack fallback = new ItemStack(Material.SAND, 1);
                for (int slot : OUTPUT_SLOTS) {
                    ItemStack current = inv.getItem(slot);
                    if (current == null || current.getType() == Material.AIR) {
                        inv.setItem(slot, fallback);
                        runningTasks.remove(location);
                        restartAnalysisIfNeeded(location);
                        restarted = isAnalyzing(location);
                        break;
                    }
                }
            }

            if (!restarted) {
                inv.setItem(13, null); // Nettoyer la barre de progression uniquement si rien ne redémarre
            }

            runningTasks.remove(location);
        });
    }

    private void restartAnalysisIfNeeded(Location location) {
        Inventory inv = analyzerInventories.get(location);
        if (inv == null) return;

        for (int slot : INPUT_SLOTS) {
            ItemStack item = inv.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                Block block = location.getBlock();
                analyze(block, inv.getContents());
                break;
            }
        }
    }

    public void cancelAnalysisIfEmpty(Location location) {
        Inventory inv = analyzerInventories.get(location);
        if (inv == null) return;

        Integer activeSlot = activeSlots.get(location);
        if (activeSlot != null) {
            ItemStack activeItem = inv.getItem(activeSlot);
            if (activeItem == null || activeItem.getType() == Material.AIR) {
                List<AnalyzerTask> tasks = runningTasks.remove(location);
                activeSlots.remove(location);
                if (tasks != null) {
                    for (AnalyzerTask task : tasks) {
                        task.cancel();
                    }
                }
                inv.setItem(13, null);
                return;
            }
        }

        boolean hasInput = INPUT_SLOTS.stream().anyMatch(slot -> {
            ItemStack item = inv.getItem(slot);
            return item != null && item.getType() != Material.AIR;
        });

        if (!hasInput && runningTasks.containsKey(location)) {
            List<AnalyzerTask> tasks = runningTasks.remove(location);
            activeSlots.remove(location);
            if (tasks != null) {
                for (AnalyzerTask task : tasks) {
                    task.cancel();
                }
            }
            inv.setItem(13, null);
        }
    }

    public AnalyzerStorage getStorage() {
        return storage;
    }

    public Inventory getInventory(Location loc) {
        return analyzerInventories.get(loc);
    }

    public Map<Location, Inventory> getInventories() {
        return analyzerInventories;
    }

    public boolean isAnalyzing(Location location) {
        return runningTasks.containsKey(location);
    }

    public static Inventory getInventoryStatic(Location location) {
        return Main.get().getAnalyzerManager().getInventory(location);
    }

    public Set<Location> getAnalyzerBlocks() {
        return analyzerBlocks;
    }
}
