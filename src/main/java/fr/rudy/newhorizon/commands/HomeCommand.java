package fr.rudy.newhorizon.commands;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.home.HomesManager;
import fr.rudy.newhorizon.utils.MessageUtil;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class HomeCommand implements CommandExecutor {
    private final HomesManager homesManager;

    public HomeCommand() {
        homesManager = Main.get().getHomesManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, Main.get().getPrefixError(), "Seuls les joueurs peuvent utiliser cette commande.");
            return true;
        }

        final Player player = (Player) sender;
        final UUID playerUuid = player.getUniqueId();

        if (label.equalsIgnoreCase("home")) {
            // Téléporter au home
            final Location home = homesManager.getHome(playerUuid);
            if (home == null) {
                MessageUtil.sendMessage(player, Main.get().getPrefixError(), "Vous n'avez pas encore défini de home. Utilisez /sethome pour en définir un.");
                return true;
            }

            player.teleport(home);
            MessageUtil.sendMessage(player, Main.get().getPrefixInfo(), "Téléporté à votre home !");
            return true;
        }

        if (label.equalsIgnoreCase("sethome")) {
            // Définir le home
            homesManager.setHome(playerUuid, player.getLocation());
            MessageUtil.sendMessage(player, Main.get().getPrefixInfo(), "Votre home a été défini avec succès !");
            return true;
        }

        return false;
    }
}
