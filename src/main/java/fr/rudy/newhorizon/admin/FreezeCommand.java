package fr.rudy.newhorizon.admin;

import fr.rudy.newhorizon.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FreezeCommand implements CommandExecutor, Listener {

    private final Set<UUID> frozenPlayers = new HashSet<>();

    public FreezeCommand(Main plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2) {
            //sender.sendMessage("Usage: /freeze <on|off> <joueur>");
            return true;
        }

        String mode = args[0];
        Player target = Bukkit.getPlayer(args[1]);

        if (target == null) {
            //sender.sendMessage("Joueur introuvable.");
            return true;
        }

        if (mode.equalsIgnoreCase("on")) {
            frozenPlayers.add(target.getUniqueId());
            //sender.sendMessage("Le joueur est maintenant freeze.");
            //target.sendMessage("Tu as été freeze par un administrateur.");
        } else if (mode.equalsIgnoreCase("off")) {
            frozenPlayers.remove(target.getUniqueId());
            //sender.sendMessage("Le joueur est maintenant défreeze.");
            //target.sendMessage("Tu es désormais libre de tes mouvements.");
        } else {
            //sender.sendMessage("Mode invalide. Utilise on ou off.");
        }
        return true;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (frozenPlayers.contains(event.getPlayer().getUniqueId())) {
            if (!event.getFrom().toVector().equals(event.getTo().toVector()) ||
                    event.getFrom().getYaw() != event.getTo().getYaw() ||
                    event.getFrom().getPitch() != event.getTo().getPitch()) {
                event.setTo(event.getFrom());
            }
        }
    }
}