package fr.rudy.newhorizon.commands;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.spawn.CoreSpawnManager;
import fr.rudy.newhorizon.utils.MessageUtil;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnTeleportCommand implements CommandExecutor {

    private final CoreSpawnManager spawnManager = Main.get().getCoreSpawnManager();

    Main plugin = Main.get();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            //sender.sendMessage("§cSeuls les joueurs peuvent utiliser cette commande !");
            return true;
        }

        Location spawn = spawnManager.getGlobalSpawn();
        if (spawn == null) {
            MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Aucun spawn n’a encore été défini.");
            return true;
        }

        player.teleport(spawn);
        //player.sendMessage("§aTéléportation au spawn !");
        return true;
    }
}
