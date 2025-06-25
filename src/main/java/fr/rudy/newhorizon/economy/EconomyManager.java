package fr.rudy.newhorizon.economy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class EconomyManager {
    private final Connection database;

    public EconomyManager(Connection database) {
        this.database = database;
    }

    public void addMoneyColumnIfNotExists() {
        try (Statement statement = database.createStatement()) {
            statement.executeUpdate("ALTER TABLE newhorizon_player_data ADD COLUMN money INTEGER DEFAULT 0;");
        } catch (SQLException e) {
            if (!e.getMessage().contains("duplicate column name")) {
                e.printStackTrace();
            }
        }
    }

    public double getMoney(UUID player) {
        try (PreparedStatement statement = database.prepareStatement(
                "SELECT money FROM newhorizon_player_data WHERE uuid = ?;"
        )) {
            statement.setString(1, player.toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("money");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public void setMoney(UUID player, double amount) {
        try (PreparedStatement statement = database.prepareStatement(
                "UPDATE newhorizon_player_data SET money = ? WHERE uuid = ?;"
        )) {
            statement.setDouble(1, amount);
            statement.setString(2, player.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addMoney(UUID player, double amount) {
        double current = getMoney(player);
        setMoney(player, current + amount);
    }
}
