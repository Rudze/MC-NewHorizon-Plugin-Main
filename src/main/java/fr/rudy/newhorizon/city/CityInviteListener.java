package fr.rudy.newhorizon.city;

import fr.rudy.newhorizon.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;

public class CityInviteListener implements Listener {

    public static final Set<UUID> awaitingInviteInput = new HashSet<>();
    public static final Set<UUID> awaitingPromoteInput = new HashSet<>();
    public static final Set<UUID> awaitingDemoteInput = new HashSet<>();
    public static final Map<UUID, UUID> awaitingConfirmPromote = new HashMap<>();

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        UUID senderUUID = sender.getUniqueId();
        String input = ChatColor.stripColor(event.getMessage().trim());

        // 🔸 Invite
        if (awaitingInviteInput.remove(senderUUID)) {
            event.setCancelled(true);

            if (input.equalsIgnoreCase("quitter")) {
                sender.sendMessage("§cInvitation annulée.");
                return;
            }

            Player target = Bukkit.getPlayerExact(input);
            if (target == null || !target.isOnline()) {
                sender.sendMessage("§cJoueur introuvable ou hors ligne.");
                return;
            }

            UUID targetUUID = target.getUniqueId();
            String cityName = Main.get().getCityManager().getCityName(senderUUID);
            CityRank senderRank = Main.get().getCityManager().getCityRank(senderUUID);

            if (cityName == null || senderRank == null || !(senderRank == CityRank.LEADER || senderRank == CityRank.COLEADER)) {
                sender.sendMessage("§cVous n'avez pas la permission d'inviter.");
                return;
            }

            if (Main.get().getCityManager().getCityName(targetUUID) != null) {
                sender.sendMessage("§cCe joueur est déjà dans une ville.");
                return;
            }

            Main.get().getPendingInvites().put(targetUUID, cityName);
            target.sendMessage("§aVous avez été invité à rejoindre la ville §b" + cityName + "§a !");
            target.sendMessage("§7Faites §e/city accept §7pour accepter ou §c/city deny §7pour refuser.");
            sender.sendMessage("§aInvitation envoyée à §e" + target.getName() + "§a.");
            return;
        }

        // 🔸 Promote
        if (awaitingPromoteInput.remove(senderUUID)) {
            event.setCancelled(true);

            if (input.equalsIgnoreCase("quitter")) {
                sender.sendMessage("§cPromotion annulée.");
                return;
            }

            Player target = Bukkit.getPlayerExact(input);
            if (target == null || !target.isOnline()) {
                sender.sendMessage("§cJoueur introuvable ou hors ligne.");
                return;
            }

            UUID targetUUID = target.getUniqueId();
            String city = Main.get().getCityManager().getCityName(senderUUID);
            CityRank senderRank = Main.get().getCityManager().getCityRank(senderUUID);
            CityRank targetRank = Main.get().getCityManager().getCityRank(targetUUID);

            if (city == null || senderRank != CityRank.LEADER) {
                sender.sendMessage("§cSeul le chef peut promouvoir un joueur.");
                return;
            }

            if (!city.equals(Main.get().getCityManager().getCityName(targetUUID))) {
                sender.sendMessage("§cCe joueur n’est pas dans votre ville.");
                return;
            }

            if (targetRank == CityRank.COLEADER) {
                awaitingConfirmPromote.put(senderUUID, targetUUID);
                sender.sendMessage("§e⚠️ Vous êtes sur le point de transférer votre rôle de chef à §b" + target.getName() + "§e.");
                sender.sendMessage("§7Tapez §a/city confirm §7pour confirmer ou ignorez pour annuler.");
            } else {
                boolean success = Main.get().getCityManager().setMember(city, targetUUID, CityRank.COLEADER);
                if (success) {
                    sender.sendMessage("§a" + target.getName() + " est maintenant Sous-chef !");
                    target.sendMessage("§aVous avez été promu Sous-chef par §e" + sender.getName() + "§a !");
                } else {
                    sender.sendMessage("§cErreur lors de la promotion.");
                }
            }
            return;
        }

        // 🔸 Demote
        if (awaitingDemoteInput.remove(senderUUID)) {
            event.setCancelled(true);

            if (input.equalsIgnoreCase("quitter")) {
                sender.sendMessage("§cRétrogradation annulée.");
                return;
            }

            Player target = Bukkit.getPlayerExact(input);
            if (target == null || !target.isOnline()) {
                sender.sendMessage("§cJoueur introuvable ou hors ligne.");
                return;
            }

            UUID targetUUID = target.getUniqueId();
            String city = Main.get().getCityManager().getCityName(senderUUID);
            CityRank senderRank = Main.get().getCityManager().getCityRank(senderUUID);
            CityRank targetRank = Main.get().getCityManager().getCityRank(targetUUID);

            if (city == null || senderRank != CityRank.LEADER) {
                sender.sendMessage("§cSeul le chef peut rétrograder un joueur.");
                return;
            }

            if (!city.equals(Main.get().getCityManager().getCityName(targetUUID))) {
                sender.sendMessage("§cCe joueur n’est pas dans votre ville.");
                return;
            }

            if (targetUUID.equals(senderUUID)) {
                sender.sendMessage("§cVous ne pouvez pas vous rétrograder vous-même.");
                return;
            }

            if (targetRank == null || targetRank == CityRank.MEMBER) {
                sender.sendMessage("§cCe joueur ne peut pas être rétrogradé davantage.");
                return;
            }

            boolean success = Main.get().getCityManager().setMember(city, targetUUID, CityRank.MEMBER);
            if (success) {
                sender.sendMessage("§aLe joueur §e" + target.getName() + " §aa été rétrogradé au rang §cMembre§a.");
                target.sendMessage("§cVous avez été rétrogradé au rang §fMembre §cdans la ville §e" + city + "§c.");
            } else {
                sender.sendMessage("§cErreur lors de la rétrogradation.");
            }
        }
    }
}
