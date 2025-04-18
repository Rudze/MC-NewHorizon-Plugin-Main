package fr.rudy.newhorizon.world;

import java.sql.*;
import org.bukkit.*;

public class WorldSpawnManager {

    private final Connection connection;

    public WorldSpawnManager(Connection connection) {
        this.connection = connection;
    }

    public void setSpawn(String worldName, Location loc) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "REPLACE INTO newhorizon_world_spawns (world_name, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?)")) {
            stmt.setString(1, worldName);
            stmt.setDouble(2, loc.getX());
            stmt.setDouble(3, loc.getY());
            stmt.setDouble(4, loc.getZ());
            stmt.setFloat(5, loc.getYaw());
            stmt.setFloat(6, loc.getPitch());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Location getSpawn(String worldName) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT x, y, z, yaw, pitch FROM newhorizon_world_spawns WHERE world_name = ?")) {
            stmt.setString(1, worldName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                World world = Bukkit.getWorld(worldName);
                if (world == null) return null;
                return new Location(
                        world,
                        rs.getDouble("x"),
                        rs.getDouble("y"),
                        rs.getDouble("z"),
                        rs.getFloat("yaw"),
                        rs.getFloat("pitch")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
