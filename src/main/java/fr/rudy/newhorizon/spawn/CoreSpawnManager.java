package fr.rudy.newhorizon.spawn;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CoreSpawnManager {

    private final Connection db;

    public CoreSpawnManager(Connection db) {
        this.db = db;
    }

    // ✅ Définir le spawn principal
    public void setSpawn(Location loc) {
        try (PreparedStatement ps = db.prepareStatement(
                "REPLACE INTO newhorizon_core_spawn (id, world, x, y, z, yaw, pitch) VALUES (0, ?, ?, ?, ?, ?, ?)"
        )) {
            ps.setString(1, loc.getWorld().getName());
            ps.setDouble(2, loc.getX());
            ps.setDouble(3, loc.getY());
            ps.setDouble(4, loc.getZ());
            ps.setFloat(5, loc.getYaw());
            ps.setFloat(6, loc.getPitch());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ Récupérer le spawn principal
    public Location getSpawn() {
        try (PreparedStatement ps = db.prepareStatement(
                "SELECT world, x, y, z, yaw, pitch FROM newhorizon_core_spawn WHERE id = 0"
        )) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                World world = Bukkit.getWorld(rs.getString("world"));
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

    // ✅ Alias pour getGlobalSpawn() (si tu veux nommer ainsi ailleurs)
    public Location getGlobalSpawn() {
        return getSpawn();
    }
}
