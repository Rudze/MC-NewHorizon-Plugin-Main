package fr.rudy.newhorizon.commands;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.level.LevelsManager;
import fr.rudy.newhorizon.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class LevelCommand implements CommandExecutor {
    private final LevelsManager levelsManager;

    public LevelCommand() {
        levelsManager = Main.get().getLevelsManager();
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, Main.get().getPrefixError(), "Seuls les joueurs peuvent utiliser cette commande !");
            return true;
        }

        // Si aucune sous-commande n'est spécifiée, afficher les informations du joueur
        if (args.length == 0) {
            final UUID player = ((Player) sender).getUniqueId();
            MessageUtil.sendMessage(sender, Main.get().getPrefixInfo(), "Votre niveau : " + levelsManager.getLevel(player));
            MessageUtil.sendMessage(sender, Main.get().getPrefixInfo(), "Votre expérience actuelle : " + levelsManager.getExp(player));
            MessageUtil.sendMessage(sender, Main.get().getPrefixInfo(), "Expérience prochain level : " + levelsManager.expToNextLevel(player));
            return true;
        }

        // Gérer la sous-commande set
        if (args.length > 1 && args[0].equalsIgnoreCase("set")) {
            // Vérifier les permissions
            if (!sender.isOp()) {
                MessageUtil.sendMessage(sender, Main.get().getPrefixError(), "Vous n'avez pas la permission d'utiliser cette commande.");
                return true;
            }

            final Player player = Bukkit.getPlayer(args.length > 2 ? args[2] : sender.getName());
            if (player == null) {
                //TODO: Message
                MessageUtil.sendMessage(sender, Main.get().getPrefixError(), "");

                return true;
            }

            try {
                levelsManager.setExp(player.getUniqueId(), Integer.parseInt(args[1]));
                //TODO: Message
                MessageUtil.sendMessage(sender, Main.get().getPrefixError(), "");
            } catch (NumberFormatException exception) {
                //TODO: Message + Stacktrace
                MessageUtil.sendMessage(sender, Main.get().getPrefixError(), "");

            }

            return true;
        }

        // Si la sous-commande est invalide
        sender.sendMessage("Commande invalide. Utilisez /level ou /level reset <exp|lvl> <player>.");
        return false;
    }
}
