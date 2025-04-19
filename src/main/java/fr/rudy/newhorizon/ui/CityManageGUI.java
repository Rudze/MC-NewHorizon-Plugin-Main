package fr.rudy.newhorizon.ui;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.city.CityManager;
import fr.rudy.newhorizon.city.CityRank;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class CityManageGUI {

    private final CityManager cityManager = Main.get().getCityManager();

    public void open(Player player) {
        UUID uuid = player.getUniqueId();
        String city = cityManager.getCityName(uuid);
        CityRank rank = cityManager.getCityRank(uuid);

        Inventory gui = Bukkit.createInventory(null, 54, ":offset_-48::phone_menu::offset_-251::mycity_menu:");

        gui.setItem(0, createItem("§7Retour"));
        gui.setItem(8, createItem("§bWiki & Guide"));

        gui.setItem(7, createItem(
                rank == CityRank.LEADER ? "§4Supprimer la ville" : "§4Quitter la ville"
        ));

        ItemStack protect = createItem("§7Protéger");
        gui.setItem(37, protect);
        gui.setItem(38, protect);
        gui.setItem(39, protect);

        ItemStack spawn = createItem("§7Placer le spawn", "§f Définir le point d’apparition", "§f Position actuelle du joueur");
        gui.setItem(19, spawn);
        gui.setItem(20, spawn);
        gui.setItem(21, spawn);

        ItemStack modif = createItem("§7Modifier la bannière");
        gui.setItem(28, modif);
        gui.setItem(30, modif);
        gui.setItem(16, modif);

        // Membres
        int[] memberSlots = {23, 24, 25, 32, 33, 34, 40, 41};
        List<UUID> members = cityManager.getSortedMembersByRank(city);
        for (int i = 0; i < Math.min(memberSlots.length, members.size()); i++) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(members.get(i));
            CityRank memberRank = cityManager.getCityRank(members.get(i));
            gui.setItem(memberSlots[i], createItem("§7" + memberRank.getDisplayName() + " §f" + target.getName()));
        }

        gui.setItem(42, createItem("§aAjouter un membre"));

        player.openInventory(gui);
    }

    private ItemStack createItem(String name, String... lore) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        meta.setCustomModelData(10077);
        item.setItemMeta(meta);
        return item;
    }
}
