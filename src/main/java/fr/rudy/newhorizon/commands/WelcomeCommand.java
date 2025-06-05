package fr.rudy.newhorizon.commands;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.chat.WelcomeManager;
import fr.rudy.newhorizon.utils.MessageUtil;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class WelcomeCommand implements CommandExecutor {

    private final Main plugin;
    private final WelcomeManager welcomeManager;
    private final Economy economy;

    public WelcomeCommand(Main plugin, WelcomeManager welcomeManager) {
        this.plugin = plugin;
        this.welcomeManager = welcomeManager;
        this.economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        UUID newPlayerUUID = welcomeManager.getLastNewPlayer();
        Player newPlayer = newPlayerUUID != null ? Bukkit.getPlayer(newPlayerUUID) : null;

        if (newPlayer == null) {
            MessageUtil.sendMessage(player, plugin.getPrefixError(), "Aucun nouveau joueur actuellement.");
            return true;
        }

        if (!welcomeManager.isWithinWindow(5 * 60 * 1000)) {
            MessageUtil.sendMessage(player, plugin.getPrefixError(), "Le délai pour souhaiter la bienvenue est expiré !");
            welcomeManager.clear();
            return true;
        }

        if (newPlayer.getUniqueId().equals(player.getUniqueId())) {
            return true;
        }

        economy.depositPlayer(player, 50.0);

        player.chat("&fBienvenue &d" + newPlayer.getName() + "&f !");
        welcomeManager.clear();
        return true;
    }
}
