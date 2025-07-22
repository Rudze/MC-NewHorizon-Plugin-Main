/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.md_5.bungee.api.ChatColor
 *  net.md_5.bungee.api.chat.BaseComponent
 *  net.md_5.bungee.api.chat.ClickEvent
 *  net.md_5.bungee.api.chat.ClickEvent$Action
 *  net.md_5.bungee.api.chat.TextComponent
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package fr.rudy.newhorizon.vote;

import fr.rudy.newhorizon.Main;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VoteLinkCommand
implements CommandExecutor {
    private static final String VOTE_URL = "https://newhorizon.galaxynetwork.fr/vote";
    private final Main plugin = Main.get();

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player)sender;
        TextComponent link = new TextComponent("\u00a7f\ue01f\u00a7b Clique ici pour voter !");
        link.setColor(ChatColor.AQUA);
        link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, VOTE_URL));
        player.spigot().sendMessage((BaseComponent)link);
        return true;
    }
}

