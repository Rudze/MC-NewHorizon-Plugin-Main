package fr.rudy.newhorizon.commands;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.utils.MessageUtil;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class EventsCommand implements CommandExecutor {

    private final Main plugin = Main.get();

    // Définition des boss et de leurs emplacements
    private final Map<String, Location> bossLocations = new HashMap<>();

    public EventsCommand() {
        // On initialise les emplacements des boss
        bossLocations.put("magnus", new Location(Bukkit.getWorld("world_newhorizon"), 706, 54, -631));
        bossLocations.put("lumberjack", new Location(Bukkit.getWorld("world_newhorizon"), 706, 54, -631));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("admin")) {
            MessageUtil.sendMessage(sender, plugin.getPrefixError(), "Vous n'avez pas les permissions pour faire ça !");
            return false;
        }

        if (args.length == 0) {
            MessageUtil.sendMessage(sender, plugin.getPrefixError(), "Usage: /event <nom_du_boss>");
            return false;
        }

        String bossName = args[0].toLowerCase();

        if (!bossLocations.containsKey(bossName)) {
            MessageUtil.sendMessage(sender, plugin.getPrefixError(), "Boss inconnu !");
            return false;
        }

        Location spawnPoint = bossLocations.get(bossName);
        startBossEvent(bossName, spawnPoint);

        return true;
    }

    private void startBossEvent(String bossName, Location spawnPoint) {
        MessageUtil.broadcastMessage(plugin.getPrefixInfo(), "L'événement BOSS va commencer dans 5min.");

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "rg flag -w world_newhorizon arene pvp deny");

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            MessageUtil.broadcastMessage(plugin.getPrefixInfo(), "Début dans §b30s");

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                MessageUtil.broadcastMessage(plugin.getPrefixInfo(), "Le boss " + bossName + " est apparu !");

                MythicMob mob = MythicBukkit.inst().getMobManager().getMythicMob(bossName).orElse(null);
                if (mob != null) {
                    ActiveMob boss = mob.spawn(BukkitAdapter.adapt(spawnPoint), 0);
                }

                MessageUtil.broadcastMessage(plugin.getPrefixInfo(), "L'événement " + bossName + " vient de commencer.");
                MessageUtil.broadcastMessage(plugin.getPrefixInfo(), "Rejoignez-le dans l'arène du village.");
            }, 30 * 20); // 30 secondes
        }, 270 * 20); // 5 minutes - 30 secondes
    }
}
