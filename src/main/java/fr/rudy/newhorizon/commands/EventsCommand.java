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

public class EventsCommand implements CommandExecutor {

    Main plugin = Main.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("admin")) {
            MessageUtil.sendMessage(sender, plugin.getPrefixError(), "Vous n'avez pas les permissions pour faire ça !");
            return false;
        }

        /*if (args.length == 0) {
            sender.sendMessage("§f Arguments manquants !");
            return false;
        }

        Player player = Main.getInstance().getServer().getPlayer(sender.getName());

        if (player == null) {
            sender.sendMessage("§f Commande executable uniquement par un joueur !");
            return false;
        }*/

        switch (args[0]) {
            case "magnus":
                MessageUtil.broadcastMessage(plugin.getPrefixInfo(), "L'événement BOSS va commencer dans 5min.");

                // Désactivation du PVP dans l'arêne
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "rg flag -w world_newhorizon arene pvp deny");

                // Utilisation de l'instance du plugin principal pour le scheduler
                Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                    MessageUtil.broadcastMessage(plugin.getPrefixInfo(),"Début dans §b30s");

                    Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                        MessageUtil.broadcastMessage(plugin.getPrefixInfo(), "Le Magnus est apparu !");

                        // Spawn du boss Magnus
                        MythicMob mob = MythicBukkit.inst().getMobManager().getMythicMob("magnus").orElse(null);
                        Location spawnPoint = new Location(Main.getInstance().getServer().getWorld("world_resource"), 0, 63, 0);
                        ActiveMob magnus = mob.spawn(BukkitAdapter.adapt(spawnPoint), 0);

                        MessageUtil.broadcastMessage(plugin.getPrefixInfo(), "L'événement Magnus vient de commencer.");
                        MessageUtil.broadcastMessage(plugin.getPrefixInfo(), "Rejoignez-le dans l'arène du village§f.");
                    }, 2 * 20); //30 secondes //30
                }, 5 * 20); //5 minutes - 30 secondes //270
                break;
            default:
                MessageUtil.sendMessage(sender, plugin.getPrefixInfo(), "Boss inconnu !");
                break;
        }

        return true;
    }
}
