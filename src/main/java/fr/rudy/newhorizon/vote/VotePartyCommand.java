/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 */
package fr.rudy.newhorizon.vote;

import fr.rudy.newhorizon.vote.VoteManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class VotePartyCommand
implements CommandExecutor {
    private final VoteManager voteManager;

    public VotePartyCommand(VoteManager voteManager) {
        this.voteManager = voteManager;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("newhorizon.vote.admin")) {
            return false;
        }
        this.voteManager.resetVoteParty();
        sender.sendMessage("Vote Party reset !");
        return true;
    }
}

