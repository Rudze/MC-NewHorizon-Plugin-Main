package fr.rudy.newhorizon.archaeology;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ArchaeologistGUI {

    public static final String GUI_TITLE = ":offset_-48::analyze:";

    public static Inventory createGUI() {
        Inventory inv = Bukkit.createInventory(null, 27, GUI_TITLE);

        ItemStack decor = new ItemStack(Material.PAPER);
        ItemMeta meta = decor.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§7");
            meta.setCustomModelData(10077);
            decor.setItemMeta(meta);
        }

        // Remplir tous les slots avec du décor
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, decor);
        }

        // Slot 4 = dépôt fossil
        inv.setItem(4, null);

        // Slots 21, 22, 23 = boutons d’analyse
        for (int i : new int[]{21, 22, 23}) {
            ItemStack analyze = new ItemStack(Material.PAPER);
            ItemMeta analyzeMeta = analyze.getItemMeta();
            if (analyzeMeta != null) {
                analyzeMeta.setDisplayName("§7Analyser");
                analyzeMeta.setCustomModelData(10077);
                analyze.setItemMeta(analyzeMeta);
            }
            inv.setItem(i, analyze);
        }

        // Slot 8 = bouton Wiki
        ItemStack wiki = new ItemStack(Material.PAPER);
        ItemMeta wikiMeta = wiki.getItemMeta();
        if (wikiMeta != null) {
            wikiMeta.setDisplayName("§b§lQu'est-ce que l'analyse de fossile ?");
            List<String> lore = new ArrayList<>();
            lore.add("§fPermet d'analyser les fossiles trouvés");
            lore.add("§fdans le sable ou le gravier suspect.");
            lore.add("§fAvec un peu de chance, Lyra pourra");
            lore.add("§fextraire de l'ADN d'une ancienne créature !");
            wikiMeta.setLore(lore);
            wikiMeta.setCustomModelData(10077);
            wiki.setItemMeta(wikiMeta);
        }
        inv.setItem(8, wiki);

        return inv;
    }
}
