package fr.rudy.newhorizon.ui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

public class MenuItemManager implements Listener {

    private final Plugin plugin;
    private final ItemStack menuItem;
    private final int customModelData = 10298; // 👈 Ton CustomModelData ici
    private final Material itemMaterial = Material.PAPER; // 👈 Le Matériau ici

    public MenuItemManager(Plugin plugin) {
        this.plugin = plugin;

        menuItem = new ItemStack(itemMaterial);
        ItemMeta meta = menuItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§fTéléphone");
            meta.setLore(Arrays.asList("§f\uE021 Pour utiliser"));
            meta.setCustomModelData(customModelData);
            menuItem.setItemMeta(meta);
        }
    }

    public void giveMenuItem(Player player) {
        player.getInventory().setItem(8, menuItem);
    }

    private boolean isMenuItem(ItemStack item) {
        if (item == null || item.getType() != itemMaterial) return false;
        if (!item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        return meta.hasDisplayName()
                && "§b» Ouvrir le Menu".equals(meta.getDisplayName())
                && meta.hasCustomModelData()
                && meta.getCustomModelData() == customModelData;
    }

    private boolean hasMenuItem(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isMenuItem(item)) return true;
        }
        return false;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!hasMenuItem(player)) {
                giveMenuItem(player);
            }
        }, 10L);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (isMenuItem(item)) {
            event.setCancelled(true);
            event.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
            Player player = event.getPlayer();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "dmenu open phone_menu " + player.getName());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            ItemStack current = event.getCurrentItem();
            ItemStack cursor = event.getCursor();

            if (isMenuItem(current) || isMenuItem(cursor)) {
                event.setCancelled(true);

                if (isMenuItem(current)) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "dmenu open phone_menu " + player.getName());
                }

                if (isMenuItem(cursor)) {
                    event.setCursor(null);
                }
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (isMenuItem(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.getDrops().removeIf(this::isMenuItem);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!hasMenuItem(player)) {
                giveMenuItem(player);
            }
        }, 2L);
    }
}
