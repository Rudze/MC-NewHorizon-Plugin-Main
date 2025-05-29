package fr.rudy.newhorizon.archaeology;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AnalyzerGUI {
    public static final String ANALYZER_GUI_TITLE = ":offset_-48::analyzer:";
    private final Inventory inventory;

    public AnalyzerGUI(AnalyzerManager manager, Block block) {
        this.inventory = Bukkit.createInventory(null, 27, ANALYZER_GUI_TITLE);
        ItemStack filler = createFiller();

        for (int i = 0; i < inventory.getSize(); i++) {
            // Ne pas mettre de décor dans le slot 13 (barre de chargement)
            if (i == 13) continue;
            inventory.setItem(i, filler);
        }

        AnalyzerManager.INPUT_SLOTS.forEach(slot -> inventory.setItem(slot, null));
        AnalyzerManager.OUTPUT_SLOTS.forEach(slot -> inventory.setItem(slot, null));
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
