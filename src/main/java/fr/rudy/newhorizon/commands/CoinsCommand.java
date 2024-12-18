package fr.rudy.newhorizon.commands;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.economy.EconomyManager;
import fr.rudy.newhorizon.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CoinsCommand implements CommandExecutor {

    Main plugin = Main.get();
    private final EconomyManager economyManager;

    public CoinsCommand(EconomyManager economyManager) {
        this.economyManager = economyManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 && sender instanceof Player) {
            Player player = (Player) sender;
            int balance = economyManager.getMoney(player.getUniqueId());
            MessageUtil.broadcastMessage(plugin.getPrefixInfo(), "Vous avez " + balance + " pièces.");
            return true;
        } else if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            if (!sender.hasPermission("economy.set")) {
                MessageUtil.broadcastMessage(plugin.getPrefixError(), "Vous n'avez pas la permission.");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                MessageUtil.broadcastMessage(plugin.getPrefixError(), "Joueur introuvable.");
                return true;
            }

            try {
                int amount = Integer.parseInt(args[2]);
                economyManager.setMoney(target.getUniqueId(), amount);
                MessageUtil.broadcastMessage(plugin.getPrefixInfo(), "Argent mis à jour pour " + target.getName());
                MessageUtil.broadcastMessage(plugin.getPrefixInfo(), "Votre solde a été mis à jour : " + amount + " pièces.");
            } catch (NumberFormatException e) {
                MessageUtil.broadcastMessage(plugin.getPrefixError(), "Montant invalide.");
            }
            return true;
        }

        MessageUtil.broadcastMessage(plugin.getPrefixError(), "Utilisation: /coins ou /coins set <joueur> <montant>");
        return false;
    }
}
