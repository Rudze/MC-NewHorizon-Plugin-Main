package fr.rudy.newhorizon.home;


import fr.rudy.newhorizon.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class HomesManager {
    private final Connection database;
    private final HashMap<UUID, Location> cache = new HashMap<>();

    public HomesManager() {
        database = Main.get().getDatabase();
    }

    public Location getHome(UUID player) {
        if (cache.containsKey(player)) return cache.get(player);

        try (PreparedStatement statement = database.prepareStatement(
                "SELECT uuid, home_world, home_x, home_y, home_z, home_yax, home_pitch " +
                        "FROM newhorizon_player_data " +
                        "WHERE uuid = ?;"
        )) {
            statement.setString(1, player.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) return null;

                final Location homeLocation = new Location(
                        Bukkit.getWorld(resultSet.getString("home_world")),
                        resultSet.getDouble("home_x"),
                        resultSet.getDouble("home_y"),
                        resultSet.getDouble("home_z"),
                        resultSet.getFloat("home_yaw"),
                        resultSet.getFloat("home_pitch")
                );
                cache.put(player, homeLocation);
                return homeLocation;
            }
        } catch (SQLException exception) {
            //TODO: Message + Stacktrace
            exception.printStackTrace();
        }

        return null;
    }

    public boolean setHome(UUID player, Location home) {
        try (PreparedStatement statement = database.prepareStatement(
                "INSERT INTO newhorizon_player_data (uuid, home_world, home_x, home_y, home_z, home_yaw, home_pitch) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "home_world = VALUES(home_world), " +
                        "home_x = VALUES(home_x), " +
                        "home_y = VALUES(home_y), " +
                        "home_z = VALUES(home_z), " +
                        "home_yaw = VALUES(home_yaw), " +
                        "home_pitch = VALUES(home_pitch);"
        )) {
            statement.setString(1, player.toString());
            statement.setString(2, Objects.requireNonNull(home.getWorld()).getName());
            statement.setDouble(3, home.getX());
            statement.setDouble(4, home.getY());
            statement.setDouble(5, home.getZ());
            statement.setFloat(6, home.getYaw());
            statement.setFloat(7, home.getPitch());
            statement.executeUpdate();
            cache.merge(player, home, (prev, next) -> next);
        } catch (SQLException exception) {
            //TODO: Message + Stacktrace
            exception.printStackTrace();
            return false;
        }

        return true;
    }
}
