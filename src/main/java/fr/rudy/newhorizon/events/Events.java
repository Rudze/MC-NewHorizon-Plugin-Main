package fr.rudy.newhorizon.events;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.utils.MessageUtil;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.*;

public class Events implements Listener {

    Main plugin = Main.get();
    private final HashMap<Player, Double> participants = new HashMap<>();

    @EventHandler
    public void onMagnusDeath(MythicMobDeathEvent event) {
        // Vérifie si le type du mob est "magnus"
        if (!event.getMob().getType().getInternalName().equalsIgnoreCase("magnus")) return;

        // Annonce et récompenses
        MessageUtil.broadcastMessage(plugin.getPrefixInfo(), "Le Magnus a été vaincu ! Les joueurs participants recevront une récompense.");

        participants.forEach((player, damage) -> {
            // Give de la récompense au participants
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "iagive " + player.getName() + " newhorizon:magnus_shard ");
            // Téléportation des participants au spawn
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "warp newhorizon" + player.getName());
        });

        List<Map.Entry<Player, Double>> sortedEntries = new ArrayList<>(participants.entrySet());
        sortedEntries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        Bukkit.broadcastMessage("--------------------------");
        Bukkit.broadcastMessage("Top 3 joueurs :");
        for (int i = 0; i < Math.min(3, sortedEntries.size()); i++) {
            Map.Entry<Player, Double> entry = sortedEntries.get(i);
            Bukkit.broadcastMessage(entry.getKey().getName() + " - Score: " + entry.getValue());
        }
        Bukkit.broadcastMessage("--------------------------");

        // Réinitialiser la liste des participants
        participants.clear();

        // Réinitialiser les paramètres de l'arène
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "rg flag -w world_newhorizon arene pvp allow");
    }


    @EventHandler
    public void onMythicDamage(EntityDamageByEntityEvent event) {
        // Ajouter les joueurs infligeant des dégâts à Magnus

        if (!(event.getDamager() instanceof Player)) return;
        if (!event.getEntity().getType().equals(EntityType.IRON_GOLEM)) return;
        if (!event.getEntity().getName().equals("§fMagnus")) return;

        participants.merge((Player) event.getDamager(), event.getDamage(), Double::sum);
    }
}
