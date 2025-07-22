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
package fr.rudy.newhorizon.commands;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.dialogue.DialogueManager;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DialogueCommand
        implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("reset")) {
            if (args.length < 2) {
                sender.sendMessage("\u00a7cUsage: /dialogue reset <playerName> [npc_name]");
                return true;
            }
            String playerName = args[1];
            Player targetPlayer = Bukkit.getPlayerExact((String)playerName);
            if (targetPlayer == null || !targetPlayer.isOnline()) {
                sender.sendMessage("\u00a7cLe joueur " + playerName + " n'est pas en ligne.");
                return true;
            }
            UUID targetUuid = targetPlayer.getUniqueId();
            if (args.length == 3) {
                String npcName = args[2];
                Main.get().getDialogueProgressManager().setPlayerDialogueStep(targetUuid, npcName, 0);
                sender.sendMessage("\u00a7aProgr\u00e8s du dialogue pour le PNJ " + npcName + " de " + playerName + " r\u00e9initialis\u00e9 \u00e0 l'\u00e9tape 0.");
            } else {
                Main.get().getDialogueProgressManager().deleteAllPlayerDialogues(targetUuid);
                sender.sendMessage("\u00a7aTous les progr\u00e8s de dialogue pour " + playerName + " ont \u00e9t\u00e9 r\u00e9initialis\u00e9s.");
            }
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage("\u00a7cUsage: /dialogue <playerName> <npc> <step> [true/false for skip]");
            return false;
        }
        String playerName = args[0];
        String npc = args[1];
        String step = args[2];
        boolean skip = args.length >= 4 && args[3].equalsIgnoreCase("true");
        Player target = Bukkit.getPlayerExact((String)playerName);
        if (target == null || !target.isOnline()) {
            sender.sendMessage("\u00a7cLe joueur " + playerName + " n'est pas en ligne.");
            return true;
        }
        DialogueManager.startDialogue(target, npc, step, skip);
        return true;
    }
}

