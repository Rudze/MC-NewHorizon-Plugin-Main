package fr.rudy.newhorizon.commands;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.utils.MessageUtil;
import fr.rudy.newhorizon.warp.Warp;
import fr.rudy.newhorizon.warp.WarpManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
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
        if (args.length == 1 && sender instanceof Player) {
            Player player = (Player) sender;

            if (!player.hasPermission("newhorizon.warp")) {
                MessageUtil.sendMessage(player, Main.get().getPrefixError(), "Vous n'avez pas la permission d'utiliser cette commande.");
                return true;
            }

            String warpName = args[0].toLowerCase();
            if (!warpManager.warpExists(warpName)) {
                MessageUtil.sendMessage(player, Main.get().getPrefixError(), "Le warp '" + warpName + "' n'existe pas.");
                return true;
            }

            Warp warp = warpManager.getWarp(warpName);
            World.Environment env = warp.getLocation().getWorld().getEnvironment();

            if (env == World.Environment.NETHER && !player.hasPermission("lvl.20")) {
                MessageUtil.sendMessage(player, Main.get().getPrefixError(), "§cVous devez être niveau §e20 §cpour accéder au Nether.");
                return true;
            } else if (env == World.Environment.THE_END && !player.hasPermission("lvl.40")) {
                MessageUtil.sendMessage(player, Main.get().getPrefixError(), "§cVous devez être niveau §e40 §cpour accéder à l'End.");
                return true;
            }

            player.teleport(warp.getLocation());
            MessageUtil.sendMessage(player, Main.get().getPrefixInfo(), "Téléporté au warp '" + warpName + "'.");
            return true;
        }

        if (args.length == 2) {
            String warpName = args[0].toLowerCase();
            String playerName = args[1];

            if (!warpManager.warpExists(warpName)) {
                MessageUtil.sendMessage(sender, Main.get().getPrefixError(), "Le warp '" + warpName + "' n'existe pas.");
                return true;
            }

            Player target = Bukkit.getPlayerExact(playerName);
            if (target == null) {
                MessageUtil.sendMessage(sender, Main.get().getPrefixError(), "Le joueur '" + playerName + "' est introuvable.");
                return true;
            }

            Warp warp = warpManager.getWarp(warpName);
            World.Environment env = warp.getLocation().getWorld().getEnvironment();

            if (env == World.Environment.NETHER && !target.hasPermission("lvl.20")) {
                MessageUtil.sendMessage(target, Main.get().getPrefixError(), "Vous devez être niveau 20 §cpour accéder au Nether.");
                return true;
            } else if (env == World.Environment.THE_END && !target.hasPermission("lvl.40")) {
                MessageUtil.sendMessage(target, Main.get().getPrefixError(), "Vous devez être niveau 40 §cpour accéder à l'End.");
                return true;
            }

            target.teleport(warp.getLocation());
            MessageUtil.sendMessage(sender, Main.get().getPrefixInfo(), "Le joueur " + target.getName() + " a été téléporté au warp '" + warpName + "'.");
            return true;
        }

        MessageUtil.sendMessage(sender, Main.get().getPrefixError(), "Usage : /warp <nom> [joueur]");
        return true;
    }
}
