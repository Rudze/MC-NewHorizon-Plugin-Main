package fr.rudy.newhorizon.warp;

import fr.rudy.newhorizon.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class WarpManager {

    private final Map<String, Warp> warps = new HashMap<>();

    public void loadWarpsFromDatabase() {
        try (PreparedStatement statement = Main.get().getDatabase().prepareStatement(
                "SELECT name, world, x, y, z, yaw, pitch FROM newhorizon_warps"
        )) {
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                String worldName = rs.getString("world");
                double x = rs.getDouble("x");
                double y = rs.getDouble("y");
                double z = rs.getDouble("z");
                float yaw = rs.getFloat("yaw");
                float pitch = rs.getFloat("pitch");

                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    Location location = new Location(world, x, y, z, yaw, pitch);
                    warps.put(name.toLowerCase(), new Warp(name, location));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveWarp(Warp warp) {
        try (PreparedStatement statement = Main.get().getDatabase().prepareStatement(
                "REPLACE INTO newhorizon_warps (name, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?)"
        )) {
            Location loc = warp.getLocation();
            statement.setString(1, warp.getName());
            statement.setString(2, loc.getWorld().getName());
            statement.setDouble(3, loc.getX());
            statement.setDouble(4, loc.getY());
            statement.setDouble(5, loc.getZ());
            statement.setFloat(6, loc.getYaw());
            statement.setFloat(7, loc.getPitch());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Warp getWarp(String name) {
        return warps.get(name.toLowerCase());
    }

    public boolean warpExists(String name) {
        return warps.containsKey(name.toLowerCase());
    }

    public Map<String, Warp> getWarps() {
        return warps;
    }
}
