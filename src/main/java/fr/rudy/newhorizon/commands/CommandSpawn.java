package fr.rudy.newhorizon.commands;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.spawn.CoreSpawnManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class CommandSpawn implements CommandExecutor {

    private final CoreSpawnManager spawnManager = Main.get().getCoreSpawnManager();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cCommande réservée aux joueurs !");
            return true;
        }

        Location loc = player.getLocation();
        spawnManager.setSpawn(loc);
        player.sendMessage("§a✔ Spawn du serveur défini ici !");
        return true;
    }
}
