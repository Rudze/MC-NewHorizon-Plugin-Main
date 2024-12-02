package fr.rudy.newhorizon.level;

import fr.rudy.newhorizon.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.*;
import java.util.regex.Pattern;

public class PlayerListener implements Listener {
    private final LevelsManager levelsManager;
    private final List<HashMap<String, Integer>> breakBlocks;

    public PlayerListener() {
        levelsManager = Main.get().getLevelsManager();
        breakBlocks = (List<HashMap<String, Integer>>) Main.get().getConfig().getList("levels.break_blocks", new ArrayList<>());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        //if(event.getPlayer().getWorld().equals(Main.get().getServer().getWorld("world_newhorizon"))) return;

        blocksLoop:
        for (HashMap<String, Integer> block : breakBlocks) {
            final Set<String> keys = new HashSet<>(block.keySet());
            if (keys.stream().findFirst().isEmpty()) continue;

            final String name = keys.stream().findFirst().get();
            if (!Pattern.compile(name.replace("*", ".*").toLowerCase())
                    .matcher(event.getBlock().getType().toString().toLowerCase())
                    .matches()
            ) continue;
            keys.remove(name);

            for (String metadata : keys)
                if (!event.getBlock().getBlockData().getAsString().toLowerCase().contains((metadata + "=" + block.get(metadata)).toLowerCase()))
                    continue blocksLoop;
            levelsManager.setExp(event.getPlayer().getUniqueId(), levelsManager.getExp(event.getPlayer().getUniqueId()) + block.get(name));
        }
    }
}