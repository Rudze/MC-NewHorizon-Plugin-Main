package fr.rudy.newhorizon.city;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.UUID;

public class ChunkProtectionListener implements Listener {

    private final ClaimManager claimManager = new ClaimManager();
    private final Main plugin = Main.get();

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (!checkAccess(event.getPlayer().getUniqueId(), event.getBlock().getLocation())) {
            event.setCancelled(true);
            MessageUtil.sendMessage(event.getPlayer(), plugin.getPrefixError(), "&cCe chunk est revendiqué. Vous ne pouvez pas casser ici.");
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;

        if (!checkAccess(event.getPlayer().getUniqueId(), event.getClickedBlock().getLocation())) {
            event.setCancelled(true);
            //MessageUtil.sendMessage(event.getPlayer(), plugin.getPrefixError(), "&cCe chunk est revendiqué. Vous ne pouvez pas interagir ici.");
        }
    }

    private boolean checkAccess(UUID player, Location loc) {
        Integer cityId = claimManager.getChunkOwnerId(loc);
        if (cityId == null) return true; // Pas de claim ici

        String playerCity = Main.get().getCityManager().getCityName(player);
        if (playerCity == null) return false;

        int playerCityId = Main.get().getCityManager().getCityId(player);
        return playerCityId == cityId;
    }
}
