package fr.rudy.newhorizon.friend;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class FriendMenu implements Listener {

    private final FriendManager friendManager;

    public FriendMenu(FriendManager friendManager) {
        this.friendManager = friendManager;
        // Assure-toi que "NewHorizon" est le nom correct de ton plugin principal
        Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugin("NewHorizon"));
    }

    public void open(Player player) {
        player.openInventory(createFriendsInventory(player));
    }

    private Inventory createFriendsInventory(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, ":offset_-48::friends_list:");

        // Slot 7 : bouton "Demandes reçues"
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta meta = paper.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§7Demandes reçues");
            meta.setCustomModelData(10077);
            paper.setItemMeta(meta);
        }
        inv.setItem(7, paper);

        // Slots 9 à 43 : amis
        List<UUID> friends = friendManager.getFriends(player.getUniqueId());
        int slot = 9;
        for (UUID friendId : friends) {
            if (slot >= 44) break;

            OfflinePlayer friend = Bukkit.getOfflinePlayer(friendId);
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            if (skullMeta != null) {
                skullMeta.setOwningPlayer(friend);
                skullMeta.setDisplayName("§f" + friend.getName());
                skull.setItemMeta(skullMeta);
            }
            inv.setItem(slot, skull);
            slot++;
        }

        return inv;
    }

    private Inventory createRequestsInventory(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, ":offset_-48::friends_add:");
        List<UUID> requests = friendManager.getPendingRequests(player.getUniqueId());

        // Slot 6 : bouton retour
        ItemStack back = new ItemStack(Material.PAPER);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§7Liste d'amis");
            backMeta.setCustomModelData(10077);
            back.setItemMeta(backMeta);
        }
        inv.setItem(6, back);

        int slot = 9;
        for (UUID requestId : requests) {
            if (slot >= 44) break;

            OfflinePlayer requester = Bukkit.getOfflinePlayer(requestId);
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(requester);
                meta.setDisplayName("§f" + requester.getName());
                skull.setItemMeta(meta);
            }
            inv.setItem(slot, skull);
            slot++;
        }

        return inv;
    }

    private Inventory createDecisionMenu(Player player, String requesterName) {
        Inventory inv = Bukkit.createInventory(null, 9, ":offset_-48::confirm_2:" + requesterName);

        // Slot 4 : Tête du joueur
        OfflinePlayer requester = Bukkit.getOfflinePlayer(requesterName);
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(requester);
            meta.setDisplayName("§f" + requester.getName());
            skull.setItemMeta(meta);
        }
        inv.setItem(4, skull);

        // Slot 6 et 7 : Accepter
        ItemStack accept = new ItemStack(Material.PAPER);
        ItemMeta acceptMeta = accept.getItemMeta();
        if (acceptMeta != null) {
            acceptMeta.setDisplayName("§aAccepter");
            acceptMeta.setCustomModelData(10077);
            accept.setItemMeta(acceptMeta);
        }
        inv.setItem(6, accept);
        inv.setItem(7, accept);

        // Slot 12 : Refuser
        ItemStack deny = new ItemStack(Material.PAPER);
        ItemMeta denyMeta = deny.getItemMeta();
        if (denyMeta != null) {
            denyMeta.setDisplayName("§cRefuser");
            denyMeta.setCustomModelData(10077);
            deny.setItemMeta(denyMeta);
        }
        inv.setItem(1, deny);
        inv.setItem(2, deny);

        return inv;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null) return;

        String title = event.getView().getTitle();
        ItemStack item = event.getCurrentItem();
        if (item == null) return;

        // Déplace event.setCancelled(true) à l'intérieur des conditions de titre
        // pour n'annuler les clics que dans tes GUIs personnalisées.

        // Liste d'amis
        if (title.contains(":offset_-48::friends_list:")) {
            event.setCancelled(true); // Annule le clic SEULEMENT dans cet inventaire
            if (item.getType() == Material.PLAYER_HEAD) {
                String name = Optional.ofNullable(item.getItemMeta()).map(ItemMeta::getDisplayName).orElse("").replace("§f", "");
                if (!name.isEmpty()) {
                    player.closeInventory();
                    player.performCommand("profile " + name);
                }
            } else if (item.getType() == Material.PAPER && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().contains("Demandes reçues")) {
                player.openInventory(createRequestsInventory(player));
            }
        }

        // Menu des demandes reçues
        else if (title.contains(":offset_-48::friends_add:")) {
            event.setCancelled(true); // Annule le clic SEULEMENT dans cet inventaire
            if (item.getType() == Material.PLAYER_HEAD) {
                String name = Optional.ofNullable(item.getItemMeta()).map(ItemMeta::getDisplayName).orElse("").replace("§f", "");
                if (!name.isEmpty()) {
                    player.openInventory(createDecisionMenu(player, name));
                }
            } else if (item.getType() == Material.PAPER && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().contains("Liste d'amis")) {
                player.openInventory(createFriendsInventory(player));
            }
        }

        // Menu décision
        else if (title.contains(":offset_-48::confirm_2:")) {
            event.setCancelled(true); // Annule le clic SEULEMENT dans cet inventaire
            String requesterName = title.split(":confirm_2:")[1];
            OfflinePlayer requester = Bukkit.getOfflinePlayer(requesterName);

            if (item.getItemMeta() != null && item.getItemMeta().hasDisplayName()) {
                String display = item.getItemMeta().getDisplayName();
                if (display.contains("Accepter")) {
                    friendManager.acceptRequest(player.getUniqueId(), requester.getUniqueId());
                    player.sendMessage("§aTu es maintenant ami avec " + requester.getName());
                    player.openInventory(createFriendsInventory(player));
                } else if (display.contains("Refuser")) {
                    friendManager.denyRequest(player.getUniqueId(), requester.getUniqueId());
                    player.sendMessage("§cTu as refusé la demande de " + requester.getName());
                    player.openInventory(createRequestsInventory(player));
                }
            }
        }
        // Si le titre de l'inventaire ne correspond à aucun de tes menus,
        // l'événement ne sera PAS annulé, permettant l'interaction normale avec l'inventaire du joueur.
    }
}