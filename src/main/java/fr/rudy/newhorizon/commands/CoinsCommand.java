package fr.rudy.newhorizon.commands;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.economy.EconomyManager;
import fr.rudy.newhorizon.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
            double balance = economyManager.getMoney(player.getUniqueId());
            String formatted = String.format("%.2f", balance);
            MessageUtil.sendMessage(player, Main.get().getPrefixInfo(), "Vous avez " + formatted + " pièces.");
            return true;

        } else if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            if (!sender.hasPermission("economy.set")) {
                sender.sendMessage("§cVous n'avez pas la permission.");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cJoueur introuvable.");
                return true;
            }

            try {
                int amount = Integer.parseInt(args[2]);
                economyManager.setMoney(target.getUniqueId(), amount);
                sender.sendMessage("§aArgent mis à jour pour " + target.getName() + " : " + amount + " pièces.");
                target.sendMessage("§aVotre solde a été mis à jour : " + amount + " pièces.");
            } catch (NumberFormatException e) {
                sender.sendMessage("§cMontant invalide.");
            }
            return true;

        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("economy.give")) {
                sender.sendMessage("§cVous n'avez pas la permission.");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cJoueur introuvable.");
                return true;
            }

            try {
                int amount = Integer.parseInt(args[2]);
                economyManager.addMoney(target.getUniqueId(), amount);
                sender.sendMessage("§aVous avez donné " + amount + " pièces à " + target.getName() + ".");
                target.sendMessage("§aVous avez reçu " + amount + " pièces !");
            } catch (NumberFormatException e) {
                sender.sendMessage("§cMontant invalide.");
            }
            return true;
        }

        return false;
    }
}
