package fr.rudy.newhorizon.commands;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.stats.SessionStatManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class StatsCommand implements CommandExecutor {

    private static final int GUI_SIZE = 54;
    private static final String GUI_TITLE = ":offset_-48::phone_menu::offset_-251::top:";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cCommande réservée aux joueurs.");
            return true;
        }

        SessionStatManager stats = Main.get().getSessionStatManager();
        stats.loadStats(player);

        Inventory gui = Bukkit.createInventory(null, GUI_SIZE, GUI_TITLE);

        // Retour
        gui.setItem(0, createPaperItem("§7Retour", 10077));

        // Stats : Stone
        boolean hasPerm = player.hasPermission("stats.stone.50000");
        int stone = stats.get(player, "stone_mined");

        if (stone >= 50000 && !hasPerm) {
            gui.setItem(1, createPaperItem("§a[Récompense] Pierres minées", 1,
                    "§7Objectif atteint: §f" + stone + " pierres",
                    "",
                    "§eClique pour recevoir ta récompense !"));
        } else if (hasPerm) {
            gui.setItem(1, createStoneItem("§fPierres minées ✔", 1,
                    "§7Tu as déjà reçu ta récompense."));
        } else {
            gui.setItem(1, createPaperItem("§fPierres minées", 1,
                    "§7" + stone + " / 50000"));
        }

        player.openInventory(gui);
        return true;
    }

    private ItemStack createPaperItem(String name, int modelData, String... lore) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore.length > 0 ? java.util.Arrays.asList(lore) : Collections.emptyList());
        meta.setCustomModelData(modelData);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createStoneItem(String name, int modelData, String... lore) {
        ItemStack item = new ItemStack(Material.STONE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore.length > 0 ? java.util.Arrays.asList(lore) : Collections.emptyList());
        meta.setCustomModelData(modelData);
        item.setItemMeta(meta);
        return item;
    }
}
