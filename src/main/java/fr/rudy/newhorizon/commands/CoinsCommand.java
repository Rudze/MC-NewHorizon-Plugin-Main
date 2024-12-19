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

import java.math.BigDecimal;
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

            // Obtenez le solde en BigDecimal
            BigDecimal balance = economyManager.getMoney(player.getUniqueId());

            // Formatez le solde pour avoir deux chiffres après la virgule
            String formattedBalance = String.format("%.2f", balance.doubleValue());

            // Affichez le solde au joueur
            sender.sendMessage(ChatColor.GREEN + "Vous avez " + formattedBalance + " pièces.");
            return true;
        } else if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            if (!sender.hasPermission("economy.set")) {
                sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission.");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Joueur introuvable.");
                return true;
            }

            try {
                BigDecimal amount = new BigDecimal(args[2]);
                economyManager.setMoney(target.getUniqueId(), amount);
                sender.sendMessage(ChatColor.GREEN + "Argent mis à jour pour " + target.getName() + " : " + String.format("%.2f", amount.doubleValue()) + " pièces.");
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Montant invalide.");
            }
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Utilisation: /coins ou /coins set <joueur> <montant>");
        return false;
    }


}
