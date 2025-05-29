package fr.rudy.newhorizon.archaeology;

import dev.lone.itemsadder.api.CustomStack;
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

public class IncubatorManager {

    public static final List<Integer> INPUT_SLOT = Collections.singletonList(2);
    public static final List<Integer> OUTPUT_SLOT = Collections.singletonList(6);
    public static final List<Integer> MILK_SLOT = Collections.singletonList(22);

    private final Plugin plugin;
    private final IncubatorStorage storage;
    private final Map<Location, Inventory> inventories = new HashMap<>();
    private final Set<Location> incubators = new HashSet<>();
    private final Map<Location, IncubatorTask> tasks = new HashMap<>();
    private final Map<Location, Integer> activeInputSlot = new HashMap<>();

    public IncubatorManager(Plugin plugin) {
        this.plugin = plugin;
        this.storage = new IncubatorStorage(plugin);
        this.incubators.addAll(storage.loadIncubators());

        for (Location loc : incubators) {
            Inventory inv = storage.loadInventory(loc);
            if (inv != null) inventories.put(loc, inv);
        }
    }

    public void openGUI(Player player, Block block) {
        Location loc = block.getLocation();

        if (!incubators.contains(loc)) {
            incubators.add(loc);
            storage.saveIncubator(loc);
        }

        Inventory inv = inventories.get(loc);
        if (inv == null) {
            inv = new IncubatorGUI(this, block).getInventory();
            inventories.put(loc, inv);
        } else {
            ItemStack filler = new ItemStack(Material.PAPER);
            ItemMeta meta = filler.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§7");
                meta.setCustomModelData(10077);
                filler.setItemMeta(meta);
            }
            for (int i = 0; i < inv.getSize(); i++) {
                if (!INPUT_SLOT.contains(i) && !OUTPUT_SLOT.contains(i) && !MILK_SLOT.contains(i)) {
                    ItemStack current = inv.getItem(i);
                    if (current == null || current.getType() == Material.AIR) {
                        inv.setItem(i, filler);
                    }
                }
            }
        }

        player.openInventory(inv);
        cancelIfInvalid(loc);
        if (!isRunning(loc)) {
            tryStart(loc, inv.getContents());
        }
    }
    public void tryStart(Location location, ItemStack[] contents) {
        ItemStack dna = contents[INPUT_SLOT.get(0)];
        ItemStack milk = contents[MILK_SLOT.get(0)];

        if (!isValidDNA(dna) || !isValidMilk(milk)) return;

        IncubatorTask task = new IncubatorTask(plugin, location, result -> {
            Inventory inv = inventories.get(location);
            if (inv == null) return;

            // Consommer l'ADN et le lait
            for (int slot : INPUT_SLOT) {
                ItemStack item = inv.getItem(slot);
                if (item != null) {
                    item.setAmount(item.getAmount() - 1);
                    if (item.getAmount() <= 0) inv.setItem(slot, null);
                }
            }
            for (int slot : MILK_SLOT) {
                ItemStack item = inv.getItem(slot);
                if (item != null && item.getType() == Material.MILK_BUCKET) {
                    inv.setItem(slot, new ItemStack(Material.BUCKET));
                }
            }

            // Placer l'œuf
            for (int slot : OUTPUT_SLOT) {
                if (inv.getItem(slot) == null || inv.getItem(slot).getType() == Material.AIR) {
                    inv.setItem(slot, result);
                    break;
                }
            }

            tasks.remove(location);

            if (!isRunning(location)) {
                inv.setItem(13, null); // Nettoyer la barre de progression si aucune autre incubation ne démarre
            }
        });

        task.start();
        tasks.put(location, task);
        activeInputSlot.put(location, INPUT_SLOT.get(0));
    }

    public void cancelIfInvalid(Location location) {
        Inventory inv = inventories.get(location);
        if (inv == null) return;

        ItemStack dna = inv.getItem(INPUT_SLOT.get(0));
        ItemStack milk = inv.getItem(MILK_SLOT.get(0));

        if (!isValidDNA(dna) || !isValidMilk(milk)) {
            IncubatorTask task = tasks.remove(location);
            if (task != null) task.cancel();
            inv.setItem(13, null);
        }
    }

    public boolean isRunning(Location loc) {
        return tasks.containsKey(loc);
    }

    private boolean isValidDNA(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        CustomStack stack = CustomStack.byItemStack(item);
        return stack != null && stack.getNamespacedID().endsWith("_dna");
    }

    private boolean isValidMilk(ItemStack item) {
        return item != null && item.getType() == Material.MILK_BUCKET;
    }

    public Inventory getInventory(Location loc) {
        return inventories.get(loc);
    }

    public static Inventory getInventoryStatic(Location loc) {
        return fr.rudy.newhorizon.Main.get().getIncubatorManager().getInventory(loc);
    }

    public Map<Location, Inventory> getInventories() {
        return inventories;
    }

    public Set<Location> getIncubators() {
        return incubators;
    }

    public IncubatorStorage getStorage() {
        return storage;
    }
}
