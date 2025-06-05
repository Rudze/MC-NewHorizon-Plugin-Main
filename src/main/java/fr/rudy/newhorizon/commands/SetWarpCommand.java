package fr.rudy.newhorizon.commands;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.utils.MessageUtil;
import fr.rudy.newhorizon.warp.Warp;
import fr.rudy.newhorizon.warp.WarpManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Location;

public class SetWarpCommand implements CommandExecutor {

    private final WarpManager warpManager;

    public SetWarpCommand(WarpManager warpManager) {
        this.warpManager = warpManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, Main.get().getPrefixError(), "Cette commande doit être exécutée par un joueur.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("newhorizon.setwarp")) {
            MessageUtil.sendMessage(player, Main.get().getPrefixError(), "Vous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }

        if (args.length != 1) {
            MessageUtil.sendMessage(player, Main.get().getPrefixError(), "Usage : /setwarp <nom>");
            return true;
        }

        String warpName = args[0].toLowerCase();
        Location location = player.getLocation();

        Warp warp = new Warp(warpName, location);
        warpManager.saveWarp(warp);
        warpManager.getWarps().put(warpName, warp);

        MessageUtil.sendMessage(player, Main.get().getPrefixInfo(), "Le warp '" + warpName + "' a été défini avec succès.");
        return true;
    }
}
