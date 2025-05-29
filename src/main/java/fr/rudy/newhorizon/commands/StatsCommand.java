package fr.rudy.newhorizon.commands;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.stats.SessionStatManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
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

        // Stone
        boolean hasStone = player.hasPermission("stats.stone.50000");
        int stone = stats.get(player, "stone_mined");
        if (stone >= 50000 && !hasStone) {
            gui.setItem(1, createPaperItem("§a[Récompense] Pierres minées", 1,
                    "§7Objectif atteint: §f" + stone + " pierres",
                    "",
                    "§eClique pour recevoir ta récompense !"));
        } else if (hasStone) {
            gui.setItem(1, createStoneItem("§fPierres minées ✔", 1,
                    "§7Tu as déjà reçu ta récompense."));
        } else {
            gui.setItem(1, createPaperItem("§fPierres minées", 1,
                    "§7" + stone + " / 50000"));
        }

        // Skeletons
        boolean hasSkeleton = player.hasPermission("stats.kill.15000");
        int kills = player.getStatistic(Statistic.KILL_ENTITY, EntityType.SKELETON);
        if (kills >= 15000 && !hasSkeleton) {
            gui.setItem(2, createPaperItem("§a[Récompense] Squelettes tués", 1,
                    "§7Objectif atteint: §f" + kills + " squelettes",
                    "",
                    "§eClique pour recevoir ta récompense !"));
        } else if (hasSkeleton) {
            gui.setItem(2, createStoneItem("§fSquelettes tués ✔", 1,
                    "§7Tu as déjà reçu ta récompense."));
        } else {
            gui.setItem(2, createPaperItem("§fSquelettes tués", 1,
                    "§7" + kills + " / 15000"));
        }

        // TNT
        boolean hasTnt = player.hasPermission("stats.tnt.500");
        int tnt = player.getStatistic(Statistic.USE_ITEM, Material.TNT);
        if (tnt >= 500 && !hasTnt) {
            gui.setItem(3, createPaperItem("§a[Récompense] TNT placées", 1,
                    "§7Objectif atteint: §f" + tnt + " TNT",
                    "",
                    "§eClique pour recevoir ta récompense !"));
        } else if (hasTnt) {
            gui.setItem(3, createStoneItem("§fTNT placées ✔", 1,
                    "§7Tu as déjà reçu ta récompense."));
        } else {
            gui.setItem(3, createPaperItem("§fTNT placées", 1,
                    "§7" + tnt + " / 500"));
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
