package fr.rudy.newhorizon.archaeology;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class IncubatorGUI {
    public static final String INCUBATOR_GUI_TITLE = ":offset_-48::incubator:"; // Titre avec un offset pour ItemsAdder
    private final Inventory inventory;

    public IncubatorGUI(IncubatorManager manager, Block block) {
        this.inventory = Bukkit.createInventory(null, 27, INCUBATOR_GUI_TITLE);
        ItemStack filler = createFiller();

        for (int i = 0; i < inventory.getSize(); i++) {
            if (i == 13) continue; // Laisser libre pour la barre de progression
            inventory.setItem(i, filler);
        }

        IncubatorManager.INPUT_SLOT.forEach(slot -> inventory.setItem(slot, null));
        IncubatorManager.OUTPUT_SLOT.forEach(slot -> inventory.setItem(slot, null));
        IncubatorManager.MILK_SLOT.forEach(slot -> inventory.setItem(slot, null));
    }

    public Inventory getInventory() {
        return inventory;
    }

    private ItemStack createFiller() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§7");
            meta.setCustomModelData(10077);
            item.setItemMeta(meta);
        }
        return item;
    }
}
