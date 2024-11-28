package fr.rudy.newhorizon.warp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

public class WarpManager {

    private final Map<String, Warp> warps = new HashMap<>();

    public void loadWarpsFromConfig() {
        // Exemple de récupération des warps depuis la config
        if (!Bukkit.getPluginManager().getPlugin("NewHorizon").getConfig().contains("warps")) {
            return;
        }
        Bukkit.getPluginManager().getPlugin("NewHorizon").getConfig().getConfigurationSection("warps").getKeys(false).forEach(warpName -> {
            String path = "warps." + warpName;
            String worldName = Bukkit.getPluginManager().getPlugin("NewHorizon").getConfig().getString(path + ".world");
            double x = Bukkit.getPluginManager().getPlugin("NewHorizon").getConfig().getDouble(path + ".x");
            double y = Bukkit.getPluginManager().getPlugin("NewHorizon").getConfig().getDouble(path + ".y");
            double z = Bukkit.getPluginManager().getPlugin("NewHorizon").getConfig().getDouble(path + ".z");
            float yaw = (float) Bukkit.getPluginManager().getPlugin("NewHorizon").getConfig().getDouble(path + ".yaw");
            float pitch = (float) Bukkit.getPluginManager().getPlugin("NewHorizon").getConfig().getDouble(path + ".pitch");
            World world = Bukkit.getWorld(worldName);

            if (world != null) {
                Location location = new Location(world, x, y, z, yaw, pitch);
                warps.put(warpName, new Warp(warpName, location));
            }
        });
    }

    public Warp getWarp(String name) {
        return warps.get(name);
    }

    public boolean warpExists(String name) {
        return warps.containsKey(name);
    }

    public Map<String, Warp> getWarps() {
        return warps;
    }
}
