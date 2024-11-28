package fr.rudy.newhorizon.commands;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.utils.MessageUtil;
import fr.rudy.newhorizon.warp.Warp;
import fr.rudy.newhorizon.warp.WarpManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarpCommand implements CommandExecutor {

    private final WarpManager warpManager;

    public WarpCommand(WarpManager warpManager) {
        this.warpManager = warpManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp() || !sender.hasPermission("newhorizon.warp")) {
            MessageUtil.sendMessage(sender, Main.get().getPrefixError(),"Vous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }

        if (args.length != 2) {
            return true;
        }

        String warpName = args[0];
        String playerName = args[1];

        if (!warpManager.warpExists(warpName)) {
            MessageUtil.sendMessage(sender, Main.get().getPrefixError(),"Le warp '" + warpName + "' n'existe pas.");
            return true;
        }

        Player player = Bukkit.getPlayer(playerName);

        if (player == null) {
            MessageUtil.sendMessage(sender, Main.get().getPrefixError(),"Le joueur '" + playerName + "' est introuvable.");
            return true;
        }

        Warp warp = warpManager.getWarp(warpName);
        player.teleport(warp.getLocation());
        MessageUtil.sendMessage(sender, Main.get().getPrefixError(),"Le joueur " + player.getName() + " a été téléporté au warp '" + warpName + "'.");
        return true;
    }
}
