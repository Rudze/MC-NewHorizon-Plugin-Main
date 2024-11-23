package fr.rudy.newhorizon.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.*;
import java.util.HashMap;
import java.util.UUID;

import static org.bukkit.Bukkit.getLogger;

public class DatabaseManager {

    private final String url;
    private final String username;
    private final String password;
    private Connection connection;

    public DatabaseManager(String host, int port, String username, String password, String database) {
        this.url = "jdbc:mysql://" + host + ":" + port + "/" + database;
        this.username = username;
        this.password = password;
    }

    public void connect() {
        try {
            connection = DriverManager.getConnection(url, username, password);
            getLogger().info("Connexion établie avec succès !");
            initializeDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
            getLogger().severe("Échec de la connexion à la base de données.");
        }
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                getLogger().info( "[Connexion à la base de données à été établie avec succès !");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void initializeDatabase() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS newhorizon_player_data (" +
                            "uuid VARCHAR(36) PRIMARY KEY, " +
                            "level INT DEFAULT 0, " +
                            "experience INT DEFAULT 0, " +
                            "home_world VARCHAR(64), " +
                            "home_x DOUBLE, " +
                            "home_y DOUBLE, " +
                            "home_z DOUBLE, " +
                            "home_yaw FLOAT, " +
                            "home_pitch FLOAT" +
                            ")"
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public void loadPlayerData(HashMap<UUID, Integer> playerLevels, HashMap<UUID, Integer> playerExp) {
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM newhorizon_player_data");
            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                int level = resultSet.getInt("level");
                int experience = resultSet.getInt("experience");
                playerLevels.put(uuid, level);
                playerExp.put(uuid, experience);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void savePlayerData(HashMap<UUID, Integer> playerLevels, HashMap<UUID, Integer> playerExp) {
        try (PreparedStatement statement = connection.prepareStatement(
                "REPLACE INTO newhorizon_player_data (uuid, level, experience) VALUES (?, ?, ?)"
        )) {
            for (UUID uuid : playerLevels.keySet()) {
                statement.setString(1, uuid.toString());
                statement.setInt(2, playerLevels.get(uuid));
                statement.setInt(3, playerExp.get(uuid));
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadHomes(HashMap<UUID, Location> playerHomes) {
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT uuid, home_world, home_x, home_y, home_z, home_yaw, home_pitch FROM newhorizon_player_data");
            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                String worldName = resultSet.getString("home_world");
                double x = resultSet.getDouble("home_x");
                double y = resultSet.getDouble("home_y");
                double z = resultSet.getDouble("home_z");
                float yaw = resultSet.getFloat("home_yaw");
                float pitch = resultSet.getFloat("home_pitch");

                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    playerHomes.put(uuid, new Location(world, x, y, z, yaw, pitch));
                } else {
                    Bukkit.getLogger().warning("[DatabaseManager] Monde introuvable pour le joueur " + uuid + ". Home ignoré.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void saveHome(UUID playerUuid, Location location) {
        if (location == null || location.getWorld() == null) {
            getLogger().warning("[DatabaseManager] Impossible de sauvegarder le home : localisation ou monde invalide pour le joueur " + playerUuid);
            return;
        }

        // Ajouter un log pour déboguer la sauvegarde
        getLogger().info("[DatabaseManager] Sauvegarde du home : " + location + " pour le joueur " + playerUuid);

        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO newhorizon_player_data (uuid, home_world, home_x, home_y, home_z, home_yaw, home_pitch) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "home_world = VALUES(home_world), " +
                        "home_x = VALUES(home_x), " +
                        "home_y = VALUES(home_y), " +
                        "home_z = VALUES(home_z), " +
                        "home_yaw = VALUES(home_yaw), " +
                        "home_pitch = VALUES(home_pitch)"
        )) {
            statement.setString(1, playerUuid.toString());
            statement.setString(2, location.getWorld().getName());
            statement.setDouble(3, location.getX());
            statement.setDouble(4, location.getY());
            statement.setDouble(5, location.getZ());
            statement.setFloat(6, location.getYaw());
            statement.setFloat(7, location.getPitch());
            statement.executeUpdate();
        } catch (SQLException e) {
            getLogger().severe("[DatabaseManager] Erreur lors de la sauvegarde du home pour le joueur " + playerUuid);
            e.printStackTrace();
        }
    }



    public Location getHome(UUID playerUuid) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT home_world, home_x, home_y, home_z, home_yaw, home_pitch FROM newhorizon_player_data WHERE uuid = ?"
        )) {
            statement.setString(1, playerUuid.toString());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String worldName = resultSet.getString("home_world");
                double x = resultSet.getDouble("home_x");
                double y = resultSet.getDouble("home_y");
                double z = resultSet.getDouble("home_z");
                float yaw = resultSet.getFloat("home_yaw");
                float pitch = resultSet.getFloat("home_pitch");

                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    getLogger().warning("[DatabaseManager] Le monde '" + worldName + "' n'est pas chargé pour le joueur " + playerUuid);
                    return null;
                }

                return new Location(world, x, y, z, yaw, pitch);
            } else {
                getLogger().info("[DatabaseManager] Aucun home trouvé pour le joueur " + playerUuid);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
