package fr.rudy.newhorizon.profile;

import com.hibiscusmc.hmccosmetics.api.HMCCosmeticsAPI;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.home.HomesManager;
import fr.rudy.newhorizon.friend.FriendManager;
import fr.rudy.newhorizon.utils.MessageUtil;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ProfileManager implements Listener {

    private static final int HEAD_SLOT = 0;
    private static final int WORN_HELMET = 7, WORN_CHEST = 16, WORN_LEGS = 25, WORN_BOOTS = 34;
    private static final int BTN_HOME_1 = 27, BTN_HOME_2 = 28;
    private static final int BTN_INVITE = 29, BTN_FRIEND = 30, BTN_DUEL = 31, BTN_TRADE = 32, BTN_VAULT = 33;

    private static final String GUI_PREFIX = ":offset_-48::profile:";
    private static final ConcurrentHashMap<UUID, UUID> OPENED = new ConcurrentHashMap<>();

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, Main.get());
    }

    public static void openProfileMenu(Player opener, OfflinePlayer target) {
        boolean isOnline = target.isOnline();

        if (!isOnline && !opener.getUniqueId().equals(target.getUniqueId())) {
            MessageUtil.sendMessage(opener, Main.get().getPrefixError(),
                    "Le joueur '" + target.getName() + "' est hors ligne, son profil est privé.");
            return;
        }

        FriendManager fm = Main.get().getFriendManager();
        Economy eco = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();

        UUID openerUUID = opener.getUniqueId();
        UUID targetUUID = target.getUniqueId();
        boolean isSelf = openerUUID.equals(targetUUID);
        boolean isFriend = !isSelf && fm.areFriends(openerUUID, targetUUID);

        StringBuilder title = new StringBuilder(GUI_PREFIX);
        if (isSelf) {
            title.append(":offset_-150::friend_disable:");
        } else if (isFriend) {
            title.append(":offset_-150::friend_remove:");
        } else {
            title.append(":offset_-150::friend_add:");
        }
        boolean isAdmin = isOnline && target.getPlayer().hasPermission("admin");
        title.append(":offset_-28:").append(isAdmin ? ":tag_admin:" : ":tag_player:");

        Inventory inv = Bukkit.createInventory(null, 36, title.toString());

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta sm = (SkullMeta) head.getItemMeta();
        if (sm != null) {
            sm.setOwningPlayer(target);
            sm.setDisplayName(ChatColor.GRAY + target.getName());
            sm.setCustomModelData(100);
            head.setItemMeta(sm);
        }
        inv.setItem(HEAD_SLOT, head);

        // Économie slot 23
        ItemStack ecoItem = new ItemStack(Material.PAPER);
        ItemMeta ecoMeta = ecoItem.getItemMeta();
        if (ecoMeta != null) {
            double balance = eco.getBalance(target);
            ecoMeta.setDisplayName(ChatColor.YELLOW + String.format("%.2f", balance) + ChatColor.WHITE + " \uE0BA");
            ecoMeta.setCustomModelData(10077);
            ecoItem.setItemMeta(ecoMeta);
        }
        inv.setItem(23, ecoItem);

        // Temps de jeu slot 24
        ItemStack timeItem = new ItemStack(Material.PAPER);
        ItemMeta timeMeta = timeItem.getItemMeta();
        if (timeMeta != null) {
            String playtime = "hors ligne";
            if (isOnline) {
                int ticks = target.getPlayer().getStatistic(Statistic.PLAY_ONE_MINUTE);
                int minutes = ticks / 1200;
                playtime = (minutes / 60) + "h " + (minutes % 60) + "m";
            }
            timeMeta.setDisplayName(ChatColor.GREEN + playtime);
            timeMeta.setCustomModelData(10077);
            timeItem.setItemMeta(timeMeta);
        }
        inv.setItem(24, timeItem);

        if (!isOnline) {
            MessageUtil.sendMessage(opener, Main.get().getPrefixError(),
                    "Le profil de " + target.getName() + " est privé (hors ligne).");
            return;
        }

        Player online = target.getPlayer();
        setWorn(inv, online.getInventory().getHelmet(), WORN_HELMET, "Casque");
        setWorn(inv, online.getInventory().getChestplate(), WORN_CHEST, "Plastron");
        setWorn(inv, online.getInventory().getLeggings(), WORN_LEGS, "Pantalon");
        setWorn(inv, online.getInventory().getBoots(), WORN_BOOTS, "Bottes");

        var user = HMCCosmeticsAPI.getUser(targetUUID);
        if (user != null) {
            setCos(inv, user.getCosmetic(CosmeticSlot.HELMET), WORN_HELMET + 1, "Casque Cosmétique");
            setCos(inv, user.getCosmetic(CosmeticSlot.BACKPACK), WORN_CHEST + 1, "Sac à Dos");
            setCos(inv, user.getCosmetic(CosmeticSlot.OFFHAND), WORN_LEGS + 1, "Offhand");
            setCos(inv, user.getCosmetic(CosmeticSlot.BALLOON), WORN_BOOTS + 1, "Ballon");
        }

        inv.setItem(BTN_HOME_1, makeButton("§7Se téléporter au home", 10077));
        inv.setItem(BTN_HOME_2, makeButton("§7Se téléporter au home", 10077));
        inv.setItem(BTN_INVITE, makeButton("§7Inviter dans un groupe", 10077));
        String friendLabel = isSelf ? "§7Ajouter en amie"
                : (isFriend ? "§7Supprimer l'ami" : "§7Ajouter en amie");
        inv.setItem(BTN_FRIEND, makeButton(friendLabel, 10077));
        inv.setItem(BTN_DUEL, makeButton("§7Proposer un duel", 10077));
        inv.setItem(BTN_TRADE, makeButton("§7Proposer un échange", 10077));
        inv.setItem(BTN_VAULT, makeButton("§7Coffre-fort", 10077));

        OPENED.put(openerUUID, targetUUID);
        opener.openInventory(inv);
    }

    private static ItemStack makeButton(String title, int modelData) {
        ItemStack it = new ItemStack(Material.PAPER);
        ItemMeta m = it.getItemMeta();
        if (m != null) {
            m.setDisplayName(title);
            m.setCustomModelData(modelData);
            it.setItemMeta(m);
        }
        return it;
    }

    private static void setWorn(Inventory inv, ItemStack item, int slot, String name) {
        if (item == null || item.getType().isAir()) return;
        ItemStack clone = item.clone();
        ItemMeta m = clone.getItemMeta();
        if (m != null) {
            m.setDisplayName(ChatColor.BLUE + name);
            clone.setItemMeta(m);
        }
        inv.setItem(slot, clone);
    }

    private static void setCos(Inventory inv, Cosmetic cos, int slot, String label) {
        if (cos == null) return;
        ItemStack item = cos.getItem().clone();
        ItemMeta m = item.getItemMeta();
        if (m != null) {
            m.setDisplayName("§a" + label + " : §e" + cos.getId());
            item.setItemMeta(m);
        }
        inv.setItem(slot, item);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent e) {
        Player clicker = (Player) e.getWhoClicked();
        if (!e.getView().getTitle().startsWith(GUI_PREFIX)) return;
        e.setCancelled(true);

        UUID target = OPENED.get(clicker.getUniqueId());
        if (target == null) return;

        FriendManager fm = Main.get().getFriendManager();
        HomesManager hm = Main.get().getHomesManager();

        int slot = e.getSlot();
        if (slot == BTN_HOME_1 || slot == BTN_HOME_2) {
            Location home = hm.getHome(target);
            if (home == null) {
                MessageUtil.sendMessage(clicker, Main.get().getPrefixError(),
                        "Ce joueur n’a pas de home défini.");
            } else {
                clicker.teleport(home);
                MessageUtil.sendMessage(clicker, Main.get().getPrefixInfo(),
                        "Téléporté au home de " + Bukkit.getOfflinePlayer(target).getName());
            }
        } else if (slot == BTN_INVITE) {
            clicker.performCommand("party invite " + Bukkit.getOfflinePlayer(target).getName());
        } else if (slot == BTN_FRIEND) {
            boolean already = fm.areFriends(clicker.getUniqueId(), target);
            if (!clicker.getUniqueId().equals(target) && !already) {
                clicker.performCommand("friend add " + Bukkit.getOfflinePlayer(target).getName());
            } else if (already) {
                clicker.performCommand("friend remove " + Bukkit.getOfflinePlayer(target).getName());
            }
        } else if (slot == BTN_DUEL || slot == BTN_TRADE || slot == BTN_VAULT) {
            MessageUtil.sendMessage(clicker, Main.get().getPrefixInfo(), "Fonctionnalité bientôt disponible.");
        }
    }
}
