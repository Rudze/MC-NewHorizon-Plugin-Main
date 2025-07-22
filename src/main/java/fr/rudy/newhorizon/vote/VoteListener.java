/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerJoinEvent
 */
package fr.rudy.newhorizon.vote;

import fr.rudy.newhorizon.vote.VoteManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class VoteListener
implements Listener {
    private final VoteManager voteManager;

    public VoteListener(VoteManager voteManager) {
        this.voteManager = voteManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        int votes = this.voteManager.getPendingVotes(event.getPlayer().getUniqueId());
        if (votes > 0) {
            event.getPlayer().sendMessage("&f\ue01f&b \u00a7bTu as " + votes + "\u00a7b vote(s) en attente. Fais /vote pour les r\u00e9cup\u00e9rer.");
        }
    }
}

