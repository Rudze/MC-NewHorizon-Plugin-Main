package fr.rudy.newhorizon.commands;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class LevelCommand implements CommandExecutor {

    private final HashMap<UUID, Integer> playerLevels;
    private final HashMap<UUID, Integer> playerExp;
    private final Main plugin; // Référence au plugin principal

    public LevelCommand(Main plugin, HashMap<UUID, Integer> playerLevels, HashMap<UUID, Integer> playerExp) {
        this.plugin = plugin;
        this.playerLevels = playerLevels;
        this.playerExp = playerExp;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Si aucune sous-commande n'est spécifiée, afficher les informations du joueur
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                MessageUtil.sendMessage(sender, plugin.getPrefixError(), "Seuls les joueurs peuvent utiliser cette commande !");
                return true;
            }

            Player player = (Player) sender;
            UUID uuid = player.getUniqueId();

            int level = playerLevels.getOrDefault(uuid, 1);
            int exp = playerExp.getOrDefault(uuid, 0);

            MessageUtil.sendMessage(sender, plugin.getPrefixInfo(), "Votre niveau : " + level);
            MessageUtil.sendMessage(sender, plugin.getPrefixInfo(), "Votre expérience actuelle : " + exp);
            return true;
        }

        // Gérer la sous-commande reset
        if (args[0].equalsIgnoreCase("reset")) {
            // Vérifier les permissions
            if (!sender.isOp()) {
                MessageUtil.sendMessage(sender, plugin.getPrefixError(), "§cVous n'avez pas la permission d'utiliser cette commande.");
                return true;
            }

            // Vérifier les arguments
            if (args.length != 3) {
                sender.sendMessage("§eUsage: /level reset <exp|lvl> <player>");
                return true;
            }

            String resetType = args[1].toLowerCase();
            String playerName = args[2];
            Player targetPlayer = Bukkit.getPlayer(playerName);

            // Vérifie si le joueur existe et est en ligne
            if (targetPlayer == null) {
                sender.sendMessage("§cLe joueur " + playerName + " n'est pas en ligne.");
                return true;
            }

            UUID targetUUID = targetPlayer.getUniqueId();

            // Effectuer le reset selon le type (exp ou lvl)
            if (resetType.equals("exp")) {
                playerExp.put(targetUUID, 0);
                sender.sendMessage("§aVous avez réinitialisé l'expérience de " + playerName + ".");
                targetPlayer.sendMessage("§eVotre expérience a été réinitialisée par un administrateur.");
            } else if (resetType.equals("lvl")) {
                playerLevels.put(targetUUID, 1); // Remettre au niveau 1
                sender.sendMessage("§aVous avez réinitialisé le niveau de " + playerName + ".");
                targetPlayer.sendMessage("§eVotre niveau a été réinitialisé par un administrateur.");
            } else {
                sender.sendMessage("§cType de reset invalide. Utilisez 'exp' ou 'lvl'.");
            }

            return true;
        }

        // Si la sous-commande est invalide
        sender.sendMessage("§cCommande invalide. Utilisez /level ou /level reset <exp|lvl> <player>.");
        return true;
    }
}
