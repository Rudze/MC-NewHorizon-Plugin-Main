package fr.rudy.newhorizon.stats;

import fr.rudy.newhorizon.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StatsGUIListener implements Listener {

    private static final String GUI_TITLE = ":offset_-48::phone_menu::offset_-251::top:";

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory inventory = event.getInventory();
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        ItemMeta meta = clicked.getItemMeta();
        if (!meta.hasDisplayName()) return;

        String displayName = meta.getDisplayName();

        if (displayName.equals("§7Retour")) {
            player.closeInventory();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "dmenu open phone_menu " + player.getName());
            return;
        }

        if (displayName.contains("[Récompense] Pierres minées")) {
            int stone = Main.get().getSessionStatManager().get(player, "stone_mined");
            if (stone >= 50000 && !player.hasPermission("stats.stone.50000")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set stats.stone.50000");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set mcpet.cubee-stone");
                Bukkit.getScheduler().runTaskLater(Main.get(), () -> Bukkit.dispatchCommand(player, "stats"), 2L);
            }
        }

        if (displayName.contains("[Récompense] Squelettes tués")) {
            int skeletonKills = player.getStatistic(Statistic.KILL_ENTITY, EntityType.SKELETON);
            if (skeletonKills >= 15000 && !player.hasPermission("stats.kill.15000")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set stats.kill.15000");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set mcpet.cubee-skeleton");
                Bukkit.getScheduler().runTaskLater(Main.get(), () -> Bukkit.dispatchCommand(player, "stats"), 2L);
            }
        }
    }
}
