package fr.rudy.newhorizon.friend;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.UUID;

public class FriendCommand implements CommandExecutor {

    private final FriendManager friendManager;

    public FriendCommand(FriendManager friendManager) {
        this.friendManager = friendManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return false;

        if (args.length == 0) {
            new FriendMenu(friendManager).open(p);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "add" -> {
                if (args.length != 2) return sendHelp(p);
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

                if (target.getUniqueId().equals(p.getUniqueId())) {
                    p.sendMessage("§cTu ne peux pas t'ajouter toi-même !");
                    return true;
                }

                if (friendManager.areFriends(p.getUniqueId(), target.getUniqueId())) {
                    p.sendMessage("§cVous êtes déjà amis !");
                    return true;
                }

                if (friendManager.hasPendingRequest(p.getUniqueId(), target.getUniqueId())) {
                    p.sendMessage("§eDemande déjà envoyée à " + target.getName());
                    return true;
                }

                if (friendManager.sendRequest(p.getUniqueId(), target.getUniqueId())) {
                    p.sendMessage("§aDemande envoyée à " + target.getName());
                    if (target.isOnline()) {
                        Player t = target.getPlayer();
                        if (t != null) {
                            t.sendMessage("§e" + p.getName() + " t'a envoyé une demande d'ami !");
                            t.sendMessage("§7Utilise §a/friend accept " + p.getName() + "§7 pour accepter, ou §c/friend deny " + p.getName());
                        }
                    }
                } else {
                    p.sendMessage("§cErreur lors de l'envoi de la demande.");
                }
            }

            case "accept" -> {
                if (args.length != 2) return sendHelp(p);
                OfflinePlayer senderPlayer = Bukkit.getOfflinePlayer(args[1]);

                if (friendManager.acceptRequest(p.getUniqueId(), senderPlayer.getUniqueId())) {
                    p.sendMessage("§aTu es maintenant ami avec " + senderPlayer.getName());
                    if (senderPlayer.isOnline()) {
                        senderPlayer.getPlayer().sendMessage("§a" + p.getName() + " a accepté ta demande !");
                    }
                } else {
                    p.sendMessage("§cAucune demande trouvée de " + senderPlayer.getName());
                }
            }

            case "deny" -> {
                if (args.length != 2) return sendHelp(p);
                OfflinePlayer senderPlayer = Bukkit.getOfflinePlayer(args[1]);

                if (friendManager.denyRequest(p.getUniqueId(), senderPlayer.getUniqueId())) {
                    p.sendMessage("§cTu as refusé la demande de " + senderPlayer.getName());
                } else {
                    p.sendMessage("§cAucune demande trouvée de " + senderPlayer.getName());
                }
            }

            case "pending" -> {
                List<UUID> requests = friendManager.getPendingRequests(p.getUniqueId());
                if (requests.isEmpty()) {
                    p.sendMessage("§eAucune demande en attente.");
                } else {
                    p.sendMessage("§aDemandes en attente:");
                    for (UUID id : requests) {
                        OfflinePlayer op = Bukkit.getOfflinePlayer(id);
                        p.sendMessage("- " + op.getName());
                    }
                }
            }

            case "list" -> {
                List<UUID> friends = friendManager.getFriends(p.getUniqueId());
                if (friends.isEmpty()) {
                    p.sendMessage("§eTu n'as pas encore d'amis 😢");
                } else {
                    p.sendMessage("§aTes amis:");
                    for (UUID id : friends) {
                        OfflinePlayer op = Bukkit.getOfflinePlayer(id);
                        p.sendMessage("- " + op.getName());
                    }
                }
            }

            case "remove" -> {
                if (args.length != 2) return sendHelp(p);
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

                if (friendManager.removeFriend(p.getUniqueId(), target.getUniqueId())) {
                    p.sendMessage("§a" + target.getName() + " a été retiré de tes amis.");
                } else {
                    p.sendMessage("§cVous n'étiez pas amis.");
                }
            }

            default -> sendHelp(p);
        }

        return true;
    }

    private boolean sendHelp(Player p) {
        p.sendMessage("§eUtilisation:");
        p.sendMessage("§7/friend add <pseudo>");
        p.sendMessage("§7/friend accept <pseudo>");
        p.sendMessage("§7/friend deny <pseudo>");
        p.sendMessage("§7/friend pending");
        p.sendMessage("§7/friend list");
        p.sendMessage("§7/friend remove <pseudo>");
        return true;
    }
}
