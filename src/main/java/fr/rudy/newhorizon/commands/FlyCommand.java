package fr.rudy.newhorizon.commands;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlyCommand implements CommandExecutor {

    private final Main plugin = Main.get();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (!player.hasPermission("vip")) {
            MessageUtil.sendMessage(player, plugin.getPrefixError(), "§cTu n'as pas la permission pour utiliser cette commande.");
            return true;
        }

        boolean fly = !player.getAllowFlight();
        player.setAllowFlight(fly);
        player.setFlying(fly);
        MessageUtil.sendMessage(player, plugin.getPrefixInfo(), "Mode vol " + (fly ? "§dactivé" : "§ddésactivé") + "§b !");
        return true;
    }
}
