package fr.rudy.newhorizon.chat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandTabCompleter implements TabCompleter {

    private static final List<String> PUBLIC_COMMANDS = Arrays.asList("wiki", "discord");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }

        Player player = (Player) sender;

        // Si c'est une commande publique, on autorise tout le monde
        if (PUBLIC_COMMANDS.contains(command.getName().toLowerCase())) {
            return Collections.emptyList(); // laisser Spigot gérer le tab complet normalement
        }

        // Sinon, on vérifie si le joueur a la permission admin
        if (player.hasPermission("newhorizon.admin")) {
            return null; // laisser la complétion normale pour les admins
        }

        // Pas d'accès à la complétion sinon
        return Collections.emptyList();
    }
}
