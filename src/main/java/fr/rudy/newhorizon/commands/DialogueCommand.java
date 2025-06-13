package fr.rudy.newhorizon.commands;

import fr.rudy.newhorizon.dialogue.DialogueManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DialogueCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length < 3) {
            //.sendMessage("§cUsage: /dialogue <joueur> <npc> <étape>");
            return false;
        }

        String playerName = args[0];
        String npc = args[1];
        String step = args[2];

        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null || !target.isOnline()) {
            //sender.sendMessage("§cLe joueur '" + playerName + "' n'est pas en ligne.");
            return true;
        }

        DialogueManager.startDialogue(target, npc, step);
        return true;
    }
}
