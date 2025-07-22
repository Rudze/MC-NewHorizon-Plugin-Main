/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package fr.rudy.newhorizon.vote;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.utils.MessageUtil;
import fr.rudy.newhorizon.vote.VoteManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VoteCommand
implements CommandExecutor {
    private final Main plugin;
    private final VoteManager voteManager;

    public VoteCommand(Main plugin, VoteManager voteManager) {
        this.plugin = plugin;
        this.voteManager = voteManager;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player)sender;
        int votes = this.voteManager.getPendingVotes(player.getUniqueId());
        if (votes > 0) {
            for (int i = 0; i < votes; ++i) {
                Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), (String)("iagive " + player.getName() + " newhorizon:common_key 1"));
            }
            this.voteManager.clearVotes(player.getUniqueId());
            MessageUtil.sendMessage(sender, this.plugin.getPrefixInfo(), "Tu as re\u00e7u " + votes + " cl\u00e9(s) de vote.");
        } else {
            MessageUtil.sendMessage(sender, this.plugin.getPrefixInfo(), "Tu n'as aucun vote en attente.");
        }
        return true;
    }
}

