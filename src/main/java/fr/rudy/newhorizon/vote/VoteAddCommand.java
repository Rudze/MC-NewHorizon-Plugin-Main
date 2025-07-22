/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 */
package fr.rudy.newhorizon.vote;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.vote.VoteManager;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class VoteAddCommand
implements CommandExecutor {
    private final Main plugin;
    private final VoteManager voteManager;

    public VoteAddCommand(Main plugin, VoteManager voteManager) {
        this.plugin = plugin;
        this.voteManager = voteManager;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("newhorizon.vote.admin")) {
            return false;
        }
        if (args.length != 1) {
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer((String)args[0]);
        UUID uuid = target.getUniqueId();
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage("\u274c Le joueur n'a jamais jou\u00e9 sur le serveur.");
            return true;
        }
        this.voteManager.addVote(uuid);
        sender.sendMessage("\u2705 Vote ajout\u00e9 pour " + target.getName() + " (UUID: " + String.valueOf(uuid) + ")");
        return true;
    }
}

