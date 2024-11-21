package fr.rudy.newhorizon.commands;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.utils.DatabaseManager;
import fr.rudy.newhorizon.utils.MessageUtil;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class HomeCommand implements CommandExecutor {

    private final DatabaseManager databaseManager;
    private final Main plugin;

    public HomeCommand(DatabaseManager databaseManager, Main plugin) {
        this.databaseManager = databaseManager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, plugin.getPrefixError(), "Seuls les joueurs peuvent utiliser cette commande.");
            return true;
        }

        Player player = (Player) sender;
        UUID playerUuid = player.getUniqueId();

        if (label.equalsIgnoreCase("sethome")) {
            // Définir le home
            Location location = player.getLocation();
            databaseManager.saveHome(playerUuid, location);
            MessageUtil.sendMessage(player, plugin.getPrefixInfo(), "§aVotre home a été défini avec succès !");
            return true;
        }

        if (label.equalsIgnoreCase("home")) {
            // Téléporter au home
            Location home = databaseManager.getHome(playerUuid);
            if (home == null) {
                MessageUtil.sendMessage(player, plugin.getPrefixError(), "§cVous n'avez pas encore défini de home. Utilisez /sethome pour en définir un.");
                return true;
            }

            // Vérifier si le monde est valide
            if (home.getWorld() == null) {
                MessageUtil.sendMessage(player, plugin.getPrefixError(), "§cLe monde de votre home est introuvable ou non chargé.");
                return true;
            }

            player.teleport(home);
            MessageUtil.sendMessage(player, plugin.getPrefixInfo(), "§aTéléporté à votre home !");
            return true;
        }

        return false;
    }
}
