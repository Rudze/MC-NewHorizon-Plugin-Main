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

    private final Main plugin = Main.get();
    private final HashMap<Player, Double> participants = new HashMap<>();

    // Récompenses par boss
    private final Map<String, String> bossRewards = new HashMap<>();

    public Events() {
        bossRewards.put("magnus", "items.magnus_shard");
        bossRewards.put("lumberjack", "items.lumberjack_shard");
    }

    @EventHandler
    public void onBossDeath(MythicMobDeathEvent event) {
        String bossName = event.getMob().getType().getInternalName().toLowerCase();

        if (!bossRewards.containsKey(bossName)) return;

        MessageUtil.broadcastMessage(plugin.getPrefixInfo(), "Le boss " + bossName + " a été vaincu ! Les joueurs participants recevront une récompense.");

        participants.forEach((player, damage) -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "customitems " + bossRewards.get(bossName) + " " + player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "warp newhorizon " + player.getName());
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

        participants.clear();

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "rg flag -w world_newhorizon arene pvp allow");
    }

    @EventHandler
    public void onMythicDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity().getType().equals(EntityType.IRON_GOLEM))) return;

        String entityName = event.getEntity().getCustomName();
        if (entityName == null) return;

        String bossName = entityName.replace("§f", "").toLowerCase();
        if (!bossRewards.containsKey(bossName)) return;

        participants.merge((Player) event.getDamager(), event.getDamage(), Double::sum);
    }
}
