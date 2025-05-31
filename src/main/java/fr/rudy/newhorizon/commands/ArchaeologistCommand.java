package fr.rudy.newhorizon.commands;

import fr.rudy.newhorizon.archaeology.ArchaeologistGUI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArchaeologistCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2 || !args[0].equalsIgnoreCase("open")) {
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            return true;
        }

        target.openInventory(ArchaeologistGUI.createGUI());
        return true;
    }
}
