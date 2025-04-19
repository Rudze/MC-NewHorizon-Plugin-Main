package fr.rudy.newhorizon.commands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WikiCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cCommande réservée aux joueurs uniquement.");
            return true;
        }

        TextComponent message = new TextComponent("§f§b Clique ici pour ouvrir le wiki");
        message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://mc.galaxynetwork.fr/wiki"));

        player.spigot().sendMessage(message);
        return true;
    }
}
