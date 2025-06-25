package fr.rudy.newhorizon.profile;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ProfileCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Seuls les joueurs peuvent utiliser cette commande.");
            return true;
        }

        Player player = (Player) sender;
        OfflinePlayer targetPlayer;

        if (args.length == 0) {
            // Pas de pseudo, ouvre le profil du joueur lui-même
            targetPlayer = player;
        } else {
            // Pseudo fourni, essaie de trouver le joueur
            String targetName = args[0];
            targetPlayer = Bukkit.getOfflinePlayer(targetName);

            // Vérifier si le joueur existe ou a déjà joué sur le serveur
            if (targetPlayer == null || (!targetPlayer.hasPlayedBefore() && !targetPlayer.isOnline())) {
                player.sendMessage(ChatColor.RED + "Le joueur '" + targetName + "' n'a pas été trouvé ou n'a jamais joué sur ce serveur.");
                return true;
            }
        }

        ProfileManager.openProfileMenu(player, targetPlayer);
        return true;
    }
}
