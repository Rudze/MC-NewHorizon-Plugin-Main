package fr.rudy.newhorizon.stats;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionStatManager {

    private final Map<UUID, Map<String, Integer>> sessionStats = new HashMap<>();

    public void loadStats(Player player) {
        UUID uuid = player.getUniqueId();
        Map<String, Integer> map = new HashMap<>();

        int stone = player.getStatistic(Statistic.MINE_BLOCK, Material.STONE);
        map.put("stone_mined", stone);

        sessionStats.put(uuid, map);
    }

    public int get(Player player, String key) {
        return sessionStats
                .getOrDefault(player.getUniqueId(), Collections.emptyMap())
                .getOrDefault(key, 0);
    }

    public void unload(Player player) {
        sessionStats.remove(player.getUniqueId());
    }
}
