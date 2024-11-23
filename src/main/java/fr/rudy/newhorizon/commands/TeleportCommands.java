package fr.rudy.newhorizon.commands;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.teleport.TPModule;
import fr.rudy.newhorizon.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeleportCommands implements CommandExecutor {

    private final TPModule tpModule;

    public TeleportCommands(TPModule tpModule) {
        this.tpModule = tpModule;
    }

    Main plugin = Main.get();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendMessage(sender, plugin.getPrefixError(), "Cette commande doit être exécutée par un joueur.");
            return true;
        }

        switch (label.toLowerCase()) {
            case "tpa":
                if (args.length < 1) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(), "Utilisation: /tpa <joueur>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(), "Ce joueur n'est pas connecté.");
                    return true;
                }
                if (!tpModule.isRequestsEnabled(target)) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(), "Ce joueur n'accepte pas de demandes de téléportation.");
                    return true;
                }
                tpModule.sendRequest(player, target);
                return true;

            case "tpaccept":
                tpModule.acceptRequest(player);
                return true;

            case "tpdeny":
                tpModule.denyRequest(player);
                return true;

            case "tptoggle":
                tpModule.toggleRequests(player);
                return true;

            default:
                return false;
        }
    }
}
