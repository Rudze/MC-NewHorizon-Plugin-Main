package fr.rudy.newhorizon.commands;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.spawn.CoreSpawnManager;
import fr.rudy.newhorizon.utils.MessageUtil;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class CommandSpawn implements CommandExecutor {

    private final CoreSpawnManager spawnManager = Main.get().getCoreSpawnManager();

    Main plugin = Main.get();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        Location loc = player.getLocation();
        spawnManager.setSpawn(loc);
        MessageUtil.sendMessage(sender, plugin.getPrefixInfo(),"Spawn du serveur défini !");
        return true;
    }
}
