package fr.rudy.newhorizon.party;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class PartyMenu implements Listener {

    private final PartyManager partyManager;
    private final Main plugin = Main.get();

    public PartyMenu(PartyManager partyManager) {
        this.partyManager = partyManager;
        Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugin("NewHorizon"));
    }

    public void open(Player viewer) {
        Inventory inv = Bukkit.createInventory(null, 45, ":offset_-48::party:");
        List<Player> members = partyManager.getPartyMembers(viewer);

        // Slot 10 : retour vers /dmenu
        ItemStack back = new ItemStack(Material.PAPER);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§7Retour");
            backMeta.setCustomModelData(10077);
            back.setItemMeta(backMeta);
        }
        inv.setItem(10, back);

        // Têtes des membres à partir du slot 19
        int slot = 19;
        for (Player member : members) {
            if (slot >= 44) break;
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta sm = (SkullMeta) skull.getItemMeta();
            if (sm != null) {
                sm.setOwningPlayer(member);
                sm.setDisplayName("§f" + member.getName());
                skull.setItemMeta(sm);
            }
            inv.setItem(slot++, skull);
        }

        // Slot 15 : créer ou inviter
        ItemStack invite = new ItemStack(Material.PAPER);
        ItemMeta inviteMeta = invite.getItemMeta();
        if (inviteMeta != null) {
            inviteMeta.setDisplayName(members.isEmpty()
                    ? "§aCréer un groupe"
                    : "§aInviter un joueur");
            inviteMeta.setCustomModelData(10077);
            invite.setItemMeta(inviteMeta);
        }
        inv.setItem(15, invite);

        // Slot 16 : quitter le groupe (visible seulement si membre ou leader)
        if (!members.isEmpty() && (members.size() > 1 || partyManager.isLeader(viewer))) {
            ItemStack leave = new ItemStack(Material.PAPER);
            ItemMeta leaveMeta = leave.getItemMeta();
            if (leaveMeta != null) {
                leaveMeta.setDisplayName("§cQuitter le groupe");
                leaveMeta.setCustomModelData(10077);
                leave.setItemMeta(leaveMeta);
            }
            inv.setItem(16, leave);
        }

        viewer.openInventory(inv);
    }

    private void openInviteMenu(Player viewer) {
        if (!partyManager.isLeader(viewer)) {
            MessageUtil.sendMessage(viewer, plugin.getPrefixError(), "Seul le chef de groupe peut inviter.");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 45, ":offset_-48::party_invite:");

        // Slot 10 : retour vers le menu du groupe
        ItemStack back = new ItemStack(Material.PAPER);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§7Retour");
            backMeta.setCustomModelData(10077);
            back.setItemMeta(backMeta);
        }
        inv.setItem(10, back);

        // Têtes des joueurs en ligne à partir du slot 19
        int slot = 19;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (slot >= 44 || online.equals(viewer)) continue;
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta sm = (SkullMeta) skull.getItemMeta();
            if (sm != null) {
                sm.setOwningPlayer(online);
                sm.setDisplayName("§f" + online.getName());
                skull.setItemMeta(sm);
            }
            inv.setItem(slot++, skull);
        }

        viewer.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player viewer)) return;
        if (e.getClickedInventory() == null) return;

        String title = e.getView().getTitle();
        if (!title.equals(":offset_-48::party:")) return;
        if (!e.getInventory().equals(e.getClickedInventory())) return;

        ItemStack item = e.getCurrentItem();
        if (item == null || item.getItemMeta() == null) return;

        e.setCancelled(true);
        String name = item.getItemMeta().getDisplayName();

        if (name.contains("Retour")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "dmenu open menu_dungeons " + viewer.getName());
            viewer.closeInventory();
            return;
        }

        if (name.contains("Quitter")) {
            partyManager.leave(viewer);
            MessageUtil.sendMessage(viewer, plugin.getPrefixInfo(), "Tu as quitté ton groupe.");
            open(viewer);
            return;
        }

        if (item.getType() == Material.PAPER) {
            if (name.contains("Créer un groupe")) {
                boolean created = partyManager.createParty(viewer);
                if (created) {
                    MessageUtil.sendMessage(viewer, plugin.getPrefixInfo(), "Ton groupe a été créé.");
                } else {
                    MessageUtil.sendMessage(viewer, plugin.getPrefixError(), "Tu fais déjà partie d'un groupe.");
                }
                open(viewer);
            } else if (name.contains("Inviter un joueur")) {
                openInviteMenu(viewer);
            }
        }
    }

    @EventHandler
    public void onInviteClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player viewer)) return;
        if (e.getClickedInventory() == null) return;

        String title = e.getView().getTitle();
        if (!title.equals(":offset_-48::party_invite:")) return;
        if (!e.getInventory().equals(e.getClickedInventory())) return;

        ItemStack item = e.getCurrentItem();
        if (item == null || item.getItemMeta() == null) return;

        e.setCancelled(true);
        String name = item.getItemMeta().getDisplayName();

        if (name.contains("Retour")) {
            open(viewer);
            return;
        }

        if (!partyManager.isLeader(viewer)) {
            MessageUtil.sendMessage(viewer, plugin.getPrefixError(), "Seul le chef du groupe peut inviter.");
            viewer.closeInventory();
            return;
        }

        if (item.getType() == Material.PLAYER_HEAD) {
            String playerName = name.replace("§f", "");
            Player target = Bukkit.getPlayer(playerName);
            if (target != null) {
                partyManager.invite(viewer, target);
                MessageUtil.sendMessage(viewer, plugin.getPrefixInfo(), "Invitation envoyée à " + playerName + ".");
            }
            viewer.closeInventory();
        }
    }
}
