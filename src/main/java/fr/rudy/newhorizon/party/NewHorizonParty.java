package fr.rudy.newhorizon.party;

import net.playavalon.mythicdungeons.api.party.IDungeonParty;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NewHorizonParty implements IDungeonParty {

    private final UUID leader;
    private final List<UUID> playerUUIDs = new ArrayList<>();

    public NewHorizonParty(Player leader, Plugin plugin) {
        this.leader = leader.getUniqueId();
        this.playerUUIDs.add(this.leader);
        initDungeonParty(plugin);
        plugin.getLogger().info("NewHorizonParty créée pour " + leader.getName());
    }

    @Override
    public void addPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        if (!playerUUIDs.contains(uuid)) {
            playerUUIDs.add(uuid);
        }
    }

    @Override
    public void removePlayer(Player player) {
        playerUUIDs.remove(player.getUniqueId());
    }

    @Override
    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<>();
        for (UUID uuid : playerUUIDs) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) players.add(player);
        }
        return players;
    }

    @Override
    public Player getLeader() {
        return Bukkit.getPlayer(leader);
    }
}
